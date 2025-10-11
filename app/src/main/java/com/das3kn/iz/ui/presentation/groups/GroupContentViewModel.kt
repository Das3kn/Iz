package com.das3kn.iz.ui.presentation.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Group
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.model.MediaType
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.GroupRepository
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupContentUiState(
    val group: Group? = null,
    val posts: List<Post> = emptyList(),
    val members: List<User> = emptyList(),
    val availableFriends: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val isInviting: Boolean = false,
    val error: String? = null,
    val isMember: Boolean = false,
    val isOwner: Boolean = false,
    val currentUserId: String? = null
)

@HiltViewModel
class GroupContentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupContentUiState(isLoading = true))
    val uiState: StateFlow<GroupContentUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    private val groupId: String = savedStateHandle.get<String>(GROUP_ID_KEY).orEmpty()

    init {
        loadCurrentUser()
        refresh()
    }

    fun refresh() {
        if (groupId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Geçersiz grup") }
            return
        }
        loadGroup()
        loadPosts()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserProfile()
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.update { state -> state.copy(currentUserId = user.id) }
                    updateMembership(_uiState.value.group, user)
                }
        }
    }

    private fun loadGroup() {
        if (groupId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            groupRepository.getGroupById(groupId)
                .onSuccess { group ->
                    _uiState.update { state ->
                        state.copy(
                            group = group,
                            isLoading = false
                        )
                    }
                    updateMembership(group, _currentUser.value)
                    loadMembers(group)
                    updateAvailableFriends(group)
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = exception.message ?: "Grup bilgileri alınamadı"
                        )
                    }
                }
        }
    }

    private fun loadMembers(group: Group) {
        viewModelScope.launch {
            if (group.memberIds.isEmpty()) {
                _uiState.update { it.copy(members = emptyList()) }
                return@launch
            }

            userRepository.getUsersByIds(group.memberIds)
                .onSuccess { users ->
                    _uiState.update { state ->
                        state.copy(
                            members = users.sortedBy { it.displayName.ifBlank { it.username } }
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Üyeler yüklenemedi")
                    }
                }
        }
    }

    private fun updateAvailableFriends(group: Group) {
        val currentUser = _currentUser.value ?: return
        val candidateIds = currentUser.friends.filterNot { friendId ->
            group.memberIds.contains(friendId) || group.pendingInvites.contains(friendId)
        }

        if (candidateIds.isEmpty()) {
            _uiState.update { it.copy(availableFriends = emptyList()) }
            return
        }

        viewModelScope.launch {
            userRepository.getUsersByIds(candidateIds)
                .onSuccess { friends ->
                    _uiState.update { state ->
                        state.copy(
                            availableFriends = friends.sortedBy { it.displayName.ifBlank { it.username } }
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Arkadaşlar yüklenemedi")
                    }
                }
        }
    }

    private fun loadPosts() {
        if (groupId.isBlank()) return
        viewModelScope.launch {
            postRepository.getPostsByGroup(groupId)
                .onSuccess { posts ->
                    _uiState.update { state -> state.copy(posts = posts) }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Gönderiler yüklenemedi")
                    }
                }
        }
    }

    fun createTextPost(content: String) {
        val currentUser = _currentUser.value ?: return
        val group = _uiState.value.group ?: return
        val text = content.trim()
        if (text.isEmpty()) {
            _uiState.update { it.copy(error = "Paylaşım metni boş olamaz") }
            return
        }

        val post = Post(
            userId = currentUser.id,
            username = currentUser.displayName.ifBlank { currentUser.username.ifBlank { "Kullanıcı" } },
            userProfileImage = currentUser.profileImageUrl,
            content = text,
            mediaUrls = emptyList(),
            mediaType = MediaType.TEXT,
            likes = emptyList(),
            comments = emptyList(),
            commentCount = 0,
            shares = 0,
            saves = emptyList(),
            createdAt = System.currentTimeMillis(),
            tags = emptyList(),
            category = "",
            groupId = group.id
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isPosting = true, error = null) }
            postRepository.createPost(post)
                .onSuccess {
                    loadPosts()
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Paylaşım oluşturulamadı")
                    }
                }
            _uiState.update { it.copy(isPosting = false) }
        }
    }

    fun sharePostToGroup(post: Post) {
        val currentUser = _currentUser.value ?: return
        if (post.id.isBlank()) {
            _uiState.update { it.copy(error = "Gönderi bulunamadı") }
            return
        }

        viewModelScope.launch {
            postRepository.createGroupRepost(post, currentUser, groupId)
                .onSuccess {
                    loadPosts()
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(error = exception.message ?: "Yeniden paylaşım başarısız")
                    }
                }
        }
    }

    fun sendInvites(friendIds: List<String>) {
        val inviterId = authRepository.currentUser?.uid ?: return
        val group = _uiState.value.group ?: return
        if (friendIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isInviting = true, error = null) }
            var errorMessage: String? = null
            for (friendId in friendIds.distinct()) {
                val result = groupRepository.sendGroupInvite(group.id, inviterId, friendId)
                if (result.isFailure) {
                    errorMessage = result.exceptionOrNull()?.message ?: "Davet gönderilemedi"
                    break
                }
            }
            _uiState.update { it.copy(isInviting = false) }
            errorMessage?.let { message ->
                _uiState.update { it.copy(error = message) }
            }
            loadGroup()
        }
    }

    private fun updateMembership(group: Group?, user: User?) {
        val currentUserId = user?.id ?: authRepository.currentUser?.uid
        if (group == null || currentUserId.isNullOrBlank()) {
            _uiState.update { it.copy(isMember = false, isOwner = false) }
            return
        }

        _uiState.update { state ->
            state.copy(
                isMember = group.memberIds.contains(currentUserId),
                isOwner = group.ownerId == currentUserId
            )
        }
    }

    companion object {
        const val GROUP_ID_KEY = "groupId"
    }
}
