package com.moneyscribbl.ui.goal

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.EmeraldPrimary
import com.moneyscribbl.viewmodel.SavingsGoalFormData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GoalFormRoute(
    formData: SavingsGoalFormData,
    onNavigateBack: () -> Unit,
    onSaveGoal: (Double, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    GoalFormScreen(
        formData = formData,
        onNavigateBack = onNavigateBack,
        onSaveGoal = onSaveGoal,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalFormScreen(
    formData: SavingsGoalFormData,
    onNavigateBack: () -> Unit,
    onSaveGoal: (Double, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
    var amount by rememberSaveable { mutableStateOf(if (formData.targetAmount == 0.0) "" else formData.targetAmount.toString()) }
    var startDate by rememberSaveable { mutableStateOf(formData.startDateMillis) }
    var targetDate by rememberSaveable { mutableStateOf(formData.targetDateMillis) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-8).dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Budget Goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            // Amount Input Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Target Amount", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("₹ ", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)) },
                        textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        placeholder = { Text("0.00", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) }
                    )
                }
            }

            // Details Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Date Pickers
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Start Date (Optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .clickable {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = startDate ?: System.currentTimeMillis() }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            startDate = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                set(Calendar.HOUR_OF_DAY, 0)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = startDate?.let { formatter.format(Date(it)) } ?: "No date selected",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (startDate != null) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { startDate = null }.padding(8.dp)
                                )
                            }
                        }

                        Text("Target Date (Optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .clickable {
                                    val calendar = Calendar.getInstance().apply { timeInMillis = targetDate ?: System.currentTimeMillis() }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            targetDate = Calendar.getInstance().apply {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = targetDate?.let { formatter.format(Date(it)) } ?: "No date selected",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (targetDate != null) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { targetDate = null }.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            error?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 8.dp))
            }

            // Save Button
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        error = "Enter a valid target amount."
                        return@Button
                    }
                    if (startDate != null && targetDate != null && startDate!! > targetDate!!) {
                        error = "Start date must be before target date."
                        return@Button
                    }
                    onSaveGoal(parsedAmount, startDate, targetDate)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

