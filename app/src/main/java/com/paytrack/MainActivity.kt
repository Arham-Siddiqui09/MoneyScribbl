package com.paytrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paytrack.data.PayTrackDatabase
import com.paytrack.data.FinanceRepository
import com.paytrack.data.UserRepository
import com.paytrack.navigation.FinanceNavGraph
import com.paytrack.ui.theme.PayTrackTheme
import com.paytrack.viewmodel.HomeViewModel
import com.paytrack.viewmodel.HomeViewModelFactory
import com.paytrack.viewmodel.ProfileViewModel
import com.paytrack.viewmodel.ProfileViewModelFactory

class MainActivity : ComponentActivity() {

    private val financeRepository by lazy { FinanceRepository.getInstance(applicationContext) }
    private val userRepository by lazy {
        UserRepository(
            context = applicationContext,
            userDao = PayTrackDatabase.getInstance(applicationContext).userDao()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profileUiState = profileViewModel.uiState.collectAsStateWithLifecycle().value
            PayTrackTheme(darkTheme = profileUiState.isDarkMode) {
                FinanceNavGraph(
                    homeViewModel = homeViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}
