package com.moneyscribbl.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private const val PREFERENCES_NAME = "moneyscribbl_preferences"

val Context.moneyscribblPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

fun Flow<Preferences>.catchPreferences(): Flow<Preferences> {
    return catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}

