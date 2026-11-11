package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val content: String,
    val sender: String, // "USER" or "AI" or "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isError: Boolean = false
) {
    val isUser: Boolean get() = sender.equals("USER", ignoreCase = true)
    val isAi: Boolean get() = sender.equals("AI", ignoreCase = true)
    val isSystem: Boolean get() = sender.equals("SYSTEM", ignoreCase = true)
}
