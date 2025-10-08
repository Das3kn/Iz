package com.das3kn.iz.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.SavedPostRepository
import com.das3kn.iz.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val savedPostRepository: SavedPostRepository
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
        if (userId.isBlank() || postId.isBlank()) {
            android.util.Log.d("HomeViewModel", "toggleLike: missing information")
            return
        }

        viewModelScope.launch {
            try {
                val result = postRepository.togglePostLike(postId, userId)
                result.fold(
                    onSuccess = {
                        loadPosts()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("HomeViewModel", "toggleLike failed", exception)
                        _uiState.update { it.copy(error = exception.message ?: "Beğeni işlemi başarısız") }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "toggleLike exception", e)
                _uiState.update { it.copy(error = e.message ?: "Beğeni işlemi başarısız") }
            }
        }
    }

    fun toggleSave(postId: String, userId: String) {
        if (userId.isBlank() || postId.isBlank()) {
            android.util.Log.d("HomeViewModel", "toggleSave: missing information")
            return
        }

        viewModelScope.launch {
            try {
                val result = savedPostRepository.toggleSavePost(userId, postId)
                result.fold(
                    onSuccess = {
                        loadPosts()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("HomeViewModel", "toggleSave failed", exception)
                        _uiState.update { it.copy(error = exception.message ?: "Kaydetme işlemi başarısız") }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "toggleSave exception", e)
                _uiState.update { it.copy(error = e.message ?: "Kaydetme işlemi başarısız") }
            }
        }
    }

    fun repostPost(originalPost: Post, currentUserId: String, userProfile: User?) {
        if (currentUserId.isBlank()) {
            android.util.Log.d("HomeViewModel", "repostPost: user id is blank")
            return
        }

        if (originalPost.id.isBlank()) {
            android.util.Log.d("HomeViewModel", "repostPost: original post id is blank")
            return
        }

        val profile = userProfile?.let {
            if (it.id.isBlank()) it.copy(id = currentUserId) else it
        } ?: User(id = currentUserId)

        viewModelScope.launch {
            try {
                val result = postRepository.createRepost(originalPost, profile)
                result.fold(
                    onSuccess = {
                        loadPosts()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("HomeViewModel", "repostPost failed", exception)
                        _uiState.update { it.copy(error = exception.message ?: "Yeniden paylaşım başarısız") }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "repostPost exception", e)
                _uiState.update { it.copy(error = e.message ?: "Yeniden paylaşım başarısız") }
            }
        }
    }
}

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
