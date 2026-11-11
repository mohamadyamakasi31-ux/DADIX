package com.example.data.remote

import com.example.data.local.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class DeployResult {
    data class Success(val url: String, val message: String) : DeployResult()
    data class Failure(val errorMessage: String) : DeployResult()
}

class CloudflareDeployService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun deployProject(
        accountId: String?,
        apiToken: String?,
        projectName: String,
        files: List<ProjectFileEntity>
    ): DeployResult = withContext(Dispatchers.IO) {
        if (accountId.isNullOrBlank() || apiToken.isNullOrBlank()) {
            return@withContext DeployResult.Failure(
                "بيانات Cloudflare غير مكتملة. يرجى إدخال Account ID و API Token في الإعدادات للمتابعة في النشر الحقيقي."
            )
        }

        val cleanProjectName = projectName.lowercase()
            .replace(Regex("[^a-z0-9-]"), "-")
            .trim('-')
            .ifBlank { "my-site" }

        try {
            // 1. Ensure Cloudflare Pages project exists
            val projectUrl = "https://api.cloudflare.com/client/v4/accounts/$accountId/pages/projects"
            val createProjectJson = JSONObject().apply {
                put("name", cleanProjectName)
                put("production_branch", "main")
            }

            val createReq = Request.Builder()
                .url(projectUrl)
                .header("Authorization", "Bearer $apiToken")
                .header("Content-Type", "application/json")
                .post(createProjectJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            // execute creation (or ignore if already exists)
            val createRes = client.newCall(createReq).execute()
            createRes.close()

            // 2. Upload deployment files (Multipart)
            val deployUrl = "https://api.cloudflare.com/client/v4/accounts/$accountId/pages/projects/$cleanProjectName/deployments"
            val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

            for (file in files) {
                if (file.isFolder) continue
                val partName = file.path.removePrefix("/")
                val mediaType = when {
                    file.isHtml -> "text/html; charset=utf-8".toMediaType()
                    file.isCss -> "text/css; charset=utf-8".toMediaType()
                    file.isJs -> "application/javascript; charset=utf-8".toMediaType()
                    file.isJson -> "application/json; charset=utf-8".toMediaType()
                    else -> "text/plain; charset=utf-8".toMediaType()
                }
                multipartBuilder.addFormDataPart(
                    partName,
                    partName,
                    file.content.toRequestBody(mediaType)
                )
            }

            val deployReq = Request.Builder()
                .url(deployUrl)
                .header("Authorization", "Bearer $apiToken")
                .post(multipartBuilder.build())
                .build()

            val deployRes = client.newCall(deployReq).execute()
            val deployBody = deployRes.body?.string().orEmpty()

            if (!deployRes.isSuccessful) {
                val errorMsg = runCatching {
                    val json = JSONObject(deployBody)
                    val errors = json.optJSONArray("errors")
                    errors?.optJSONObject(0)?.optString("message") ?: deployBody
                }.getOrDefault(deployBody)

                return@withContext DeployResult.Failure("فشل النشر على Cloudflare (${deployRes.code}): $errorMsg")
            }

            val resJson = JSONObject(deployBody)
            val resultObj = resJson.optJSONObject("result")
            val publishedUrl = resultObj?.optString("url") ?: "https://$cleanProjectName.pages.dev"

            DeployResult.Success(
                url = publishedUrl,
                message = "تم نشر الموقع بنجاح على Cloudflare Pages!"
            )
        } catch (e: Exception) {
            DeployResult.Failure("خطأ في الاتصال بـ Cloudflare: ${e.localizedMessage}")
        }
    }
}
