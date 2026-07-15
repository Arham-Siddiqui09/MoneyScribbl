package com.moneyscribbl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moneyscribbl.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        seedUser()
    }

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val currentState = _uiState.value
            userRepository.saveUserProfile(
                name = name,
                phone = currentState.savedPhone,
                profileImagePath = currentState.savedProfileImagePath
            )
        }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update {
            it.copy(
                phone = phone,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val currentState = _uiState.value
            userRepository.saveUserProfile(
                name = currentState.savedName,
                phone = phone,
                profileImagePath = currentState.savedProfileImagePath
            )
        }
    }

    fun onProfileImageSelected(profileImagePath: String?) {
        _uiState.update {
            it.copy(
                profileImagePath = profileImagePath,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val currentState = _uiState.value
            userRepository.saveUserProfile(
                name = currentState.savedName,
                phone = currentState.savedPhone,
                profileImagePath = profileImagePath
            )
        }
    }

    fun deleteProfileImage() {
        _uiState.update {
            it.copy(
                profileImagePath = null,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val currentState = _uiState.value
            userRepository.saveUserProfile(
                name = currentState.savedName,
                phone = currentState.savedPhone,
                profileImagePath = null
            )
        }
    }

    fun onDarkModeToggle(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.setDarkMode(enabled)
        }
    }

    fun onCurrencyChanged(currency: String) {
        viewModelScope.launch {
            userRepository.setCurrency(currency)
        }
    }

    fun onBudgetCycleChanged(dayOfMonth: Int) {
        viewModelScope.launch {
            userRepository.setBudgetCycleStart(dayOfMonth)
        }
    }


    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                userRepository.observeUser(),
                userRepository.observeDarkMode(),
                userRepository.observeCurrency(),
                userRepository.observeBudgetCycleStart()
            ) { user, isDarkMode, currency, budgetCycleStart ->
                Array(4) { i -> arrayOf(user, isDarkMode, currency, budgetCycleStart)[i] }
            }.collect { values ->
                val user = values[0] as com.moneyscribbl.data.UserEntity?
                val isDarkMode = values[1] as Boolean
                val currency = values[2] as String
                val budgetCycleStartDay = values[3] as Int
                _uiState.update { currentState ->
                    val incomingName = user?.name.orEmpty()
                    val incomingPhone = user?.phone.orEmpty()
                    val incomingImagePath = user?.profileImagePath
                    val hasUnsavedChanges = currentState.name != currentState.savedName ||
                        currentState.phone != currentState.savedPhone ||
                        currentState.profileImagePath != currentState.savedProfileImagePath

                    currentState.copy(
                        name = if (currentState.isLoading || !hasUnsavedChanges) incomingName else currentState.name,
                        phone = if (currentState.isLoading || !hasUnsavedChanges) incomingPhone else currentState.phone,
                        profileImagePath = if (currentState.isLoading || !hasUnsavedChanges) {
                            incomingImagePath
                        } else {
                            currentState.profileImagePath
                        },
                        savedName = incomingName,
                        savedPhone = incomingPhone,
                        savedProfileImagePath = incomingImagePath,
                        isDarkMode = isDarkMode,
                        currency = currency,
                        budgetCycleStartDay = budgetCycleStartDay,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun seedUser() {
        viewModelScope.launch {
            userRepository.seedDefaultUserIfNeeded()
        }
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

