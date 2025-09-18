package com.das3kn.iz.data.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val parentId: String? = null, // null ise ana yorum, değilse yanıt
    val likes: List<String> = emptyList(), // Beğenen kullanıcı ID'leri
    val replies: List<Comment> = emptyList() // Runtime'da hesaplanacak, Firestore'da saklanmayacak
)
