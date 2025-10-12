package com.das3kn.iz.ui.presentation.comments

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: CommentsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                    Text(
                        text = "Yorumlar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommentAvatar(
                        avatarUrl = userProfile?.profileImageUrl,
                        initials = userProfile?.displayName?.firstOrNull()?.uppercaseChar()?.toString()
                            ?: userProfile?.username?.firstOrNull()?.uppercaseChar()?.toString()
                            ?: "?"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = uiState.newCommentText,
                        onValueChange = viewModel::updateNewCommentText,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(text = "Yorumunu yaz...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            val userId = currentUser?.uid
                            val username = userProfile?.username ?: userProfile?.displayName
                            if (!userId.isNullOrBlank() && !username.isNullOrBlank()) {
                                viewModel.submitComment(userId, username)
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val userId = currentUser?.uid
                            val username = userProfile?.username ?: userProfile?.displayName
                            if (!userId.isNullOrBlank() && !username.isNullOrBlank()) {
                                viewModel.submitComment(userId, username)
                            }
                        },
                        enabled = uiState.newCommentText.isNotBlank() && currentUser != null,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Gönder")
                    }
                }
            }
        }
    ) { innerPadding ->
        val post = uiState.post
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoadingPost -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                post == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gönderi bulunamadı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        item {
                            PostHeader(
                                post = post,
                                onUserClick = onUserClick
                            )
                        }

                        item {
                            Divider(color = Color(0xFFE5E7EB))
                        }

                        when {
                            uiState.isLoadingComments -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            uiState.comments.isEmpty() -> {
                                item {
                                    Text(
                                        text = "Henüz yorum yok. İlk yorumu sen yap!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }

                            else -> {
                                items(uiState.comments, key = { it.id }) { comment ->
                                    CommentItem(
                                        comment = comment,
                                        currentUserId = currentUser?.uid,
                                        onUserClick = onUserClick,
                                        onLike = {
                                            val userId = currentUser?.uid
                                            if (!userId.isNullOrBlank()) {
                                                viewModel.toggleCommentLike(comment.id, userId)
                                            }
                                        },
                                        onReplyLike = { replyId ->
                                            val userId = currentUser?.uid
                                            if (!userId.isNullOrBlank()) {
                                                viewModel.toggleCommentLike(replyId, userId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostHeader(
    post: Post,
    onUserClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CommentAvatar(
                avatarUrl = post.userProfileImage,
                initials = post.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                onClick = {
                    if (post.userId.isNotBlank()) {
                        onUserClick(post.userId)
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = post.username.ifBlank { "Kullanıcı" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .clip(MaterialTheme.shapes.small)
                )
                Text(
                    text = formatRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (post.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val mediaUrl = post.mediaUrls.firstOrNull()
        if (!mediaUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onLike: () -> Unit,
    onReplyLike: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(alignment = Alignment.Top) {
            CommentAvatar(
                avatarUrl = null,
                initials = comment.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                onClick = {
                    if (comment.userId.isNotBlank()) {
                        onUserClick(comment.userId)
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = comment.username.ifBlank { "Kullanıcı" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatRelativeTime(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (comment.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onLike,
                    enabled = currentUserId != null,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val isLiked = currentUserId != null && comment.likes.contains(currentUserId)
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = comment.likes.size.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLiked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 52.dp)
            ) {
                comment.replies.forEach { reply ->
                    ReplyItem(
                        reply = reply,
                        currentUserId = currentUserId,
                        onUserClick = onUserClick,
                        onLike = { onReplyLike(reply.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyItem(
    reply: Comment,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onLike: () -> Unit
) {
    Row(alignment = Alignment.Top) {
        CommentAvatar(
            avatarUrl = null,
            initials = reply.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            onClick = {
                if (reply.userId.isNotBlank()) {
                    onUserClick(reply.userId)
                }
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = reply.username.ifBlank { "Kullanıcı" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRelativeTime(reply.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (reply.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reply.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onLike,
                enabled = currentUserId != null,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val isLiked = currentUserId != null && reply.likes.contains(currentUserId)
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reply.likes.size.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLiked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CommentAvatar(
    avatarUrl: String?,
    initials: String,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .then(clickableModifier),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
                .then(clickableModifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "Az önce"
        minutes < 60 -> "${minutes} dk"
        hours < 24 -> "${hours} sa"
        days < 7 -> "${days} gün"
        else -> {
            val weeks = days / 7
            "${weeks} hf"
        }
    }
}
