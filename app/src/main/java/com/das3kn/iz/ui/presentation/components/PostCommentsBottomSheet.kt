package com.das3kn.iz.ui.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.das3kn.iz.ui.presentation.posts.CommentItem
import com.das3kn.iz.ui.presentation.posts.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentsBottomSheet(
    viewModel: PostDetailViewModel,
    currentUserId: String,
    currentUsername: String,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val commenterName = currentUsername.ifBlank { "Kullanıcı" }

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus()
            viewModel.clearReplyMode()
            onDismiss()
        },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = BottomSheetDefaults.ContainerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Yorumlar (${uiState.comments.size})",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            when {
                uiState.isLoading || uiState.isLoadingComments -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.comments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz yorum yapılmamış. İlk yorumu sen yap!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUserId = currentUserId,
                                onLike = { viewModel.toggleCommentLike(comment.id, currentUserId) },
                                onReply = { viewModel.setReplyMode(comment) },
                                onReplyLike = { replyId ->
                                    viewModel.toggleCommentLike(replyId, currentUserId)
                                }
                            )
                        }
                    }
                }
            }

            uiState.replyMode?.let { replyComment ->
                ReplyModeHeader(
                    username = replyComment.username,
                    onClear = {
                        viewModel.clearReplyMode()
                        focusManager.clearFocus()
                    }
                )
            }

            val isReplying = uiState.replyMode != null
            val commentText = if (isReplying) uiState.newReplyText else uiState.newCommentText
            val canSend = commentText.isNotBlank() && currentUserId.isNotBlank()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        if (isReplying) {
                            viewModel.updateNewReplyText(it)
                        } else {
                            viewModel.updateNewCommentText(it)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(if (isReplying) "Yanıt yazın..." else "Yorum yazın...")
                    },
                    enabled = currentUserId.isNotBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) {
                                if (isReplying) {
                                    viewModel.addComment(
                                        content = uiState.newReplyText,
                                        userId = currentUserId,
                                        username = commenterName,
                                        parentId = uiState.replyMode?.id
                                    )
                                    viewModel.clearReplyMode()
                                } else {
                                    viewModel.addComment(
                                        content = uiState.newCommentText,
                                        userId = currentUserId,
                                        username = commenterName
                                    )
                                }
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.size(12.dp))

                IconButton(
                    onClick = {
                        if (canSend) {
                            if (isReplying) {
                                viewModel.addComment(
                                    content = uiState.newReplyText,
                                    userId = currentUserId,
                                    username = commenterName,
                                    parentId = uiState.replyMode?.id
                                )
                                viewModel.clearReplyMode()
                            } else {
                                viewModel.addComment(
                                    content = uiState.newCommentText,
                                    userId = currentUserId,
                                    username = commenterName
                                )
                            }
                            focusManager.clearFocus()
                        }
                    },
                    enabled = canSend
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gönder",
                        tint = if (canSend) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (currentUserId.isBlank()) {
                TextButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Yorum yapmak için giriş yapmalısın",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyModeHeader(
    username: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\"$username\" yorumunu yanıtlıyorsun",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onClear
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat"
                )
            }
        }
    }
}
