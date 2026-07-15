package com.moneyscribbl.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarExpenditureDialog(
    dailyExpenditures: Map<LocalDate, Double>,
    currencyCode: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daily Spending",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val currentMonth = remember { YearMonth.now() }
                val startMonth = remember { currentMonth.minusMonths(12) }
                val endMonth = remember { currentMonth.plusMonths(1) }
                val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
                
                val state = rememberCalendarState(
                    startMonth = startMonth,
                    endMonth = endMonth,
                    firstVisibleMonth = currentMonth,
                    firstDayOfWeek = firstDayOfWeek
                )
                
                val coroutineScope = rememberCoroutineScope()
                val visibleMonth by remember(state) { 
                    derivedStateOf { 
                        try {
                            state.firstVisibleMonth.yearMonth
                        } catch (e: Exception) {
                            currentMonth
                        }
                    } 
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            state.animateScrollToMonth(visibleMonth.minusMonths(1))
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${visibleMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = {
                        coroutineScope.launch {
                            state.animateScrollToMonth(visibleMonth.plusMonths(1))
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val daysOfWeek = remember { 
                    val days = java.time.DayOfWeek.values()
                    val firstDay = firstDayOfWeekFromLocale()
                    val shift = firstDay.ordinal
                    Array(7) { days[(it + shift) % 7] }
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayOfWeek in daysOfWeek) {
                        Text(
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                HorizontalCalendar(
                    state = state,
                    dayContent = { day ->
                        DayContent(day, dailyExpenditures[day.date] ?: 0.0, currencyCode)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun DayContent(day: CalendarDay, amount: Double, currencyCode: String) {
    val formatter = remember(currencyCode) { 
        val locale = when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.forLanguageTag("en-IE")
            "GBP" -> Locale.UK
            else -> Locale.forLanguageTag("en-IN")
        }
        java.text.NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        } 
    }
    
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val alpha = if (isCurrentMonth) 1f else 0.3f
    
    val bgColor = if (amount > 0) {
        val intensity = (amount / 2000.0).coerceIn(0.05, 0.4).toFloat()
        Color.Red.copy(alpha = intensity * alpha)
    } else {
        Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            if (amount > 0 && isCurrentMonth) {
                Text(
                    text = formatter.format(amount),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

