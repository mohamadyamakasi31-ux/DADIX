package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectChatMessageDao {
    @Query("SELECT * FROM web_project_chat WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProject(projectId: Long): Flow<List<ProjectChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ProjectChatMessageEntity): Long

    @Query("DELETE FROM web_project_chat WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: Long)
}
