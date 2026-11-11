package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ProjectChatMessageEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectFileEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.ProjectType
import com.example.data.model.TechStack
import com.example.data.remote.CloudflareDeployService
import com.example.data.remote.DeployResult
import com.example.data.remote.GeminiWebBuilderService
import com.example.data.remote.StarterTemplateProvider
import com.example.data.remote.ZipExporterService
import kotlinx.coroutines.flow.Flow
import java.io.File

class WebProjectRepository(
    private val database: AppDatabase,
    private val preferences: AppPreferences,
    private val geminiService: GeminiWebBuilderService = GeminiWebBuilderService(),
    private val deployService: CloudflareDeployService = CloudflareDeployService()
) {
    val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()

    fun getProjectFiles(projectId: Long): Flow<List<ProjectFileEntity>> =
        database.projectFileDao().getFilesForProject(projectId)

    fun getChatMessages(projectId: Long): Flow<List<ProjectChatMessageEntity>> =
        database.projectChatMessageDao().getMessagesForProject(projectId)

    suspend fun getProjectById(id: Long): ProjectEntity? =
        database.projectDao().getProjectById(id)

    suspend fun createNewProject(
        name: String,
        description: String,
        projectType: ProjectType,
        techStack: TechStack,
        targetLanguage: AppLanguage,
        prompt: String? = null
    ): ProjectEntity {
        val project = ProjectEntity(
            name = name.ifBlank { projectType.titleAr },
            description = description.ifBlank { prompt ?: projectType.defaultPromptAr },
            projectType = projectType.id,
            techStack = techStack.id,
            targetLanguage = targetLanguage.code
        )
        val id = database.projectDao().insert(project)
        val savedProject = project.copy(id = id)

        // 1. Generate Starter Files first so user has immediate real files
        val starterFiles = StarterTemplateProvider.generateStarterFiles(
            projectId = id,
            projectName = savedProject.name,
            projectType = projectType,
            techStack = techStack
        )
        database.projectFileDao().insertAll(starterFiles)

        // 2. If prompt is provided and Gemini API key is available, run AI generation in background/sequentially
        val effectiveKey = preferences.getEffectiveApiKey()
        if (!prompt.isNullOrBlank() && !effectiveKey.isNullOrBlank()) {
            val genResult = geminiService.generateWebsite(
                apiKey = effectiveKey,
                model = preferences.selectedModel.value,
                prompt = prompt,
                projectType = projectType,
                techStack = techStack,
                language = targetLanguage,
                projectId = id
            )
            if (genResult.isSuccess) {
                val aiFiles = genResult.getOrThrow().files
                database.projectFileDao().insertAll(aiFiles)
                database.projectChatMessageDao().insert(
                    ProjectChatMessageEntity(
                        projectId = id,
                        sender = "AI",
                        content = genResult.getOrThrow().summary,
                        filesChangedCount = aiFiles.size
                    )
                )
            }
        }

        return savedProject
    }

    suspend fun saveFileContent(projectId: Long, path: String, content: String) {
        val existing = database.projectFileDao().getFileByPath(projectId, path)
        if (existing != null) {
            database.projectFileDao().update(
                existing.copy(content = content, updatedAt = System.currentTimeMillis())
            )
        } else {
            database.projectFileDao().insert(
                ProjectFileEntity(
                    projectId = projectId,
                    path = path,
                    content = content
                )
            )
        }
        database.projectDao().touch(projectId)
    }

    suspend fun createNewFile(projectId: Long, path: String, initialContent: String = "") {
        val cleanPath = path.trim().removePrefix("/")
        database.projectFileDao().insert(
            ProjectFileEntity(
                projectId = projectId,
                path = cleanPath,
                content = initialContent
            )
        )
        database.projectDao().touch(projectId)
    }

    suspend fun deleteFile(projectId: Long, path: String) {
        database.projectFileDao().deleteByPath(projectId, path)
        database.projectDao().touch(projectId)
    }

    suspend fun renameFile(projectId: Long, oldPath: String, newPath: String) {
        val existing = database.projectFileDao().getFileByPath(projectId, oldPath) ?: return
        database.projectFileDao().deleteByPath(projectId, oldPath)
        database.projectFileDao().insert(
            existing.copy(path = newPath.trim().removePrefix("/"), updatedAt = System.currentTimeMillis())
        )
        database.projectDao().touch(projectId)
    }

    suspend fun deleteProject(projectId: Long) {
        database.projectFileDao().deleteAllForProject(projectId)
        database.projectChatMessageDao().deleteForProject(projectId)
        database.projectDao().deleteById(projectId)
    }

    suspend fun sendChatModification(
        projectId: Long,
        instruction: String,
        model: AiModel,
        language: AppLanguage
    ): Result<String> {
        // Log user prompt
        database.projectChatMessageDao().insert(
            ProjectChatMessageEntity(
                projectId = projectId,
                sender = "USER",
                content = instruction
            )
        )

        val files = database.projectFileDao().getFilesListForProject(projectId)
        val apiKey = preferences.getEffectiveApiKey()

        val result = geminiService.modifyWebsite(
            apiKey = apiKey,
            model = model,
            userInstruction = instruction,
            existingFiles = files,
            language = language,
            projectId = projectId
        )

        return if (result.isSuccess) {
            val mod = result.getOrThrow()
            for (f in mod.changedFiles) {
                database.projectFileDao().insert(f)
            }
            database.projectDao().touch(projectId)

            database.projectChatMessageDao().insert(
                ProjectChatMessageEntity(
                    projectId = projectId,
                    sender = "AI",
                    content = mod.summary,
                    summaryOfChanges = mod.summary,
                    filesChangedCount = mod.changedFiles.size
                )
            )
            Result.success(mod.summary)
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تعديل الملفات."
            database.projectChatMessageDao().insert(
                ProjectChatMessageEntity(
                    projectId = projectId,
                    sender = "SYSTEM",
                    content = errorMsg
                )
            )
            Result.failure(result.exceptionOrNull() ?: Exception(errorMsg))
        }
    }

    suspend fun exportZip(context: Context, projectId: Long): Result<File> {
        val project = database.projectDao().getProjectById(projectId)
            ?: return Result.failure(Exception("المشروع غير موجود"))
        val files = database.projectFileDao().getFilesListForProject(projectId)
        return ZipExporterService.exportProjectToZip(context, project.name, files)
    }

    suspend fun deployToCloudflare(projectId: Long): DeployResult {
        val project = database.projectDao().getProjectById(projectId)
            ?: return DeployResult.Failure("المشروع غير موجود")
        val files = database.projectFileDao().getFilesListForProject(projectId)

        val accountId = preferences.getCloudflareAccountId()
        val apiToken = preferences.getCloudflareApiToken()

        database.projectDao().updateDeployment(projectId, "BUILDING", null, "جاري تحضير ورفع الملفات...")

        val res = deployService.deployProject(accountId, apiToken, project.name, files)

        when (res) {
            is DeployResult.Success -> {
                database.projectDao().updateDeployment(projectId, "DEPLOYED", res.url, res.message)
            }
            is DeployResult.Failure -> {
                database.projectDao().updateDeployment(projectId, "FAILED", null, res.errorMessage)
            }
        }
        return res
    }
}
