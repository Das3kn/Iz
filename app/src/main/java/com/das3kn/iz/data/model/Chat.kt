package com.das3kn.iz.data.model

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val lastMessageTime: Long = 0,
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val mediaUrl: String = "",
    val mediaType: MessageMediaType = MessageMediaType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val replyTo: String? = null
)

enum class MessageMediaType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE
}
