package com.das3kn.iz.ui.presentation.posts

import android.content.Context
import android.net.Uri
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

    fun addMediaUri(uri: Uri, isVideo: Boolean = false) {
        android.util.Log.d("CreatePostViewModel", "addMediaUri called - uri: $uri, isVideo: $isVideo")
        val currentMedia = _uiState.value.selectedMediaUris.toMutableList()
        val mediaItem = MediaItem(uri = uri, isVideo = isVideo)
        currentMedia.add(mediaItem)
        _uiState.update { 
            it.copy(
                selectedMediaUris = currentMedia,
                mediaType = when {
                    currentMedia.any { it.isVideo } && currentMedia.any { !it.isVideo } -> MediaType.MIXED
                    currentMedia.any { it.isVideo } -> MediaType.VIDEO
                    currentMedia.any { !it.isVideo } -> MediaType.IMAGE
                    else -> MediaType.TEXT
                }
            ) 
        }
        android.util.Log.d("CreatePostViewModel", "Media added. Total media count: ${currentMedia.size}")
    }

    fun removeImage(index: Int) {
        val currentImages = _uiState.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _uiState.update { it.copy(selectedImages = currentImages) }
        }
    }

    fun removeMediaUri(index: Int) {
        val currentMedia = _uiState.value.selectedMediaUris.toMutableList()
        if (index in currentMedia.indices) {
            currentMedia.removeAt(index)
            _uiState.update { 
                it.copy(
                    selectedMediaUris = currentMedia,
                    mediaType = when {
                        currentMedia.any { it.isVideo } && currentMedia.any { !it.isVideo } -> MediaType.MIXED
                        currentMedia.any { it.isVideo } -> MediaType.VIDEO
                        currentMedia.any { !it.isVideo } -> MediaType.IMAGE
                        else -> MediaType.TEXT
                    }
                ) 
            }
        }
    }

    fun createPost(context: Context) {
        android.util.Log.d("CreatePostViewModel", "createPost called")
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
                        // Önce post'u oluştur (ID almak için)
                        val tempPost = Post(
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
                            mediaUrls = emptyList(), // Önce boş, sonra güncellenecek
                            mediaType = uiState.value.mediaType,
                            likes = emptyList(),
                            comments = emptyList(),
                            shares = 0,
                            createdAt = System.currentTimeMillis(),
                            tags = emptyList(),
                            category = ""
                        )

                        val createResult = postRepository.createPost(tempPost)
                        createResult.fold(
                            onSuccess = { createdPost ->
                                // Şimdi medya dosyalarını upload et
                                val mediaUrls = if (uiState.value.selectedMediaUris.isNotEmpty()) {
                                    android.util.Log.d("CreatePostViewModel", "Starting media upload for ${uiState.value.selectedMediaUris.size} files")
                                    val uploadResult = postRepository.uploadMultipleMedia(
                                        uiState.value.selectedMediaUris.map { it.uri },
                                        createdPost.id,
                                        context
                                    )
                                    uploadResult.fold(
                                        onSuccess = { urls ->
                                            android.util.Log.d("CreatePostViewModel", "Media upload successful: $urls")
                                            urls
                                        },
                                        onFailure = { error ->
                                            android.util.Log.e("CreatePostViewModel", "Media upload failed: ${error.message}", error)
                                            emptyList()
                                        }
                                    )
                                } else {
                                    emptyList()
                                }

                                // Post'u medya URL'leri ile güncelle
                                val updatedPost = createdPost.copy(mediaUrls = mediaUrls)
                                val updateResult = postRepository.updatePost(updatedPost)
                                updateResult.fold(
                                    onSuccess = { finalPost ->
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
                                                error = "Medya yüklenirken hata oluştu: ${exception.message}"
                                            ) 
                                        }
                                    }
                                )
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
    val selectedMediaUris: List<MediaItem> = emptyList(),
    val mediaType: MediaType = MediaType.TEXT,
    val isPostCreated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean = false
)
