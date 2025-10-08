package com.das3kn.iz.ui.presentation.posts

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.home.components.ContentFunctions
import com.das3kn.iz.ui.presentation.components.PostMediaGallery
import com.das3kn.iz.ui.presentation.components.VideoPlayerDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    val username = userProfile?.username ?: userProfile?.displayName ?: "Kullanıcı"
    
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Hata mesajını göster ve logla
            Log.e("PostDetailScreen", "Error: $error")
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gönderi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.post?.let { post ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 100.dp) // Bottom bar için alan
                ) {
                    // Post detayı
                    item {
                        PostDetailCard(
                            post = post, 
                            currentUserId = currentUser?.uid ?: "",
                            onLike = { viewModel.togglePostLike(post.id, currentUser?.uid ?: "") },
                            onSave = { viewModel.togglePostSave(post.id, currentUser?.uid ?: "") }
                        )
                    }
                    
                    // Yorumlar başlığı
                    item {
                        Text(
                            text = "Yorumlar (${uiState.comments.size})",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    
                    // Yorumlar
                    if (uiState.isLoadingComments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Yorumlar yükleniyor...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (uiState.comments.isEmpty()) {
                        item {
                            Text(
                                text = "Henüz yorum yapılmamış. İlk yorumu sen yap!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUserId = currentUser?.uid ?: "",
                                onLike = { viewModel.toggleCommentLike(comment.id, currentUser?.uid ?: "") },
                                onReply = { viewModel.setReplyMode(comment) },
                                onReplyLike = { replyId -> viewModel.toggleCommentLike(replyId, currentUser?.uid ?: "") }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Bottom comment input
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
                // Reply mode header
                uiState.replyMode?.let { replyComment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\"${replyComment.username}\" yorumunu yanıtlıyorsun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearReplyMode() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Yanıtlamayı iptal et",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = if (uiState.replyMode != null) uiState.newReplyText else uiState.newCommentText,
                        onValueChange = { 
                            if (uiState.replyMode != null) {
                                viewModel.updateNewReplyText(it)
                            } else {
                                viewModel.updateNewCommentText(it)
                            }
                        },
                        placeholder = { 
                            Text(
                                if (uiState.replyMode != null) "Yanıt yazın..." else "Yorum yazın..."
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val replyMode = uiState.replyMode
                                if (replyMode != null) {
                                    if (uiState.newReplyText.isNotBlank()) {
                                        viewModel.addComment(
                                            content = uiState.newReplyText,
                                            userId = currentUser?.uid ?: "",
                                            username = username,
                                            parentId = replyMode.id
                                        )
                                        viewModel.clearReplyMode()
                                        focusManager.clearFocus()
                                    }
                                } else {
                                    if (uiState.newCommentText.isNotBlank()) {
                                        viewModel.addComment(
                                            content = uiState.newCommentText,
                                            userId = currentUser?.uid ?: "",
                                            username = username
                                        )
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            val replyMode = uiState.replyMode
                            if (replyMode != null) {
                                if (uiState.newReplyText.isNotBlank()) {
                                    viewModel.addComment(
                                        content = uiState.newReplyText,
                                        userId = currentUser?.uid ?: "",
                                        username = username,
                                        parentId = replyMode.id
                                    )
                                    viewModel.clearReplyMode()
                                    focusManager.clearFocus()
                                }
                            } else {
                                if (uiState.newCommentText.isNotBlank()) {
                                    viewModel.addComment(
                                        content = uiState.newCommentText,
                                        userId = currentUser?.uid ?: "",
                                        username = username
                                    )
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        enabled = if (uiState.replyMode != null) uiState.newReplyText.isNotBlank() else uiState.newCommentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Gönder",
                            tint = if ((uiState.replyMode != null && uiState.newReplyText.isNotBlank()) || 
                                       (uiState.replyMode == null && uiState.newCommentText.isNotBlank())) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostDetailCard(
    post: Post,
    currentUserId: String,
    onLike: () -> Unit,
    onSave: () -> Unit
) {
    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTimeAgo(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            // Media content
            if (post.mediaUrls.isNotEmpty()) {
                PostMediaGallery(
                    mediaUrls = post.mediaUrls,
                    mediaType = post.mediaType,
                    modifier = Modifier.fillMaxWidth(),
                    onVideoClick = { selectedVideoUrl = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            ContentFunctions(
                onComment = { /* Already in comment section */ },
                onLike = onLike,
                onRepost = { /* TODO: Repost functionality */ },
                onSave = onSave,
                onMore = { /* TODO: More options */ },
                isLiked = post.likes.contains(currentUserId),
                isSaved = post.saves.contains(currentUserId),
                likeCount = post.likes.size,
                commentCount = post.commentCount,
                saveCount = post.saves.size
            )
        }
    }

    selectedVideoUrl?.let { url ->
        VideoPlayerDialog(
            videoUrl = url,
            onDismiss = { selectedVideoUrl = null },
            title = post.username
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onReplyLike: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Comment header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(1.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comment actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(
                    onClick = onLike,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (comment.likes.contains(currentUserId)) "Beğenildi" else "Beğen",
                            color = if (comment.likes.contains(currentUserId)) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (comment.likes.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${comment.likes.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (comment.likes.contains(currentUserId)) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = onReply,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = "Yanıtla",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Replies
            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                comment.replies.forEach { reply ->
                    ReplyItem(
                        reply = reply,
                        currentUserId = currentUserId,
                        onLike = { onReplyLike(reply.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyItem(
    reply: Comment,
    currentUserId: String,
    onLike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(1.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reply.username,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTimeAgo(reply.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            TextButton(
                onClick = onLike,
                modifier = Modifier.padding(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (reply.likes.contains(currentUserId)) "Beğenildi" else "Beğen",
                        color = if (reply.likes.contains(currentUserId)) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (reply.likes.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${reply.likes.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (reply.likes.contains(currentUserId)) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    
    return when {
        diff < 60000 -> "Az önce"
        diff < 3600000 -> "${diff / 60000} dakika önce"
        diff < 86400000 -> "${diff / 3600000} saat önce"
        diff < 2592000000 -> "${diff / 86400000} gün önce"
        else -> "${diff / 2592000000} ay önce"
    }
}
