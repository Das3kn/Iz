package com.das3kn.iz.ui.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.repository.CommentRepository
import com.das3kn.iz.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    fun load(postId: String) {
        val currentState = _uiState.value
        if (currentState.post?.id == postId) {
            _uiState.value = currentState.copy(replyingTo = null, newCommentText = "")
            refreshComments(postId)
            return
        }

        _uiState.value = currentState.copy(
            post = null,
            comments = emptyList(),
            isLoadingPost = true,
            isLoadingComments = true,
            newCommentText = "",
            replyingTo = null,
            error = null
        )

        viewModelScope.launch {
            val postResult = postRepository.getPostById(postId)
            postResult.fold(
                onSuccess = { post ->
                    _uiState.value = _uiState.value.copy(
                        post = post,
                        isLoadingPost = false,
                        isLoadingComments = true,
                        error = null
                    )
                    refreshComments(post.id)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingPost = false,
                        isLoadingComments = false,
                        error = exception.message ?: "Gönderi yüklenemedi"
                    )
                }
            )
        }
    }

    fun refreshComments(postId: String? = _uiState.value.post?.id) {
        val targetPostId = postId ?: return
        _uiState.value = _uiState.value.copy(isLoadingComments = true)
        viewModelScope.launch {
            val result = commentRepository.getCommentsForPost(targetPostId)
            result.fold(
                onSuccess = { comments ->
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        isLoadingComments = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingComments = false,
                        error = exception.message ?: "Yorumlar yüklenemedi"
                    )
                }
            )
        }
    }

    fun updateNewCommentText(text: String) {
        _uiState.value = _uiState.value.copy(newCommentText = text)
    }

    fun submitComment(userId: String, username: String) {
        val trimmed = _uiState.value.newCommentText.trim()
        val post = _uiState.value.post ?: return
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val replyingTo = _uiState.value.replyingTo
            val result = commentRepository.addComment(
                Comment(
                    postId = post.id,
                    userId = userId,
                    username = username,
                    content = trimmed,
                    parentId = replyingTo?.id
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        newCommentText = "",
                        replyingTo = null
                    )
                    refreshComments(post.id)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Yorum eklenemedi"
                    )
                }
            )
        }
    }

    fun startReply(comment: Comment) {
        _uiState.value = _uiState.value.copy(replyingTo = comment)
    }

    fun cancelReply() {
        _uiState.value = _uiState.value.copy(replyingTo = null)
    }


    fun toggleCommentLike(commentId: String, userId: String) {
        val postId = _uiState.value.post?.id ?: return
        viewModelScope.launch {
            val result = commentRepository.toggleCommentLike(commentId, userId)
            result.fold(
                onSuccess = { refreshComments(postId) },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Beğeni işlemi başarısız"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CommentsUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoadingPost: Boolean = false,
    val isLoadingComments: Boolean = false,
    val newCommentText: String = "",
    val replyingTo: Comment? = null,
    val error: String? = null
)
