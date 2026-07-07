package com.paytrack.ui.home

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paytrack.ui.home.components.WeeklyExpenseChart
import com.paytrack.ui.theme.*
import com.paytrack.viewmodel.*
import java.util.Calendar
import java.util.Date

@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onOpenProfile: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenQr: () -> Unit,
    onEditGoal: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.paytrack.viewmodel.TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreen(
        uiState = uiState,
        onOpenProfile = onOpenProfile,
        onAddTransaction = onAddTransaction,
        onOpenTransactions = onOpenTransactions,
        onOpenQr = onOpenQr,
        onEditGoal = onEditGoal,
        onCreateFolder = onCreateFolder,
        onSaveFolderLimit = onSaveFolderLimit,
        onClearFolderLimit = onClearFolderLimit,
        onDeleteFolder = onDeleteFolder,
        onClearFolderMessage = onClearFolderMessage,
        onChartPeriodSelected = onChartPeriodSelected,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenProfile: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenQr: () -> Unit,
    onEditGoal: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.paytrack.viewmodel.TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<FolderUiState?>(null) }
    var createAttempted by remember { mutableStateOf(false) }

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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        HeroBalanceCard(
                            balance = uiState.currentBalance,
                            income = uiState.totalIncome,
                            expenses = uiState.totalExpenses,
                            onOpenProfile = onOpenProfile
                        )
                    }
                    item {
                        ActionRow(
                            onAddTransaction = onAddTransaction,
                            onOpenQr = onOpenQr,
                            onOpenTransactions = onOpenTransactions
                        )
                    }
                    item {
                        GoalCard(
                            progress = uiState.savingsProgress,
                            progressLabel = uiState.savingsProgressLabel,
                            goalSummary = uiState.goalSummary,
                            onEditGoal = onEditGoal
                        )
                    }
                    item {
                        WeeklyChartCard(
                            chartState = uiState.weeklyExpenseChart,
                            onChartPeriodSelected = onChartPeriodSelected
                        )
                    }
                    if (uiState.topCategories.isNotEmpty()) {
                        item {
                            SectionTitle(title = "Top Categories")
                        }
                        items(uiState.topCategories.withIndex().toList()) { (index, category) ->
                            val color = ChartColors[index % ChartColors.size]
                            CategorySummaryRow(title = category.name, value = category.amount, color = color)
                        }
                    } else {
                        item {
                            SectionTitle(title = "Top Categories")
                        }
                        item { EmptyCard("Your category spend summary will appear after the first expense.") }
                    }
                    item {
                        FolderSectionHeader(onCreateFolder = {
                            onClearFolderMessage()
                            createAttempted = false
                            showCreateDialog = true
                        })
                    }
                    uiState.folderMessage?.let { message ->
                        item { EmptyCard(message) }
                    }
                    if (uiState.folders.isEmpty()) {
                        item { EmptyCard("📁 Create a folder to track your budgets.") }
                    } else {
                        items(uiState.folders) { folder ->
                            FolderCard(
                                folder = folder,
                                usage = uiState.folderUsage.find { it.name == folder.name },
                                onSetLimit = {
                                    onClearFolderMessage()
                                    selectedFolder = folder
                                },
                                onDeleteFolder = onDeleteFolder,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateFolderDialog(
            onDismiss = {
                showCreateDialog = false
                createAttempted = false
                onClearFolderMessage()
            },
            errorMessage = uiState.folderMessage,
            onCreateFolder = {
                createAttempted = true
                onCreateFolder(it)
            }
        )
    }

    selectedFolder?.let { folder ->
        EditFolderLimitDialog(
            folder = folder,
            errorMessage = uiState.folderMessage,
            onDismiss = {
                selectedFolder = null
                onClearFolderMessage()
            },
            onSave = { amount, endDate ->
                onSaveFolderLimit(folder.name, amount, endDate)
                selectedFolder = null
            },
            onClearLimit = {
                onClearFolderLimit(folder.name)
                selectedFolder = null
            }
        )
    }

    LaunchedEffect(uiState.folders.size, uiState.folderMessage, showCreateDialog, createAttempted) {
        if (showCreateDialog && createAttempted && uiState.folderMessage == null) {
            showCreateDialog = false
            createAttempted = false
        }
    }
}

@Composable
private fun HeroBalanceCard(balance: String, income: String, expenses: String, onOpenProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = GradientHero
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Good morning ✨",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    IconButton(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Open profile",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = balance,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroMetricPill(
                        icon = Icons.Outlined.ArrowUpward,
                        label = "Income",
                        value = income,
                        iconTint = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricPill(
                        icon = Icons.Outlined.ArrowDownward,
                        label = "Expenses",
                        value = expenses,
                        iconTint = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ActionRow(
    onAddTransaction: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionPill(
            label = "Add",
            icon = Icons.Outlined.Add,
            backgroundColor = IndigoPrimary,
            contentColor = Color.White,
            onClick = onAddTransaction,
            modifier = Modifier.weight(1f)
        )
        ActionPill(
            label = "Scan QR",
            icon = Icons.Outlined.QrCodeScanner,
            backgroundColor = VioletAccent,
            contentColor = Color.White,
            onClick = onOpenQr,
            modifier = Modifier.weight(1f)
        )
        ActionPill(
            label = "History",
            icon = Icons.Outlined.ReceiptLong,
            backgroundColor = IncomeGreen,
            contentColor = Color.White,
            onClick = onOpenTransactions,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

@Composable
private fun GoalCard(
    progress: Float,
    progressLabel: String,
    goalSummary: String,
    onEditGoal: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "progressAnim"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Savings Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onEditGoal)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(progressLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(goalSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(10.dp)
                        .background(
                            brush = Brush.linearGradient(colors = GradientHero),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun WeeklyChartCard(
    chartState: WeeklyExpenseChartUiState,
    onChartPeriodSelected: (com.paytrack.viewmodel.TimePeriod) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expense Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    com.paytrack.viewmodel.TimePeriod.values().forEach { period ->
                        val isSelected = chartState.selectedChartPeriod == period
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
            
            if (chartState.isEmpty) {
                Text("No expenses recorded for this period yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WeeklyExpenseChart(
                    data = chartState.values,
                    labels = chartState.labels,
                    currentDayIndex = chartState.currentDayIndex,
                    isLineGraph = chartState.isLineGraph,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun FolderSectionHeader(onCreateFolder: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(title = "My Folders")
        Text(
            text = "+ Create",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onCreateFolder)
        )
    }
}



@Composable
private fun CategorySummaryRow(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FolderProgressRing(
    progress: Float,
    hasLimit: Boolean,
    initial: String
) {
    val Teal = Color(0xFF0F766E)
    val Amber = Color(0xFFB45309)
    val Red = Color(0xFFB42318)
    val TrackColor = Color(0xFFE4E7EC)
    
    val ringColor = when {
        progress < 0.8f -> Teal
        progress < 1.0f -> Amber
        else -> Red
    }
    
    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.5.dp.toPx()
            if (!hasLimit) {
                drawCircle(
                    color = TrackColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )
            } else {
                drawCircle(
                    color = TrackColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = (progress * 360f).coerceIn(0f, 360f),
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
        }
        
        if (!hasLimit) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF101828)
            )
        } else {
            val percentString = "${(progress * 100).toInt()}%"
            Text(
                text = percentString,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF101828)
            )
        }
    }
}

@Composable
private fun FolderCard(
    folder: FolderUiState,
    usage: FolderUsageUiState?,
    onSetLimit: () -> Unit,
    onDeleteFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val Teal = Color(0xFF0F766E)
    val Amber = Color(0xFFB45309)
    val Red = Color(0xFFB42318)
    val TextMain = Color(0xFF101828)
    val BorderColor = Color(0xFFE4E7EC)
    
    val hasLimit = folder.hasLimit
    val progress = usage?.progress ?: 0f
    
    val ringColor = when {
        progress < 0.8f -> Teal
        progress < 1.0f -> Amber
        else -> Red
    }
    
    val statusText = when {
        !hasLimit -> "No limit set"
        progress < 0.8f -> "On track"
        progress < 1.0f -> "Nearly there"
        else -> "Over limit"
    }
    
    var showDropdown by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FolderProgressRing(progress = progress, hasLimit = hasLimit, initial = folder.name.take(1).uppercase())
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = TextMain
                    )
                    Text(
                        text = statusText,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontWeight = FontWeight.Medium,
                            fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = ringColor
                    )
                }
                
                Box {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color(0xFF98A2B3)
                        )
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set limit", color = TextMain) },
                            onClick = { 
                                showDropdown = false
                                onSetLimit() 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = TextMain) },
                            onClick = { showDropdown = false }
                        )
                        if (folder.isRemovable) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = ExpenseRed) },
                                onClick = { 
                                    showDropdown = false
                                    onDeleteFolder(folder.name)
                                }
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (hasLimit) {
                    val spent = usage?.usedAmount ?: "₹0"
                    val limitStr = usage?.totalAmount ?: "₹0"
                    val formatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.ENGLISH)
                    val dateStr = folder.limitEndDateMillis?.let { formatter.format(java.util.Date(it)) } ?: ""
                    Text(
                        text = "$spent / $limitStr",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = FontWeight.Medium,
                            fontFeatureSettings = "tnum"
                        ),
                        color = TextMain
                    )
                    Text(
                        text = "till $dateStr",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF98A2B3)
                    )
                } else {
                    Text(
                        text = "Set a limit to track this folder",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = FontWeight.Medium
                        ),
                        color = Teal,
                        modifier = Modifier.clickable { onSetLimit() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    errorMessage: String?,
    onCreateFolder: (String) -> Unit
) {
    var folderName by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreateFolder(folderName) }) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun EditFolderLimitDialog(
    folder: FolderUiState,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Double, Long) -> Unit,
    onClearLimit: () -> Unit
) {
    val context = LocalContext.current
    var amount by rememberSaveable(folder.name) {
        mutableStateOf(folder.limitAmount?.toString().orEmpty())
    }
    var endDate by rememberSaveable(folder.name) { mutableStateOf(folder.limitEndDateMillis) }
    var localError by remember(folder.name) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${folder.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        localError = null
                    },
                    label = { Text("Limit Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = endDate?.let { "Deadline: ${formatDisplayDate(it)}" } ?: "No deadline",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = endDate ?: System.currentTimeMillis()
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    endDate = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        set(Calendar.HOUR_OF_DAY, 12)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    )
                }

                (localError ?: errorMessage)?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    when {
                        parsedAmount == null || parsedAmount <= 0.0 -> localError = "Enter a valid limit amount."
                        endDate == null -> localError = "Choose a deadline."
                        else -> onSave(parsedAmount, endDate!!)
                    }
                }
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (folder.hasLimit) {
                    TextButton(onClick = onClearLimit) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

private fun formatDisplayDate(timeMillis: Long): String {
    return java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(Date(timeMillis))
}
