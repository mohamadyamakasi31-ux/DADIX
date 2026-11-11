package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AiModel
import com.example.data.remote.GeminiApiService
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val database: AppDatabase,
    private val preferences: AppPreferences,
    private val apiService: GeminiApiService = GeminiApiService()
) {
    val conversations: Flow<List<ConversationEntity>> =
        database.conversationDao().getAllConversations()

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForConversation(conversationId)

    suspend fun getConversation(id: Long): ConversationEntity? =
        database.conversationDao().getConversationById(id)

    suspend fun createConversation(initialPrompt: String, model: AiModel): Long {
        val title = if (initialPrompt.length > 36) {
            "${initialPrompt.take(36)}…"
        } else {
            initialPrompt.ifBlank { "محادثة جديدة" }
        }
        val entity = ConversationEntity(
            title = title,
            modelId = model.id,
            snippet = initialPrompt.take(60)
        )
        return database.conversationDao().insert(entity)
    }

    suspend fun sendUserMessage(
        conversationId: Long,
        userPrompt: String,
        model: AiModel
    ): Result<MessageEntity> {
        val trimmedPrompt = userPrompt.trim()
        if (trimmedPrompt.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be blank"))
        }

        // 1. Insert User Message
        val userMessage = MessageEntity(
            conversationId = conversationId,
            content = trimmedPrompt,
            sender = "USER",
            timestamp = System.currentTimeMillis(),
            modelUsed = null,
            isError = false
        )
        database.messageDao().insert(userMessage)

        // Update conversation last snippet
        database.conversationDao().updateLastActivity(
            id = conversationId,
            updatedAt = System.currentTimeMillis(),
            snippet = trimmedPrompt.take(60)
        )

        // 2. Fetch history for context
        val recentHistory = database.messageDao().getRecentMessages(conversationId, limit = 10).reversed()

        // 3. Obtain effective API key
        val apiKey = preferences.getEffectiveApiKey()
        val language = preferences.language.value

        // 4. Query AI API (Real calls, absolutely no mock generation)
        val apiResult = apiService.generateContent(
            apiKey = apiKey,
            model = model,
            history = recentHistory,
            userPrompt = trimmedPrompt,
            language = language
        )

        return if (apiResult.isSuccess) {
            val responseText = apiResult.getOrThrow()
            val aiMessage = MessageEntity(
                conversationId = conversationId,
                content = responseText,
                sender = "AI",
                timestamp = System.currentTimeMillis(),
                modelUsed = model.id,
                isError = false
            )
            val msgId = database.messageDao().insert(aiMessage)
            database.conversationDao().updateLastActivity(
                id = conversationId,
                updatedAt = System.currentTimeMillis(),
                snippet = responseText.take(60)
            )
            Result.success(aiMessage.copy(id = msgId))
        } else {
            val errorText = apiResult.exceptionOrNull()?.message ?: "حدث خطأ غير متوقع."
            val errorMessage = MessageEntity(
                conversationId = conversationId,
                content = errorText,
                sender = "AI",
                timestamp = System.currentTimeMillis(),
                modelUsed = model.id,
                isError = true
            )
            val msgId = database.messageDao().insert(errorMessage)
            Result.failure(apiResult.exceptionOrNull() ?: Exception(errorText))
        }
    }

    suspend fun deleteMessage(messageId: Long) {
        database.messageDao().deleteById(messageId)
    }

    suspend fun deleteConversation(conversationId: Long) {
        database.conversationDao().deleteById(conversationId)
    }

    suspend fun clearAllConversations() {
        database.conversationDao().clearAll()
    }

    suspend fun renameConversation(conversationId: Long, newTitle: String) {
        val existing = database.conversationDao().getConversationById(conversationId) ?: return
        database.conversationDao().update(existing.copy(title = newTitle.trim()))
    }
}
