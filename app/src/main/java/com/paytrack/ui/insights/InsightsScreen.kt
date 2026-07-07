package com.paytrack.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paytrack.ui.home.components.WeeklyExpenseChart
import com.paytrack.ui.theme.ChartColors
import com.paytrack.ui.theme.IndigoPrimary
import com.paytrack.ui.theme.VioletAccent
import com.paytrack.viewmodel.InsightsUiState
import com.paytrack.viewmodel.TimePeriod

@Composable
fun InsightsRoute(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    InsightsScreen(
        uiState = uiState,
        onChartPeriodSelected = onChartPeriodSelected,
        modifier = modifier
    )
}

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = IndigoPrimary
                )
            } else if (uiState.categoryBreakdown.isEmpty() && uiState.monthlyTrendPoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("📊", style = MaterialTheme.typography.displayLarge)
                        Text("No Data Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Add some transactions to see your insights.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Insights into your spending habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SummaryCard(
                                title = "Top Category",
                                value1 = uiState.highestSpendingCategory,
                                value2 = uiState.highestSpendingAmount,
                                icon = Icons.Outlined.EmojiEvents,
                                iconBg = VioletAccent,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "This Week",
                                value1 = uiState.weekComparisonLabel,
                                value2 = uiState.frequentTransactionType,
                                icon = Icons.Outlined.TrendingUp,
                                iconBg = IndigoPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Spending Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        TimePeriod.values().forEach { period ->
                                            val isSelected = uiState.selectedTimePeriod == period
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                    .clickable { onChartPeriodSelected(period) }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                if (uiState.monthlyTrendPoints.isEmpty()) {
                                    Text(
                                        "No data available for this period.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    WeeklyExpenseChart(
                                        data = uiState.monthlyTrendPoints.map { it.amount.toFloat() },
                                        labels = uiState.monthlyTrendPoints.map { it.label },
                                        currentDayIndex = uiState.monthlyTrendPoints.lastIndex,
                                        isLineGraph = uiState.isLineGraph,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Text("Category Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    
                                    val maxAmount = uiState.categoryBreakdown.maxOfOrNull { it.amount } ?: 1.0
                                    
                                    uiState.categoryBreakdown.take(5).forEachIndexed { index, breakdown ->
                                        val color = ChartColors[index % ChartColors.size]
                                        val animatedProgress by animateFloatAsState(
                                            targetValue = (breakdown.amount / maxAmount).toFloat().coerceIn(0f, 1f),
                                            animationSpec = spring(),
                                            label = "barAnim"
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(color, CircleShape)
                                                    )
                                                    Text(breakdown.category, style = MaterialTheme.typography.titleSmall)
                                                }
                                                Text(
                                                    breakdown.amount.toMonthlyExpenseLabel(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(animatedProgress)
                                                        .height(8.dp)
                                                        .background(color, RoundedCornerShape(999.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value1: String,
    value2: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value1.ifBlank { "--" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    value2.ifBlank { "--" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Double.toMonthlyExpenseLabel(): String {
    return if (this <= 0.0) "\u20B90" else "\u20B9${toInt()}"
}
