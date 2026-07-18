package com.moneyscribbl.ui.home

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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.home.components.WeeklyExpenseChart
import com.moneyscribbl.ui.theme.*
import com.moneyscribbl.viewmodel.*
import java.util.Calendar
import java.util.Date
import kotlin.io.path.Path
import kotlin.io.path.moveTo

@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onOpenProfile: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenQr: () -> Unit,
    onEditGoal: () -> Unit,
    onClearGoal: () -> Unit,
    onCreateFolder: (String, String?) -> Unit,
    onRenameFolder: (String, String, String?) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.moneyscribbl.viewmodel.TimePeriod) -> Unit,
    onHeroPeriodSelected: (com.moneyscribbl.viewmodel.HeroPeriod) -> Unit,
    onConfirmPendingTransaction: (String) -> Unit = {},
    onDeletePendingTransaction: (String) -> Unit = {},
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
        onRenameFolder = onRenameFolder,
        onSaveFolderLimit = onSaveFolderLimit,
        onClearFolderLimit = onClearFolderLimit,
        onDeleteFolder = onDeleteFolder,
        onClearFolderMessage = onClearFolderMessage,
        onChartPeriodSelected = onChartPeriodSelected,
        onHeroPeriodSelected = onHeroPeriodSelected,
        onConfirmPendingTransaction = onConfirmPendingTransaction,
        onDeletePendingTransaction = onDeletePendingTransaction,
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
    onCreateFolder: (String, String?) -> Unit,
    onRenameFolder: (String, String, String?) -> Unit,
    onSaveFolderLimit: (String, Double, Long) -> Unit,
    onClearFolderLimit: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onClearFolderMessage: () -> Unit,
    onChartPeriodSelected: (com.moneyscribbl.viewmodel.TimePeriod) -> Unit,
    onHeroPeriodSelected: (com.moneyscribbl.viewmodel.HeroPeriod) -> Unit,
    onConfirmPendingTransaction: (String) -> Unit = {},
    onDeletePendingTransaction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<FolderUiState?>(null) }
    var folderToRename by remember { mutableStateOf<FolderUiState?>(null) }
    var createAttempted by remember { mutableStateOf(false) }
    var renameAttempted by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HomeTopAppBar(
                            userName = uiState.userName,
                            profileImageUri = uiState.profileImageUri,
                            onOpenProfile = onOpenProfile,
                            onOpenCalendar = { showCalendarDialog = true }
                        )
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
                    if (uiState.pendingTransactions.isNotEmpty()) {
                        item {
                            PendingTransactionsBanner(
                                transactions = uiState.pendingTransactions,
                                onConfirm = onConfirmPendingTransaction,
                                onDelete = onDeletePendingTransaction
                            )
                        }
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
                            currencyCode = uiState.currencyCode,
                            onChartPeriodSelected = onChartPeriodSelected
                        )
                    }
                    item {
                        val bgColors = remember {
                            listOf(CategoryFoodBg, CategoryShoppingBg, CategoryEntertainmentBg, CategorySalaryBg, IndigoSoft, GreenSoft, RoseSoft, AmberSoft)
                        }
                        val fgColors = remember {
                            listOf(ChartColors[0], ChartColors[1], ChartColors[2], ChartColors[3], Indigo, Green, Rose, Amber)
                        }
                        val categoryList = remember(uiState.topCategories) {
                            uiState.topCategories.mapIndexed { index, cat ->
                                CategorySpend(
                                    name = cat.name,
                                    amount = cat.amount,
                                    iconLetter = cat.name.firstOrNull()?.uppercase() ?: "?",
                                    bgColor = bgColors[index % bgColors.size],
                                    fgColor = fgColors[index % fgColors.size],
                                    sharePercent = cat.sharePercent,
                                    emoji = cat.emoji
                                )
                            }
                        }

                        SectionHeader(
                            title = "Top Categories",
                            subtitle = uiState.topCategoriesSubtitle,
                            actionText = "",
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
                            EmptyCard("📉 Create a folder to track your budgets.")
                        } else {
                            val folderList = remember(uiState.folders, uiState.folderUsage) {
                                uiState.folders.map { folder ->
                                    val usage = uiState.folderUsage.find { it.name == folder.name }
                                    FolderItem(
                                        name = folder.name,
                                        iconLetter = folder.name.firstOrNull()?.uppercase() ?: "?",
                                        emoji = folder.emoji,
                                        spent = usage?.usedAmount?.replace("₹", "")?.trim(),
                                        limit = usage?.totalAmount?.replace("₹", "")?.trim()?.takeIf { folder.hasLimit },
                                        rawName = folder.name,
                                        progress = usage?.progress ?: 0f
                                    )
                                }.sortedWith(compareByDescending<FolderItem> { it.hasLimit }.thenByDescending { it.progress })
                            }
                            // Cache the map — avoid .associate {} allocation on every scroll recomposition
                            val isRemovableMap = remember(uiState.folders) {
                                uiState.folders.associate { it.name to it.isRemovable }
                            }
                            MyFoldersCard(
                                folders = folderList,
                                onSetLimitClick = { folderName ->
                                    onClearFolderMessage()
                                    selectedFolder = uiState.folders.find { it.name == folderName }
                                },
                                onRenameFolderClick = { folderName ->
                                    onClearFolderMessage()
                                    renameAttempted = false
                                    folderToRename = uiState.folders.find { it.name == folderName }
                                },
                                onDeleteFolder = onDeleteFolder,
                                isRemovableMap = isRemovableMap
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(48.dp)) }
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
            onCreateFolder = { name, emoji ->
                createAttempted = true
                onCreateFolder(name, emoji)
            }
        )
    }

    folderToRename?.let { folder ->
        RenameFolderDialog(
            initialName = folder.name,
            initialEmoji = folder.emoji,
            onDismiss = {
                folderToRename = null
                renameAttempted = false
                onClearFolderMessage()
            },
            errorMessage = uiState.folderMessage,
            onRenameFolder = { newName, newEmoji ->
                renameAttempted = true
                onRenameFolder(folder.name, newName, newEmoji)
                if (uiState.folderMessage == null) {
                    folderToRename = null
                }
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

    if (showCalendarDialog) {
        CalendarExpenditureDialog(
            dailyExpenditures = uiState.dailyExpenditures,
            currencyCode = uiState.currencyCode,
            onDismiss = { showCalendarDialog = false }
        )
    }



    LaunchedEffect(uiState.folders.size, uiState.folderMessage, showCreateDialog, createAttempted, folderToRename, renameAttempted) {
        if (showCreateDialog && createAttempted && uiState.folderMessage == null) {
            showCreateDialog = false
            createAttempted = false
        }
        if (folderToRename != null && renameAttempted && uiState.folderMessage == null) {
            folderToRename = null
            renameAttempted = false
        }
    }
}

@Composable
private fun HomeTopAppBar(
    userName: String,
    profileImageUri: String?,
    onOpenProfile: () -> Unit,
    onOpenCalendar: () -> Unit
) {
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
            // Cache the gradient brush — avoids allocating a new Brush object on every recomposition
            val avatarBackground = remember(profileImageUri) {
                if (profileImageUri == null) Brush.linearGradient(GradientHero)
                else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(avatarBackground)
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    coil.compose.AsyncImage(
                        model = java.io.File(profileImageUri),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val initials = userName.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "A"
                    Text(
                        text = initials,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Text
            Column {

                Text(
                    text = userName.ifBlank { "Welcome" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = com.moneyscribbl.ui.theme.SpaceGrotesk
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = onOpenCalendar) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "View Calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

// New imports needed beyond what your file already has:
// import androidx.compose.material3.LocalTextStyle
// import androidx.compose.ui.graphics.drawscope.Stroke
// import androidx.compose.ui.text.style.TextOverflow
// import androidx.compose.foundation.layout.VerticalDivider   (Material3 1.2+)
// New imports needed beyond what your file already has:
// import androidx.compose.material3.LocalTextStyle
// import androidx.compose.ui.graphics.drawscope.Stroke
// import androidx.compose.ui.text.style.TextOverflow
// import androidx.compose.foundation.layout.VerticalDivider   (Material3 1.2+)
// Note: TextStyle.brush (used for the gradient balance text) requires
// androidx.compose.ui:ui-text 1.4.0+ — you're almost certainly already past that.
@Composable
// New imports needed beyond what your file already has:
// import androidx.compose.material3.LocalTextStyle
// import androidx.compose.ui.graphics.Path
// import androidx.compose.ui.graphics.StrokeCap
// import androidx.compose.ui.graphics.drawscope.Stroke
// import androidx.compose.ui.text.style.TextOverflow
// import androidx.compose.foundation.layout.VerticalDivider   (Material3 1.2+)
// Note: TextStyle.brush (used for the gradient balance text) requires
// androidx.compose.ui:ui-text 1.4.0+ — you're almost certainly already past that.
private fun HeroBalanceCard(
    balance: String,
    income: String,
    expenses: String,
    heroPeriod: com.moneyscribbl.viewmodel.HeroPeriod,
    onHeroPeriodSelected: (com.moneyscribbl.viewmodel.HeroPeriod) -> Unit
) {
    // Fix 1: shadow() forces an offscreen render pass on every scroll frame — use cardElevation instead.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Colors sampled directly from the reference: a muted, deep indigo — not a
        // bright saturated violet. Darkest corner ~#28264E, highlight zone ~#7A74AC.
        val bgBrush = remember {
            Brush.linearGradient(
                colors = listOf(Color(0xFF2A274E), Color(0xFF453E80), Color(0xFF211F42)),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1400f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Fix 2: clip content — not just the Card background — so the watermark
                // and shine overlay never bleed past the rounded corners.
                .clip(RoundedCornerShape(26.dp))
                .background(brush = bgBrush)
        ) {
            // Fix 3: a curved diagonal ribbon — bright, then a darker curved shadow right
            // behind it — sweeping from the upper-right down to the left-center of the card.
            // Correction from my last pass: this needs an actual curve, not a straight
            // linear band. drawWithCache already caches the Path/Brush per size (only
            // rebuilt on resize, not per frame), so a curved Path here isn't the
            // performance problem I originally flagged — only the un-cached Color
            // allocation in the very first version was.
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        fun ribbon(yOffset: Float) = Path().apply {
                            moveTo(w * 0.95f, -h * 0.05f + yOffset)
                            quadraticTo(w * 0.55f, h * 0.28f + yOffset, w * 0.15f, h * 0.55f + yOffset)
                            lineTo(w * 0.02f, h * 0.68f + yOffset)
                        }

                        val highlightPath = ribbon(0f)
                        val shadowPath = ribbon(h * 0.14f)

                        onDrawBehind {
                            drawPath(
                                path = highlightPath,
                                color = Color.White.copy(alpha = 0.14f),
                                style = Stroke(width = h * 0.16f, cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = shadowPath,
                                color = Color.Black.copy(alpha = 0.16f),
                                style = Stroke(width = h * 0.14f, cap = StrokeCap.Round)
                            )
                        }
                    }
            )

            // Outlined rupee watermark — the reference sits mid-right, clear of the corner,
            // and modestly sized (roughly 40% of the card's height, not a giant corner mark).
            Text(
                text = "₹",
                fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                fontWeight = FontWeight.Bold,
                fontSize = 130.sp,
                style = LocalTextStyle.current.copy(
                    color = Color.White.copy(alpha = 0.14f),
                    drawStyle = Stroke(width = 2.dp.value)
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-20).dp, y = 26.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        val isNegative = balance.trimStart().startsWith("-")
                        // The reference renders the negative balance as a left-to-right
                        // gradient — deep coral at the start fading to near-white pink at
                        // the end — not a flat color. Sampled stops: #E2686E -> #FCEBFE.
                        Text(
                            text = balance,
                            fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.SemiBold,
                            style = if (isNegative) {
                                LocalTextStyle.current.copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFE2686E), Color(0xFFFCEBFE))
                                    )
                                )
                            } else {
                                LocalTextStyle.current.copy(color = Color.White)
                            }
                        )
                    }


                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.16f))
                                .clickable { menuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (heroPeriod == com.moneyscribbl.viewmodel.HeroPeriod.ALL) "All" else "This Month",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select period",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        val isAllSelected = heroPeriod == com.moneyscribbl.viewmodel.HeroPeriod.ALL
                        val isMonthSelected = heroPeriod == com.moneyscribbl.viewmodel.HeroPeriod.THIS_MONTH

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
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
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
                                    onHeroPeriodSelected(com.moneyscribbl.viewmodel.HeroPeriod.ALL)
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
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
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
                                    onHeroPeriodSelected(com.moneyscribbl.viewmodel.HeroPeriod.THIS_MONTH)
                                    menuExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Unified Income / Expenses strip — one rounded container split by a hairline
                // divider, matching the reference design instead of two separate white pills.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.14f))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroMetricItem(
                        icon = Icons.Outlined.ArrowUpward,
                        label = "Income",
                        value = income,
                        badgeColor = Color(0xFF16C784),
                        modifier = Modifier.weight(1f)
                    )

                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 12.dp),
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    HeroMetricItem(
                        icon = Icons.Outlined.ArrowDownward,
                        label = "Expenses",
                        value = expenses,
                        badgeColor = Color(0xFFE5533D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Solid colored badge (not a white circle + tinted icon) — matches the reference art directly.
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = value,
                fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
            subtitle = "Add manually",
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
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
    // Fix 4: tween(600ms) settles in one pass; spring() kept recomposing GoalCard for ~600ms after settling
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "progressAnim"
    )

    val Teal = Color(0xFF0F766E)
    val Amber = Color(0xFFB45309)
    val Red = Color(0xFFB42318)
    val TextMain = MaterialTheme.colorScheme.onSurface
    val BorderColor = MaterialTheme.colorScheme.outlineVariant

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "Edit budget", 
                                    color = TextMain,
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = TextMain,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditGoal()
                            }
                        )
                        if (isSet) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "Clear budget", 
                                        color = Red,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = null,
                                        tint = Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
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
                    val isExceeded = progress >= 1.0f
                    val bgColor = if (isExceeded) MaterialTheme.colorScheme.errorContainer else statusColor.copy(alpha = 0.1f)
                    val contentColor = if (isExceeded) MaterialTheme.colorScheme.onErrorContainer else statusColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isExceeded) Icons.Outlined.ErrorOutline else Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isExceeded) "You have exceeded your budget limit for this period." else "You are approaching your budget limit.",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = contentColor
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
    currencyCode: String,
    onChartPeriodSelected: (com.moneyscribbl.viewmodel.TimePeriod) -> Unit
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
                    com.moneyscribbl.viewmodel.TimePeriod.values().forEach { period ->
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
                    currencyCode = currencyCode,
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
                color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurface
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
    val TextMain = MaterialTheme.colorScheme.onSurface
    val BorderColor = MaterialTheme.colorScheme.outlineVariant
    
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
    onCreateFolder: (String, String?) -> Unit
) {
    var folderName by rememberSaveable { mutableStateOf("") }
    var folderEmoji by rememberSaveable { mutableStateOf("") }

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
                OutlinedTextField(
                    value = folderEmoji,
                    onValueChange = { 
                        if (it.length <= 2) folderEmoji = it 
                    },
                    label = { Text("Icon") },
                    placeholder = { Text("e.g. ðŸ’¼ (Use emoji keyboard)") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SentimentSatisfied,
                            contentDescription = "Emoji Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
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
            TextButton(onClick = { onCreateFolder(folderName, folderEmoji.takeIf { it.isNotBlank() }) }) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun RenameFolderDialog(
    initialName: String,
    initialEmoji: String?,
    onDismiss: () -> Unit,
    errorMessage: String?,
    onRenameFolder: (String, String?) -> Unit
) {
    var folderName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var folderEmoji by rememberSaveable(initialEmoji) { mutableStateOf(initialEmoji.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
                OutlinedTextField(
                    value = folderEmoji,
                    onValueChange = { 
                        if (it.length <= 2) folderEmoji = it 
                    },
                    label = { Text("Icon") },
                    placeholder = { Text("e.g. 💼 (Use emoji keyboard)") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SentimentSatisfied,
                            contentDescription = "Emoji Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
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
            TextButton(onClick = { onRenameFolder(folderName, folderEmoji.takeIf { it.isNotBlank() }) }) {
                Text("Save", fontWeight = FontWeight.Bold)
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

@Composable
fun PendingTransactionsBanner(
    transactions: List<RecentTransactionUiState>,
    onConfirm: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${transactions.size} Pending Transaction${if (transactions.size > 1) "s" else ""}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            /*Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )*/
        }
        Text(
            text = "Please confirm if these UPI payments were successful.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Fix 3: key(txn.id) gives Compose stable identity per row — unchanged rows are skipped on recomposition
        transactions.forEach { txn ->
            key(txn.id) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(txn.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(txn.amount, style = MaterialTheme.typography.bodySmall, color = if (txn.isExpense) ExpenseRed else IncomeGreen)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .clickable { onDelete(txn.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onConfirm(txn.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
