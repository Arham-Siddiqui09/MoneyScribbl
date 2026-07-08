package com.paytrack.ui.home

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onClearGoal: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.paytrack.viewmodel.TimePeriod) -> Unit,
    onHeroPeriodSelected: (com.paytrack.viewmodel.HeroPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreen(
        uiState = uiState,
        onOpenProfile = onOpenProfile,
        onAddTransaction = onAddTransaction,
        onOpenTransactions = onOpenTransactions,
        onOpenQr = onOpenQr,
        onEditGoal = onEditGoal,
        onClearGoal = onClearGoal,
        onCreateFolder = onCreateFolder,
        onSaveFolderLimit = onSaveFolderLimit,
        onClearFolderLimit = onClearFolderLimit,
        onDeleteFolder = onDeleteFolder,
        onClearFolderMessage = onClearFolderMessage,
        onChartPeriodSelected = onChartPeriodSelected,
        onHeroPeriodSelected = onHeroPeriodSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenProfile: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenQr: () -> Unit,
    onEditGoal: () -> Unit,
    onClearGoal: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.paytrack.viewmodel.TimePeriod) -> Unit,
    onHeroPeriodSelected: (com.paytrack.viewmodel.HeroPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<FolderUiState?>(null) }
    var createAttempted by remember { mutableStateOf(false) }
    var showAllFoldersSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top=8.dp,bottom = 12.dp)
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
                    item {
                        HomeTopAppBar(onOpenProfile = onOpenProfile)
                    }
                  //  item { Spacer(modifier = Modifier.height(4.dp)) }
                    item {
                        HeroBalanceCard(
                            balance = uiState.currentBalance,
                            income = uiState.totalIncome,
                            expenses = uiState.totalExpenses,
                            heroPeriod = uiState.heroPeriod,
                            onHeroPeriodSelected = onHeroPeriodSelected
                        )
                    }
                    item {
                        ActionRow(
                            onAddTransaction = onAddTransaction,
                            onOpenQr = onOpenQr
                        )
                    }
                    item {
                        GoalCard(
                            progress = uiState.savingsProgress,
                            progressLabel = uiState.savingsProgressLabel,
                            goalSummary = uiState.goalSummary,
                            onEditGoal = onEditGoal,
                            onClearGoal = onClearGoal
                        )
                    }
                    item {
                        WeeklyChartCard(
                            chartState = uiState.weeklyExpenseChart,
                            onChartPeriodSelected = onChartPeriodSelected
                        )
                    }
                    item {
                        val categoryList = uiState.topCategories.mapIndexed { index, cat ->
                            val topCatAmount = uiState.topCategories.maxOfOrNull { it.rawAmount } ?: 1.0
                            val sharePercent = if (topCatAmount > 0) ((cat.rawAmount / topCatAmount) * 100).toInt() else 0
                            
                            val bgColors = listOf(CategoryFoodBg, CategoryShoppingBg, CategoryEntertainmentBg, CategorySalaryBg, IndigoSoft, GreenSoft, RoseSoft, AmberSoft)
                            val fgColors = listOf(ChartColors[0], ChartColors[1], ChartColors[2], ChartColors[3], Indigo, Green, Rose, Amber)
                            
                            CategorySpend(
                                name = cat.name,
                                amount = cat.amount,
                                iconLetter = cat.name.firstOrNull()?.uppercase() ?: "?",
                                bgColor = bgColors[index % bgColors.size],
                                fgColor = fgColors[index % fgColors.size],
                                sharePercent = sharePercent
                            )
                        }
                        
                        val subtitle = if (uiState.topCategories.isNotEmpty()) {
                            val totalSpend = uiState.topCategories.sumOf { it.rawAmount }
                            val formattedSpend = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-IN")).format(totalSpend)
                            "This month · $formattedSpend across ${uiState.topCategories.size} categories"
                        } else null
                        
                        SectionHeader(
                            title = "Top Categories",
                            subtitle = subtitle,
                            actionText = "See all",
                            onActionClick = { /* Navigate to full category breakdown */ }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (categoryList.isEmpty()) {
                            EmptyCard("Your category spend summary will appear after the first expense.")
                        } else {
                            TopCategoriesCard(categories = categoryList)
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    
                    item {
                        SectionHeader(
                            title = "My Folders",
                            actionText = "+ Create",
                            onActionClick = {
                                onClearFolderMessage()
                                createAttempted = false
                                showCreateDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (uiState.folderMessage != null) {
                            EmptyCard(uiState.folderMessage)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        
                        if (uiState.folders.isEmpty()) {
                            EmptyCard("📁 Create a folder to track your budgets.")
                        } else {
                            val folderList = uiState.folders.map { folder ->
                                val usage = uiState.folderUsage.find { it.name == folder.name }
                                FolderItem(
                                    name = folder.name,
                                    iconLetter = folder.name.firstOrNull()?.uppercase() ?: "?",
                                    spent = usage?.usedAmount?.replace("₹", "")?.trim(),
                                    limit = usage?.totalAmount?.replace("₹", "")?.trim()?.takeIf { folder.hasLimit },
                                    rawName = folder.name,
                                    progress = usage?.progress ?: 0f
                                )
                            }.sortedWith(compareByDescending<FolderItem> { it.hasLimit }.thenByDescending { it.progress })
                            
                            val isRemovableMap = uiState.folders.associate { it.name to it.isRemovable }
                            MyFoldersCard(
                                folders = folderList,
                                onSetLimitClick = { folderName ->
                                    onClearFolderMessage()
                                    selectedFolder = uiState.folders.find { it.name == folderName }
                                },
                                onDeleteFolder = onDeleteFolder,
                                isRemovableMap = isRemovableMap,
                                onViewAllClick = { showAllFoldersSheet = true }
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

    if (showAllFoldersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAllFoldersSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "All Folders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                LazyColumn {
                    val sortedFolders = uiState.folders.map { folder ->
                        val usage = uiState.folderUsage.find { it.name == folder.name }
                        FolderItem(
                            name = folder.name,
                            iconLetter = folder.name.firstOrNull()?.uppercase() ?: "?",
                            spent = usage?.usedAmount?.replace("₹", "")?.trim(),
                            limit = usage?.totalAmount?.replace("₹", "")?.trim()?.takeIf { folder.hasLimit },
                            rawName = folder.name,
                            progress = usage?.progress ?: 0f
                        )
                    }.sortedWith(compareByDescending<FolderItem> { it.hasLimit }.thenByDescending { it.progress })

                    items(sortedFolders) { folderItem ->
                        FolderRow(
                            folder = folderItem,
                            onSetLimitClick = {
                                showAllFoldersSheet = false
                                onClearFolderMessage()
                                selectedFolder = uiState.folders.find { it.name == folderItem.rawName }
                            },
                            onDeleteFolder = {
                                onDeleteFolder(folderItem.rawName)
                            },
                            isRemovable = uiState.folders.find { it.name == folderItem.rawName }?.isRemovable ?: false
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.folders.size, uiState.folderMessage, showCreateDialog, createAttempted) {
        if (showCreateDialog && createAttempted && uiState.folderMessage == null) {
            showCreateDialog = false
            createAttempted = false
        }
    }
}

@Composable
private fun HomeTopAppBar(onOpenProfile: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientHero))
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Text
            Column {
                Text(
                    text = "Good morning ✨",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Arham",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = com.paytrack.ui.theme.SpaceGrotesk
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

@Composable
private fun HeroBalanceCard(
    balance: String, 
    income: String, 
    expenses: String,
    heroPeriod: com.paytrack.viewmodel.HeroPeriod,
    onHeroPeriodSelected: (com.paytrack.viewmodel.HeroPeriod) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF7C6BFF),
                spotColor = Color(0xFF5B4CFC)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF5B4CFC), Color(0xFF7C6BFF), Color(0xFF9C8CFF)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Ambient texture wave
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.6f)
                    quadraticBezierTo(size.width * 0.3f, size.height * 0.9f, size.width * 0.7f, size.height * 0.5f)
                    quadraticBezierTo(size.width * 0.9f, size.height * 0.3f, size.width, size.height * 0.4f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = path, color = Color.White.copy(alpha = 0.15f))
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = balance,
                            fontFamily = com.paytrack.ui.theme.IBMPlexMono,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { menuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (heroPeriod == com.paytrack.viewmodel.HeroPeriod.ALL) "All" else "This Month",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select period",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        val isAllSelected = heroPeriod == com.paytrack.viewmodel.HeroPeriod.ALL
                        val isMonthSelected = heroPeriod == com.paytrack.viewmodel.HeroPeriod.THIS_MONTH

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 8.dp)
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(
                                                        if (isAllSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.DateRange,
                                                    contentDescription = null,
                                                    tint = if (isAllSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Text(
                                                text = "All Time",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAllSelected) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }

                                        if (isAllSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onHeroPeriodSelected(com.paytrack.viewmodel.HeroPeriod.ALL)
                                    menuExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.foundation.layout.Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(
                                                        if (isMonthSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = if (isMonthSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Text(
                                                text = "This Month",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isMonthSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isMonthSelected) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }

                                        if (isMonthSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onHeroPeriodSelected(com.paytrack.viewmodel.HeroPeriod.THIS_MONTH)
                                    menuExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeroMetricPill(
                        icon = Icons.Outlined.ArrowUpward,
                        label = "Income",
                        value = income,
                        iconTint = Color(0xFF0FA968),
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricPill(
                        icon = Icons.Outlined.ArrowDownward,
                        label = "Expenses",
                        value = expenses,
                        iconTint = Color(0xFFE53935),
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
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(text = value, fontFamily = com.paytrack.ui.theme.IBMPlexMono, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}


@Composable
private fun ActionRow(
    onAddTransaction: () -> Unit,
    onOpenQr: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ActionPill(
            label = "Add",
            subtitle = "Log a transaction",
            icon = Icons.Outlined.Add,
            iconColor = IndigoPrimary,
            iconBgColor = IndigoLight,
            cardBgColor = IndigoLight.copy(alpha = 0.35f),
            onClick = onAddTransaction,
            modifier = Modifier.weight(1f)
        )
        ActionPill(
            label = "Pay",
            subtitle = "Pay via UPI",
            icon = Icons.Outlined.CurrencyRupee,
            iconColor = IncomeGreen,
            iconBgColor = IncomeGreenBg,
            cardBgColor = IncomeGreenBg.copy(alpha = 0.35f),
            onClick = onOpenQr,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ActionPill(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    cardBgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, iconColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconBgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GoalCard(
    progress: Float,
    progressLabel: String,
    goalSummary: String,
    onEditGoal: () -> Unit,
    onClearGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "progressAnim"
    )

    val Teal = Color(0xFF0F766E)
    val Amber = Color(0xFFB45309)
    val Red = Color(0xFFB42318)
    val TextMain = Color(0xFF101828)
    val BorderColor = Color(0xFFE4E7EC)

    val isSet = progressLabel != "Set a budget to stay on track"

    val statusColor = when {
        !isSet -> Color(0xFF98A2B3)
        progress < 0.8f -> Teal
        progress < 1.0f -> Amber
        else -> Red
    }

    val statusText = when {
        !isSet -> "No budget set"
        progress < 0.8f -> "On track"
        progress < 1.0f -> "Approaching limit"
        else -> "Limit exceeded"
    }

    val percentText = if (isSet) "${(progress * 100).toInt()}% spent" else ""

    Card(
        modifier = modifier.fillMaxWidth(),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Budget Goal",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = TextMain
                        )
                        Text(
                            text = statusText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = statusColor
                        )
                    }
                }

                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF98A2B3)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit budget", color = TextMain) },
                            onClick = {
                                menuExpanded = false
                                onEditGoal()
                            }
                        )
                        if (isSet) {
                            DropdownMenuItem(
                                text = { Text("Clear budget", color = Red) },
                                onClick = {
                                    menuExpanded = false
                                    onClearGoal()
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = BorderColor, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = progressLabel,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = TextMain
                        )
                        Text(
                            text = goalSummary,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF98A2B3)
                        )
                    }
                    if (isSet) {
                        Text(
                            text = percentText,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = statusColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFF2F4F7), RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isSet) animatedProgress else 0f)
                            .height(8.dp)
                            .background(
                                color = statusColor,
                                shape = RoundedCornerShape(999.dp)
                            )
                    )
                }
                
                if (progress >= 0.8f && isSet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (progress >= 1.0f) Icons.Outlined.ErrorOutline else Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (progress >= 1.0f) "You have exceeded your budget limit for this period." else "You are approaching your budget limit.",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = statusColor
                        )
                    }
                }
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
