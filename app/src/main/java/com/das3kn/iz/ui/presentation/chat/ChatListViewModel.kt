package com.das3kn.iz.ui.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: com.das3kn.iz.data.repository.AuthRepository
) : ViewModel() {

    private val useSampleData = true

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _chatUsers = MutableStateFlow<Map<String, List<User>>>(emptyMap())
    val chatUsers: StateFlow<Map<String, List<User>>> = _chatUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadChats() {
        if (useSampleData) {
            loadSampleChats()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    val result = chatRepository.getChats(currentUser.uid)
                    result.onSuccess { chatList ->
                        _chats.value = chatList
                        _error.value = null
                        loadChatUsers(chatList)
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Sohbetler yüklenemedi"
                    }
                } else {
                    _error.value = "Kullanıcı giriş yapmamış"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSampleChats() {
        _isLoading.value = true
        val sampleChats = ChatSampleData.getSampleChats()
        _chats.value = sampleChats.map { it.chat }
        _chatUsers.value = sampleChats.associate { it.chat.id to it.participants }
        _error.value = null
        _isLoading.value = false
    }

    private suspend fun loadChatUsers(chats: List<Chat>) {
        if (useSampleData) return

        try {
            val chatUsersMap = mutableMapOf<String, List<User>>()

            for (chat in chats) {
                val usersResult = chatRepository.getUsersByIds(chat.participants)
                usersResult.onSuccess { users ->
                    chatUsersMap[chat.id] = users
                }
            }

            _chatUsers.value = chatUsersMap
        } catch (e: Exception) {
            println("Kullanıcı bilgileri yüklenemedi: ${e.message}")
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshChats() {
        loadChats()
    }

    fun getCurrentUserId(): String? {
        return if (useSampleData) {
            ChatSampleData.currentUser.id
        } else {
            authRepository.currentUser?.uid
        }
    }
}
