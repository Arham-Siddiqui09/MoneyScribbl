package com.paytrack.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paytrack.ui.goal.GoalFormRoute
import com.paytrack.ui.home.HomeRoute
import com.paytrack.ui.home.SplashScreen
import com.paytrack.ui.insights.InsightsRoute
import com.paytrack.ui.profile.ProfileRoute
import com.paytrack.ui.profile.PrivacyPolicyScreen
import com.paytrack.ui.profile.TermsOfServiceScreen
import com.paytrack.ui.qr.QrScanRoute
import com.paytrack.ui.transactions.TransactionFormRoute
import com.paytrack.ui.transactions.TransactionsRoute
import com.paytrack.ui.theme.IndigoPrimary
import com.paytrack.viewmodel.HomeViewModel
import com.paytrack.viewmodel.ProfileViewModel

private const val SPLASH_ROUTE = "splash"
private const val HOME_ROUTE = "home"
private const val TRANSACTIONS_ROUTE = "transactions"
private const val QR_ROUTE = "qr_scan"
private const val INSIGHTS_ROUTE = "insights"
private const val PROFILE_ROUTE = "profile"
private const val TRANSACTION_FORM_ROUTE = "transaction_form"
private const val GOAL_FORM_ROUTE = "goal_form"
private const val PRIVACY_POLICY_ROUTE = "privacy_policy"
private const val TERMS_OF_SERVICE_ROUTE = "terms_of_service"
private const val TRANSACTION_ID_ARG = "transactionId"

