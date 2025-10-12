package com.das3kn.iz.ui.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: com.das3kn.iz.data.repository.AuthRepository
) : ViewModel() {

    private val useSampleData = true

    private val _chatState = MutableStateFlow<Chat?>(null)
    val chatState: StateFlow<Chat?> = _chatState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatUsers = MutableStateFlow<List<User>>(emptyList())
    val chatUsers: StateFlow<List<User>> = _chatUsers.asStateFlow()

    private val _currentUser = MutableStateFlow(ChatSampleData.currentUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _activeChatUser = MutableStateFlow<User?>(null)
    val activeChatUser: StateFlow<User?> = _activeChatUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (!useSampleData) {
            authRepository.currentUser?.let { firebaseUser ->
                _currentUser.value = _currentUser.value.copy(
                    id = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: firebaseUser.email ?: "Kullanıcı",
                    email = firebaseUser.email.orEmpty()
                )
            }
        }
    }

    fun loadChat(chatId: String) {
        if (useSampleData) {
            loadSampleChat(chatId)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = chatRepository.getChatById(chatId)
                result.onSuccess { chat ->
                    _chatState.value = chat
                    loadChatUsers(chat.participants)
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Sohbet yüklenemedi"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSampleChat(chatId: String) {
        _isLoading.value = true
        val sampleChat = ChatSampleData.getSampleChat(chatId)
        if (sampleChat != null) {
            _chatState.value = sampleChat.chat
            _messages.value = sampleChat.messages
            _chatUsers.value = sampleChat.participants
            _activeChatUser.value = sampleChat.participants.firstOrNull { it.id != _currentUser.value.id }
        } else {
            _chatState.value = null
            _messages.value = emptyList()
            _chatUsers.value = emptyList()
            _activeChatUser.value = null
            _error.value = "Sohbet bulunamadı"
        }
        _isLoading.value = false
    }

    private suspend fun loadChatUsers(participantIds: List<String>) {
        if (useSampleData) return

        try {
            val usersResult = chatRepository.getUsersByIds(participantIds)
            usersResult.onSuccess { users ->
                _chatUsers.value = users
                _activeChatUser.value = users.firstOrNull { it.id != getCurrentUserId() }
            }
        } catch (e: Exception) {
            println("Kullanıcı bilgileri yüklenemedi: ${e.message}")
        }
    }

    fun loadMessages(chatId: String) {
        if (useSampleData) {
            ChatSampleData.getSampleChat(chatId)?.let { sample ->
                _messages.value = sample.messages
            }
            return
        }

        viewModelScope.launch {
            try {
                val result = chatRepository.getMessages(chatId)
                result.onSuccess { messageList ->
                    _messages.value = messageList

                    val currentUser = authRepository.currentUser
                    if (currentUser != null) {
                        chatRepository.markMessagesAsRead(chatId, currentUser.uid)
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Mesajlar yüklenemedi"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        if (content.isBlank()) return

        if (useSampleData) {
            val newMessage = ChatSampleData.createOutgoingMessage(chatId, content)
            _messages.update { it + newMessage }
            _chatState.update { chat ->
                chat?.copy(
                    lastMessage = newMessage,
                    lastMessageTime = newMessage.timestamp,
                    unreadCount = chat.unreadCount.toMutableMap().apply {
                        this[_currentUser.value.id] = 0
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    val message = Message(
                        chatId = chatId,
                        content = content,
                        senderId = currentUser.uid,
                        senderName = currentUser.displayName ?: currentUser.email ?: "Kullanıcı",
                    )

                    val result = chatRepository.sendMessage(chatId, message)
                    result.onSuccess {
                        loadMessages(chatId)
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Mesaj gönderilemedi"
                    }
                } else {
                    _error.value = "Kullanıcı giriş yapmamış"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getCurrentUserId(): String? {
        return if (useSampleData) {
            _currentUser.value.id
        } else {
            authRepository.currentUser?.uid
        }
    }
}
