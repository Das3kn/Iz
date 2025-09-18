package com.das3kn.iz.data.model

data class SavedPost(
    val id: String = "",
    val userId: String = "",
    val postId: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
