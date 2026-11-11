package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "web_project_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["projectId", "path"], unique = true)
    ]
)
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val path: String, // e.g. "index.html", "style.css", "src/App.jsx", "README.md"
    val content: String,
    val isFolder: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val fileName: String
        get() = path.substringAfterLast('/')

    val extension: String
        get() = path.substringAfterLast('.', "")

    val isHtml: Boolean get() = extension.equals("html", ignoreCase = true)
    val isCss: Boolean get() = extension.equals("css", ignoreCase = true)
    val isJs: Boolean get() = extension.equals("js", ignoreCase = true) || extension.equals("jsx", ignoreCase = true) || extension.equals("ts", ignoreCase = true) || extension.equals("tsx", ignoreCase = true)
    val isJson: Boolean get() = extension.equals("json", ignoreCase = true)
    val isMarkdown: Boolean get() = extension.equals("md", ignoreCase = true)
}
