package com.das3kn.iz.data.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val coverImageUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val pendingInvites: List<String> = emptyList(),
    val inviteMetadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

fun Group.hasMember(userId: String): Boolean = memberIds.contains(userId)

fun Group.isPending(userId: String): Boolean = pendingInvites.contains(userId)
