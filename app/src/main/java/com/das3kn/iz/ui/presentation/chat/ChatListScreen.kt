package com.das3kn.iz.ui.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val chatUsers by viewModel.chatUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    // Ekran focus olduğunda sohbetleri yenile
    LaunchedEffect(Unit) {
        viewModel.refreshChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sohbetler") },
                actions = {
                    IconButton(onClick = onNavigateToNewChat) {
                        Icon(Icons.Default.Add, contentDescription = "Yeni Sohbet")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "Bilinmeyen hata",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz sohbet yok",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chats) { chat ->
                    val users = chatUsers[chat.id] ?: emptyList()
                    ChatItem(
                        chat = chat,
                        users = users,
                        onClick = { onNavigateToChat(chat.id) },
                        currentUserId = viewModel.getCurrentUserId()
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    users: List<com.das3kn.iz.data.model.User>,
    onClick: () -> Unit,
    currentUserId: String?
) {
    // Mevcut kullanıcı hariç diğer kullanıcıları al (karşı taraf)
    val otherUsers = users.filter { it.id != currentUserId }
    
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
                    text = otherUsers.firstOrNull()?.displayName?.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = otherUsers.map { it.displayName }.filter { it.isNotEmpty() }.joinToString(", "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                chat.lastMessage?.let { message ->
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTime(chat.lastMessageTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Okunmamış mesaj sayısı (sadece karşı tarafın mesajları için)
            if (currentUserId != null) {
                val unreadCount = chat.unreadCount[currentUserId] ?: 0
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - timestamp
    
    return when {
        diff < 60000 -> "Şimdi" // 1 dakika
        diff < 3600000 -> "${diff / 60000} dk" // 1 saat
        diff < 86400000 -> "${diff / 3600000} sa" // 1 gün
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}
