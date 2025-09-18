package com.das3kn.iz.ui.presentation.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.repository.CommentRepository
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.SavedPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val savedPostRepository: SavedPostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Post'u getir
                val postResult = postRepository.getPostById(postId)
                postResult.fold(
                    onSuccess = { post ->
                        _uiState.value = _uiState.value.copy(
                            post = post,
                            isLoading = false
                        )
                        // Yorumları yükle
                        loadComments(postId)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Post yüklenemedi",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata",
                    isLoading = false
                )
            }
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostDetailViewModel", "loadComments: postId=$postId")
                val result = commentRepository.getCommentsForPost(postId)
                result.fold(
                    onSuccess = { comments ->
                        android.util.Log.d("PostDetailViewModel", "loadComments: ${comments.size} comments loaded")
                        _uiState.value = _uiState.value.copy(
                            comments = comments,
                            isLoadingComments = false
                        )
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PostDetailViewModel", "loadComments failed", exception)
                        val errorMessage = when {
                            exception.message?.contains("index") == true -> 
                                "Yorumlar yüklenirken index hatası oluştu. Lütfen tekrar deneyin."
                            else -> exception.message ?: "Yorumlar yüklenemedi"
                        }
                        _uiState.value = _uiState.value.copy(
                            error = errorMessage,
                            isLoadingComments = false
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "loadComments exception", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata",
                    isLoadingComments = false
                )
            }
        }
    }

    fun addComment(content: String, userId: String, username: String, parentId: String? = null) {
        android.util.Log.d("PostDetailViewModel", "addComment called: content='$content', userId='$userId', username='$username'")
        
        if (content.isBlank()) {
            android.util.Log.w("PostDetailViewModel", "addComment: content is blank")
            return
        }
        
        val post = _uiState.value.post
        if (post == null) {
            android.util.Log.e("PostDetailViewModel", "addComment: post is null")
            return
        }
        
        android.util.Log.d("PostDetailViewModel", "addComment: postId=${post.id}")
        
        viewModelScope.launch {
            try {
                val comment = Comment(
                    postId = post.id,
                    userId = userId,
                    username = username,
                    content = content.trim(),
                    parentId = parentId
                )
                
                android.util.Log.d("PostDetailViewModel", "Comment object created: $comment")
                
                val result = commentRepository.addComment(comment)
                result.fold(
                    onSuccess = { savedComment ->
                        android.util.Log.d("PostDetailViewModel", "Comment saved successfully: ${savedComment.id}")
                        // Yorum eklendikten sonra yorumları yeniden yükle
                        loadComments(post.id)
                        // UI state'i temizle
                        _uiState.value = _uiState.value.copy(
                            newCommentText = "",
                            newReplyText = ""
                        )
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PostDetailViewModel", "addComment failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Yorum eklenemedi"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "addComment exception", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun toggleCommentLike(commentId: String, userId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostDetailViewModel", "toggleCommentLike: commentId=$commentId, userId=$userId")
                val result = commentRepository.toggleCommentLike(commentId, userId)
                result.fold(
                    onSuccess = {
                        android.util.Log.d("PostDetailViewModel", "Comment like toggled successfully")
                        // Yorumları yeniden yükle
                        _uiState.value.post?.let { loadComments(it.id) }
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PostDetailViewModel", "Comment like toggle failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Beğeni işlemi başarısız"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "Comment like toggle exception", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    fun updateNewCommentText(text: String) {
        _uiState.value = _uiState.value.copy(newCommentText = text)
    }

    fun updateNewReplyText(text: String) {
        _uiState.value = _uiState.value.copy(newReplyText = text)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun setReplyMode(comment: Comment) {
        _uiState.value = _uiState.value.copy(
            replyMode = comment,
            newReplyText = ""
        )
    }
    
    fun clearReplyMode() {
        _uiState.value = _uiState.value.copy(replyMode = null)
    }
    
    fun togglePostLike(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostDetailViewModel", "togglePostLike: postId=$postId, userId=$userId")
                val result = postRepository.togglePostLike(postId, userId)
                result.fold(
                    onSuccess = {
                        android.util.Log.d("PostDetailViewModel", "Post like toggled successfully")
                        // Post'u yeniden yükle
                        loadPost(postId)
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PostDetailViewModel", "Post like toggle failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Post beğeni işlemi başarısız"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "Post like toggle exception", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }
    
    fun togglePostSave(postId: String, userId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("PostDetailViewModel", "togglePostSave: postId=$postId, userId=$userId")
                val result = savedPostRepository.toggleSavePost(userId, postId)
                result.fold(
                    onSuccess = {
                        android.util.Log.d("PostDetailViewModel", "Post save toggled successfully")
                        // Post'u yeniden yükle
                        loadPost(postId)
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PostDetailViewModel", "Post save toggle failed", exception)
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Post kaydetme işlemi başarısız"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "Post save toggle exception", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }
}

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val error: String? = null,
    val newCommentText: String = "",
    val newReplyText: String = "",
    val replyMode: Comment? = null // Yanıtlanacak yorum
)