private data class BottomBarDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FinanceNavGraph(
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val transactionsUiState by homeViewModel.transactionsUiState.collectAsStateWithLifecycle()
    val qrUiState by homeViewModel.qrUiState.collectAsStateWithLifecycle()
    val insightsUiState by homeViewModel.insightsUiState.collectAsStateWithLifecycle()
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val bottomBarItems = listOf(
        BottomBarDestination(HOME_ROUTE, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomBarDestination(TRANSACTIONS_ROUTE, "History", Icons.AutoMirrored.Filled.ListAlt, Icons.AutoMirrored.Outlined.ListAlt),
        BottomBarDestination(QR_ROUTE, "Pay", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
        BottomBarDestination(INSIGHTS_ROUTE, "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    )

    val currentRoute = currentDestination?.route
    val hideBottomBar = currentRoute == SPLASH_ROUTE ||
        currentRoute?.startsWith(TRANSACTION_FORM_ROUTE) == true ||
        currentRoute == GOAL_FORM_ROUTE ||
        currentRoute == PROFILE_ROUTE ||
        currentRoute == PRIVACY_POLICY_ROUTE ||
        currentRoute == TERMS_OF_SERVICE_ROUTE

    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {}
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = SPLASH_ROUTE,
                enterTransition = { androidx.compose.animation.EnterTransition.None },
                exitTransition = { androidx.compose.animation.ExitTransition.None },
                popEnterTransition = { androidx.compose.animation.EnterTransition.None },
                popExitTransition = { androidx.compose.animation.ExitTransition.None },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable(SPLASH_ROUTE) {
                    SplashScreen(
                        onNavigateToHome = {
                            navController.navigate(HOME_ROUTE) {
                                popUpTo(SPLASH_ROUTE) { inclusive = true }
                            }
                        }
                    )
                }

                composable(HOME_ROUTE) {
                    HomeRoute(
                        uiState = homeUiState,
                        onOpenProfile = { navController.navigate(PROFILE_ROUTE) },
                        onAddTransaction = { navController.navigate(TRANSACTION_FORM_ROUTE) },
                        onOpenTransactions = { navController.navigate(TRANSACTIONS_ROUTE) },
                        onOpenQr = { navController.navigate(QR_ROUTE) },
                        onEditGoal = { navController.navigate(GOAL_FORM_ROUTE) },
                        onClearGoal = { homeViewModel.clearSavingsGoal() },
                        onCreateFolder = homeViewModel::createFolder,
                        onRenameFolder = homeViewModel::renameFolder,
                        onSaveFolderLimit = homeViewModel::saveFolderLimit,
                        onClearFolderLimit = homeViewModel::clearFolderLimit,
                        onDeleteFolder = homeViewModel::deleteFolder,
                        onClearFolderMessage = homeViewModel::clearFolderMessage,
                        onChartPeriodSelected = homeViewModel::updateHomeChartPeriod,
                        onHeroPeriodSelected = homeViewModel::updateHeroPeriod
                    )
                }

                composable(TRANSACTIONS_ROUTE) {
                    TransactionsRoute(
                        uiState = transactionsUiState,
                        onSearchChanged = homeViewModel::updateTransactionSearchQuery,
                        onTypeFilterChanged = homeViewModel::updateTransactionTypeFilter,
                        onCategoryFilterChanged = homeViewModel::updateTransactionCategoryFilter,
                        onAddTransaction = { navController.navigate(TRANSACTION_FORM_ROUTE) },
                        onEditTransaction = { transactionId ->
                            navController.navigate("$TRANSACTION_FORM_ROUTE?$TRANSACTION_ID_ARG=$transactionId")
                        },
                        onDeleteTransaction = homeViewModel::deleteTransaction
                    )
                }

                composable(QR_ROUTE) {
                    QrScanRoute(
                        uiState = qrUiState,
                        onPermissionResult = homeViewModel::setCameraPermission,
                        onAmountChanged = homeViewModel::updateAmountInput,
                        onCategorySelected = homeViewModel::updateSelectedQrCategory,
                        onRefreshApps = homeViewModel::refreshInstalledUpiApps,
                        onOpenUpiApp = homeViewModel::openUpiAppDirectly,
                        onReturnFromUpiApp = homeViewModel::onReturnFromUpiApp,
                        onConfirmPayment = homeViewModel::confirmPaymentLogged,
                        onDismissConfirm = homeViewModel::dismissConfirmDialog
                    )
                }

                composable(INSIGHTS_ROUTE) {
                    InsightsRoute(
                        uiState = insightsUiState,
                        onChartPeriodSelected = homeViewModel::updateInsightsChartPeriod,
                        onCategoryBreakdownPeriodSelected = homeViewModel::updateCategoryBreakdownPeriod,
                        onDeleteVault = homeViewModel::deleteSavingsVaultEntry
                    )
                }

                composable(PROFILE_ROUTE) {
                    ProfileRoute(
                        uiState = profileUiState,
                        onNavigateBack = { navController.popBackStack() },
                        onDarkModeToggle = profileViewModel::onDarkModeToggle,
                        onNameChanged = profileViewModel::onNameChanged,
                        onProfileImageSelected = profileViewModel::onProfileImageSelected,
                        onDeleteProfileImage = profileViewModel::deleteProfileImage,
                        onImportSms = homeViewModel::importSmsHistory,
                        onCurrencyChanged = profileViewModel::onCurrencyChanged,
                        onBudgetCycleChanged = profileViewModel::onBudgetCycleChanged,
                        dailyExpenditures = homeUiState.dailyExpenditures,
                        onOpenPrivacyPolicy = { navController.navigate(PRIVACY_POLICY_ROUTE) },
                        onOpenTerms = { navController.navigate(TERMS_OF_SERVICE_ROUTE) },
                        onClearData = { homeViewModel.clearAllData() }
                    )
                }

                composable(PRIVACY_POLICY_ROUTE) {
                    PrivacyPolicyScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(TERMS_OF_SERVICE_ROUTE) {
                    TermsOfServiceScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "$TRANSACTION_FORM_ROUTE?$TRANSACTION_ID_ARG={$TRANSACTION_ID_ARG}",
                    arguments = listOf(
                        navArgument(TRANSACTION_ID_ARG) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { entry ->
                    val transactionId = entry.arguments?.getString(TRANSACTION_ID_ARG)
                    val formData by produceState<com.paytrack.viewmodel.TransactionFormData?>(initialValue = null, transactionId) {
                        value = homeViewModel.getTransactionFormData(transactionId)
                    }
                    formData?.let { transaction ->
                        TransactionFormRoute(
                            formData = transaction,
                            categories = transactionsUiState.availableCategories,
                            onNavigateBack = { navController.popBackStack() },
                            onSave = { id, amount, type, category, dateMillis, note ->
                                homeViewModel.saveTransaction(id, amount, type, category, dateMillis, note)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(GOAL_FORM_ROUTE) {
                    GoalFormRoute(
                        formData = homeViewModel.getGoalFormData(),
                        onNavigateBack = { navController.popBackStack() },
                        onSaveGoal = { targetAmount, startDateMillis, targetDateMillis ->
                            homeViewModel.saveSavingsGoal(targetAmount, startDateMillis, targetDateMillis)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        // Bottom Navigation
        AnimatedVisibility(
            visible = !hideBottomBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            StandardBottomNav(
                items = bottomBarItems,
                currentDestination = currentDestination,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(HOME_ROUTE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                bottomPadding = navInsets.calculateBottomPadding()
            )
        }
    }
}

@Composable
private fun StandardBottomNav(
    items: List<BottomBarDestination>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(bottom = bottomPadding) // extra bottom padding to clear gesture bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { destination ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

                val interactionSource = remember { MutableInteractionSource() }
                val color = if (isSelected) activeColor else inactiveColor

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onNavigate(destination.route) }
                        )
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = destination.label,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default, // Inter assumed default
                            fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = color
                    )
                }
            }
        }
    }
}
