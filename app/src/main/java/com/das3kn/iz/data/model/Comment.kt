package com.das3kn.iz.data.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val content: String = "",
    val likes: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val replies: List<Comment> = emptyList()
)
