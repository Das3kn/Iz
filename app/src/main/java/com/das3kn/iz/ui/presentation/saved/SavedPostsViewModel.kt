package com.das3kn.iz.ui.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.SavedPostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    private val savedPostRepository: SavedPostRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedPostsUiState())
    val uiState: StateFlow<SavedPostsUiState> = _uiState.asStateFlow()

    fun loadSavedPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = savedPostRepository.getSavedPosts(_uiState.value.currentUserId)
                result.fold(
                    onSuccess = { posts ->
                        _uiState.update { 
                            it.copy(
                                savedPosts = posts,
                                isLoading = false,
                                error = null
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = exception.message ?: "Kaydedilen post'lar yüklenemedi"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Beklenmeyen hata oluştu"
                    ) 
                }
            }
        }
    }
    
    fun setCurrentUserId(userId: String) {
        _uiState.update { it.copy(currentUserId = userId) }
    }
    
    // Like/unlike toggle
    fun toggleLike(postId: String, userId: String) {
        if (userId.isBlank() || postId.isBlank()) return
        
        viewModelScope.launch {
            try {
                val currentPost = _uiState.value.savedPosts.find { it.id == postId }
                if (currentPost == null) return@launch
                
                val isCurrentlyLiked = currentPost.likes.contains(userId)
                
                val result = if (isCurrentlyLiked) {
                    postRepository.unlikePost(postId, userId)
                } else {
                    postRepository.likePost(postId, userId)
                }
                
                result.fold(
                    onSuccess = {
                        val updatedPosts = _uiState.value.savedPosts.map { post ->
                            if (post.id == postId) {
                                if (isCurrentlyLiked) {
                                    post.copy(likes = post.likes - userId)
                                } else {
                                    post.copy(likes = post.likes + userId)
                                }
                            } else {
                                post
                            }
                        }
                        _uiState.update { it.copy(savedPosts = updatedPosts) }
                    },
                    onFailure = { exception ->
                        // TODO: Hata mesajı göster
                    }
                )
            } catch (e: Exception) {
                // TODO: Hata mesajı göster
            }
        }
    }
    
    // Save/unsave toggle
    fun toggleSave(postId: String, userId: String) {
        if (userId.isBlank() || postId.isBlank()) return
        
        viewModelScope.launch {
            try {
                val currentPost = _uiState.value.savedPosts.find { it.id == postId }
                if (currentPost == null) return@launch
                
                val isCurrentlySaved = currentPost.saves.contains(userId)
                
                val result = if (isCurrentlySaved) {
                    savedPostRepository.unsavePost(userId, postId)
                } else {
                    savedPostRepository.savePost(userId, postId)
                }
                
                result.fold(
                    onSuccess = {
                        if (isCurrentlySaved) {
                            // Post kaydı kaldırıldı, listeden çıkar
                            val updatedPosts = _uiState.value.savedPosts.filter { it.id != postId }
                            _uiState.update { it.copy(savedPosts = updatedPosts) }
                        } else {
                            // Post kaydedildi, UI'ı güncelle
                            val updatedPosts = _uiState.value.savedPosts.map { post ->
                                if (post.id == postId) {
                                    post.copy(saves = post.saves + userId)
                                } else {
                                    post
                                }
                            }
                            _uiState.update { it.copy(savedPosts = updatedPosts) }
                        }
                    },
                    onFailure = { exception ->
                        // TODO: Hata mesajı göster
                    }
                )
            } catch (e: Exception) {
                // TODO: Hata mesajı göster
            }
        }
    }
}

data class SavedPostsUiState(
    val savedPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)
