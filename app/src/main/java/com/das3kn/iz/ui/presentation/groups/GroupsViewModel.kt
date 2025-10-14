package com.das3kn.iz.ui.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Group
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = authRepository.currentUser?.uid

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, creationInProgress = false) }
            val result = groupRepository.getGroups()
            if (result.isSuccess) {
                val groups = result.getOrNull().orEmpty()
                val visibleGroups = groups
                    .map { it.toUiModel(currentUserId) }
                    .filter { group ->
                        !group.isPrivate || (currentUserId != null && isGroupVisibleToUser(group, currentUserId))
                    }
                _uiState.update {
                    it.copy(
                        groups = visibleGroups,
                        isLoading = false,
                        creationInProgress = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        creationInProgress = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleJoin(groupId: String) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { group ->
                if (group.id == groupId) {
                    val joined = !group.isJoined
                    group.copy(
                        isJoined = joined,
                        membersCount = (group.membersCount + if (joined) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    group
                }
            }
            state.copy(groups = updatedGroups)
        }
    }

    fun onGroupUpdated(updatedGroup: GroupUiModel) {
        _uiState.update { state ->
            val updatedGroups = state.groups.map { group ->
                if (group.id == updatedGroup.id) updatedGroup else group
            }
            state.copy(groups = updatedGroups)
        }
    }

    fun createGroup(name: String, description: String, imageUrl: String) {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(creationInProgress = true, errorMessage = null) }
            val username = currentUser.email?.substringBefore('@') ?: currentUser.displayName.orEmpty()
            val group = Group(
                name = name,
                description = description,
                imageUrl = imageUrl,
                adminId = currentUser.uid,
                adminName = currentUser.displayName ?: username,
                adminUsername = username,
                adminAvatarUrl = currentUser.photoUrl?.toString().orEmpty(),
                membersCount = 1,
                postsCount = 0,
                memberIds = listOf(currentUser.uid),
                invitedUserIds = emptyList(),
                pendingMemberIds = emptyList(),
                isPrivate = false
            )

            val result = groupRepository.createGroup(group)
            if (result.isSuccess) {
                loadGroups()
            } else {
                _uiState.update {
                    it.copy(
                        creationInProgress = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                    )
                }
            }
        }
    }

    private fun isGroupVisibleToUser(group: GroupUiModel, userId: String): Boolean {
        return !group.isPrivate ||
            group.admin.id == userId ||
            group.memberIds.contains(userId) ||
            group.invitedUserIds.contains(userId) ||
            group.pendingMemberIds.contains(userId)
    }
}

data class GroupsUiState(
    val groups: List<GroupUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val creationInProgress: Boolean = false
)
