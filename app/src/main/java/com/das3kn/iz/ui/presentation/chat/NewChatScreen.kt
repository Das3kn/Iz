package com.das3kn.iz.ui.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NewChatViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val shouldNavigateBack by viewModel.shouldNavigateBack.collectAsState()
    val createdChatId by viewModel.createdChatId.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            viewModel.searchUsers(searchQuery)
        }
    }

    // Başarı mesajını göster
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
        }
    }

    // Hata mesajını göster
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            snackbarHostState.showSnackbar(message = errorMessage)
        }
    }

    // Oluşturulan sohbeti aç
    LaunchedEffect(createdChatId) {
        createdChatId?.let { chatId ->
            onNavigateToChat(chatId)
        }
    }

    // Başarılı sohbet oluşturma sonrası geri dön
    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Sohbet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Arama çubuğu
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Kullanıcı ara...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Ara")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.length >= 2) {
                            viewModel.searchUsers(searchQuery)
                        }
                    }
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Bilinmeyen hata",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kullanıcı bulunamadı",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sohbet etmek istediğiniz kişiyi arayın",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { user ->
                        UserItem(
                            user = user,
                            onClick = {
                                viewModel.startChat(user.id)
                                // Hemen geri dönme, kullanıcıya mesajı göster
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: com.das3kn.iz.data.model.User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil resmi placeholder'ı
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "@${user.username}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                user.bio?.let { bio ->
                    if (bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bio,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
