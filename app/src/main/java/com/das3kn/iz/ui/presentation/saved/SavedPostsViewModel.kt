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
                val result = postRepository.togglePostLike(postId, userId)
                result.fold(
                    onSuccess = {
                        loadSavedPosts()
                    },
                    onFailure = { exception ->
                        _uiState.update { it.copy(error = exception.message ?: "Beğeni işlemi başarısız") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Beğeni işlemi başarısız") }
            }
        }
    }

    fun toggleSave(postId: String, userId: String) {
        if (userId.isBlank() || postId.isBlank()) return

        viewModelScope.launch {
            try {
                val result = savedPostRepository.toggleSavePost(userId, postId)
                result.fold(
                    onSuccess = {
                        loadSavedPosts()
                    },
                    onFailure = { exception ->
                        _uiState.update { it.copy(error = exception.message ?: "Kaydetme işlemi başarısız") }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Kaydetme işlemi başarısız") }
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
