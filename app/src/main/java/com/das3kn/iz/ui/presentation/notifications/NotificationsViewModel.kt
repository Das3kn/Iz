package com.das3kn.iz.ui.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Group
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.GroupRepository
import com.das3kn.iz.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val friendRequests: List<User> = emptyList(),
    val groupInvites: List<GroupInviteNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class GroupInviteNotification(
    val group: Group,
    val invitedBy: User?
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadNotifications() {
        val currentUserId = authRepository.currentUser?.uid ?: run {
            _uiState.value = NotificationsUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getUserById(currentUserId)
                .onSuccess { currentUser ->
                    val requesterIds = currentUser.incomingFriendRequests
                    val friendRequestUsers = if (requesterIds.isEmpty()) {
                        emptyList()
                    } else {
                        userRepository.getUsersByIds(requesterIds).getOrElse { emptyList() }
                    }

                    val groupInvites = groupRepository.getGroupInvitesForUser(currentUserId)
                        .getOrElse { emptyList() }

                    val inviterIds = groupInvites.mapNotNull { group ->
                        group.inviteMetadata[currentUserId]
                    }.distinct()

                    val inviters = if (inviterIds.isNotEmpty()) {
                        userRepository.getUsersByIds(inviterIds).getOrElse { emptyList() }
                    } else {
                        emptyList()
                    }

                    val inviterMap = inviters.associateBy { it.id }

                    val inviteNotifications = groupInvites.map { group ->
                        GroupInviteNotification(
                            group = group,
                            invitedBy = group.inviteMetadata[currentUserId]?.let { inviterMap[it] }
                        )
                    }

                    _uiState.value = NotificationsUiState(
                        friendRequests = friendRequestUsers,
                        groupInvites = inviteNotifications
                    )
                }
                .onFailure { exception ->
                    _uiState.value = NotificationsUiState(
                        friendRequests = emptyList(),
                        groupInvites = emptyList(),
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
                    loadNotifications()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "İstek kabul edilemedi"
                    )
                }
        }
    }

    fun acceptGroupInvite(groupId: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            groupRepository.acceptGroupInvite(groupId, currentUserId)
                .onSuccess {
                    loadNotifications()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Davet kabul edilemedi"
                    )
                }
        }
    }

    fun declineGroupInvite(groupId: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            groupRepository.declineGroupInvite(groupId, currentUserId)
                .onSuccess {
                    loadNotifications()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Davet reddedilemedi"
                    )
                }
        }
    }
}

