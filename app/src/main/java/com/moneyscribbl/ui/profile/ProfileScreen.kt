package com.moneyscribbl.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.moneyscribbl.viewmodel.ProfileUiState
import android.app.DatePickerDialog
import java.util.Calendar
import java.time.LocalDate
import com.moneyscribbl.ui.home.CalendarExpenditureDialog

private val IndigoPrimary = Color(0xFF5B4FE9)
private val VioletAccent = Color(0xFF8B5CF6)
private val GradientAvatar = listOf(IndigoPrimary, VioletAccent)
private val GradientSms = listOf(IndigoPrimary.copy(alpha = 0.05f), VioletAccent.copy(alpha = 0.05f))

@Composable
fun ProfileRoute(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onNameChanged: (String) -> Unit,
    onProfileImageSelected: (String?) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onBudgetCycleChanged: (Int) -> Unit,
    dailyExpenditures: Map<LocalDate, Double>,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val onHelpSupportImpl = {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("moneyscribbl@gmail.com"))
                putExtra(android.content.Intent.EXTRA_SUBJECT, "MoneyScribbl Support Request")
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "No email app found.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val onRateAppImpl = {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
            context.startActivity(intent)
        }
    }

    ProfileScreen(
        name = uiState.name,
        profileImageUri = uiState.profileImagePath,
        isDarkMode = uiState.isDarkMode,
        onNameChange = onNameChanged,
        onProfileImageSelected = onProfileImageSelected,
        onDarkModeToggle = onDarkModeToggle,
        currentCurrency = uiState.currency,
        onChangeCurrency = onCurrencyChanged,
        budgetCycleStartDay = uiState.budgetCycleStartDay,
        onChangeBudgetCycle = onBudgetCycleChanged,
        onHelpSupport = onHelpSupportImpl,
        onRateApp = onRateAppImpl,
        onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        onOpenTerms = onOpenTerms,
        onClearData = onClearData,
        onBack = onNavigateBack,
        dailyExpenditures = dailyExpenditures,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String,
    profileImageUri: String?,
    isDarkMode: Boolean,
    onNameChange: (String) -> Unit,
    onProfileImageSelected: (String?) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    currentCurrency: String,
    onChangeCurrency: (String) -> Unit,
    budgetCycleStartDay: Int,
    onChangeBudgetCycle: (Int) -> Unit,
    onHelpSupport: () -> Unit,
    onRateApp: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onClearData: () -> Unit,
    onBack: () -> Unit,
    dailyExpenditures: Map<LocalDate, Double>,
    modifier: Modifier = Modifier
) {
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
              //  .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            AvatarSection(
                name = name,
                profileImageUri = profileImageUri,
                onProfileImageSelected = onProfileImageSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
            PersonalDetailsCard(
                name = name,
                onNameChange = onNameChange
            )
            
            SectionLabel("PREFERENCES")
            PreferencesCard(
                isDarkMode = isDarkMode,
                onDarkModeToggle = onDarkModeToggle,
                currentCurrency = currentCurrency,
                onChangeCurrency = onChangeCurrency,
                budgetCycleStartDay = budgetCycleStartDay,
                onChangeBudgetCycle = onChangeBudgetCycle,
                dailyExpenditures = dailyExpenditures
            )
            
            SectionLabel("ABOUT")
            AboutCard(
                onHelpSupport = onHelpSupport,
                onRateApp = onRateApp,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                onOpenTerms = onOpenTerms,
                onClearData = { showClearDataDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MoneyScribbl · v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all your data? This action cannot be undone and you will lose all your transactions, folders, and settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp)
    )
}

@Composable
private fun AvatarSection(
    name: String,
    profileImageUri: String?,
    onProfileImageSelected: (String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = java.io.File(context.filesDir, "profile_pic_${System.currentTimeMillis()}.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    onProfileImageSelected(file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
    val displayInitials = initials.ifEmpty { "?" }

    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Brush.linearGradient(GradientAvatar), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                coil.compose.AsyncImage(
                    model = java.io.File(profileImageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(
                    text = displayInitials,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(2.dp)
                .background(IndigoPrimary, CircleShape)
                .clickable {
                    launcher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Change photo",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PersonalDetailsCard(
    name: String,
    onNameChange: (String) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("NAME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = IndigoPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
private fun PreferencesCard(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    currentCurrency: String,
    onChangeCurrency: (String) -> Unit,
    budgetCycleStartDay: Int,
    onChangeBudgetCycle: (Int) -> Unit,
    dailyExpenditures: Map<LocalDate, Double>
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBudgetCycleDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        val currencies = listOf(
            "INR" to "Rupee (₹)",
            "USD" to "US Dollar ($)",
            "EUR" to "Euro (€)",
            "GBP" to "British Pound (£)"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChangeCurrency(code)
                                    showCurrencyDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentCurrency == code,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val currencyDisplay = when (currentCurrency) {
        "INR" -> "INR (₹)"
        "USD" -> "USD ($)"
        "EUR" -> "EUR (€)"
        "GBP" -> "GBP (£)"
        else -> currentCurrency
    }
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            SettingsRow(
                icon = Icons.Outlined.DarkMode,
                title = "Dark Mode",
                subtitle = if (isDarkMode) "Enabled" else "Disabled",
                trailing = { Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.CurrencyRupee,
                title = "Currency",
                subtitle = currencyDisplay,
                onClick = { showCurrencyDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.CalendarToday,
                title = "Daily Spending Calendar",
                subtitle = "View your daily expenses",
                onClick = {
                    showBudgetCycleDialog = true
                }
            )
        }
    }
    
    if (showBudgetCycleDialog) {
        CalendarExpenditureDialog(
            dailyExpenditures = dailyExpenditures,
            currencyCode = currentCurrency,
            onDismiss = { showBudgetCycleDialog = false }
        )
    }
}

@Composable
private fun AboutCard(
    onHelpSupport: () -> Unit,
    onRateApp: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onClearData: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            ClickableSettingsRow(
                icon = Icons.Outlined.HelpOutline,
                title = "Help & Support",
                subtitle = "",
                onClick = onHelpSupport,
                iconColor = Color(0xFFD9364F)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.Star,
                title = "Rate moneyscribbl",
                subtitle = "",
                onClick = onRateApp,
                iconColor = Color(0xFFF59E0B)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.Security,
                title = "Privacy Policy",
                subtitle = "",
                onClick = onOpenPrivacyPolicy,
                iconColor = Color(0xFF64748B)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.Description,
                title = "Terms of Service",
                subtitle = "",
                onClick = onOpenTerms,
                iconColor = Color(0xFF38BDF8)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ClickableSettingsRow(
                icon = Icons.Outlined.DeleteForever,
                title = "Clear Data",
                subtitle = "Delete all app data",
                onClick = onClearData,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun ClickableSettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = IndigoPrimary,
    isDestructive: Boolean = false
) {
    val appliedIconColor = if (isDestructive) MaterialTheme.colorScheme.error else iconColor
    val appliedTitleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(appliedIconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = appliedIconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = appliedTitleColor)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(IndigoPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = IndigoPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
