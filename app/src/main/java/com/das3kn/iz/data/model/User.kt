package com.das3kn.iz.data.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val incomingFriendRequests: List<String> = emptyList(),
    val outgoingFriendRequests: List<String> = emptyList(),
    val groups: List<String> = emptyList(),
    val groupInvitations: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = ""
)
