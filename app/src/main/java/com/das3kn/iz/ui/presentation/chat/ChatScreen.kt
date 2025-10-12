package com.das3kn.iz.ui.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val chatUsers by viewModel.chatUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()
    val listState = rememberLazyListState()
    var messageText by rememberSaveable { mutableStateOf("") }

    val partner = activeChatUser ?: chatUsers.firstOrNull { it.id != currentUser.id }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val backgroundColor = Color(0xFFF9FAFB)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            ChatTopBar(
                user = partner,
                onBack = onNavigateBack,
                onUserClick = {
                    partner?.let { user ->
                        onNavigateToProfile(user.id)
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(chatId, messageText.trim())
                        messageText = ""
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState
        ) {
            itemsIndexed(messages) { index, message ->
                val isOwnMessage = message.senderId == currentUser.id
                val previousSenderId = messages.getOrNull(index - 1)?.senderId
                val showAvatar = !isOwnMessage && (previousSenderId == null || previousSenderId != message.senderId)

                ChatMessageRow(
                    message = message,
                    isOwnMessage = isOwnMessage,
                    showAvatar = showAvatar,
                    partner = partner
                )
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    user: User?,
    onBack: () -> Unit,
    onUserClick: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                UserAvatar(
                    user = user,
                    size = 40.dp,
                    modifier = Modifier.clickable(onClick = onUserClick)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.clickable(onClick = onUserClick)
                ) {
                    Text(
                        text = user?.displayName?.takeIf { it.isNotBlank() } ?: "Sohbet",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Çevrimiçi",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Seçenekler",
                        tint = Color(0xFF111827)
                    )
                }
            }

            Divider(color = Color(0xFFE5E7EB))
        }
    }
}

@Composable
private fun ChatMessageRow(
    message: Message,
    isOwnMessage: Boolean,
    showAvatar: Boolean,
    partner: User?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwnMessage) {
            if (showAvatar) {
                UserAvatar(user = partner, size = 32.dp)
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        ChatMessageBubble(message = message, isOwnMessage = isOwnMessage)
    }
}

@Composable
private fun ChatMessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    val bubbleColor = if (isOwnMessage) Color(0xFF7C3AED) else Color.White
    val textColor = if (isOwnMessage) Color.White else Color(0xFF111827)
    val timeColor = if (isOwnMessage) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isOwnMessage) 20.dp else 4.dp,
                    bottomEnd = if (isOwnMessage) 4.dp else 20.dp
                )
            )
            .background(bubbleColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message.content,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = formatMessageTime(message.timestamp),
            color = timeColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Mesajınızı yazın...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF3F4F6),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    textColor = Color(0xFF111827),
                    placeholderColor = Color(0xFF9CA3AF)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSend,
                enabled = messageText.isNotBlank(),
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFB7A6F6),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Gönder"
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(Color(0xFFE5E7EB))

    Box(
        modifier = avatarModifier,
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

private fun formatMessageTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val formatter = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
    return formatter.format(Date(timestamp))
}
