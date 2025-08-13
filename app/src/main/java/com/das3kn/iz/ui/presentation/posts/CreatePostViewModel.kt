package com.das3kn.iz.ui.presentation.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.MediaType
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Post yüklendikten sonra HomeViewModel'i güncellemek için event
    private val _postCreatedEvent = MutableStateFlow(false)
    val postCreatedEvent: StateFlow<Boolean> = _postCreatedEvent.asStateFlow()

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun addImage(imageResId: Int) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        if (!currentImages.contains(imageResId)) {
            currentImages.add(imageResId)
            _uiState.update { it.copy(selectedImages = currentImages) }
        }
    }

    fun removeImage(index: Int) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _uiState.update { it.copy(selectedImages = currentImages) }
        }
    }

    fun createPost() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = authRepository.currentUser
                if (currentUser == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Kullanıcı giriş yapmamış"
                        ) 
                    }
                    return@launch
                }

                // Firestore'dan kullanıcı profil bilgilerini al
                val userProfileResult = authRepository.getCurrentUserProfile()
                userProfileResult.fold(
                    onSuccess = { userProfile ->
                        // Post oluştur
                        val post = Post(
                            userId = currentUser.uid,
                            username = userProfile.displayName.ifBlank { 
                                userProfile.username.ifBlank { 
                                    currentUser.email?.split("@")?.firstOrNull() ?: "Kullanıcı" 
                                } 
                            },
                            userProfileImage = userProfile.profileImageUrl.ifBlank { 
                                currentUser.photoUrl?.toString() ?: "" 
                            },
                            content = uiState.value.content,
                            mediaUrls = emptyList(), // TODO: Gerçek media upload implement edilecek
                            mediaType = if (uiState.value.selectedImages.isNotEmpty()) MediaType.IMAGE else MediaType.TEXT,
                            likes = emptyList(),
                            comments = emptyList(),
                            shares = 0,
                            createdAt = System.currentTimeMillis(),
                            tags = emptyList(),
                            category = ""
                        )

                                        val result = postRepository.createPost(post)
                result.fold(
                    onSuccess = { createdPost ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                isPostCreated = true 
                            ) 
                        }
                        // Post yüklendikten sonra event'i tetikle
                        _postCreatedEvent.value = true
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = exception.message ?: "Post oluşturulamadı"
                            ) 
                        }
                    }
                )
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Kullanıcı bilgileri alınamadı: ${exception.message}"
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

    fun resetState() {
        _uiState.value = CreatePostUiState()
    }
}

data class CreatePostUiState(
    val content: String = "",
    val selectedImages: List<Int> = emptyList(),
    val isPostCreated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
