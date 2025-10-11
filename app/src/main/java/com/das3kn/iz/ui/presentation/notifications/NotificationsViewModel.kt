package com.das3kn.iz.ui.presentation.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class NotificationUser(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class NotificationGroup(
    val id: String,
    val name: String
)

data class NotificationPost(
    val id: String,
    val imageUrl: String? = null,
    val title: String? = null
)

enum class NotificationType {
    FRIEND_REQUEST,
    GROUP_INVITE,
    LIKE,
    COMMENT,
    TAG,
    REPOST
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val user: NotificationUser,
    val timestamp: Instant,
    val content: String? = null,
    val group: NotificationGroup? = null,
    val post: NotificationPost? = null,
    val isRead: Boolean = false
)

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val now = Instant.now()
        val sampleNotifications = listOf(
            NotificationItem(
                id = "1",
                type = NotificationType.FRIEND_REQUEST,
                user = NotificationUser(
                    id = "user_1",
                    name = "Ayşe Yılmaz",
                    avatarUrl = "https://randomuser.me/api/portraits/women/44.jpg"
                ),
                timestamp = now.minus(15, ChronoUnit.MINUTES)
            ),
            NotificationItem(
                id = "2",
                type = NotificationType.GROUP_INVITE,
                user = NotificationUser(
                    id = "user_2",
                    name = "Mert Kaya",
                    avatarUrl = "https://randomuser.me/api/portraits/men/32.jpg"
                ),
                timestamp = now.minus(2, ChronoUnit.HOURS),
                group = NotificationGroup(
                    id = "group_1",
                    name = "Flutter Geliştiricileri"
                )
            ),
            NotificationItem(
                id = "3",
                type = NotificationType.COMMENT,
                user = NotificationUser(
                    id = "user_3",
                    name = "Selin Demir",
                    avatarUrl = "https://randomuser.me/api/portraits/women/21.jpg"
                ),
                timestamp = now.minus(6, ChronoUnit.HOURS),
                content = "Yeni tasarım gerçekten harika olmuş!",
                post = NotificationPost(
                    id = "post_1",
                    imageUrl = "https://picsum.photos/200/200?random=1"
                )
            ),
            NotificationItem(
                id = "4",
                type = NotificationType.LIKE,
                user = NotificationUser(
                    id = "user_4",
                    name = "Emre Aksoy",
                    avatarUrl = "https://randomuser.me/api/portraits/men/12.jpg"
                ),
                timestamp = now.minus(1, ChronoUnit.DAYS),
                post = NotificationPost(
                    id = "post_2",
                    imageUrl = "https://picsum.photos/200/200?random=2"
                ),
                isRead = true
            ),
            NotificationItem(
                id = "5",
                type = NotificationType.TAG,
                user = NotificationUser(
                    id = "user_5",
                    name = "Gizem Çelik",
                    avatarUrl = "https://randomuser.me/api/portraits/women/8.jpg"
                ),
                timestamp = now.minus(3, ChronoUnit.DAYS)
            ),
            NotificationItem(
                id = "6",
                type = NotificationType.REPOST,
                user = NotificationUser(
                    id = "user_6",
                    name = "Kerem Ekinci",
                    avatarUrl = "https://randomuser.me/api/portraits/men/65.jpg"
                ),
                timestamp = now.minus(5, ChronoUnit.DAYS),
                post = NotificationPost(
                    id = "post_3",
                    imageUrl = "https://picsum.photos/200/200?random=3"
                )
            )
        )

        _uiState.value = NotificationsUiState(notifications = sampleNotifications)
    }

    fun markAsRead(notificationId: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
            )
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }

    fun acceptFriendRequest(notificationId: String) {
        removeNotification(notificationId)
    }

    fun rejectFriendRequest(notificationId: String) {
        removeNotification(notificationId)
    }

    fun acceptGroupInvite(notificationId: String) {
        removeNotification(notificationId)
    }

    fun rejectGroupInvite(notificationId: String) {
        removeNotification(notificationId)
    }

    private fun removeNotification(notificationId: String) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.filterNot { it.id == notificationId }
            )
        }
    }
}

