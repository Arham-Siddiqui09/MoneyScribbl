package com.moneyscribbl.ui.qr

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.moneyscribbl.viewmodel.CategoryOptionUiState
import com.moneyscribbl.viewmodel.QrScanUiState
import com.moneyscribbl.viewmodel.UpiAppUiState

// Theme colors
private val InkPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val MutedGray @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val LightGray @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val DeepTeal @Composable get() = MaterialTheme.colorScheme.primary
private val Amber = Color(0xFFB45309)
private val CrimsonRed @Composable get() = MaterialTheme.colorScheme.error
private val CardBorder @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val PageBackground @Composable get() = MaterialTheme.colorScheme.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScanRoute(
    uiState: QrScanUiState,
    onPermissionResult: (Boolean) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onRefreshApps: () -> Unit,
    onOpenUpiApp: (String) -> Intent?,
    onReturnFromUpiApp: () -> Unit,
    onConfirmPayment: () -> Unit,
    onDismissConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onReturnFromUpiApp()
    }

    LaunchedEffect(Unit) {
        onRefreshApps()
    }

    if (uiState.showPaymentConfirmDialog) {
        val formattedAmount = uiState.pendingConfirmAmount.toDoubleOrNull()
            ?.let { "₹%.2f".format(it) } ?: "₹${uiState.pendingConfirmAmount}"
        AlertDialog(
            onDismissRequest = onDismissConfirm,
            title = { Text("Payment Successful?", fontFamily = FontFamily.Serif) },
            text = {
                Text(
                    "Did your $formattedAmount payment via ${uiState.pendingConfirmAppLabel} go through?\n\n" +
                        "Tap 'Yes' to log it under \"${uiState.pendingConfirmCategory}\".",
                    fontFamily = FontFamily.SansSerif
                )
            },
            confirmButton = {
                Button(onClick = onConfirmPayment, colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)) {
                    Text("Yes, Log It")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirm) {
                    Text("No, Cancel", color = MutedGray)
                }
            }
        )
    }

    // Modal Bottom Sheet state for Folders
    var showFolderSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    if (showFolderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFolderSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FolderSelectionSheetContent(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { 
                    onCategorySelected(it)
                    showFolderSheet = false
                },
                onClose = { showFolderSheet = false }
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top=8.dp,bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )   {
            // Header
            Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                Text(
                    text = "Pay via UPI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = "Enter the amount and pick an app to scan the merchant QR.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.SansSerif,
                    color = MutedGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Step 1: Amount & Folder
            StepLabel("STEP 1 · AMOUNT & FOLDER")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.amountInput,
                        onValueChange = onAmountChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Amount", color = MutedGray) },
                        placeholder = { Text("0.00", color = LightGray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { 
                            Text(
                                "₹ ", 
                                color = InkPrimary, 
                                fontWeight = FontWeight.SemiBold,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            ) 
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                            fontFeatureSettings = "tnum"
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepTeal,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = DeepTeal
                        )
                    )

                    // Compact folder selector row
                    val selectedFolderInfo = uiState.categories.find { it.name == uiState.selectedCategory }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable { showFolderSheet = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (selectedFolderInfo != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FolderAvatar(selectedFolderInfo.name, 36, selectedFolderInfo.emoji)
                                Column {
                                    Text(selectedFolderInfo.name, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                                    selectedFolderInfo.availableBudgetLabel?.let {
                                        Text(
                                            "Available $it", 
                                            fontSize = 12.sp, 
                                            color = if (it.startsWith("(")) CrimsonRed else DeepTeal,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                    }
                                }
                            }
                        } else {
                            Text("Select a folder", color = MutedGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Select Folder", tint = MutedGray)
                    }

                    if (uiState.categories.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.categories.take(4)) { category ->
                                val isSelected = category.name == uiState.selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onCategorySelected(category.name) },
                                    label = { Text(category.name, color = if (isSelected) Color.White else InkPrimary) },
                                    leadingIcon = if (category.emoji != null) {
                                        { Text(category.emoji) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Choose UPI App
            StepLabel("STEP 2 · CHOOSE UPI APP")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!uiState.canLaunchPayment) {
                        Text(
                            text = "Fill in amount and select a folder to enable UPI apps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (uiState.availableUpiApps.isEmpty()) {
                        Text(
                            text = "No UPI apps found on this device.",
                            color = MutedGray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.heightIn(max = 400.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = false
                        ) {
                            items(uiState.availableUpiApps) { app ->
                                UpiAppTile(
                                    app = app,
                                    enabled = uiState.canLaunchPayment,
                                    onClick = {
                                        val intent = onOpenUpiApp(app.packageName)
                                        if (intent != null) {
                                            runCatching { appLauncher.launch(intent) }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    uiState.paymentError?.let { error ->
                        Text(
                            text = error,
                            color = CrimsonRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // How It Works
            var isHowItWorksExpanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.padding(bottom = 64.dp) // extra padding for bottom navigation breathing room
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHowItWorksExpanded = !isHowItWorksExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "How it works",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Icon(
                            imageVector = if (isHowItWorksExpanded) Icons.Default.Close else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MutedGray
                        )
                    }
                    AnimatedVisibility(visible = isHowItWorksExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                "1️⃣  Enter the amount and pick a folder",
                                "2️⃣  Tap a UPI app to open it",
                                "3️⃣  Scan the merchant QR inside the app",
                                "4️⃣  Enter the amount & pay normally",
                                "5️⃣  Come back — MoneyScribbl logs your expense!",
                                "6️⃣  If you forget to return, the amount will be saved as pending"
                            ).forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedGray
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectionSheetContent(
    categories: List<CategoryOptionUiState>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCategories = categories.filter { 
        it.name.contains(searchQuery, ignoreCase = true) 
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select folder",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedGray)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            placeholder = { Text("Search folders", color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = LightGray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepTeal,
                unfocusedBorderColor = CardBorder
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredCategories.size) { index ->
                val category = filteredCategories[index]
                val isSelected = category.name == selectedCategory
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelected(category.name) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FolderAvatar(category.name, 40, category.emoji)
                        Column {
                            Text(category.name, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                            category.availableBudgetLabel?.let {
                                val isLow = it.startsWith("(") || it.contains("0.00") // rough heuristic
                                Text(
                                    "Available $it", 
                                    fontSize = 13.sp, 
                                    color = if (isLow) CrimsonRed else DeepTeal,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(DeepTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("âœ“", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                        }
                    }
                }
                if (index < filteredCategories.size - 1) {
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
fun UpiAppTile(
    app: UpiAppUiState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val bitmap = app.icon?.toBitmap()?.asImageBitmap()
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                alpha = alpha
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray.copy(alpha = alpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(app.label.take(1).uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = app.label,
            fontSize = 12.sp,
            color = InkPrimary.copy(alpha = alpha),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FolderAvatar(name: String, sizeDp: Int, emoji: String? = null) {
    val initial = name.take(1).uppercase()
    val colors = listOf(Color(0xFFE0F2FE), Color(0xFFFEF3C7), Color(0xFFFCE7F3), Color(0xFFD1FAE5))
    val textColors = listOf(Color(0xFF0369A1), Color(0xFFB45309), Color(0xFFBE185D), Color(0xFF047857))
    val hash = kotlin.math.abs(name.hashCode()) % colors.size
    
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .background(if (emoji != null) Color.Transparent else colors[hash], CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (emoji != null) {
            Text(
                text = emoji,
                fontSize = (sizeDp / 1.5).sp
            )
        } else {
            Text(
                text = initial,
                color = textColors[hash],
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp / 2.2).sp
            )
        }
    }
}

@Composable
fun StepLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MutedGray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

