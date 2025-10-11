package com.das3kn.iz.data.model

import com.google.firebase.firestore.Exclude

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImage: String = "",
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: MediaType = MediaType.TEXT,
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val commentCount: Int = 0,
    val shares: Int = 0,
    val saves: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val category: String = "",
    val repostOfPostId: String? = null,
    val repostedByUserId: String? = null,
    val repostedByUsername: String? = null,
    val repostedByDisplayName: String? = null,
    val repostedByProfileImage: String? = null,
    val repostedAt: Long? = null,
    val groupId: String? = null,
    @get:Exclude val originalPost: Post? = null
)

enum class MediaType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    MIXED
}
