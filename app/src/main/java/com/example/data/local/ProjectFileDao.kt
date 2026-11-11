package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectFileDao {
    @Query("SELECT * FROM web_project_files WHERE projectId = :projectId ORDER BY isFolder DESC, path ASC")
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM web_project_files WHERE projectId = :projectId ORDER BY isFolder DESC, path ASC")
    suspend fun getFilesListForProject(projectId: Long): List<ProjectFileEntity>

    @Query("SELECT * FROM web_project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: ProjectFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<ProjectFileEntity>)

    @Update
    suspend fun update(file: ProjectFileEntity)

    @Delete
    suspend fun delete(file: ProjectFileEntity)

    @Query("DELETE FROM web_project_files WHERE projectId = :projectId AND path = :path")
    suspend fun deleteByPath(projectId: Long, path: String)

    @Query("DELETE FROM web_project_files WHERE projectId = :projectId")
    suspend fun deleteAllForProject(projectId: Long)
}
