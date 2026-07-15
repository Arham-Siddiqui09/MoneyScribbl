package com.moneyscribbl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.moneyscribbl.data.moneyscribblDatabase
import com.moneyscribbl.data.FinanceRepository
import com.moneyscribbl.data.UserRepository
import com.moneyscribbl.data.migrateDataStoreToRoom
import com.moneyscribbl.navigation.FinanceNavGraph
import com.moneyscribbl.ui.theme.moneyscribblTheme
import com.moneyscribbl.viewmodel.HomeViewModel
import com.moneyscribbl.viewmodel.HomeViewModelFactory
import com.moneyscribbl.viewmodel.ProfileViewModel
import com.moneyscribbl.viewmodel.ProfileViewModelFactory
import com.moneyscribbl.viewmodel.OnboardingViewModel
import com.moneyscribbl.viewmodel.OnboardingViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val db by lazy { moneyscribblDatabase.getInstance(applicationContext) }

    private val financeRepository by lazy {
        FinanceRepository.getInstance(db.financeDao())
    }
    private val userRepository by lazy {
        UserRepository(
            context = applicationContext,
            userDao = db.userDao()
        )
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            repository = financeRepository,
            userRepository = userRepository,
            appContext = applicationContext,
            owner = this
        )
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(userRepository = userRepository)
    }

    private val onboardingViewModel: OnboardingViewModel by viewModels {
        OnboardingViewModelFactory(userRepository = userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Run one-shot DataStore → Room migration before the UI reads any data
        lifecycleScope.launch {
            migrateDataStoreToRoom(applicationContext, db.financeDao())
        }

        setContent {
            val profileUiState = profileViewModel.uiState.collectAsStateWithLifecycle().value
            moneyscribblTheme(darkTheme = profileUiState.isDarkMode) {
                FinanceNavGraph(
                    homeViewModel = homeViewModel,
                    profileViewModel = profileViewModel,
                    onboardingViewModel = onboardingViewModel
                )
            }
        }
    }
}
