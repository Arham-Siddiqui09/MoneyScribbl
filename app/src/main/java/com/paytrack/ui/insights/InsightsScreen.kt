package com.paytrack.ui.insights



import kotlin.math.roundToInt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paytrack.ui.home.components.WeeklyExpenseChart
import com.paytrack.ui.theme.ChartColors
import com.paytrack.ui.theme.GradientVault
import com.paytrack.ui.theme.IndigoPrimary
import com.paytrack.ui.theme.VioletAccent
import com.paytrack.ui.theme.VioletLight
import com.paytrack.viewmodel.InsightsUiState
import com.paytrack.viewmodel.SavingsLedgerEntryUiState
import com.paytrack.viewmodel.TimePeriod
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// ─── Route ──────────────────────────────────────────────────────────────────

@Composable
fun InsightsRoute(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
    onDeleteVault: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    InsightsScreen(
        uiState = uiState,
        onChartPeriodSelected = onChartPeriodSelected,
        onDeleteVault = onDeleteVault,
        modifier = modifier
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onChartPeriodSelected: (TimePeriod) -> Unit,
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
                .padding(innerPadding)
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
                    // ── Header ──────────────────────────────────────────────
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

                    // ── Stat Row ─────────────────────────────────────────────
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

                    // ── Spending Trend ───────────────────────────────────────
                    item {
                        SpendingTrendCard(uiState = uiState, onChartPeriodSelected = onChartPeriodSelected)
                    }

                    // ── Savings Vault ────────────────────────────────────────
                    item {
                        SavingsVaultCard(
                            vaultTotal = uiState.savingsVaultTotal,
                            vaultRawTotal = uiState.savingsVaultRawTotal,
                            goalCount = uiState.vaultGoalCount,
                            ledger = uiState.savingsLedger,
                            onDeleteVault = onDeleteVault
                        )
                    }

                    // ── Category Breakdown ───────────────────────────────────
                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item {
                            CategoryBreakdownCard(breakdown = uiState.categoryBreakdown)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

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
                            fontFamily = com.paytrack.ui.theme.IBMPlexMono
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
                        fontFamily = if (!primaryIsMonospace) com.paytrack.ui.theme.IBMPlexMono else null
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Spending Trend Card ─────────────────────────────────────────────────────

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
                // Week / Month toggle
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimePeriod.entries.forEach { period ->
                        val isSelected = uiState.selectedTimePeriod == period
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { onChartPeriodSelected(period) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = period.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
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

// ─── Savings Vault Card ──────────────────────────────────────────────────────

@Composable
private fun SavingsVaultCard(
    vaultTotal: String,
    vaultRawTotal: Double,
    goalCount: Int,
    ledger: List<SavingsLedgerEntryUiState>,
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
    val displayTotal = remember(animatedValue.value) {
        val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        formatter.format(animatedValue.value.toDouble())
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
                    fontFamily = com.paytrack.ui.theme.IBMPlexMono,
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

// ─── Ledger Row ───────────────────────────────────────────────────────────────

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
                fontFamily = com.paytrack.ui.theme.IBMPlexMono,
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
                containerColor = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(16.dp))
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

// ─── Spent Ring (Canvas) ─────────────────────────────────────────────────────

@Composable
private fun SpentRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val trackColor = Color(0xFFD1FAE5)
    val fillColor = Color(0xFF0FA968)

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
            val textPaint = android.graphics.Paint().apply {
                textSize = 9.sp.toPx()
                color = android.graphics.Color.parseColor("#0FA968")
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawText(
                pct,
                size.width / 2f,
                size.height / 2f + textPaint.textSize / 3f,
                textPaint
            )
        }
    }
}

// ─── Category Breakdown Card ──────────────────────────────────────────────────

@Composable
private fun CategoryBreakdownCard(
    breakdown: List<com.paytrack.viewmodel.CategoryBreakdownUiState>
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            val othersColor = Color(0xFFB0BEC5)
            val topCount = 5
            val topItems = breakdown.take(topCount)
            val otherItems = breakdown.drop(topCount)
            val othersAmount = otherItems.sumOf { it.amount }
            
            val total = breakdown.sumOf { it.amount }.let { if (it <= 0.0) 1.0 else it }
            
            val chartItems = if (othersAmount > 0) {
                topItems +  com.paytrack.viewmodel.CategoryBreakdownUiState("Others", othersAmount)
            } else {
                topItems
            }
            val donutColors = topItems.mapIndexed { index, _ -> ChartColors[index % ChartColors.size] } + listOf(othersColor)

            // Donut chart with center total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    items = chartItems,
                    total = total,
                    colors = donutColors,
                    modifier = Modifier.size(190.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "₹${String.format("%,.0f", total)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = com.paytrack.ui.theme.IBMPlexMono
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
                breakdown.forEachIndexed { index, item ->
                    val color = if (index < topCount) ChartColors[index % ChartColors.size] else othersColor
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
                                fontFamily = com.paytrack.ui.theme.IBMPlexMono
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
    items: List<com.paytrack.viewmodel.CategoryBreakdownUiState>,
    total: Double,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(items) { animationPlayed = true }

    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )

    val baseGapDegrees = if (items.size > 1) 4f else 0f

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.16f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        items.forEachIndexed { index, item ->
            val rawSweep = (item.amount / total * 360f).toFloat()
            // Make sure small slices don't get swallowed by the gap, allowing them to animate
            val actualGap = minOf(baseGapDegrees, rawSweep * 0.5f)
            val sweep = (rawSweep - actualGap).coerceAtLeast(0f) * progress

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += rawSweep
        }
    }
}