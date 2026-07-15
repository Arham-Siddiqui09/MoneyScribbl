package com.moneyscribbl.ui.insights



import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.home.components.WeeklyExpenseChart
import com.moneyscribbl.ui.theme.ChartColors
import com.moneyscribbl.ui.theme.GradientVault
import com.moneyscribbl.ui.theme.IndigoPrimary
import com.moneyscribbl.ui.theme.VioletAccent
import com.moneyscribbl.ui.theme.VioletLight
import com.moneyscribbl.viewmodel.InsightsUiState
import com.moneyscribbl.viewmodel.SavingsLedgerEntryUiState
import com.moneyscribbl.viewmodel.TimePeriod
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// â”€â”€â”€ Route â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun InsightsRoute(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
    onCategoryBreakdownPeriodSelected: (TimePeriod) -> Unit,
    onDeleteVault: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    InsightsScreen(
        uiState = uiState,
        onChartPeriodSelected = onChartPeriodSelected,
        onCategoryBreakdownPeriodSelected = onCategoryBreakdownPeriodSelected,
        onDeleteVault = onDeleteVault,
        modifier = modifier
    )
}

// â”€â”€â”€ Screen â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
    onCategoryBreakdownPeriodSelected: (TimePeriod) -> Unit,
    onDeleteVault: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = IndigoPrimary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // â”€â”€ Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 28.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Insights into your spending habits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // â”€â”€ Stat Row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            StatCard(
                                title = "Top Category",
                                valuePrimary = uiState.highestSpendingCategory.ifBlank { "--" },
                                valueSecondary = uiState.highestSpendingAmount.ifBlank { "₹0" },
                                icon = Icons.Outlined.EmojiEvents,
                                iconBg = Color(0xFFF1ECFF),
                                iconTint = VioletAccent,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "This Week",
                                valuePrimary = uiState.weeklyExpenseAmount.ifBlank { "₹0" },
                                valueSecondary = "Expense",
                                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                                iconBg = Color(0xFFEEECFF),
                                iconTint = IndigoPrimary,
                                modifier = Modifier.weight(1f),
                                primaryIsMonospace = true
                            )
                        }
                    }

                    // â”€â”€ Spending Trend â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    item {
                        SpendingTrendCard(uiState = uiState, onChartPeriodSelected = onChartPeriodSelected)
                    }

                    // â”€â”€ Savings Vault â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                    item {
                        SavingsVaultCard(
                            vaultTotal = uiState.savingsVaultTotal,
                            vaultRawTotal = uiState.savingsVaultRawTotal,
                            goalCount = uiState.vaultGoalCount,
                            ledger = uiState.savingsLedger,
                            currencyCode = uiState.currencyCode,
                            onDeleteVault = onDeleteVault
                        )
                    }

                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item {
                            CategoryBreakdownCard(
                                breakdown = uiState.categoryBreakdown,
                                selectedPeriod = uiState.selectedCategoryBreakdownPeriod,
                                onPeriodSelected = onCategoryBreakdownPeriodSelected
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// â”€â”€â”€ Stat Card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun StatCard(
    title: String,
    valuePrimary: String,
    valueSecondary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    primaryIsMonospace: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valuePrimary,
                    style = if (primaryIsMonospace)
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono
                        )
                    else
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = valueSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = if (!primaryIsMonospace) com.moneyscribbl.ui.theme.IBMPlexMono else null
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// â”€â”€â”€ Spending Trend Card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SpendingTrendCard(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Header row with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Spending Trend",style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                TimePeriodToggle(
                    selectedPeriod = uiState.selectedTimePeriod,
                    onPeriodSelected = onChartPeriodSelected
                )
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

