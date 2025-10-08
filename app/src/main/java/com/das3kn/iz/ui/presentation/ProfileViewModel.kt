package com.das3kn.iz.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class UserPostsState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _postsState = MutableStateFlow(UserPostsState())
    val postsState: StateFlow<UserPostsState> = _postsState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    private val _friendshipState = MutableStateFlow(FriendshipState())
    val friendshipState: StateFlow<FriendshipState> = _friendshipState.asStateFlow()

    init {
        _currentUserId.value = authRepository.currentUser?.uid
        loadCurrentUserProfile()
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _userState.value = _userState.value.copy(isLoading = true, error = null)

            authRepository.getUserProfile(userId)
                .onSuccess { user ->
                    _userState.value = _userState.value.copy(
                        user = user,
                        isLoading = false
                    )
                    updateFriendshipState(user)
                }
                .onFailure { exception ->
                    _userState.value = _userState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Kullanıcı yüklenemedi"
                    )
                }
        }
    }

    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _postsState.value = _postsState.value.copy(isLoading = true, error = null)

            postRepository.getPostsByUser(userId)
                .onSuccess { posts ->
                    _postsState.value = _postsState.value.copy(
                        posts = posts,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _postsState.value = _postsState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Postlar yüklenemedi"
                    )
                }
        }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUserProfile()
                .onSuccess { user ->
                    _currentUserProfile.value = user
                    _userState.value.user?.let { updateFriendshipState(it) }
                }
        }
    }

    private fun updateFriendshipState(targetUser: User?) {
        val currentUser = _currentUserProfile.value
        if (targetUser == null || currentUser == null) {
            _friendshipState.value = FriendshipState()
            return
        }

        val isFriend = currentUser.friends.contains(targetUser.id)
        val isRequestPending = currentUser.outgoingFriendRequests.contains(targetUser.id)
        val hasIncomingRequest = currentUser.incomingFriendRequests.contains(targetUser.id)

        _friendshipState.value = FriendshipState(
            isFriend = isFriend,
            isRequestPending = isRequestPending,
            hasIncomingRequest = hasIncomingRequest
        )
    }

    fun sendFriendRequest(targetUserId: String) {
        val currentId = _currentUserId.value ?: return
        viewModelScope.launch {
            userRepository.sendFriendRequest(currentId, targetUserId)
                .onSuccess {
                    loadCurrentUserProfile()
                }
        }
    }

    fun acceptFriendRequest(requesterId: String) {
        val currentId = _currentUserId.value ?: return
        viewModelScope.launch {
            userRepository.acceptFriendRequest(currentId, requesterId)
                .onSuccess {
                    loadCurrentUserProfile()
                    _userState.value.user?.let { updateFriendshipState(it) }
                }
        }
    }
}

data class FriendshipState(
    val isFriend: Boolean = false,
    val isRequestPending: Boolean = false,
    val hasIncomingRequest: Boolean = false
)
