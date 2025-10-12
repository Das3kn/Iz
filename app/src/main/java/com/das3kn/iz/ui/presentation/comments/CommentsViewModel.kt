package com.das3kn.iz.ui.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Basit bir yorum ViewModel'i. Gerçek verileri yüklemeye çalışırken örnek verilerle
 * de deneyim sunar.
 */
data class CommentsUiState(
    val postId: String? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    fun loadComments(postId: String) {
        _uiState.value = CommentsUiState(postId = postId, isLoading = true)

        viewModelScope.launch {
            commentRepository.getCommentsForPost(postId)
                .onSuccess { comments ->
                    val resolvedComments = if (comments.isEmpty()) {
                        buildSampleComments(postId)
                    } else {
                        comments
                    }
                    _uiState.value = CommentsUiState(
                        postId = postId,
                        comments = resolvedComments,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = CommentsUiState(
                        postId = postId,
                        comments = buildSampleComments(postId),
                        isLoading = false,
                        errorMessage = error.message ?: "Yorumlar yüklenemedi"
                    )
                }
        }
    }

    fun toggleLike(commentId: String, userId: String) {
        val currentState = _uiState.value
        val updatedComments = currentState.comments.map { comment ->
            if (comment.id == commentId) {
                val updatedLikes = comment.likes.toMutableList().apply {
                    if (contains(userId)) remove(userId) else add(userId)
                }
                comment.copy(likes = updatedLikes)
            } else {
                comment
            }
        }
        _uiState.value = currentState.copy(comments = updatedComments)
    }

    fun addLocalComment(authorId: String, authorName: String, content: String) {
        val postId = _uiState.value.postId ?: return
        val newComment = Comment(
            id = "local-${System.currentTimeMillis()}",
            postId = postId,
            userId = authorId,
            username = authorName,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            comments = listOf(newComment) + _uiState.value.comments
        )
    }

    private fun buildSampleComments(postId: String): List<Comment> = listOf(
        Comment(
            id = "sample-1",
            postId = postId,
            userId = "user-2",
            username = "Ayşe Demir",
            content = "Bu konu gerçekten çok ilginç, detayları merak ediyorum!",
            createdAt = System.currentTimeMillis() - 45 * 60 * 1000,
            likes = listOf("user-4", "user-5")
        ),
        Comment(
            id = "sample-2",
            postId = postId,
            userId = "user-3",
            username = "Mehmet Kaya",
            content = "Ben de benzer bir projede çalışıyorum, deneyimlerimi paylaşabilirim.",
            createdAt = System.currentTimeMillis() - 30 * 60 * 1000,
            likes = listOf("user-1")
        ),
        Comment(
            id = "sample-3",
            postId = postId,
            userId = "user-1",
            username = "Elif Kaya",
            content = "Katkılarınız için teşekkürler! Beraber bir çalışma yapabiliriz.",
            createdAt = System.currentTimeMillis() - 10 * 60 * 1000
        )
    )
}
