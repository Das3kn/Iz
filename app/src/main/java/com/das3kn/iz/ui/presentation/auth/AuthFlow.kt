package com.das3kn.iz.ui.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.das3kn.iz.ui.presentation.navigation.MainNavigation

@Composable
fun AuthFlow() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    var loginEmail by rememberSaveable(uiState.savedEmail) { mutableStateOf(uiState.savedEmail) }
    var loginPassword by rememberSaveable(uiState.savedPassword) { mutableStateOf(uiState.savedPassword) }
    var showSplash by rememberSaveable { mutableStateOf(uiState.isLoading) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetAuthState()
        }
    }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            showSplash = true
        }
    }

    when {
        showSplash -> {
            SocialHubSplashScreen(
                isLoading = uiState.isLoading,
                onFinished = { showSplash = false }
            )
        }
        currentUser != null -> {
            MainNavigation(authViewModel = authViewModel)
        }

        uiState.showSignUp -> {
            SignUpScreen(
                onSignUp = { name, username, email, password ->
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        username = username,
                        displayName = name
                    )
                },
                onBack = {
                    authViewModel.resetAuthState()
                    authViewModel.showLoginScreen()
                    loginPassword = ""
                },
                isLoading = authState is AuthState.Loading,
                errorMessage = (authState as? AuthState.Error)?.message
            )
        }

        else -> {
            LoginScreen(
                email = loginEmail,
                password = loginPassword,
                onEmailChange = { loginEmail = it },
                onPasswordChange = { loginPassword = it },
                onLogin = {
                    authViewModel.signIn(
                        email = loginEmail.trim(),
                        password = loginPassword,
                        rememberCredentials = true
                    )
                },
                onNavigateToSignUp = {
                    authViewModel.resetAuthState()
                    authViewModel.showSignUpScreen()
                    loginPassword = ""
                },
                onForgotPassword = {},
                isLoading = authState is AuthState.Loading,
                errorMessage = (authState as? AuthState.Error)?.message
            )
        }
    }
}
