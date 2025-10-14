package com.das3kn.iz.ui.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class GroupSettingsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState: StateFlow<GroupSettingsUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = authRepository.currentUser?.uid

    fun load(groupId: String) {
        if (_uiState.value.group?.id == groupId && !_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = groupRepository.getGroupById(groupId)
            if (result.isSuccess) {
                val group = result.getOrNull()
                val uiModel = group?.toUiModel(currentUserId)
                val mockMembers = GroupMockData.groupDetail(groupId)?.members.orEmpty()
                _uiState.update {
                    it.copy(
                        group = uiModel,
                        members = mockMembers,
                        isAdmin = uiModel?.admin?.id == currentUserId,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage,
                    )
                }
            }
        }
    }

    fun saveSettings(groupId: String, data: GroupSettingsData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val updates = mapOf(
                "name" to data.name,
                "description" to data.description,
                "imageUrl" to data.coverImageUrl,
                "profileImageUrl" to data.profileImageUrl,
                "isPrivate" to data.isPrivate,
            )
            val result = groupRepository.updateGroup(groupId, updates)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updatedGroup = state.group?.copy(
                        name = data.name,
                        description = data.description,
                        imageUrl = data.coverImageUrl,
                        profileImageUrl = data.profileImageUrl,
                        isPrivate = data.isPrivate,
                    )
                    state.copy(
                        isSaving = false,
                        group = updatedGroup,
                        navigateBackAfterSave = true,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage,
                    )
                }
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            val result = groupRepository.deleteGroup(groupId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isDeleting = false, navigateUpToGroups = true)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage,
                    )
                }
            }
        }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateBackAfterSave = false, navigateUpToGroups = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class GroupSettingsUiState(
    val group: GroupUiModel? = null,
    val members: List<GroupUserUiModel> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val navigateBackAfterSave: Boolean = false,
    val navigateUpToGroups: Boolean = false,
)

data class GroupSettingsData(
    val name: String,
    val description: String,
    val coverImageUrl: String,
    val profileImageUrl: String,
    val isPrivate: Boolean,
)
