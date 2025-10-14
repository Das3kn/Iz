package com.das3kn.iz.ui.presentation.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = authRepository.currentUser?.uid

    private var groupObserverJob: Job? = null

    fun loadGroup(groupId: String) {
        if (_uiState.value.group?.id == groupId && groupObserverJob != null) return
        groupObserverJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null, observedGroupId = groupId) }
        viewModelScope.launch {
            val initialResult = groupRepository.getGroupById(groupId)
            if (initialResult.isSuccess) {
                val group = initialResult.getOrNull()
                _uiState.update { state ->
                    val uiModel = group?.toUiModel(currentUserId)
                    state.copy(
                        group = uiModel,
                        isAdmin = uiModel?.admin?.id == currentUserId,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = initialResult.exceptionOrNull()?.localizedMessage
                    )
                }
            }
        }

        groupObserverJob = viewModelScope.launch {
            groupRepository.observeGroup(groupId).collect { result ->
                if (result.isSuccess) {
                    val group = result.getOrNull()
                    _uiState.update { state ->
                        val uiModel = group?.toUiModel(currentUserId)
                        state.copy(
                            group = uiModel,
                            isAdmin = uiModel?.admin?.id == currentUserId,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage)
                    }
                }
            }
        }
    }

    fun openSettings(force: Boolean = false) {
        if (_uiState.value.isAdmin || force) {
            _uiState.update { it.copy(isSettingsOpen = true) }
        }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun saveSettings(settings: GroupSettingsData) {
        val groupId = _uiState.value.group?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val updates = mapOf(
                "name" to settings.name,
                "description" to settings.description,
                "imageUrl" to settings.imageUrl,
                "isPrivate" to settings.isPrivate
            )
            val result = groupRepository.updateGroup(groupId, updates)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        isSettingsOpen = false,
                        group = state.group?.copy(
                            name = settings.name,
                            description = settings.description,
                            imageUrl = settings.imageUrl,
                            isPrivate = settings.isPrivate
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                    )
                }
            }
        }
    }

    fun deleteGroup() {
        val groupId = _uiState.value.group?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(deleteInProgress = true, errorMessage = null) }
            val result = groupRepository.deleteGroup(groupId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(deleteInProgress = false, navigateBackAfterDelete = true)
                }
            } else {
                _uiState.update {
                    it.copy(
                        deleteInProgress = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                    )
                }
            }
        }
    }

    fun consumeNavigationEvent() {
        _uiState.update { it.copy(navigateBackAfterDelete = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        groupObserverJob?.cancel()
        super.onCleared()
    }
}

data class GroupDetailUiState(
    val group: GroupUiModel? = null,
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val deleteInProgress: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val errorMessage: String? = null,
    val navigateBackAfterDelete: Boolean = false,
    val observedGroupId: String? = null
)

data class GroupSettingsData(
    val name: String,
    val description: String,
    val imageUrl: String,
    val isPrivate: Boolean
)