// â”€â”€â”€ Savings Vault Card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SavingsVaultCard(
    vaultTotal: String,
    vaultRawTotal: Double,
    goalCount: Int,
    ledger: List<SavingsLedgerEntryUiState>,
    currencyCode: String,
    onDeleteVault: (String) -> Unit
) {
    val gradientBrush = Brush.linearGradient(colors = GradientVault)

    // Count-up animation for vault total
    val animatedValue = remember { Animatable(0f) }
    LaunchedEffect(vaultRawTotal) {
        animatedValue.animateTo(
            targetValue = vaultRawTotal.toFloat(),
            animationSpec = tween(durationMillis = 1200)
        )
    }
    // Cache the formatter — NumberFormat creation is expensive; recreate only when currency changes
    val vaultFormatter = remember(currencyCode) {
        val locale = when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.forLanguageTag("en-IE")
            "GBP" -> Locale.UK
            else -> Locale.forLanguageTag("en-IN")
        }
        NumberFormat.getCurrencyInstance(locale)
    }
    val displayTotal = remember(animatedValue.value, vaultFormatter) {
        vaultFormatter.format(animatedValue.value.toDouble())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(gradientBrush)
    ) {
        // Decorative oversized circles
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                radius = 130.dp.toPx(),
                center = Offset(size.width - 30.dp.toPx(), (-20).dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = 90.dp.toPx(),
                center = Offset(30.dp.toPx(), size.height + 20.dp.toPx())
            )
        }

        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "Savings Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (goalCount > 0) {
                    // Achievement pill
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$goalCount ${if (goalCount == 1) "cycle" else "cycles"} closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Caption
            Text(
                "Money you kept by staying under budget",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            // Large total
            Text(
                text = displayTotal,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                ),
                color = Color.White
            )

            // Inset ledger panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xF0FFFFFF))
                    .padding(vertical = 6.dp)
            ) {
                if (ledger.isEmpty()) {
                    Text(
                        text = "No closed cycles yet.\nSet a folder spending limit to start tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    var isExpanded by remember { mutableStateOf(false) }
                    val displayLedger = if (isExpanded) ledger else ledger.take(3)
                    
                    Column {
                        displayLedger.forEachIndexed { index, entry ->
                            LedgerRow(
                                entry = entry,
                                onDelete = { onDeleteVault(entry.id) }
                            )
                            if (index < displayLedger.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .height(1.dp)
                                        .background(Color(0x12000000))
                                )
                            }
                        }
                        
                        if (ledger.size > 3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .height(1.dp)
                                    .background(Color(0x12000000))
                            )
                            androidx.compose.material3.TextButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) "Show Less" else "View all ${ledger.size} vaults",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF5B4CFC),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Footer caption
            Text(
                "Automatically added to your Savings Goal on Home.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// â”€â”€â”€ Ledger Row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun LedgerRow(
    entry: SavingsLedgerEntryUiState,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = entry.spentPercent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ring_${entry.folderName}"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular progress ring
        SpentRing(progress = animatedProgress, modifier = Modifier.size(42.dp))

        // Labels
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = entry.folderName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF14141F)
            )
            Text(
                text = "${entry.closedDateLabel} · ${entry.spentLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                maxLines = 1
            )
        }

        // Saved amount
        Text(
            text = entry.savedLabel,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0FA968)
            )
        )

        Box {
            androidx.compose.material3.IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color(0xFF9CA3AF) // Subtle grey
                )
            }
            androidx.compose.material3.DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .width(200.dp)
            ) {
                // Edit option (non-destructive, shown above for context)


                // Delete option (destructive)
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete Record",
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// â”€â”€â”€ Spent Ring (Canvas) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SpentRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val trackColor = Color(0xFFD1FAE5)
    val fillColor = Color(0xFF0FA968)

    val density = androidx.compose.ui.platform.LocalDensity.current
    val textPaint = remember(density) {
        android.graphics.Paint().apply {
            textSize = with(density) { 9.sp.toPx() }
            color = android.graphics.Color.parseColor("#0FA968")
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val inset = strokeWidth / 2f
        val arcRect = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcRect,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Fill
        if (progress > 0f) {
            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcRect,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center percentage text
        val pct = "${(progress * 100).toInt()}%"
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                pct,
                size.width / 2f,
                size.height / 2f + textPaint.textSize / 3f,
                textPaint
            )
        }
    }
}

