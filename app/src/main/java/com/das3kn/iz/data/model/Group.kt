package com.das3kn.iz.data.model

import com.google.firebase.firestore.Exclude

/**
 * Represents a community group stored in Firestore.
 */
data class Group(
    @get:Exclude
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val profileImageUrl: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val adminUsername: String = "",
    val adminAvatarUrl: String = "",
    val membersCount: Int = 0,
    val postsCount: Int = 0,
    val memberIds: List<String> = emptyList(),
    val invitedUserIds: List<String> = emptyList(),
    val pendingMemberIds: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
