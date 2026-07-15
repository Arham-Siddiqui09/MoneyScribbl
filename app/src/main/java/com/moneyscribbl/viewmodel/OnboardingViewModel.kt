package com.moneyscribbl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyscribbl.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _profileImageUri = MutableStateFlow<String?>(null)
    val profileImageUri: StateFlow<String?> = _profileImageUri.asStateFlow()

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updateProfileImage(uri: String?) {
        _profileImageUri.value = uri
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            if (_name.value.isNotBlank() || _profileImageUri.value != null) {
                userRepository.saveUserProfile(
                    name = _name.value.ifBlank { "Guest" },
                    phone = "",
                    profileImagePath = _profileImageUri.value
                )
            }
            userRepository.setOnboardingCompleted(true)
            onFinished()
        }
    }
}
