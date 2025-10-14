package com.das3kn.iz.ui.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.preferences.AuthPreferences
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var autoLoginAttempted = false

    init {
        initializeState()
    }

    private fun initializeState() {
        // Mevcut kullanıcıyı kontrol et
        _currentUser.value = authRepository.currentUser

        // Kullanıcı profilini yükle
        if (_currentUser.value != null) {
            _authState.value = AuthState.Success
            loadUserProfile()
        }

        viewModelScope.launch {
            val preferences = authPreferences.authData.first()
            when {
                _currentUser.value != null -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        showSignUp = false,
                        savedEmail = preferences.email,
                        savedPassword = preferences.password,
                        hasAccount = true
                    )
                }

                preferences.email.isNotBlank() && preferences.password.isNotBlank() && !autoLoginAttempted -> {
                    autoLoginAttempted = true
                    _uiState.value = AuthUiState(
                        isLoading = true,
                        showSignUp = false,
                        savedEmail = preferences.email,
                        savedPassword = preferences.password,
                        hasAccount = preferences.hasAccount
                    )
                    signIn(
                        email = preferences.email,
                        password = preferences.password,
                        rememberCredentials = false
                    )
                }

                else -> {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        showSignUp = !preferences.hasAccount,
                        savedEmail = preferences.email,
                        savedPassword = "",
                        hasAccount = preferences.hasAccount
                    )
                }
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
        rememberCredentials: Boolean = true
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signIn(email, password)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        if (rememberCredentials) {
                            authPreferences.saveCredentials(email, password)
                        } else {
                            authPreferences.markHasAccount()
                        }
                        _authState.value = AuthState.Success
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showSignUp = false,
                            savedEmail = email,
                            savedPassword = if (rememberCredentials) password else _uiState.value.savedPassword,
                            hasAccount = true
                        )
                        loadUserProfile()
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Giriş başarısız")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showSignUp = false,
                            hasAccount = true
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Bilinmeyen hata")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSignUp = false,
                    hasAccount = true
                )
            }
        }
    }

    fun signUp(email: String, password: String, username: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signUp(email, password, username, displayName)
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        authPreferences.saveCredentials(email, password)
                        _authState.value = AuthState.Success
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showSignUp = false,
                            savedEmail = email,
                            savedPassword = password,
                            hasAccount = true
                        )
                        loadUserProfile()
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Kayıt başarısız")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showSignUp = true
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Bilinmeyen hata")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSignUp = true
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                authPreferences.clearCredentials(keepAccountFlag = true)
                _currentUser.value = null
                _userProfile.value = null
                _authState.value = AuthState.Idle
                _uiState.value = AuthUiState(
                    isLoading = false,
                    showSignUp = false,
                    hasAccount = true
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Çıkış başarısız")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return _currentUser.value != null
    }

    fun getCurrentUser() = _currentUser.value

    fun showSignUpScreen() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            showSignUp = true
        )
    }

    fun showLoginScreen() {
        viewModelScope.launch {
            authPreferences.markHasAccount()
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            showSignUp = false,
            hasAccount = true
        )
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                result.fold(
                    onSuccess = { profile ->
                        _userProfile.value = profile
                    },
                    onFailure = { exception ->
                        android.util.Log.e("AuthViewModel", "Failed to load user profile", exception)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Exception loading user profile", e)
            }
        }
    }
}
