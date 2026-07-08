package com.paytrack.viewmodel

import android.content.Context
import android.content.Intent
import android.app.Activity
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.paytrack.data.Folder
import com.paytrack.data.FinanceRepository
import com.paytrack.data.FinanceTransaction
import com.paytrack.data.FALLBACK_FOLDER
import com.paytrack.data.SavingsGoal
import com.paytrack.data.SavingsLedgerEntry
import com.paytrack.data.TransactionSource
import com.paytrack.data.TransactionType
import com.paytrack.payment.ParsedUpiQr
import com.paytrack.payment.UpiAppResolver
import com.paytrack.payment.UpiPaymentRequest
import com.paytrack.payment.UpiPaymentResultParser
import com.paytrack.payment.UpiQrParser
import com.paytrack.sms.SmsImporter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val SCANNED_QR_KEY = "scanned_qr_value"
private const val AMOUNT_INPUT_KEY = "amount_input"
private const val SELECTED_CATEGORY_KEY = "selected_category"

class HomeViewModel(
    private val repository: FinanceRepository,
    private val appContext: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var lastUpiLaunchAtMillis: Long = 0L
    private var pendingUpiPackageName: String? = null
    private var lastAttemptedUpiPackageName: String? = null


    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _transactionsUiState = MutableStateFlow(TransactionsUiState())
    val transactionsUiState: StateFlow<TransactionsUiState> = _transactionsUiState.asStateFlow()

    private val _insightsUiState = MutableStateFlow(InsightsUiState())
    val insightsUiState: StateFlow<InsightsUiState> = _insightsUiState.asStateFlow()

    private val _qrUiState = MutableStateFlow(QrScanUiState())
    val qrUiState: StateFlow<QrScanUiState> = _qrUiState.asStateFlow()

    private val allTransactions = MutableStateFlow<List<FinanceTransaction>>(emptyList())
    private val savingsGoal = MutableStateFlow<SavingsGoal?>(null)
    private val allFolders = MutableStateFlow<List<Folder>>(emptyList())
    private val savingsLedger = MutableStateFlow<List<SavingsLedgerEntry>>(emptyList())
    private var sweepDoneThisSession = false

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val timeFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)

    init {
        observeData()
        refreshInstalledUpiApps()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getTransactions(),
                repository.getSavingsGoal(),
                repository.getFolders(),
                repository.getSavingsLedger()
            ) { transactions, goal, folders, ledger ->
                object {
                    val t = transactions
                    val g = goal
                    val f = folders
                    val l = ledger
                }
            }.collect { data ->
                allTransactions.value = data.t
                savingsGoal.value = data.g
                allFolders.value = data.f
                savingsLedger.value = data.l

                // Launch sweep as a non-blocking side-effect so the UI updates immediately.
                // If sweep writes to DataStore it will trigger another emission with fresh data.
                if (!sweepDoneThisSession) {
                    sweepDoneThisSession = true
                    viewModelScope.launch {
                        repository.sweepClosedFolders(data.t, System.currentTimeMillis())
                    }
                }

                updateHomeState(data.t, data.g, data.f)
                updateTransactionsState(data.t, data.f)
                updateInsightsState(data.t, data.l)
                syncQrState(data.f)
            }
        }
    }


    fun updateTransactionSearchQuery(query: String) {
        _transactionsUiState.update { it.copy(query = query) }
        updateTransactionsState(allTransactions.value, allFolders.value)
    }

    fun updateTransactionTypeFilter(type: TransactionType?) {
        _transactionsUiState.update { it.copy(selectedType = type) }
        updateTransactionsState(allTransactions.value, allFolders.value)
    }

    fun updateTransactionCategoryFilter(category: String?) {
        _transactionsUiState.update { it.copy(selectedCategory = category) }
        updateTransactionsState(allTransactions.value, allFolders.value)
    }

    fun saveTransaction(
        id: String?,
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            if (id == null) {
                repository.addTransaction(
                    amount = amount,
                    type = type,
                    category = category,
                    dateMillis = dateMillis,
                    note = note
                )
            } else {
                val existing = repository.getTransaction(id) ?: return@launch
                repository.updateTransaction(
                    existing.copy(
                        amount = amount,
                        type = type,
                        category = category,
                        dateMillis = dateMillis,
                        note = note.trim().takeIf(String::isNotBlank)
                    )
                )
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    suspend fun getTransactionFormData(transactionId: String?): TransactionFormData {
        val defaultCategory = allFolders.value.firstOrNull()?.name ?: FALLBACK_FOLDER
        if (transactionId.isNullOrBlank()) {
            return TransactionFormData(category = defaultCategory)
        }
        val transaction = repository.getTransaction(transactionId)
        return if (transaction == null) {
            TransactionFormData(category = defaultCategory)
        } else {
            TransactionFormData(
                id = transaction.id,
                amount = transaction.amount,
                type = transaction.type,
                category = transaction.category,
                dateMillis = transaction.dateMillis,
                note = transaction.note.orEmpty()
            )
        }
    }

    fun createFolder(name: String) {
        val trimmedName = name.trim()
        when {
            trimmedName.isBlank() -> updateFolderMessage("Enter a folder name.")
            allFolders.value.any { it.name.equals(trimmedName, ignoreCase = true) } -> {
                updateFolderMessage("A folder with that name already exists.")
            }
            else -> {
                viewModelScope.launch {
                    repository.addFolder(trimmedName)
                    updateFolderMessage(null)
                }
            }
        }
    }

    fun deleteFolder(name: String) {
        if (name.equals(FALLBACK_FOLDER, ignoreCase = true)) {
            updateFolderMessage("The Other folder cannot be deleted.")
            return
        }

        viewModelScope.launch {
            repository.deleteFolder(name)
            updateFolderMessage(null)
        }
    }

    fun clearFolderMessage() {
        updateFolderMessage(null)
    }

    fun saveFolderLimit(name: String, limitAmount: Double, limitEndDateMillis: Long) {
        viewModelScope.launch {
            repository.updateFolderLimit(
                name = name,
                limitAmount = limitAmount,
                limitStartDateMillis = System.currentTimeMillis(),
                limitEndDateMillis = endOfDay(limitEndDateMillis)
            )
            updateFolderMessage(null)
        }
    }

    fun clearFolderLimit(name: String) {
        viewModelScope.launch {
            repository.clearFolderLimit(name)
            updateFolderMessage(null)
        }
    }

    fun getGoalFormData(): SavingsGoalFormData {
        val goal = savingsGoal.value
        return if (goal == null) {
            SavingsGoalFormData()
        } else {
            SavingsGoalFormData(
                targetAmount = goal.targetAmount,
                startDateMillis = goal.startDateMillis,
                targetDateMillis = goal.targetDateMillis
            )
        }
    }

    fun saveSavingsGoal(targetAmount: Double, startDateMillis: Long?, targetDateMillis: Long?) {
        viewModelScope.launch {
            repository.saveSavingsGoal(SavingsGoal(targetAmount = targetAmount, startDateMillis = startDateMillis, targetDateMillis = targetDateMillis))
        }
    }

    fun clearSavingsGoal() {
        viewModelScope.launch {
            repository.clearSavingsGoal()
        }
    }

    fun setCameraPermission(granted: Boolean) {
        _qrUiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun onQrScanned(rawValue: String) {
        val parsed = UpiQrParser.parse(rawValue)
        if (parsed == null) {
            _qrUiState.update {
                it.copy(scanError = "This QR is not a supported UPI payment code.", paymentError = null)
            }
            return
        }

        savedStateHandle[SCANNED_QR_KEY] = rawValue.trim()
        if (parsed.amount != null && savedStateHandle.get<String>(AMOUNT_INPUT_KEY).isNullOrBlank()) {
            savedStateHandle[AMOUNT_INPUT_KEY] = String.format(Locale.US, "%.2f", parsed.amount)
        }

        _qrUiState.update {
            it.copy(
                scannedMerchantName = parsed.payeeName,
                scannedPayeeVpa = parsed.payeeVpa,
                scannedNote = parsed.note,
                scannedAmountText = parsed.amount?.let(currencyFormatter::format).orEmpty(),
                amountInput = savedStateHandle.get<String>(AMOUNT_INPUT_KEY)
                    ?: parsed.amount?.let { String.format(Locale.US, "%.2f", it) }.orEmpty(),
                isAmountLocked = parsed.hasEmbeddedAmount,
                scanError = null,
                paymentError = null
            )
        }
        refreshQrDerivedState()
    }

    fun clearScannedQr() {
        savedStateHandle[SCANNED_QR_KEY] = null
        savedStateHandle[AMOUNT_INPUT_KEY] = null
        _qrUiState.update {
            it.copy(
                scannedMerchantName = "",
                scannedPayeeVpa = "",
                scannedNote = null,
                scannedAmountText = "",
                amountInput = "",
                isAmountLocked = false,
                scanError = null,
                paymentError = null,
                canLaunchPayment = false
            )
        }
        pendingUpiPackageName = null
        lastAttemptedUpiPackageName = null
        refreshQrDerivedState()
    }

    fun updateSelectedQrCategory(category: String) {
        savedStateHandle[SELECTED_CATEGORY_KEY] = category
        refreshQrDerivedState()
    }

    fun updateAmountInput(value: String) {
        if (_qrUiState.value.isAmountLocked) return
        savedStateHandle[AMOUNT_INPUT_KEY] = value
        _qrUiState.update { it.copy(amountInput = value, paymentError = null) }
        refreshQrDerivedState()
    }

    fun refreshInstalledUpiApps() {
        val apps = UpiAppResolver.resolve(appContext).map(::upiAppToUiState)
        _qrUiState.update {
            it.copy(
                availableUpiApps = apps,
                paymentError = if (apps.isEmpty()) {
                    "No compatible UPI app is installed on this device."
                } else {
                    null
                }
            )
        }
        refreshQrDerivedState()
    }

    fun buildUpiLaunchIntent(packageName: String): Intent? {
        val payload = currentParsedPayload() ?: return markQrError("Scan a valid UPI QR code first.")
        selectedQrCategory() ?: return markQrError("Choose a category before opening a UPI app.")
        val paymentRequest = buildPaymentRequest(payload)
            ?: return markQrError("Enter a valid UPI ID and amount before opening a UPI app.")
        val selectedApp = _qrUiState.value.availableUpiApps.firstOrNull { it.packageName == packageName }
            ?: return markQrError("The selected UPI app is no longer available.")
        if (isRapidRepeatLaunch()) {
            return markQrError("Please wait 2 seconds before retrying the payment app.")
        }
        pendingUpiPackageName = selectedApp.packageName
        lastAttemptedUpiPackageName = selectedApp.packageName
        return UpiAppResolver.launchIntent(
            context = appContext,
            packageName = selectedApp.packageName,
            upiId = paymentRequest.upiId,
            name = paymentRequest.payeeName,
            amount = paymentRequest.amount,
            note = paymentRequest.note,
            rawUri = paymentRequest.rawUri
        ) ?: run {
            pendingUpiPackageName = null
            markQrError("Unable to open the selected UPI app on its payment screen.")
        }
    }

    /**
     * Opens the chosen UPI app directly (its home/scanner screen) without any payment URI.
     * The user scans the QR inside the UPI app, enters the amount, and pays.
     * When the user returns, call [onReturnFromUpiApp] to show the confirm dialog.
     */
    fun openUpiAppDirectly(packageName: String): Intent? {
        val amount = enteredAmount() ?: return markQrError("Enter a valid amount before opening a UPI app.")
        val category = selectedQrCategory() ?: return markQrError("Choose a folder before opening a UPI app.")
        val selectedApp = _qrUiState.value.availableUpiApps.firstOrNull { it.packageName == packageName }
            ?: return markQrError("The selected UPI app is no longer available.")
        if (isRapidRepeatLaunch()) {
            return markQrError("Please wait 2 seconds before retrying.")
        }
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)
            ?: return markQrError("Could not open ${selectedApp.label}. Is it installed?")

        // Store pending info so the confirm dialog can show it on return
        _qrUiState.update {
            it.copy(
                pendingConfirmAmount = String.format(Locale.US, "%.2f", amount),
                pendingConfirmCategory = category,
                pendingConfirmAppLabel = selectedApp.label,
                paymentError = null
            )
        }
        pendingUpiPackageName = packageName
        lastAttemptedUpiPackageName = packageName
        return launchIntent
    }

    /** Called when the user returns from the UPI app — shows the "Did it go through?" dialog. */
    fun onReturnFromUpiApp() {
        val state = _qrUiState.value
        if (state.pendingConfirmAmount.isBlank()) return
        _qrUiState.update { it.copy(showPaymentConfirmDialog = true) }
    }

    /** User tapped "Yes, log it" in the confirm dialog. */
    fun confirmPaymentLogged() {
        val state = _qrUiState.value
        val amount = state.pendingConfirmAmount.toDoubleOrNull() ?: return
        val category = state.pendingConfirmCategory.ifBlank { return }
        val appLabel = state.pendingConfirmAppLabel
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                type = com.paytrack.data.TransactionType.EXPENSE,
                category = category,
                dateMillis = System.currentTimeMillis(),
                note = "Paid through ${appLabel.ifBlank { "UPI" }}",
                upiAppLabel = appLabel.ifBlank { null }
            )
        }
        _qrUiState.update {
            it.copy(
                showPaymentConfirmDialog = false,
                pendingConfirmAmount = "",
                pendingConfirmCategory = "",
                pendingConfirmAppLabel = "",
                amountInput = "",
                paymentError = null
            )
        }
        savedStateHandle[AMOUNT_INPUT_KEY] = null
        pendingUpiPackageName = null
    }

    /** User tapped "No" in the confirm dialog — dismiss without logging. */
    fun dismissConfirmDialog() {
        _qrUiState.update {
            it.copy(
                showPaymentConfirmDialog = false,
                pendingConfirmAmount = "",
                pendingConfirmCategory = "",
                pendingConfirmAppLabel = ""
            )
        }
        pendingUpiPackageName = null
    }

    fun onUpiPaymentResult(launchedPackageName: String?, resultCode: Int, data: Intent?) {
        val result = UpiPaymentResultParser.parse(data)
        UpiPaymentResultParser.log(result)
        Log.d(
            "UpiPaymentFlow",
            "package=${launchedPackageName ?: pendingUpiPackageName}, resultCode=$resultCode, status=${result.status}, txnId=${result.transactionId}, responseCode=${result.responseCode}"
        )

        if (result.isSuccess) {
            recordSuccessfulPayment(launchedPackageName ?: pendingUpiPackageName)
            return
        }

        pendingUpiPackageName = null
        _qrUiState.update {
            it.copy(
                paymentError = when {
                    result.status.equals("failure", ignoreCase = true) ->
                        buildFailureMessage(result.responseCode)
                    resultCode == Activity.RESULT_CANCELED && result.rawResponse.isNullOrBlank() ->
                        "UPI payment was cancelled."
                    result.status.equals("cancelled", ignoreCase = true) ->
                        "UPI payment was cancelled."
                    result.rawResponse.isNullOrBlank() ->
                        buildUnknownResultMessage()
                    else ->
                        "UPI payment did not complete. Status: ${result.status ?: "unknown"}. Try another installed UPI app."
                }
            )
        }
    }

    fun onUpiLaunchFailed() {
        pendingUpiPackageName = null
        val alternateApp = _qrUiState.value.availableUpiApps
            .firstOrNull { it.packageName != lastAttemptedUpiPackageName }
            ?.label
        _qrUiState.update {
            it.copy(
                paymentError = if (alternateApp != null) {
                    "Unable to open the selected UPI app. Try $alternateApp or use the chooser."
                } else {
                    "Unable to open the selected UPI app. No transaction was recorded."
                }
            )
        }
    }

    fun updateHomeChartPeriod(period: TimePeriod) {
        val transactions = allTransactions.value
        val (points, currentIndex) = buildChartPointsForPeriod(transactions, System.currentTimeMillis(), period)
        _homeUiState.update {
            it.copy(
                weeklyExpenseChart = it.weeklyExpenseChart.copy(
                    values = points.map { point -> point.amount.toFloat() },
                    labels = points.map { point -> point.label },
                    currentDayIndex = currentIndex,
                    isEmpty = points.none { point -> point.amount > 0.0 },
                    selectedChartPeriod = period,
                    isLineGraph = period == TimePeriod.MONTH
                )
            )
        }
    }

    fun updateInsightsChartPeriod(period: TimePeriod) {
        val transactions = allTransactions.value
        val (points, _) = buildChartPointsForPeriod(transactions, System.currentTimeMillis(), period)
        _insightsUiState.update {
            it.copy(
                monthlyTrendPoints = points,
                selectedTimePeriod = period,
                isLineGraph = period == TimePeriod.MONTH
            )
        }
    }

    fun deleteSavingsVaultEntry(id: String) {
        viewModelScope.launch {
            repository.deleteSavingsLedgerEntry(id)
        }
    }

    fun updateHeroPeriod(period: HeroPeriod) {
        _homeUiState.update { it.copy(heroPeriod = period) }
        updateHomeState(allTransactions.value, savingsGoal.value, allFolders.value)
    }

    private fun updateHomeState(
        transactions: List<FinanceTransaction>,
        goal: SavingsGoal?,
        folders: List<Folder>
    ) {
        val heroPeriod = _homeUiState.value.heroPeriod
        val heroTransactions = if (heroPeriod == HeroPeriod.THIS_MONTH) {
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)
            transactions.filter {
                val tCal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
            }
        } else {
            transactions
        }

        val totalIncome = heroTransactions.filter { it.type == TransactionType.INCOME }.sumOf(FinanceTransaction::amount)
        val totalExpenses = heroTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf(FinanceTransaction::amount)
        val savedAmount = calculateSavedAmount(transactions)
        val vaultSaved = goal?.savedAmount ?: 0.0
        val totalSaved = savedAmount + vaultSaved
        val folderUsage = buildFolderUsageInsights(folders, transactions)
        
        val currentPeriod = _homeUiState.value.weeklyExpenseChart.selectedChartPeriod
        val (points, currentIndex) = buildChartPointsForPeriod(transactions, System.currentTimeMillis(), currentPeriod)
        
        val periodExpenses = goal?.let {
            val start = it.startDateMillis?.let { ms ->
                Calendar.getInstance().apply {
                    timeInMillis = ms
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            } ?: 0L
            val end = it.targetDateMillis?.let { ms ->
                Calendar.getInstance().apply {
                    timeInMillis = ms
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
            } ?: Long.MAX_VALUE
            transactions.filter { t -> 
                t.type == TransactionType.EXPENSE && t.dateMillis in start..end 
            }.sumOf(FinanceTransaction::amount)
        } ?: 0.0
        
        _homeUiState.update {
            it.copy(
                currentBalance = currencyFormatter.format(totalIncome - totalExpenses),
                totalIncome = currencyFormatter.format(totalIncome),
                totalExpenses = currencyFormatter.format(totalExpenses),
                savingsProgress = goal?.let { savingsGoal ->
                    if (savingsGoal.targetAmount <= 0.0) 0f else (periodExpenses / savingsGoal.targetAmount).toFloat().coerceIn(0f, 1f)
                } ?: 0f,
                savingsProgressLabel = goal?.let { savingsGoal ->
                    "${currencyFormatter.format(periodExpenses)} spent of ${currencyFormatter.format(savingsGoal.targetAmount)}"
                } ?: "Set a budget to stay on track",
                goalSummary = goal?.let { savingsGoal ->
                    val startStr = savingsGoal.startDateMillis?.let { t -> dateFormatter.format(Date(t)) }
                    val endStr = savingsGoal.targetDateMillis?.let { t -> dateFormatter.format(Date(t)) }
                    if (startStr != null && endStr != null) {
                        "$startStr - $endStr"
                    } else if (startStr != null) {
                        "From $startStr"
                    } else if (endStr != null) {
                        "Target by $endStr"
                    } else {
                        "Flexible budget with no deadline"
                    }
                } ?: "Flexible budget with no deadline",
                folderUsage = folderUsage.map { usage ->
                    FolderUsageUiState(
                        name = usage.folderName,
                        usagePercent = usage.usagePercent,
                        usedAmount = currencyFormatter.format(usage.usedAmount),
                        totalAmount = currencyFormatter.format(usage.limitAmount),
                        usageSummary = "You've used ${usage.usagePercent}% of ${usage.folderName}",
                        deadlineLabel = "Till ${dateFormatter.format(Date(usage.deadlineMillis))}",
                        isWarning = usage.isWarning,
                        isExceeded = usage.isExceeded,
                        progress = usage.progress
                    )
                },
                weeklyExpenseChart = it.weeklyExpenseChart.copy(
                    values = points.map { point -> point.amount.toFloat() },
                    labels = points.map { point -> point.label },
                    currentDayIndex = currentIndex,
                    isEmpty = points.none { point -> point.amount > 0.0 },
                    selectedChartPeriod = currentPeriod,
                    isLineGraph = currentPeriod == TimePeriod.MONTH
                ),
                topCategories = transactions
                    .filter { transaction -> transaction.type == TransactionType.EXPENSE }
                    .groupBy(FinanceTransaction::category)
                    .map { (category, items) ->
                        val sumAmount = items.sumOf(FinanceTransaction::amount)
                        BudgetCategoryUiState(
                            name = category,
                            amount = currencyFormatter.format(sumAmount),
                            rawAmount = sumAmount
                        )
                    }
                    .sortedByDescending { category ->
                        transactions
                            .filter { it.type == TransactionType.EXPENSE && it.category == category.name }
                            .sumOf(FinanceTransaction::amount)
                    }
                    .take(4),
                folders = folders.map { folder ->
                    FolderUiState(
                        name = folder.name,
                        isRemovable = !folder.name.equals(FALLBACK_FOLDER, ignoreCase = true),
                        limitSummary = folder.limitAmount?.let { amount ->
                            folder.limitEndDateMillis?.let { endDate ->
                                "${currencyFormatter.format(amount)} till ${dateFormatter.format(Date(endDate))}"
                            }
                        },
                        hasLimit = folder.limitAmount != null && folder.limitEndDateMillis != null,
                        limitAmount = folder.limitAmount,
                        limitEndDateMillis = folder.limitEndDateMillis
                    )
                },
                isLoading = false
            )
        }
    }

    private fun updateTransactionsState(transactions: List<FinanceTransaction>, folders: List<Folder>) {
        val currentState = _transactionsUiState.value
        val query = currentState.query.trim()
        val folderNames = folders.map(Folder::name)
        val selectedCategory = currentState.selectedCategory?.takeIf { selected ->
            folderNames.any { it.equals(selected, ignoreCase = true) }
        }
        val filtered = transactions
            .filter { transaction -> currentState.selectedType == null || transaction.type == currentState.selectedType }
            .filter { transaction -> selectedCategory == null || transaction.category == selectedCategory }
            .filter { transaction ->
                query.isBlank() || listOfNotNull(
                    transaction.category,
                    transaction.note,
                    transaction.merchantName,
                    transaction.payeeVpa
                ).any { candidate -> candidate.contains(query, ignoreCase = true) }
            }
            .sortedByDescending(FinanceTransaction::dateMillis)

        _transactionsUiState.update {
            it.copy(
                selectedCategory = selectedCategory,
                availableCategories = folderNames,
                transactions = filtered.map(::transactionToListItem),
                isLoading = false
            )
        }
    }

    private fun updateInsightsState(transactions: List<FinanceTransaction>, ledger: List<SavingsLedgerEntry> = emptyList()) {
        val insights = buildFinanceInsights(transactions, System.currentTimeMillis())
        val currentPeriod = _insightsUiState.value.selectedTimePeriod
        val (points, _) = buildChartPointsForPeriod(transactions, System.currentTimeMillis(), currentPeriod)

        val comparisonLabel = if (insights.previousWeekExpense <= 0.0) {
            "This week spent ${currencyFormatter.format(insights.currentWeekExpense)}"
        } else {
            val delta = insights.currentWeekExpense - insights.previousWeekExpense
            val direction = if (delta >= 0) "up" else "down"
            "This week is $direction ${currencyFormatter.format(abs(delta))} versus last week"
        }

        val vaultTotal = ledger.sumOf { it.saved }
        val ledgerUiState = ledger.map { entry ->
            val spentPct = if (entry.limit > 0) (entry.spent / entry.limit).toFloat().coerceIn(0f, 1f) else 0f
            val closedDate = SimpleDateFormat("d MMM", Locale.ENGLISH).format(Date(entry.cycleEndDate))
            SavingsLedgerEntryUiState(
                id = "${entry.folderId}_${entry.closedAt}",
                folderName = entry.folderName,
                spentPercent = spentPct,
                spentLabel = "${rupeeCompact(entry.spent)} of ${rupeeCompact(entry.limit)}",
                closedDateLabel = "Closed $closedDate",
                savedRaw = entry.saved,
                savedLabel = "+${rupeeCompact(entry.saved)}"
            )
        }

        _insightsUiState.update {
            it.copy(
                highestSpendingCategory = insights.highestSpendingCategory ?: "No expenses yet",
                highestSpendingAmount = currencyFormatter.format(insights.highestSpendingAmount),
                weekComparisonLabel = comparisonLabel,
                weeklyExpenseAmount = currencyFormatter.format(insights.currentWeekExpense),
                monthlyTrendPoints = points,
                categoryBreakdown = insights.categoryBreakdown,
                frequentTransactionType = when (insights.frequentTransactionType) {
                    TransactionType.INCOME -> "Income"
                    TransactionType.EXPENSE -> "Expense"
                    null -> "No transactions yet"
                },
                selectedTimePeriod = currentPeriod,
                isLineGraph = currentPeriod == TimePeriod.MONTH,
                savingsVaultTotal = currencyFormatter.format(vaultTotal),
                savingsVaultRawTotal = vaultTotal,
                savingsLedger = ledgerUiState,
                vaultGoalCount = ledger.size,
                isLoading = false
            )
        }
    }

    /** Format a double as compact rupee string: ₹4,500 */
    private fun rupeeCompact(amount: Double): String {
        return "\u20B9${String.format("%,.0f", amount)}"
    }

    private fun syncQrState(folders: List<Folder>) {
        val folderUsages = buildFolderUsageInsights(folders, allTransactions.value)
        val categories = folders.map { folder ->
            val usage = folderUsages.find { it.folderName.equals(folder.name, ignoreCase = true) }
            val availableLabel = if (usage != null) {
                val available = usage.limitAmount - usage.usedAmount
                if (available < 0) {
                    "(-${currencyFormatter.format(kotlin.math.abs(available))})"
                } else {
                    currencyFormatter.format(available)
                }
            } else {
                null
            }
            CategoryOptionUiState(name = folder.name, availableBudgetLabel = availableLabel)
        }
        val categoryNames = categories.map { it.name }
        val selectedCategory = savedStateHandle.get<String>(SELECTED_CATEGORY_KEY)?.takeIf { savedCategory ->
            categoryNames.any { it.equals(savedCategory, ignoreCase = true) }
        }
        if (selectedCategory != null) {
            savedStateHandle[SELECTED_CATEGORY_KEY] = selectedCategory
        }
        val payload = currentParsedPayload()
        _qrUiState.update {
            it.copy(
                categories = categories,
                selectedCategory = selectedCategory,
                availableUpiApps = UpiAppResolver.resolve(appContext).map(::upiAppToUiState),
                scannedMerchantName = payload?.payeeName.orEmpty(),
                scannedPayeeVpa = payload?.payeeVpa.orEmpty(),
                scannedNote = payload?.note,
                scannedAmountText = payload?.amount?.let(currencyFormatter::format).orEmpty(),
                amountInput = savedStateHandle.get<String>(AMOUNT_INPUT_KEY)
                    ?: payload?.amount?.let { String.format(Locale.US, "%.2f", it) }.orEmpty(),
                isAmountLocked = payload?.hasEmbeddedAmount == true
            )
        }
        refreshQrDerivedState()
    }

    private fun refreshQrDerivedState() {
        // canLaunchPayment now only requires an amount + category (no QR scan needed)
        val canLaunch = selectedQrCategory() != null &&
            enteredAmount() != null &&
            _qrUiState.value.availableUpiApps.isNotEmpty()
        _qrUiState.update {
            it.copy(
                amountInput = savedStateHandle.get<String>(AMOUNT_INPUT_KEY).orEmpty(),
                selectedCategory = selectedQrCategory(),
                canLaunchPayment = canLaunch
            )
        }
    }

    private fun currentParsedPayload(): ParsedUpiQr? {
        return savedStateHandle.get<String>(SCANNED_QR_KEY)?.let(UpiQrParser::parse)
    }

    private fun selectedQrCategory(): String? = savedStateHandle.get<String>(SELECTED_CATEGORY_KEY)

    private fun enteredAmount(): Double? {
        return savedStateHandle.get<String>(AMOUNT_INPUT_KEY)?.toDoubleOrNull()?.takeIf { it > 0.0 }
    }

    private fun formattedAmount(): String? {
        return enteredAmount()?.let { String.format(Locale.US, "%.2f", it) }
    }

    private fun buildPaymentRequest(payload: ParsedUpiQr): UpiPaymentRequest? {
        val amount = formattedAmount() ?: return null
        return UpiAppResolver.createPaymentRequest(
            upiId = payload.payeeVpa,
            name = payload.payeeName,
            amount = amount,
            note = payload.note.orEmpty(),
            rawUri = payload.rawUri
        )
    }

    private fun transactionToListItem(transaction: FinanceTransaction): RecentTransactionUiState {
        return RecentTransactionUiState(
            id = transaction.id,
            title = transaction.merchantName ?: transaction.category,
            subtitle = buildList {
                add(transaction.category)
                add(qrOriginLabel(transaction.source))
                transaction.upiAppLabel?.let(::add)
            }.joinToString(" • "),
            time = timeFormatter.format(Date(transaction.dateMillis)),
            amount = if (transaction.type == TransactionType.EXPENSE) {
                "-${currencyFormatter.format(transaction.amount)}"
            } else {
                "+${currencyFormatter.format(transaction.amount)}"
            },
            rawAmount = if (transaction.type == TransactionType.EXPENSE) -transaction.amount else transaction.amount,
            rawDateMillis = transaction.dateMillis,
            isExpense = transaction.type == TransactionType.EXPENSE,
            category = transaction.category
        )
    }

    private fun upiAppToUiState(app: com.paytrack.data.UpiAppInfo): UpiAppUiState {
        return UpiAppUiState(label = app.label, packageName = app.packageName, icon = app.icon)
    }

    private fun markQrError(message: String): Intent? {
        _qrUiState.update { it.copy(paymentError = message) }
        return null
    }

    private fun isRapidRepeatLaunch(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val isRepeatedTooSoon = now - lastUpiLaunchAtMillis < 2000
        if (!isRepeatedTooSoon) {
            lastUpiLaunchAtMillis = now
        }
        return isRepeatedTooSoon
    }

    private fun recordSuccessfulPayment(packageName: String?) {
        val payload = currentParsedPayload() ?: return
        val amount = enteredAmount() ?: return
        val category = selectedQrCategory() ?: return
        val selectedApp = packageName?.let { launchedPackage ->
            _qrUiState.value.availableUpiApps.firstOrNull { it.packageName == launchedPackage }
        }

        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                category = category,
                dateMillis = System.currentTimeMillis(),
                note = payload.note ?: "Paid through UPI QR",
                merchantName = payload.payeeName,
                payeeVpa = payload.payeeVpa,
                source = TransactionSource.QR_UPI,
                upiAppPackage = selectedApp?.packageName,
                upiAppLabel = selectedApp?.label
            )
            pendingUpiPackageName = null
            clearScannedQr()
        }
    }

    private fun buildFailureMessage(responseCode: String?): String {
        val base = "UPI payment failed. Response code: ${responseCode ?: "unknown"}."
        val alternateApp = _qrUiState.value.availableUpiApps
            .firstOrNull { it.packageName != lastAttemptedUpiPackageName }
            ?.label

        return if (alternateApp != null) {
            "$base Try again or switch to $alternateApp."
        } else {
            "$base Try again after checking limits, bank availability, or PSP security prompts."
        }
    }

    private fun buildUnknownResultMessage(): String {
        val alternateApp = _qrUiState.value.availableUpiApps
            .firstOrNull { it.packageName != lastAttemptedUpiPackageName }
            ?.label

        return if (alternateApp != null) {
            "UPI app did not return a final status. You can retry or try $alternateApp."
        } else {
            "UPI app did not return a final status. You can retry after checking the payment status in your PSP app."
        }
    }

    private fun updateFolderMessage(message: String?) {
        _homeUiState.update { it.copy(folderMessage = message) }
    }

    private fun endOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // ── SMS Import ────────────────────────────────────────────────────────────

    fun importSmsHistory(onResult: (imported: Int) -> Unit) {
        viewModelScope.launch {
            val count = SmsImporter.importHistory(appContext, repository)
            onResult(count)
        }
    }
}

class HomeViewModelFactory(
    private val repository: FinanceRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                repository = repository,
                appContext = appContext,
                savedStateHandle = extras.createSavedStateHandle()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
