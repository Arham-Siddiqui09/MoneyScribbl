package com.moneyscribbl.viewmodel

import com.moneyscribbl.data.Folder
import com.moneyscribbl.data.FinanceTransaction
import com.moneyscribbl.data.TransactionSource
import com.moneyscribbl.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal data class FinanceInsightsData(
    val weeklyExpensePoints: List<ChartPointUiState>,
    val weeklyExpenseLabels: List<String>,
    val currentWeekdayIndex: Int,
    val monthlyTrendPoints: List<ChartPointUiState>,
    val categoryBreakdown: List<CategoryBreakdownUiState>,
    val currentWeekExpense: Double,
    val previousWeekExpense: Double,
    val highestSpendingCategory: String?,
    val highestSpendingAmount: Double,
    val frequentTransactionType: TransactionType?
)

internal data class FolderUsageData(
    val folderName: String,
    val usedAmount: Double,
    val limitAmount: Double,
    val usagePercent: Int,
    val deadlineMillis: Long,
    val isWarning: Boolean,
    val isExceeded: Boolean,
    val progress: Float
)

internal fun buildFinanceInsights(
    transactions: List<FinanceTransaction>,
    nowMillis: Long,
    categoryBreakdownPeriod: TimePeriod = TimePeriod.MONTH
): FinanceInsightsData {
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val weekStart = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val difference = (get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7
        add(Calendar.DAY_OF_MONTH, -difference)
    }
    val previousWeekStart = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -7) }
    val nextWeekStart = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
    val weeklyTotals = MutableList(7) { 0.0 }
    val weeklyLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val currentWeekdayIndex = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
    }.let { calendar ->
        (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    }

    expenseTransactions.forEach { transaction ->
        val dayIndex = ((startOfDay(transaction.dateMillis) - weekStart.timeInMillis) / MILLIS_PER_DAY).toInt()
        if (dayIndex in 0..6) {
            weeklyTotals[dayIndex] += transaction.amount
        }
    }

    val rollingMonth = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, -5)
    }
    val monthBuckets = linkedMapOf<Pair<Int, Int>, Double>()
    repeat(6) {
        monthBuckets[rollingMonth.get(Calendar.YEAR) to rollingMonth.get(Calendar.MONTH)] = 0.0
        rollingMonth.add(Calendar.MONTH, 1)
    }
    expenseTransactions.forEach { transaction ->
        val calendar = Calendar.getInstance().apply { timeInMillis = transaction.dateMillis }
        val key = calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH)
        if (monthBuckets.containsKey(key)) {
            monthBuckets[key] = monthBuckets.getValue(key) + transaction.amount
        }
    }

    val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val breakdownStartMillis = when (categoryBreakdownPeriod) {
        TimePeriod.WEEK -> {
            val cal = today.clone() as Calendar
            cal.firstDayOfWeek = Calendar.MONDAY
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val difference = (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + 7) % 7
            cal.add(Calendar.DAY_OF_MONTH, -difference)
            cal.timeInMillis
        }
        TimePeriod.MONTH -> {
            val cal = today.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        TimePeriod.YEARLY -> {
            val cal = today.clone() as Calendar
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
    }

    val categoryBreakdown = expenseTransactions
        .filter { it.dateMillis >= breakdownStartMillis && it.dateMillis <= endOfDay(nowMillis) }
        .groupBy(FinanceTransaction::category)
        .map { (category, items) ->
            CategoryBreakdownUiState(category = category, amount = items.sumOf(FinanceTransaction::amount))
        }
        .sortedByDescending(CategoryBreakdownUiState::amount)

    val frequentType = transactions
        .groupingBy(FinanceTransaction::type)
        .eachCount()
        .maxByOrNull { it.value }
        ?.key

    val monthFormatter = SimpleDateFormat("MMM", Locale.ENGLISH)

    return FinanceInsightsData(
        weeklyExpensePoints = weeklyLabels
            .mapIndexed { index, label -> ChartPointUiState(label = label, amount = weeklyTotals[index]) },
        weeklyExpenseLabels = weeklyLabels,
        currentWeekdayIndex = currentWeekdayIndex,
        monthlyTrendPoints = monthBuckets.map { (yearMonth, amount) ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, yearMonth.first)
                set(Calendar.MONTH, yearMonth.second)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            ChartPointUiState(label = monthFormatter.format(calendar.time), amount = amount)
        },
        categoryBreakdown = categoryBreakdown,
        currentWeekExpense = expenseTransactions
            .filter { it.dateMillis in weekStart.timeInMillis until nextWeekStart.timeInMillis }
            .sumOf(FinanceTransaction::amount),
        previousWeekExpense = expenseTransactions
            .filter { it.dateMillis in previousWeekStart.timeInMillis until weekStart.timeInMillis }
            .sumOf(FinanceTransaction::amount),
        highestSpendingCategory = categoryBreakdown.firstOrNull()?.category,
        highestSpendingAmount = categoryBreakdown.firstOrNull()?.amount ?: 0.0,
        frequentTransactionType = frequentType
    )
}

