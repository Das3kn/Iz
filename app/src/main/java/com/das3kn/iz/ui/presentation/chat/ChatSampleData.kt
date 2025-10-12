package com.das3kn.iz.ui.presentation.chat

import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.model.MessageMediaType
import com.das3kn.iz.data.model.User

object ChatSampleData {
    data class SampleChat(
        val chat: Chat,
        val participants: List<User>,
        val messages: List<Message>
    )

    val currentUser = User(
        id = "current_user",
        username = "senakorkmaz",
        email = "sena@example.com",
        displayName = "Sena Korkmaz",
        profileImageUrl = "https://i.pravatar.cc/150?img=1"
    )

    private val contacts = listOf(
        User(
            id = "user_aylin",
            username = "aylindemir",
            email = "aylin@example.com",
            displayName = "Aylin Demir",
            profileImageUrl = "https://i.pravatar.cc/150?img=47"
        ),
        User(
            id = "user_emir",
            username = "emiraksoy",
            email = "emir@example.com",
            displayName = "Emir Aksoy",
            profileImageUrl = "https://i.pravatar.cc/150?img=32"
        ),
        User(
            id = "user_elif",
            username = "elifyildiz",
            email = "elif@example.com",
            displayName = "Elif Yıldız",
            profileImageUrl = "https://i.pravatar.cc/150?img=15"
        )
    )

    private val now = System.currentTimeMillis()

    private val sampleMessages: Map<String, List<Message>> = mapOf(
        "chat_user_aylin" to listOf(
            Message(
                id = "1",
                chatId = "chat_user_aylin",
                senderId = "user_aylin",
                senderName = "Aylin Demir",
                content = "Merhaba! Nasılsın?",
                timestamp = now - 2 * 60 * 60 * 1000,
                isRead = true
            ),
            Message(
                id = "2",
                chatId = "chat_user_aylin",
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                content = "İyiyim teşekkürler, sen nasılsın?",
                timestamp = now - 60 * 60 * 1000,
                isRead = true
            ),
            Message(
                id = "3",
                chatId = "chat_user_aylin",
                senderId = "user_aylin",
                senderName = "Aylin Demir",
                content = "Ben de iyiyim! Projeni gördüm çok beğendim",
                timestamp = now - 30 * 60 * 1000,
                isRead = true
            ),
            Message(
                id = "4",
                chatId = "chat_user_aylin",
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                content = "Teşekkürler! Yarın görüşürüz 👋",
                timestamp = now - 15 * 60 * 1000,
                isRead = true
            )
        ),
        "chat_user_emir" to listOf(
            Message(
                id = "1",
                chatId = "chat_user_emir",
                senderId = "user_emir",
                senderName = "Emir Aksoy",
                content = "Toplantıyı 14.00'e aldık, uygun musun?",
                timestamp = now - 50 * 60 * 1000,
                isRead = false
            ),
            Message(
                id = "2",
                chatId = "chat_user_emir",
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                content = "Harika, ben de uygun olurum",
                timestamp = now - 35 * 60 * 1000,
                isRead = true
            )
        ),
        "chat_user_elif" to listOf(
            Message(
                id = "1",
                chatId = "chat_user_elif",
                senderId = "user_elif",
                senderName = "Elif Yıldız",
                content = "Yeni tasarımları gönderdim, göz atabilir misin?",
                timestamp = now - 5 * 60 * 60 * 1000,
                isRead = true
            ),
            Message(
                id = "2",
                chatId = "chat_user_elif",
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                content = "Tabii ki, akşam dönüş yaparım",
                timestamp = now - 4 * 60 * 60 * 1000,
                isRead = true
            )
        )
    )

    private val sampleChats: List<SampleChat> = contacts.map { contact ->
        val chatId = "chat_${contact.id}"
        val messages = sampleMessages[chatId] ?: defaultMessages(chatId, contact)
        val lastMessage = messages.lastOrNull()
        SampleChat(
            chat = Chat(
                id = chatId,
                participants = listOf(currentUser.id, contact.id),
                lastMessage = lastMessage,
                lastMessageTime = lastMessage?.timestamp ?: now,
                unreadCount = mapOf(
                    currentUser.id to messages.count { !it.isRead && it.senderId == contact.id }
                )
            ),
            participants = listOf(currentUser, contact),
            messages = messages
        )
    }

    fun getSampleChats(): List<SampleChat> = sampleChats

    fun getSampleChat(chatId: String): SampleChat? = sampleChats.find { it.chat.id == chatId }

    fun createOutgoingMessage(chatId: String, content: String): Message {
        return Message(
            id = System.currentTimeMillis().toString(),
            chatId = chatId,
            senderId = currentUser.id,
            senderName = currentUser.displayName,
            content = content,
            mediaType = MessageMediaType.TEXT,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
    }

    private fun defaultMessages(chatId: String, contact: User): List<Message> {
        val timestamp = now - 90 * 60 * 1000
        return listOf(
            Message(
                id = "default_1",
                chatId = chatId,
                senderId = contact.id,
                senderName = contact.displayName,
                content = "Merhaba!",
                timestamp = timestamp,
                isRead = true
            ),
            Message(
                id = "default_2",
                chatId = chatId,
                senderId = currentUser.id,
                senderName = currentUser.displayName,
                content = "Merhaba!",
                timestamp = timestamp + 10 * 60 * 1000,
                isRead = true
            )
        )
    }
}
