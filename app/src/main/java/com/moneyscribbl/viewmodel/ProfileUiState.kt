package com.moneyscribbl.viewmodel

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val profileImagePath: String? = null,
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val savedName: String = "",
    val savedPhone: String = "",
    val savedProfileImagePath: String? = null,
    val currency: String = "INR",
    val budgetCycleStartDay: Int = 1
)

