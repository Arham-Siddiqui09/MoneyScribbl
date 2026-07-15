package com.moneyscribbl.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_USER_ID = 1

class UserRepository(
    private val context: Context,
    private val userDao: UserDao
) {

    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")
    private val currencyKey = androidx.datastore.preferences.core.stringPreferencesKey("currency")
    private val budgetCycleStartKey = androidx.datastore.preferences.core.intPreferencesKey("budget_cycle_start")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    fun observeUser(): Flow<UserEntity?> = userDao.observeUser()

    fun observeOnboardingCompleted(): Flow<Boolean> {
        return context.moneyscribblPreferencesDataStore.data
            .catchPreferences()
            .map { preferences -> preferences[onboardingCompletedKey] ?: false }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.moneyscribblPreferencesDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }

    fun observeDarkMode(): Flow<Boolean> {
        return context.moneyscribblPreferencesDataStore.data
            .catchPreferences()
            .map { preferences -> preferences[darkModeKey] ?: false }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.moneyscribblPreferencesDataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }

    fun observeCurrency(): Flow<String> {
        return context.moneyscribblPreferencesDataStore.data
            .catchPreferences()
            .map { preferences -> preferences[currencyKey] ?: "INR" }
    }

    suspend fun setCurrency(currency: String) {
        context.moneyscribblPreferencesDataStore.edit { preferences ->
            preferences[currencyKey] = currency
        }
    }

    fun observeBudgetCycleStart(): Flow<Int> {
        return context.moneyscribblPreferencesDataStore.data
            .catchPreferences()
            .map { preferences -> preferences[budgetCycleStartKey] ?: 1 }
    }

    suspend fun setBudgetCycleStart(dayOfMonth: Int) {
        context.moneyscribblPreferencesDataStore.edit { preferences ->
            preferences[budgetCycleStartKey] = dayOfMonth
        }
    }

    suspend fun saveUserProfile(
        name: String,
        phone: String,
        profileImagePath: String?
    ) {
        val existingUser = userDao.getUser()
        val userId = existingUser?.id ?: DEFAULT_USER_ID
        userDao.upsertUser(
            UserEntity(
                id = userId,
                name = name.trim(),
                phone = phone.trim(),
                profileImagePath = profileImagePath?.takeIf(String::isNotBlank)
            )
        )
    }

    suspend fun seedDefaultUserIfNeeded() {
        if (userDao.getUser() == null) {
            userDao.upsertUser(
                UserEntity(
                    id = DEFAULT_USER_ID,
                    name = "",
                    phone = "",
                    profileImagePath = null
                )
            )
        }
    }

    suspend fun clearAllData() {
        context.moneyscribblPreferencesDataStore.edit { preferences ->
            preferences.remove(darkModeKey)
            preferences.remove(currencyKey)
            preferences.remove(budgetCycleStartKey)
            preferences.remove(onboardingCompletedKey)
        }
        userDao.clearAll()
        seedDefaultUserIfNeeded()
    }
}

