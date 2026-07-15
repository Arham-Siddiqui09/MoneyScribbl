package com.moneyscribbl.ui.transactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.viewmodel.RecentTransactionUiState
import com.moneyscribbl.viewmodel.TransactionsUiState
import java.util.Locale
import java.text.NumberFormat

// --- Tokens ---
private val BgLavenderWhite @Composable get() = MaterialTheme.colorScheme.background
private val MonthHeaderBg @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val MonthHeaderBorder @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val CardSurface @Composable get() = MaterialTheme.colorScheme.surface
private val HairlineDivider @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val PrimaryAccent @Composable get() = MaterialTheme.colorScheme.primary
private val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val TextMuted @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val TextTimestamp @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val IncomeGreen = Color(0xFF1F9D55)
private val ExpenseRed = Color(0xFFD9364F)

// Category Pairs
private val SalaryColors = Pair(Color(0xFFEAF2FF), Color(0xFF3B5BDB))
private val ShoppingColors = Pair(Color(0xFFFDEFE6), Color(0xFFD9732B))
private val FoodColors = Pair(Color(0xFFFBEAF0), Color(0xFFC23D74))
private val EntertainmentColors = Pair(Color(0xFFF1EBFC), Color(0xFF7C4FE0))
private val FreelanceColors = Pair(Color(0xFFE8F7EE), Color(0xFF1F9D55))
private val DefaultColors = Pair(Color(0xFFF0F0F0), Color(0xFF555555))

private fun getCategoryColors(category: String): Pair<Color, Color> {
    return when (category.lowercase(Locale.getDefault())) {
        "salary" -> SalaryColors
        "shopping" -> ShoppingColors
        "food" -> FoodColors
        "entertainment" -> EntertainmentColors
        "freelance" -> FreelanceColors
        else -> DefaultColors
    }
}

@Composable
fun TransactionsRoute(
    uiState: TransactionsUiState,
    onSearchChanged: (String) -> Unit,
    onTypeFilterChanged: (TransactionType?) -> Unit,
    onCategoryFilterChanged: (String?) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TransactionsScreen(
        uiState = uiState,
        onSearchChanged = onSearchChanged,
        onTypeFilterChanged = onTypeFilterChanged,
        onCategoryFilterChanged = onCategoryFilterChanged,
        onAddTransaction = onAddTransaction,
        onEditTransaction = onEditTransaction,
        onDeleteTransaction = onDeleteTransaction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    uiState: TransactionsUiState,
    onSearchChanged: (String) -> Unit,
    onTypeFilterChanged: (TransactionType?) -> Unit,
    onCategoryFilterChanged: (String?) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTransactionForAction by remember { mutableStateOf<RecentTransactionUiState?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // Apply bottom padding to clear bottom navigation bar 
            Box(modifier = Modifier.padding(bottom = 80.dp)) {
                FloatingActionButton(
                    onClick = onAddTransaction,
                    containerColor = PrimaryAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(99.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(99.dp),
                            spotColor = PrimaryAccent.copy(alpha = 0.35f),
                            ambientColor = PrimaryAccent.copy(alpha = 0.35f)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add transaction")
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryAccent
                )
            } else {
                val currencyFormat = remember(uiState.currencyCode) {
                    java.text.NumberFormat.getCurrencyInstance(
                        when (uiState.currencyCode) {
                            "USD" -> java.util.Locale.US
                            "EUR" -> java.util.Locale.forLanguageTag("en-IE")
                            "GBP" -> java.util.Locale.UK
                            else -> java.util.Locale.forLanguageTag("en-IN")
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Transactions",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    "Your complete financial history",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            // Export Button
                           /* Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, HairlineDivider, CircleShape)
                                    .clickable { /* Export action */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.FileDownload, contentDescription = "Export", tint = TextPrimary)
                            }*/
                        }
                    }

                    // 2. Search Bar
                    item {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onSearchChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            placeholder = { Text("Search...", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMuted) },
                          //  trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = "Filters", tint = PrimaryAccent) },
                            singleLine = true,
                            shape = RoundedCornerShape(99.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    item { Spacer(modifier = Modifier.padding(top = 16.dp)) }

                    // 3. Filters
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                FilterRow(
                                    options = listOf("All", "Income", "Expense"),
                                    selected = when (uiState.selectedType) {
                                        null -> "All"
                                        TransactionType.INCOME -> "Income"
                                        TransactionType.EXPENSE -> "Expense"
                                    },
                                    onSelect = { option ->
                                        onTypeFilterChanged(
                                            when (option) {
                                                "Income" -> TransactionType.INCOME
                                                "Expense" -> TransactionType.EXPENSE
                                                else -> null
                                            }
                                        )
                                    }
                                )
                            }
                            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                FilterRow(
                                    options = listOf("All") + uiState.availableCategories,
                                    selected = uiState.selectedCategory ?: "All",
                                    onSelect = { option -> onCategoryFilterChanged(option.takeUnless { it == "All" }) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.padding(top = 24.dp)) }

                    // 4. Empty State or Grouped List
                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        // Use pre-grouped data from ViewModel — no computation on the UI thread
                        uiState.groupedTransactions.forEach { (month, pair) ->
                            val (netAmount, transactions) = pair

                            // Month Sticky Header (using simple item, not actual stickyHeader due to Compose foundation limitations,
                            // we would need ExperimentalFoundationApi for stickyHeader but simple item works visually)
                            item {
                                MonthHeader(month = month, netAmount = netAmount, currencyFormat = currencyFormat)
                            }

                            // Transactions for the month
                            items(transactions) { transaction ->
                                TransactionRow(
                                    transaction = transaction,
                                    onClick = { selectedTransactionForAction = transaction }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 120.dp)) }
                }
            }
        }
    }

    // 5. Bottom Sheet for Row Action
    if (selectedTransactionForAction != null) {
        val tx = selectedTransactionForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedTransactionForAction = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val uiModel = com.moneyscribbl.ui.transactions.components.TransactionDetailUiModel(
                type = if (tx.isExpense) com.moneyscribbl.data.TransactionType.EXPENSE else com.moneyscribbl.data.TransactionType.INCOME,
                emoji = tx.categoryEmoji ?: tx.category.firstOrNull()?.uppercase()?.toString(),
                vectorIcon = null,
                title = tx.title,
                sourceLabel = tx.subtitle.ifBlank { if (tx.isExpense) "Expense" else "Income" },
                amountString = tx.amount,
                typeLabel = if (tx.isExpense) "Debit" else "Credit",
                category = tx.category,
                secondaryFieldLabel = "Note",
                secondaryFieldValue = tx.note?.takeIf { it.isNotBlank() } ?: "—",
                dateTimeString = tx.time
            )

            com.moneyscribbl.ui.transactions.components.TransactionDetailSheet(
                uiModel = uiModel,
                onEdit = {
                    onEditTransaction(tx.id)
                    selectedTransactionForAction = null
                },
                onDelete = {
                    onDeleteTransaction(tx.id)
                    selectedTransactionForAction = null
                },
                onDismiss = {
                    selectedTransactionForAction = null
                }
            )
        }
    }
}