internal fun calculateSavedAmount(transactions: List<FinanceTransaction>): Double {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf(FinanceTransaction::amount)
    val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf(FinanceTransaction::amount)
    return income - expense
}

internal fun recentTransactions(
    transactions: List<FinanceTransaction>,
    count: Int
): List<FinanceTransaction> {
    return transactions.sortedByDescending(FinanceTransaction::dateMillis).take(count)
}

internal fun qrOriginLabel(source: TransactionSource): String {
    return when (source) {
        TransactionSource.QR_UPI -> "UPI Payment"
        TransactionSource.SMS_BANK -> "Auto (SMS)"
        TransactionSource.MANUAL -> "Manual entry"
    }
}

internal fun buildFolderUsageInsights(
    folders: List<Folder>,
    transactions: List<FinanceTransaction>
): List<FolderUsageData> {
    return folders.mapNotNull { folder ->
        val limitAmount = folder.limitAmount
        val startMillis = folder.limitStartDateMillis
        val endMillis = folder.limitEndDateMillis?.let(::endOfDay)
        if (limitAmount == null || limitAmount <= 0.0 || startMillis == null || endMillis == null) {
            return@mapNotNull null
        }

        val usedAmount = transactions
            .asSequence()
            .filter { it.type == TransactionType.EXPENSE }
            .filter { it.category.equals(folder.name, ignoreCase = true) }
            .filter { it.dateMillis in startMillis..endMillis }
            .sumOf(FinanceTransaction::amount)

        val usageRatio = usedAmount / limitAmount
        FolderUsageData(
            folderName = folder.name,
            usedAmount = usedAmount,
            limitAmount = limitAmount,
            usagePercent = (usageRatio * 100).toInt(),
            deadlineMillis = folder.limitEndDateMillis,
            isWarning = usageRatio >= 0.8,
            isExceeded = usageRatio > 1.0,
            progress = usageRatio.toFloat().coerceIn(0f, 1f)
        )
    }.sortedWith(
        compareByDescending<FolderUsageData> { it.usagePercent }
            .thenByDescending { it.usedAmount }
    )
}

private fun startOfDay(timeMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
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

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

internal fun buildChartPointsForPeriod(
    transactions: List<FinanceTransaction>,
    nowMillis: Long,
    period: TimePeriod
): Pair<List<ChartPointUiState>, Int> {
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    return when (period) {
        TimePeriod.WEEK -> {
            // Rolling 7 days ending today
            val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val points = mutableListOf<ChartPointUiState>()
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            
            for (i in 6 downTo 0) {
                val dayCal = today.clone() as Calendar
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                val start = startOfDay(dayCal.timeInMillis)
                val end = endOfDay(dayCal.timeInMillis)
                
                val total = expenseTransactions
                    .filter { it.dateMillis in start..end }
                    .sumOf { it.amount }
                
                val label = dayFormat.format(dayCal.time)
                points.add(ChartPointUiState(label = label, amount = total))
            }
            Pair(points, 6) // The last index is always today
        }
        TimePeriod.MONTH -> {
            // Days from 1st of current month up to today
            val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val currentDay = today.get(Calendar.DAY_OF_MONTH)
            val points = mutableListOf<ChartPointUiState>()
            
            for (day in 1..currentDay) {
                val dayCal = today.clone() as Calendar
                dayCal.set(Calendar.DAY_OF_MONTH, day)
                
                val start = startOfDay(dayCal.timeInMillis)
                val end = endOfDay(dayCal.timeInMillis)
                
                val total = expenseTransactions
                    .filter { it.dateMillis in start..end }
                    .sumOf { it.amount }
                
                points.add(ChartPointUiState(label = day.toString(), amount = total))
            }
            Pair(points, points.lastIndex)
        }
        TimePeriod.YEARLY -> {
            // Rolling 12 months ending in current month
            val today = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val points = mutableListOf<ChartPointUiState>()
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            
            for (i in 11 downTo 0) {
                val monthCal = today.clone() as Calendar
                monthCal.add(Calendar.MONTH, -i)
                
                val startOfMonth = monthCal.clone() as Calendar
                startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
                val start = startOfDay(startOfMonth.timeInMillis)
                
                val endOfMonth = monthCal.clone() as Calendar
                endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = endOfDay(endOfMonth.timeInMillis)
                
                val total = expenseTransactions
                    .filter { it.dateMillis in start..end }
                    .sumOf { it.amount }
                
                val label = monthFormat.format(monthCal.time)
                points.add(ChartPointUiState(label = label, amount = total))
            }
            Pair(points, 11) // The last index is always current month
        }
    }
}

