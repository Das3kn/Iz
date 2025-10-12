package com.das3kn.iz.ui.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.User
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
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
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    val filteredChats by remember(chats, chatUsers, searchQuery) {
        derivedStateOf {
            filterChats(chats, chatUsers, viewModel.getCurrentUserId(), searchQuery)
        }
    }

    val backgroundColor = Color(0xFFF9FAFB)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            ChatsTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Bilinmeyen hata",
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                filteredChats.isEmpty() -> {
                    val message = if (searchQuery.isNotBlank()) {
                        "Sohbet bulunamadı"
                    } else {
                        "Henüz mesajınız yok"
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(filteredChats) { index, chat ->
                            val users = chatUsers[chat.id] ?: emptyList()
                            ChatListItem(
                                chat = chat,
                                users = users,
                                currentUserId = viewModel.getCurrentUserId(),
                                onClick = { onNavigateToChat(chat.id) }
                            )

                            if (index < filteredChats.lastIndex) {
                                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.padding(start = 88.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun filterChats(
    chats: List<Chat>,
    chatUsers: Map<String, List<User>>,
    currentUserId: String?,
    query: String
): List<Chat> {
    val trimmedQuery = query.trim().lowercase(Locale.getDefault())
    if (trimmedQuery.isEmpty()) return chats

    return chats.filter { chat ->
        val users = chatUsers[chat.id] ?: emptyList()
        val otherUsers = users.filter { it.id != currentUserId }
        otherUsers.any { user ->
            user.displayName.lowercase(Locale.getDefault()).contains(trimmedQuery) ||
                    user.username.lowercase(Locale.getDefault()).contains(trimmedQuery)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Surface(color = Color.White, shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Mesajlar",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Sohbet ara...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedPlaceholderColor = Color(0xFF9CA3AF),
                    unfocusedPlaceholderColor = Color(0xFF9CA3AF)
                )
            )
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    users: List<User>,
    currentUserId: String?,
    onClick: () -> Unit
) {
    val otherUser = users.firstOrNull { it.id != currentUserId } ?: users.firstOrNull()
    val lastMessageText = chat.lastMessage?.content?.takeIf { it.isNotBlank() } ?: "Henüz mesaj yok"
    val timestampText = if (chat.lastMessageTime > 0) formatChatTimestamp(chat.lastMessageTime) else ""
    val unreadCount = currentUserId?.let { chat.unreadCount[it] ?: 0 } ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChatAvatar(user = otherUser)

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherUser?.displayName?.takeIf { it.isNotBlank() } ?: "Sohbet",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (timestampText.isNotEmpty()) {
                        Text(
                            text = timestampText,
                            color = Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastMessageText,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(user: User?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFE5E7EB)),
        contentAlignment = Alignment.Center
    ) {
        val imageUrl = user?.profileImageUrl.orEmpty()
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = user?.displayName,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Color(0xFF4C1D95),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatChatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minute = 60_000
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < hour -> "${(diff / minute).coerceAtLeast(1)} dk"
        diff < day -> "${diff / hour} sa"
        diff < 2 * day -> "Dün"
        diff < 7 * day -> "${diff / day} gün"
        else -> "${diff / day} gün"
    }
}
