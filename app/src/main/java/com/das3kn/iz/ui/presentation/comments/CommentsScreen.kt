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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
            CommentComposer(
                initials = userProfile?.displayName?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: userProfile?.username?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: "?",
                avatarUrl = userProfile?.profileImageUrl,
                value = uiState.newCommentText,
                isEnabled = uiState.newCommentText.isNotBlank() && currentUser != null,
                replyingTo = uiState.replyingTo,
                onCancelReply = viewModel::cancelReply,
                onValueChange = viewModel::updateNewCommentText,
                onSend = {
                    val userId = currentUser?.uid
                    val username = userProfile?.username ?: userProfile?.displayName
                    if (!userId.isNullOrBlank() && !username.isNullOrBlank()) {
                        viewModel.submitComment(userId, username)
                    }
                }
            )
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
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
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Henüz yorum yok.",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "İlk yorumu sen yap!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            else -> {
                                itemsIndexed(uiState.comments, key = { _, item -> item.id }) { index, comment ->
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
                                        onReply = { viewModel.startReply(comment) },
                                        onReplyLike = { replyId ->
                                            val userId = currentUser?.uid
                                            if (!userId.isNullOrBlank()) {
                                                viewModel.toggleCommentLike(replyId, userId)
                                            }
                                        }
                                    )

                                    if (index < uiState.comments.lastIndex) {
                                        Divider(
                                            modifier = Modifier.padding(start = 76.dp),
                                            color = Color(0xFFE5E7EB)
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
    onReply: () -> Unit,
    onReplyLike: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.username.ifBlank { "Kullanıcı" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LikePill(
                        isLiked = currentUserId != null && comment.likes.contains(currentUserId),
                        likeCount = comment.likes.size,
                        onClick = onLike,
                        enabled = currentUserId != null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Yanıtla",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF7C3AED),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = currentUserId != null) { onReply() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
@Composable
private fun ReplyItem(
    reply: Comment,
    currentUserId: String?,
    onUserClick: (String) -> Unit,
    onLike: () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reply.username.ifBlank { "Kullanıcı" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "·",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            LikePill(
                isLiked = currentUserId != null && reply.likes.contains(currentUserId),
                likeCount = reply.likes.size,
                onClick = onLike,
                enabled = currentUserId != null
            )
        }
    }
}

@Composable
private fun LikePill(
    isLiked: Boolean,
    likeCount: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val heartTint = if (isLiked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = heartTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = likeCount.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = heartTint
        )
    }
}

@Composable
private fun CommentComposer(
    initials: String,
    avatarUrl: String?,
    value: String,
    replyingTo: Comment?,
    isEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Yanıtlıyor: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(replyingTo.username.ifBlank { "Kullanıcı" })
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Vazgeç",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF7C3AED),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCancelReply() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                CommentAvatar(
                    avatarUrl = avatarUrl,
                    initials = initials
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = "Yorumunu yaz...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (isEnabled) onSend() })
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = if (isEnabled) Color(0xFF7C3AED) else Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "Gönder",
                        color = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = isEnabled) { onSend() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
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
