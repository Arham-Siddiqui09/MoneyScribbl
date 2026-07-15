package com.moneyscribbl.ui.transactions

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.ui.theme.ExpenseRed
import com.moneyscribbl.ui.theme.IncomeGreen
import com.moneyscribbl.ui.theme.IndigoPrimary
import com.moneyscribbl.ui.transactions.components.FormFilterRow
import com.moneyscribbl.viewmodel.TransactionFormData
import java.util.Calendar
import java.util.Date

@Composable
fun TransactionFormRoute(
    formData: TransactionFormData,
    categories: List<String>,
    onNavigateBack: () -> Unit,
    onSave: (String?, Double, TransactionType, String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    TransactionFormScreen(
        formData = formData,
        categories = categories,
        onNavigateBack = onNavigateBack,
        onSave = onSave,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    formData: TransactionFormData,
    categories: List<String>,
    onNavigateBack: () -> Unit,
    onSave: (String?, Double, TransactionType, String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditMode = formData.id != null

    var amount by rememberSaveable { mutableStateOf(if (formData.amount > 0) formData.amount.toString() else "") }
    var selectedType by rememberSaveable { mutableStateOf(formData.type) }
    var selectedCategory by rememberSaveable { mutableStateOf(formData.category) }
    var selectedDate by rememberSaveable { mutableStateOf(formData.dateMillis) }
    var note by rememberSaveable { mutableStateOf(formData.note) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-12).dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isEditMode) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            // Amount Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "        Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val amountColor by animateColorAsState(
                    targetValue = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    animationSpec = spring(),
                    label = "amountColor"
                )

                TextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = null
                    },
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = amountColor,
                        unfocusedTextColor = amountColor
                    ),
                    prefix = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                            color = amountColor
                        )
                    },
                    placeholder = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                TypeToggleButton(
                    text = "Expense",
                    isSelected = selectedType == TransactionType.EXPENSE,
                    activeColor = ExpenseRed,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f)
                )
                TypeToggleButton(
                    text = "Income",
                    isSelected = selectedType == TransactionType.INCOME,
                    activeColor = IncomeGreen,
                    onClick = { selectedType = TransactionType.INCOME },
                    modifier = Modifier.weight(1f)
                )
            }

            // Categories
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FormFilterRow(
                    options = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }

            // Date Picker
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        selectedDate = Calendar.getInstance().apply {
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Select Date", tint = IndigoPrimary)
                    }
                }
            }

            // Notes
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("What was this for?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
                
                val isFormComplete = amount.isNotBlank() && selectedCategory.isNotBlank()
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed && isFormComplete) 0.97f else 1f,
                    animationSpec = spring(),
                    label = "btnScale"
                )
                
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            error = "Please enter a valid amount"
                            return@Button
                        }
                        if (selectedCategory.isBlank()) {
                            error = "Please select a category"
                            return@Button
                        }
                        onSave(formData.id, parsedAmount, selectedType, selectedCategory, selectedDate, note)
                    },
                    enabled = isFormComplete,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale)
                        .alpha(if (isFormComplete) 1f else 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPrimary,
                        disabledContainerColor = IndigoPrimary,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Save Transaction", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypeToggleButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color.Transparent,
        animationSpec = spring(),
        label = "typeBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1f),
        animationSpec = spring(),
        label = "typeText"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = spring(),
        label = "typeElev"
    )

    Box(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}
