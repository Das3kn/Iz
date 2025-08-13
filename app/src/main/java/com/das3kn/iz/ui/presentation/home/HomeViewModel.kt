package com.das3kn.iz.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = postRepository.getPosts(limit = 20)
                result.fold(
                    onSuccess = { posts ->
                        // Sadece ID'si olan post'ları göster
                        val validPosts = posts.filter { it.id.isNotBlank() }
                        android.util.Log.d("HomeViewModel", "loadPosts: total posts=${posts.size}, valid posts=${validPosts.size}")
                        
                        _uiState.update { 
                            it.copy(
                                posts = validPosts,
                                isLoading = false,
                                error = null
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = exception.message ?: "Post'lar yüklenemedi"
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

    fun refreshPosts() {
        loadPosts()
    }
    
    // Post yüklendikten sonra otomatik güncelleme için
    fun onPostCreated() {
        loadPosts()
    }
    
    // Like/unlike toggle
    fun toggleLike(postId: String, userId: String) {
        if (userId.isBlank()) {
            android.util.Log.d("HomeViewModel", "toggleLike: userId is blank")
            return
        }
        
        if (postId.isBlank()) {
            android.util.Log.d("HomeViewModel", "toggleLike: postId is blank, skipping like operation")
            return
        }
        
        android.util.Log.d("HomeViewModel", "toggleLike: postId=$postId, userId=$userId")
        
        viewModelScope.launch {
            try {
                // Mevcut post'u bul
                val currentPost = _uiState.value.posts.find { it.id == postId }
                if (currentPost == null) {
                    android.util.Log.d("HomeViewModel", "toggleLike: post not found")
                    return@launch
                }
                
                android.util.Log.d("HomeViewModel", "toggleLike: current post likes=${currentPost.likes}")
                
                // Like durumunu kontrol et
                val isCurrentlyLiked = currentPost.likes.contains(userId)
                android.util.Log.d("HomeViewModel", "toggleLike: isCurrentlyLiked=$isCurrentlyLiked")
                
                val result = if (isCurrentlyLiked) {
                    android.util.Log.d("HomeViewModel", "toggleLike: calling unlikePost")
                    postRepository.unlikePost(postId, userId)
                } else {
                    android.util.Log.d("HomeViewModel", "toggleLike: calling likePost")
                    postRepository.likePost(postId, userId)
                }
                
                result.fold(
                    onSuccess = {
                        android.util.Log.d("HomeViewModel", "toggleLike: success, updating UI")
                        // UI'ı güncelle
                        val updatedPosts = _uiState.value.posts.map { post ->
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
                        android.util.Log.d("HomeViewModel", "toggleLike: updated posts likes=${updatedPosts.find { it.id == postId }?.likes}")
                        _uiState.update { it.copy(posts = updatedPosts) }
                    },
                    onFailure = { exception ->
                        android.util.Log.e("HomeViewModel", "toggleLike: failed", exception)
                        // Hata durumunda UI'ı güncelleme
                        // TODO: Hata mesajı göster
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "toggleLike: exception", e)
                // Hata durumunda UI'ı güncelleme
                // TODO: Hata mesajı göster
            }
        }
    }
}

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
