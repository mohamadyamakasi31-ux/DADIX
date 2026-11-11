package com.example.data.remote

import com.example.data.local.MessageEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
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

class ProviderNotLinkedException(message: String) : Exception(message)

class GeminiApiService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        apiKey: String?,
        model: AiModel,
        history: List<MessageEntity>,
        userPrompt: String,
        language: AppLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        // 1. Strict check: No simulated or mock data if no provider is linked
        if (apiKey.isNullOrBlank() || apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            val errorMsg = when (language) {
                AppLanguage.AR -> "لم يتم ربط مزود الذكاء الاصطناعي (Gemini). تطبيق Dadix AI أصلي ولا يولد بيانات وهمية للإيهام بالعمل. يرجى إدخال مفتاح API في الإعدادات أو لوحة الأسرار."
                AppLanguage.FR -> "Aucun fournisseur d'IA (Gemini) n'est associé. Dadix AI n'utilise pas de données simulées. Veuillez renseigner votre clé API dans les Paramètres."
                AppLanguage.EN -> "No AI provider (Gemini) is linked. Dadix AI is authentic and does not fabricate mock responses. Please provide a valid API key in Settings or AI Studio Secrets."
            }
            return@withContext Result.failure(ProviderNotLinkedException(errorMsg))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.id}:generateContent?key=$apiKey"

            val requestBodyJson = JSONObject()

            // System Instruction
            val systemInstructionJson = JSONObject()
            val sysPartsArray = JSONArray()
            val systemPrompt = "You are Dadix AI, an intelligent, helpful, and highly accurate AI assistant. " +
                    "Always reply gracefully in the language used by the user. " +
                    "Format code snippets cleanly and use clear, elegant explanations."
            sysPartsArray.put(JSONObject().put("text", systemPrompt))
            systemInstructionJson.put("parts", sysPartsArray)
            requestBodyJson.put("systemInstruction", systemInstructionJson)

            // Contents array
            val contentsArray = JSONArray()

            // Append limited conversation history for multi-turn context (last 8 messages)
            val filteredHistory = history.filter { !it.isError && it.content.isNotBlank() }.takeLast(8)
            for (msg in filteredHistory) {
                val role = if (msg.isUser) "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.content))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Append current prompt
            val currentUserObj = JSONObject()
            currentUserObj.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userPrompt))
            currentUserObj.put("parts", currentParts)
            contentsArray.put(currentUserObj)

            requestBodyJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 2048)
            requestBodyJson.put("generationConfig", genConfig)

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .header("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val parsedError = parseApiError(response.code, responseBody, language)
                return@withContext Result.failure(Exception(parsedError))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val emptyMsg = when (language) {
                    AppLanguage.AR -> "لم يرجع النموذج أي رد، يرجى المحاولة مجدداً."
                    AppLanguage.FR -> "Le modèle n'a renvoyé aucune réponse. Veuillez réessayer."
                    AppLanguage.EN -> "The AI model returned no content. Please retry."
                }
                return@withContext Result.failure(Exception(emptyMsg))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val fullText = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    fullText.append(p.optString("text", ""))
                }
            }

            val resultText = fullText.toString().trim()
            if (resultText.isEmpty()) {
                val emptyMsg = when (language) {
                    AppLanguage.AR -> "استجابة فارغة من النموذج."
                    AppLanguage.FR -> "Réponse vide reçue du modèle."
                    AppLanguage.EN -> "Empty response received from model."
                }
                return@withContext Result.failure(Exception(emptyMsg))
            }

            Result.success(resultText)
        } catch (e: IOException) {
            val netError = when (language) {
                AppLanguage.AR -> "تعذر الاتصال بخوادم الذكاء الاصطناعي. يرجى التحقق من اتصال الإنترنت."
                AppLanguage.FR -> "Impossible de se connecter aux serveurs IA. Vérifiez votre connexion Internet."
                AppLanguage.EN -> "Unable to connect to AI servers. Please verify your internet connection."
            }
            Result.failure(Exception(netError, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseApiError(code: Int, body: String, lang: AppLanguage): String {
        val serverMessage = runCatching {
            val json = JSONObject(body)
            val error = json.optJSONObject("error")
            error?.optString("message", "")
        }.getOrNull()

        return when (code) {
            400 -> when (lang) {
                AppLanguage.AR -> "طلب غير صالح أو مفتاح API غير مفعل: ${serverMessage ?: "رمز الخطأ 400"}"
                AppLanguage.FR -> "Requête invalide ou clé API non activée: ${serverMessage ?: "Erreur 400"}"
                AppLanguage.EN -> "Invalid request or inactive API key: ${serverMessage ?: "Error 400"}"
            }
            403 -> when (lang) {
                AppLanguage.AR -> "تم رفض الوصول لمفتاح API (صلاحيات غير كافية أو مفتاح محظور)."
                AppLanguage.FR -> "Accès refusé pour la clé API (permissions insuffisantes)."
                AppLanguage.EN -> "Access forbidden for this API key (insufficient permissions)."
            }
            429 -> when (lang) {
                AppLanguage.AR -> "تم تجاوز حد الطلبات المسموح (Quota Exceeded). يرجى الانتظار قليلاً."
                AppLanguage.FR -> "Limite de requêtes atteinte (Quota Exceeded). Veuillez patienter."
                AppLanguage.EN -> "Rate limit reached (Quota Exceeded). Please wait a moment."
            }
            500, 503 -> when (lang) {
                AppLanguage.AR -> "خوادم Gemini تواجه ضغطاً مؤقتاً، يرجى المحاولة بعد لحظات."
                AppLanguage.FR -> "Les serveurs Gemini sont temporairement surchargés."
                AppLanguage.EN -> "Gemini servers are temporarily overloaded. Please try again soon."
            }
            else -> when (lang) {
                AppLanguage.AR -> "خطأ في الاتصال ($code): ${serverMessage ?: "يرجى التحقق من المفتاح"}"
                AppLanguage.FR -> "Erreur de connexion ($code): ${serverMessage ?: "Vérifiez vos paramètres"}"
                AppLanguage.EN -> "Connection error ($code): ${serverMessage ?: "Check your settings"}"
            }
        }
    }
}
