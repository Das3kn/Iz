package com.das3kn.iz.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTH_DATA_STORE_NAME = "auth_preferences"

private val Context.authDataStore by preferencesDataStore(
    name = AUTH_DATA_STORE_NAME
)

data class AuthPreferencesData(
    val hasAccount: Boolean = false,
    val email: String = "",
    val password: String = ""
)

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val hasAccountKey: Preferences.Key<Boolean> =
        booleanPreferencesKey("has_account")
    private val emailKey: Preferences.Key<String> =
        stringPreferencesKey("saved_email")
    private val passwordKey: Preferences.Key<String> =
        stringPreferencesKey("saved_password")

    val authData: Flow<AuthPreferencesData> = context.authDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AuthPreferencesData(
                hasAccount = preferences[hasAccountKey] ?: false,
                email = preferences[emailKey] ?: "",
                password = preferences[passwordKey] ?: ""
            )
        }

    suspend fun saveCredentials(email: String, password: String) {
        context.authDataStore.edit { preferences ->
            preferences[emailKey] = email
            preferences[passwordKey] = password
            preferences[hasAccountKey] = true
        }
    }

    suspend fun markHasAccount() {
        context.authDataStore.edit { preferences ->
            preferences[hasAccountKey] = true
        }
    }

    suspend fun clearCredentials(keepAccountFlag: Boolean) {
        context.authDataStore.edit { preferences ->
            preferences[emailKey] = ""
            preferences[passwordKey] = ""
            preferences[hasAccountKey] = keepAccountFlag
        }
    }
}
