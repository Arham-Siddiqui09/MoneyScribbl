package com.paytrack.viewmodel

import com.paytrack.data.TransactionType

data class HomeUiState(
    val appName: String = "PayTrack",
    val currentBalance: String = "",
    val totalIncome: String = "",
    val totalExpenses: String = "",
    val savingsProgress: Float = 0f,
    val savingsProgressLabel: String = "",
    val goalSummary: String = "",
    val folderUsage: List<FolderUsageUiState> = emptyList(),
    val weeklyExpenseChart: WeeklyExpenseChartUiState = WeeklyExpenseChartUiState(),
    val topCategories: List<BudgetCategoryUiState> = emptyList(),
    val folders: List<FolderUiState> = emptyList(),
    val folderMessage: String? = null,
    val heroPeriod: HeroPeriod = HeroPeriod.ALL,
    val isLoading: Boolean = true
)

enum class HeroPeriod {
    ALL, THIS_MONTH
}

enum class TimePeriod {
    WEEK, MONTH, YEARLY
}

data class WeeklyExpenseChartUiState(
    val values: List<Float> = List(7) { 0f },
    val labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val currentDayIndex: Int = 0,
    val isEmpty: Boolean = true,
    val selectedChartPeriod: TimePeriod = TimePeriod.WEEK,
    val isLineGraph: Boolean = false
)

data class BudgetCategoryUiState(
    val name: String = "",
    val amount: String = ""
)

data class FolderUiState(
    val name: String = "",
    val isRemovable: Boolean = true,
    val limitSummary: String? = null,
    val hasLimit: Boolean = false,
    val limitAmount: Double? = null,
    val limitEndDateMillis: Long? = null
)

data class FolderUsageUiState(
    val name: String = "",
    val usagePercent: Int = 0,
    val usedAmount: String = "",
    val totalAmount: String = "",
    val usageSummary: String = "",
    val deadlineLabel: String = "",
    val isWarning: Boolean = false,
    val isExceeded: Boolean = false,
    val progress: Float = 0f
)

data class RecentTransactionUiState(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val time: String = "",
    val amount: String = "",
    val rawAmount: Double = 0.0,
    val rawDateMillis: Long = 0L,
    val isExpense: Boolean = true,
    val category: String = ""
)

data class TransactionsUiState(
    val query: String = "",
    val selectedType: TransactionType? = null,
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val transactions: List<RecentTransactionUiState> = emptyList(),
    val isLoading: Boolean = true
)

data class InsightsUiState(
    val highestSpendingCategory: String = "",
    val highestSpendingAmount: String = "",
    val weekComparisonLabel: String = "",
    val weeklyExpenseAmount: String = "",
    val monthlyTrendPoints: List<ChartPointUiState> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownUiState> = emptyList(),
    val frequentTransactionType: String = "",
    val selectedTimePeriod: TimePeriod = TimePeriod.YEARLY,
    val isLineGraph: Boolean = true,
    // ── Savings Vault ─────────────────────────────────────────────────────────
    val savingsVaultTotal: String = "₹0",
    val savingsVaultRawTotal: Double = 0.0,
    val savingsLedger: List<SavingsLedgerEntryUiState> = emptyList(),
    val vaultGoalCount: Int = 0,
    // ─────────────────────────────────────────────────────────────────────────
    val isLoading: Boolean = true
)

data class SavingsLedgerEntryUiState(
    val id: String = "",
    val folderName: String = "",
    /** spent / limit, clamped to 0..1 for the progress ring. */
    val spentPercent: Float = 0f,
    /** E.g. "₹290 of ₹300" */
    val spentLabel: String = "",
    /** E.g. "Closed 23 Jul" */
    val closedDateLabel: String = "",
    /** Raw saved value, used for count-up animation. */
    val savedRaw: Double = 0.0,
    /** E.g. "+₹4,500" */
    val savedLabel: String = ""
)

data class ChartPointUiState(
    val label: String = "",
    val amount: Double = 0.0
)

data class CategoryBreakdownUiState(
    val category: String = "",
    val amount: Double = 0.0
)

data class CategoryOptionUiState(
    val name: String,
    val availableBudgetLabel: String?
)

data class QrScanUiState(
    val categories: List<CategoryOptionUiState> = emptyList(),
    val availableUpiApps: List<UpiAppUiState> = emptyList(),
    val scannedMerchantName: String = "",
    val scannedPayeeVpa: String = "",
    val scannedNote: String? = null,
    val scannedAmountText: String = "",
    val amountInput: String = "",
    val isAmountLocked: Boolean = false,
    val selectedCategory: String? = null,
    val scanError: String? = null,
    val paymentError: String? = null,
    val hasCameraPermission: Boolean = false,
    val canLaunchPayment: Boolean = false,
    // Confirm-on-return dialog state
    val showPaymentConfirmDialog: Boolean = false,
    val pendingConfirmAmount: String = "",
    val pendingConfirmCategory: String = "",
    val pendingConfirmAppLabel: String = ""
)

data class UpiAppUiState(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable? = null
)

data class TransactionFormData(
    val id: String? = null,
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class SavingsGoalFormData(
    val targetAmount: Double = 0.0,
    val startDateMillis: Long? = null,
    val targetDateMillis: Long? = null
)
