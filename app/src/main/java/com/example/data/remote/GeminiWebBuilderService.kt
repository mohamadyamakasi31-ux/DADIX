package com.example.data.remote

import com.example.data.local.ProjectFileEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.ProjectType
import com.example.data.model.TechStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class AiGenerationResult(
    val summary: String,
    val files: List<ProjectFileEntity>
)

data class AiModificationResult(
    val summary: String,
    val changedFiles: List<ProjectFileEntity>
)

class GeminiWebBuilderService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateWebsite(
        apiKey: String?,
        model: AiModel,
        prompt: String,
        projectType: ProjectType,
        techStack: TechStack,
        language: AppLanguage,
        projectId: Long
    ): Result<AiGenerationResult> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank() || apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            val errorMsg = when (language) {
                AppLanguage.AR -> "لم يتم ربط مزود الذكاء الاصطناعي (Gemini). لن نقوم بتوليد مشروع وهمي. يرجى إدخال مفتاح Gemini API في الإعدادات أو لوحة أسرار AI Studio."
                AppLanguage.FR -> "Aucun fournisseur d'IA (Gemini) associé. Veuillez renseigner votre clé Gemini API dans les Paramètres."
                AppLanguage.EN -> "No AI provider (Gemini) is linked. Please provide a valid Gemini API key in Settings or AI Studio Secrets."
            }
            return@withContext Result.failure(ProviderNotLinkedException(errorMsg))
        }

        try {
            val systemInstruction = """
                You are Dadix Web AI, an expert full-stack website builder and code generator.
                Generate a complete, modern, responsive website based on the user's prompt.
                Tech Stack: ${techStack.displayName}
                Project Type: ${projectType.name}
                Target Language: ${language.code} (If Arabic, use dir="rtl" and Arabic typography/content).

                CRITICAL OUTPUT FORMAT REQUIREMENTS:
                You MUST return ONLY a valid, parseable JSON object with NO markdown ticks or conversational text outside it.
                Schema:
                {
                  "summary": "Brief explanation of what was built and its main features",
                  "files": [
                    { "path": "index.html", "content": "<!DOCTYPE html>..." },
                    { "path": "style.css", "content": "/* styles */" },
                    { "path": "app.js", "content": "// javascript logic" },
                    { "path": "package.json", "content": "..." },
                    { "path": "README.md", "content": "# Readme..." }
                  ]
                }
                Make the website realistic, feature-complete, stylish (using Tailwind CDN and modern CSS), interactive with JavaScript, and fully working in browser previews.
            """.trimIndent()

            val responseText = executeGeminiCall(apiKey, model, systemInstruction, prompt, language)
            val parsed = parseFilesFromJson(responseText, projectId)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun modifyWebsite(
        apiKey: String?,
        model: AiModel,
        userInstruction: String,
        existingFiles: List<ProjectFileEntity>,
        language: AppLanguage,
        projectId: Long
    ): Result<AiModificationResult> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank() || apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            val errorMsg = when (language) {
                AppLanguage.AR -> "لم يتم ربط مزود الذكاء الاصطناعي (Gemini). يرجى إدخال مفتاح API في الإعدادات لتعديل المشروع."
                AppLanguage.FR -> "Clé Gemini API requise pour modifier le projet."
                AppLanguage.EN -> "Gemini API key is required to modify this project."
            }
            return@withContext Result.failure(ProviderNotLinkedException(errorMsg))
        }

        try {
            val filesContext = StringBuilder("CURRENT PROJECT FILES:\n")
            for (f in existingFiles) {
                filesContext.append("=== FILE: ${f.path} ===\n")
                filesContext.append(f.content.take(1500)) // send representative content
                filesContext.append("\n\n")
            }

            val systemInstruction = """
                You are Dadix Web AI Assistant. The user wants to modify their existing website.
                Inspect the current files and return ONLY the files that need to be created or modified to fulfill the user's request.
                Do NOT return unchanged files.
                Format output strictly as JSON:
                {
                  "summary": "Brief description of changes made",
                  "files": [
                    { "path": "filename.ext", "content": "complete updated content of this file" }
                  ]
                }
            """.trimIndent()

            val combinedPrompt = "$filesContext\nUSER REQUEST FOR MODIFICATION:\n$userInstruction"
            val responseText = executeGeminiCall(apiKey, model, systemInstruction, combinedPrompt, language)
            val parsed = parseFilesFromJson(responseText, projectId)
            Result.success(AiModificationResult(summary = parsed.summary, changedFiles = parsed.files))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeGeminiCall(
        apiKey: String,
        model: AiModel,
        systemInstruction: String,
        userPrompt: String,
        language: AppLanguage
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.id}:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject()

        val systemInstructionJson = JSONObject()
        val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
        systemInstructionJson.put("parts", sysParts)
        requestBodyJson.put("systemInstruction", systemInstructionJson)

        val contentsArray = JSONArray()
        val userObj = JSONObject()
        userObj.put("role", "user")
        userObj.put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
        contentsArray.put(userObj)
        requestBodyJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("maxOutputTokens", 8192)
        requestBodyJson.put("generationConfig", genConfig)

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val serverMsg = runCatching {
                JSONObject(responseBody).optJSONObject("error")?.optString("message", "")
            }.getOrNull()
            throw Exception("Gemini API Error (${response.code}): ${serverMsg ?: responseBody}")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: throw Exception("No candidates returned from Gemini.")
        if (candidates.length() == 0) throw Exception("Empty candidates from Gemini.")

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts") ?: throw Exception("No parts in Gemini response.")

        val textBuilder = StringBuilder()
        for (i in 0 until parts.length()) {
            textBuilder.append(parts.getJSONObject(i).optString("text", ""))
        }
        return textBuilder.toString().trim()
    }

    private fun parseFilesFromJson(raw: String, projectId: Long): AiGenerationResult {
        // Strip markdown backticks if present
        var cleanJson = raw.trim()
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.removePrefix("```json")
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.removePrefix("```")
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.removeSuffix("```")
        }
        cleanJson = cleanJson.trim()

        val jsonObj = JSONObject(cleanJson)
        val summary = jsonObj.optString("summary", "تم توليد ملفات المشروع بنجاح.")
        val filesArray = jsonObj.optJSONArray("files") ?: JSONArray()

        val resultFiles = mutableListOf<ProjectFileEntity>()
        for (i in 0 until filesArray.length()) {
            val fileObj = filesArray.getJSONObject(i)
            val path = fileObj.getString("path").trim().removePrefix("/")
            val content = fileObj.getString("content")
            resultFiles.add(
                ProjectFileEntity(
                    projectId = projectId,
                    path = path,
                    content = content
                )
            )
        }

        if (resultFiles.isEmpty()) {
            throw Exception("لم يعثر على ملفات صالحة في استجابة الذكاء الاصطناعي.")
        }

        return AiGenerationResult(summary = summary, files = resultFiles)
    }
}
