package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.ProviderStatus
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Chat(val conversationId: Long) : Screen()
    data object Settings : Screen()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val preferences = AppPreferences(application)
    private val repository = ChatRepository(database, preferences)

    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode
    val language: StateFlow<AppLanguage> = preferences.language
    val selectedModel: StateFlow<AiModel> = preferences.selectedModel
    val providerStatus: StateFlow<ProviderStatus> = preferences.providerStatus

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    val currentMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessages(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversation = MutableStateFlow<ConversationEntity?>(null)
    val activeConversation: StateFlow<ConversationEntity?> = _activeConversation.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _inputMessage = MutableStateFlow("")
    val inputMessage: StateFlow<String> = _inputMessage.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun onInputMessageChanged(text: String) {
        _inputMessage.value = text
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.Chat) {
            _activeConversationId.value = screen.conversationId
            loadActiveConversation(screen.conversationId)
        } else if (screen is Screen.Home) {
            _activeConversationId.value = null
            _activeConversation.value = null
        }
    }

    fun navigateBack(): Boolean {
        return if (_currentScreen.value !is Screen.Home) {
            navigateTo(Screen.Home)
            true
        } else {
            false
        }
    }

    private fun loadActiveConversation(id: Long) {
        viewModelScope.launch {
            _activeConversation.value = repository.getConversation(id)
        }
    }

    fun startNewChat(initialPrompt: String = "") {
        viewModelScope.launch {
            val model = selectedModel.value
            val convId = repository.createConversation(
                initialPrompt = initialPrompt.ifBlank { "محادثة جديدة" },
                model = model
            )
            _activeConversationId.value = convId
            loadActiveConversation(convId)
            _currentScreen.value = Screen.Chat(convId)

            if (initialPrompt.isNotBlank()) {
                sendMessageInternal(convId, initialPrompt)
            }
        }
    }

    fun sendMessage() {
        val prompt = _inputMessage.value.trim()
        val convId = _activeConversationId.value
        if (prompt.isEmpty() || convId == null) return

        _inputMessage.value = ""
        sendMessageInternal(convId, prompt)
    }

    fun sendQuickPrompt(promptText: String) {
        startNewChat(initialPrompt = promptText)
    }

    private fun sendMessageInternal(conversationId: Long, prompt: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            val currentModel = selectedModel.value
            val result = repository.sendUserMessage(
                conversationId = conversationId,
                userPrompt = prompt,
                model = currentModel
            )
            loadActiveConversation(conversationId)
            _isGenerating.value = false

            if (result.isFailure) {
                val errorMsg = result.exceptionOrNull()?.message ?: "حدث خطأ"
                _snackbarEvent.emit(errorMsg)
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            val msg = when (language.value) {
                AppLanguage.AR -> "تم حذف الرسالة"
                AppLanguage.FR -> "Message supprimé"
                AppLanguage.EN -> "Message deleted"
            }
            _snackbarEvent.emit(msg)
        }
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (_activeConversationId.value == conversationId) {
                navigateTo(Screen.Home)
            }
            val msg = when (language.value) {
                AppLanguage.AR -> "تم حذف المحادثة"
                AppLanguage.FR -> "Conversation supprimée"
                AppLanguage.EN -> "Conversation deleted"
            }
            _snackbarEvent.emit(msg)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.clearAllConversations()
            navigateTo(Screen.Home)
            val msg = when (language.value) {
                AppLanguage.AR -> "تم مسح جميع المحادثات"
                AppLanguage.FR -> "Toutes les conversations ont été effacées"
                AppLanguage.EN -> "All conversations cleared"
            }
            _snackbarEvent.emit(msg)
        }
    }

    fun selectModel(model: AiModel) {
        preferences.setSelectedModel(model)
        val msg = when (language.value) {
            AppLanguage.AR -> "تم اختيار نموذج ${model.displayName}"
            AppLanguage.FR -> "Modèle ${model.displayName} sélectionné"
            AppLanguage.EN -> "Model ${model.displayName} selected"
        }
        viewModelScope.launch {
            _snackbarEvent.emit(msg)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun setLanguage(lang: AppLanguage) {
        preferences.setLanguage(lang)
    }

    fun saveCustomApiKey(key: String) {
        preferences.setCustomApiKey(key)
        viewModelScope.launch {
            val status = preferences.computeProviderStatus()
            val msg = if (status.isLinked) {
                when (language.value) {
                    AppLanguage.AR -> "تم حفظ مفتاح API وربط مزود Gemini بنجاح!"
                    AppLanguage.FR -> "Clé API enregistrée et Gemini associé avec succès !"
                    AppLanguage.EN -> "API key saved and Gemini linked successfully!"
                }
            } else {
                when (language.value) {
                    AppLanguage.AR -> "تمت إزالة المفتاح، المزود غير مرتبط حالياً."
                    AppLanguage.FR -> "Clé retirée, fournisseur non associé."
                    AppLanguage.EN -> "Key removed, provider is currently unlinked."
                }
            }
            _snackbarEvent.emit(msg)
        }
    }
}
