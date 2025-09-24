package com.das3kn.iz.ui.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.model.MessageMediaType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatState by viewModel.chatState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val chatUsers by viewModel.chatUsers.collectAsState()
    val messageText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val otherUsers = chatUsers.filter { it.id != viewModel.getCurrentUserId() }
                    val otherUser = otherUsers.firstOrNull()
                    Text(
                        text = otherUsers.map { it.displayName }.filter { it.isNotEmpty() }.joinToString(", ") 
                            .ifEmpty { "Sohbet" },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            otherUser?.let { user ->
                                onNavigateToProfile(user.id)
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        bottomBar = {
            ChatInput(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                onSendClick = {
                    if (messageText.value.isNotBlank()) {
                        viewModel.sendMessage(chatId, messageText.value)
                        messageText.value = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    currentUserId = viewModel.getCurrentUserId()
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    currentUserId: String?
) {
    val isOwnMessage = message.senderId == currentUserId
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isOwnMessage) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isOwnMessage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTime(message.timestamp),
                    fontSize = 12.sp,
                    color = if (isOwnMessage) Color.White.copy(alpha = 0.7f) 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Mesajınızı yazın...") },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        FloatingActionButton(
            onClick = onSendClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Gönder")
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60000 -> "Şimdi" // 1 dakika
        diff < 3600000 -> "${diff / 60000} dk" // 1 saat
        diff < 86400000 -> "${diff / 3600000} sa" // 1 gün
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}
