package com.das3kn.iz.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val ONBOARDING_DATA_STORE_NAME = "onboarding_preferences"

private val Context.onboardingDataStore by preferencesDataStore(
    name = ONBOARDING_DATA_STORE_NAME
)

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val onboardingCompletedKey: Preferences.Key<Boolean> =
        booleanPreferencesKey("onboarding_completed")

    val onboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[onboardingCompletedKey] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }
}
