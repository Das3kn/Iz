package com.das3kn.iz.ui.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // Mevcut kullanıcıyı kontrol et
        _currentUser.value = authRepository.currentUser
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signIn(email, password)
                result.onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Giriş başarısız")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    fun signUp(email: String, password: String, username: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signUp(email, password, username, displayName)
                result.onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Kayıt başarısız")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Idle
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
}