@Composable
internal fun MonthHeader(month: String, netAmount: Double, currencyFormat: NumberFormat) {
    val isPositive = netAmount >= 0
    val formattedNet = currencyFormat.format(Math.abs(netAmount))
    val netString = if (isPositive) "+$formattedNet" else "-$formattedNet"
    val netColor = if (isPositive) IncomeGreen else ExpenseRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MonthHeaderBg)
            .border(1.dp, MonthHeaderBorder)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(month, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(netString, fontWeight = FontWeight.ExtraBold, color = netColor)
    }
}

@Composable
internal fun TransactionRow(
    transaction: RecentTransactionUiState,
    onClick: () -> Unit
) {
    val (iconBgColor, iconTextColor) = getCategoryColors(transaction.category)
    val initial = transaction.category.firstOrNull()?.uppercase() ?: "?"

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .alpha(if (transaction.isPending) 0.5f else 1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (transaction.categoryEmoji != null) {
                    Text(text = transaction.categoryEmoji, fontSize = 24.sp)
                } else {
                    Text(text = initial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = iconTextColor)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(transaction.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                val subtitlePrefix = if (transaction.isPending) "Pending • " else ""
                Text(subtitlePrefix + transaction.subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Text(transaction.time, style = MaterialTheme.typography.labelSmall, color = TextTimestamp)
            }

            Text(
                text = transaction.amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (transaction.isExpense) ExpenseRed else IncomeGreen
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 84.dp) // align with text content
                .background(HairlineDivider)
                .height(1.dp)
        )
    }
}

@Composable
internal fun FilterRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options, key = { it }) { option ->
            val isSelected = selected == option
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) TextPrimary else MaterialTheme.colorScheme.surface,
                animationSpec = spring(),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.surface else TextPrimary,
                animationSpec = spring(),
                label = "chipText"
            )
            val modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bgColor)
                .clickable { onSelect(option) }
            
            val finalModifier = if (!isSelected) {
                modifier.border(1.dp, MonthHeaderBorder, RoundedCornerShape(999.dp))
            } else modifier

            Text(
                text = option,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                modifier = finalModifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun BottomSheetAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    bgTint: Color,
    contentTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(bgTint, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = contentTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = contentTint)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Nothing matches that filter yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

