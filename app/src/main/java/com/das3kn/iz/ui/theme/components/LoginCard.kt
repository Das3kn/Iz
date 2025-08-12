package com.das3kn.iz.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.auth.AuthState
import com.das3kn.iz.ui.theme.components.button.PrimaryButton
import com.das3kn.iz.ui.theme.components.textField.TextFieldWithIcon

@Composable
fun LoginCard(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auth state'i dinle ve gerekli aksiyonları al
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                snackbarHostState.showSnackbar(
                    message = if (selectedTabIndex == 0) "Başarıyla giriş yapıldı!" else "Hesap başarıyla oluşturuldu!"
                )
                // State'i sıfırla
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message
                )
            }
            else -> {}
        }
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                ) {
                    Text(
                        text = "Giriş Yap",
                        modifier = Modifier.padding(16.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                ) {
                    Text(
                        text = "Kayıt Ol",
                        modifier = Modifier.padding(16.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTabIndex) {
                0 -> { // Giriş Yap
                    Text(
                        text = "Hesabınıza Giriş Yapın",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextFieldWithIcon(
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = Icons.Filled.Email,
                        placeholderText = "E-posta"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextFieldWithIcon(
                        value = password,
                        onValueChange = { password = it },
                        leadingIcon = Icons.Filled.Lock,
                        placeholderText = "Şifre",
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = if (authState is AuthState.Loading) "Giriş Yapılıyor..." else "Giriş Yap",
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.signIn(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (authState is AuthState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                1 -> { // Kayıt Ol
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Yeni Hesap Oluşturun",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TextFieldWithIcon(
                            value = displayName,
                            onValueChange = { displayName = it },
                            leadingIcon = Icons.Filled.Person,
                            placeholderText = "Ad Soyad"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextFieldWithIcon(
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = Icons.Filled.Person,
                            placeholderText = "Kullanıcı Adı"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextFieldWithIcon(
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = Icons.Filled.Email,
                            placeholderText = "E-posta"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextFieldWithIcon(
                            value = password,
                            onValueChange = { password = it },
                            leadingIcon = Icons.Filled.Lock,
                            placeholderText = "Şifre",
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextFieldWithIcon(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            leadingIcon = Icons.Filled.Lock,
                            placeholderText = "Şifre Tekrar",
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = if (authState is AuthState.Loading) "Hesap Oluşturuluyor..." else "Kayıt Ol",
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank() &&
                                    username.isNotBlank() && displayName.isNotBlank() &&
                                    password == confirmPassword) {
                                    authViewModel.signUp(email, password, username, displayName)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authState is AuthState.Loading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginCardPreview() {
    LoginCard()
}