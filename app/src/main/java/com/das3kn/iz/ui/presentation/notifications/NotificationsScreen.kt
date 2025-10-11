package com.das3kn.iz.ui.presentation.notifications

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Bildirimler", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (uiState.notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(text = "Tümünü okundu işaretle")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Bir hata oluştu",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.notifications.isEmpty() -> {
                EmptyNotificationsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationListItem(
                            notification = notification,
                            onUserClick = {
                                navController.navigate("${MainNavTarget.ProfileScreen.route}/${notification.user.id}")
                            },
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onAcceptFriendRequest = { viewModel.acceptFriendRequest(notification.id) },
                            onRejectFriendRequest = { viewModel.rejectFriendRequest(notification.id) },
                            onAcceptGroupInvite = { viewModel.acceptGroupInvite(notification.id) },
                            onRejectGroupInvite = { viewModel.rejectGroupInvite(notification.id) },
                            onPostClick = notification.post?.id?.let { postId ->
                                {
                                    navController.navigate("${MainNavTarget.PostDetailScreen.route}/$postId")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = "Henüz bildirim yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Bildirimleriniz burada görünecek",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationListItem(
    notification: NotificationItem,
    onUserClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onAcceptFriendRequest: () -> Unit,
    onRejectFriendRequest: () -> Unit,
    onAcceptGroupInvite: () -> Unit,
    onRejectGroupInvite: () -> Unit,
    onPostClick: (() -> Unit)?
) {
    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = !notification.isRead, onClick = onMarkAsRead)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (notification.user.avatarUrl != null) {
                    AsyncImage(
                        model = notification.user.avatarUrl,
                        contentDescription = notification.user.name,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = notification.user.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val (icon, color) = notification.type.icon()
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            onMarkAsRead()
                            onUserClick()
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notificationMessage(notification),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        notification.post?.imageUrl?.let { imageUrl ->
            Spacer(modifier = Modifier.size(12.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = notification.post.title ?: "Post görseli",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = onPostClick != null) {
                        onMarkAsRead()
                        onPostClick?.invoke()
                    }
            )
        }

        when (notification.type) {
            NotificationType.FRIEND_REQUEST -> {
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(
                        onClick = onAcceptFriendRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Kabul Et")
                    }
                    OutlinedButton(
                        onClick = onRejectFriendRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Reddet")
                    }
                }
            }

            NotificationType.GROUP_INVITE -> {
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(
                        onClick = onAcceptGroupInvite,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Katıl")
                    }
                    OutlinedButton(
                        onClick = onRejectGroupInvite,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Reddet")
                    }
                }
            }

            NotificationType.COMMENT,
            NotificationType.LIKE,
            NotificationType.TAG,
            NotificationType.REPOST -> Unit
        }
    }
}

@Composable
private fun NotificationType.icon(): Pair<ImageVector, Color> {
    return when (this) {
        NotificationType.FRIEND_REQUEST -> Icons.Outlined.PersonAdd to MaterialTheme.colorScheme.primary
        NotificationType.GROUP_INVITE -> Icons.Outlined.GroupAdd to MaterialTheme.colorScheme.tertiary
        NotificationType.LIKE -> Icons.Outlined.FavoriteBorder to MaterialTheme.colorScheme.primary
        NotificationType.COMMENT -> Icons.Outlined.ChatBubbleOutline to MaterialTheme.colorScheme.tertiary
        NotificationType.TAG -> Icons.Outlined.AlternateEmail to MaterialTheme.colorScheme.primary
        NotificationType.REPOST -> Icons.Outlined.Repeat to MaterialTheme.colorScheme.tertiary
    }
}

private fun notificationMessage(notification: NotificationItem): String {
    return when (notification.type) {
        NotificationType.FRIEND_REQUEST -> "arkadaşlık isteği gönderdi"
        NotificationType.GROUP_INVITE -> "seni ${notification.group?.name ?: "bir grup"} grubuna davet etti"
        NotificationType.LIKE -> "paylaşımını beğendi"
        NotificationType.COMMENT -> "paylaşımına yorum yaptı: \"${notification.content.orEmpty()}\""
        NotificationType.TAG -> "bir paylaşımda seni etiketledi"
        NotificationType.REPOST -> "paylaşımını yeniden paylaştı"
    }
}

private fun formatTimestamp(timestamp: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(timestamp, now)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        minutes < 60 -> "$minutes dk önce"
        hours < 24 -> "$hours sa önce"
        days == 1L -> "Dün"
        days in 2..6 -> "$days gün önce"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            timestamp.atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }
    }
}
