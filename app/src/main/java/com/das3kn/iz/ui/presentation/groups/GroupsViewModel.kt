package com.das3kn.iz.ui.presentation.groups

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingGroup: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState(isLoading = true))
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)

    init {
        refresh()
    }

    fun refresh() {
        loadCurrentUser()
        loadGroups()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            userRepository.getUserById(currentUserId)
                .onSuccess { user ->
                    _currentUser.value = user
                    loadFriends(user)
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Profil yüklenemedi")
                    }
                }
        }
    }

    private fun loadFriends(user: User) {
        viewModelScope.launch {
            val friendIds = user.friends
            if (friendIds.isEmpty()) {
                _uiState.update { state -> state.copy(friends = emptyList()) }
                return@launch
            }

            userRepository.getUsersByIds(friendIds)
                .onSuccess { friends ->
                    _uiState.update { state ->
                        state.copy(friends = friends.sortedBy { it.displayName.ifBlank { it.username } })
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Arkadaşlar yüklenemedi")
                    }
                }
        }
    }

    fun loadGroups() {
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid
            if (currentUserId == null) {
                _uiState.value = GroupsUiState(groups = emptyList(), friends = emptyList())
                return@launch
            }

            _uiState.update { state -> state.copy(isLoading = true, error = null) }

            groupRepository.getGroupsForUser(currentUserId)
                .onSuccess { groups ->
                    _uiState.update { state ->
                        state.copy(groups = groups.sortedByDescending { it.createdAt }, isLoading = false)
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = exception.message ?: "Gruplar yüklenemedi"
                        )
                    }
                }
        }
    }

    fun createGroup(name: String, description: String, invitedFriendIds: List<String>) {
        val ownerId = authRepository.currentUser?.uid ?: return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(error = "Grup adı boş olamaz") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingGroup = true, error = null) }

            groupRepository.createGroup(
                ownerId = ownerId,
                name = trimmedName,
                description = description.trim(),
                invitedUserIds = invitedFriendIds
            )
                .onSuccess {
                    loadGroups()
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Grup oluşturulamadı")
                    }
                }

            _uiState.update { it.copy(isCreatingGroup = false) }
        }
    }

    fun sendInvite(groupId: String, friendId: String) {
        val inviterId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.sendGroupInvite(groupId, inviterId, friendId)
                .onSuccess {
                    loadGroups()
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Davet gönderilemedi")
                    }
                }
        }
    }

    fun getCurrentUser(): User? = _currentUser.value

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
