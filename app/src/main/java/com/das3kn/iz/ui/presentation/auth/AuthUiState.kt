package com.das3kn.iz.ui.presentation.auth

data class AuthUiState(
    val isLoading: Boolean = true,
    val showSignUp: Boolean = true,
    val savedEmail: String = "",
    val savedPassword: String = "",
    val hasAccount: Boolean = false
)
