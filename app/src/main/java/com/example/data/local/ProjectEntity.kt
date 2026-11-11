package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val projectType: String = "e_commerce",
    val techStack: String = "tailwind",
    val targetLanguage: String = "ar",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deploymentUrl: String? = null,
    val deploymentStatus: String = "IDLE", // IDLE, BUILDING, DEPLOYED, FAILED
    val deploymentMessage: String? = null
)
