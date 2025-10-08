package com.das3kn.iz.ui.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val friendRequests: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadFriendRequests() {
        val currentUserId = authRepository.currentUser?.uid ?: run {
            _uiState.value = NotificationsUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getUserById(currentUserId)
                .onSuccess { currentUser ->
                    val requesterIds = currentUser.incomingFriendRequests
                    if (requesterIds.isEmpty()) {
                        _uiState.value = NotificationsUiState(friendRequests = emptyList())
                    } else {
                        userRepository.getUsersByIds(requesterIds)
                            .onSuccess { users ->
                                _uiState.value = NotificationsUiState(friendRequests = users)
                            }
                            .onFailure { exception ->
                                _uiState.value = NotificationsUiState(
                                    friendRequests = emptyList(),
                                    error = exception.message ?: "Bildirimler yüklenemedi"
                                )
                            }
                    }
                }
                .onFailure { exception ->
                    _uiState.value = NotificationsUiState(
                        friendRequests = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Kullanıcı bilgisi alınamadı"
                    )
                }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun acceptFriendRequest(requesterId: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            userRepository.acceptFriendRequest(currentUserId, requesterId)
                .onSuccess {
                    loadFriendRequests()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "İstek kabul edilemedi"
                    )
                }
        }
    }
}

