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
    val errorMessage: String? = null,
    val observedGroupId: String? = null
)