// â”€â”€â”€ Category Breakdown Card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun CategoryBreakdownCard(
    breakdown: List<com.moneyscribbl.viewmodel.CategoryBreakdownUiState>,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TimePeriodToggle(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }

            val othersColor = Color(0xFFB0BEC5)
            val topCount = 5

            // Cache expensive list operations — only recompute when breakdown data actually changes
            val (chartItemsWithColor, total) = remember(breakdown) {
                val sortedBreakdown = breakdown.sortedByDescending { it.amount }
                val topItems = sortedBreakdown.take(topCount)
                val otherItems = sortedBreakdown.drop(topCount)
                val othersAmount = otherItems.sumOf { it.amount }
                val computedTotal = sortedBreakdown.sumOf { it.amount }.let { if (it <= 0.0) 1.0 else it }
                val items = buildList {
                    topItems.forEachIndexed { index, item ->
                        add(Pair(item, ChartColors[index % ChartColors.size]))
                    }
                    if (othersAmount > 0) {
                        add(Pair(com.moneyscribbl.viewmodel.CategoryBreakdownUiState("Others", othersAmount), othersColor))
                    }
                }.sortedByDescending { it.first.amount }
                Pair(items, computedTotal)
            }

            // Donut chart with center total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    items = chartItemsWithColor.map { it.first },
                    total = total,
                    colors = chartItemsWithColor.map { it.second },
                    modifier = Modifier.size(190.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", total)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Spend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend list
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                chartItemsWithColor.forEach { (item, color) ->
                    val percent = (item.amount / total * 100)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${percent.roundToInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "₹${String.format("%,.0f", item.amount)}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    items: List<com.moneyscribbl.viewmodel.CategoryBreakdownUiState>,
    total: Double,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    require(colors.size >= items.size) {
        "colors list must have at least as many entries as items â€” got ${colors.size} for ${items.size} items"
    }

    var animationPlayed by remember { mutableStateOf(false) }
    // Use Unit so animation plays once on first composition, not on every list reference change
    LaunchedEffect(Unit) { animationPlayed = true }

    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.16f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        val startAngles = mutableListOf<Float>()
        var currentAngle = -90f
        items.forEach { item ->
            startAngles.add(currentAngle)
            currentAngle += (item.amount / total * 360f).toFloat()
        }

        for (i in items.indices.reversed()) {
            val item = items[i]
            val startAngle = startAngles[i]
            val rawSweep = (item.amount / total * 360f).toFloat()
            val sweep = rawSweep * progress

            drawArc(
                color = colors[i],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }
    }
}
// --- Shared Components ---------------------------------------------------------
@Composable
fun TimePeriodToggle(
    selectedPeriod: com.moneyscribbl.viewmodel.TimePeriod,
    onPeriodSelected: (com.moneyscribbl.viewmodel.TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                androidx.compose.foundation.shape.CircleShape
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        com.moneyscribbl.viewmodel.TimePeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period

            // Color animations are fast tween(250ms) — lightweight and settle quickly
            val backgroundColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected)
                    Color(0xFF6366F1)
                else
                    androidx.compose.ui.graphics.Color.Transparent,
                animationSpec = tween(durationMillis = 250),
                label = "pillBackground"
            )

            val textColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                animationSpec = tween(durationMillis = 250),
                label = "pillText"
            )

            // Removed: spring scale animation (long-running, causes recompositions during scroll)
            // Removed: shadow modifier (triggers offscreen render pass on every selected pill)
            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPeriodSelected(period) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = period.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected)
                        androidx.compose.ui.text.font.FontWeight.SemiBold
                    else
                        androidx.compose.ui.text.font.FontWeight.Medium,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
