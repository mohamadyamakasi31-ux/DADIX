package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.ApiKeySource
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.ProviderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dadix_ai_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _selectedModel = MutableStateFlow(loadSelectedModel())
    val selectedModel: StateFlow<AiModel> = _selectedModel.asStateFlow()

    private val _providerStatus = MutableStateFlow(computeProviderStatus())
    val providerStatus: StateFlow<ProviderStatus> = _providerStatus.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val code = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.code)
        return AppThemeMode.fromCode(code)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.code).apply()
        _themeMode.value = mode
    }

    private fun loadLanguage(): AppLanguage {
        // Arabic by default per user requirement
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.AR.code)
        return AppLanguage.fromCode(code)
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
        _language.value = lang
    }

    private fun loadSelectedModel(): AiModel {
        val id = prefs.getString(KEY_SELECTED_MODEL, AiModel.GEMINI_2_5_FLASH.id)
        return AiModel.fromId(id)
    }

    fun setSelectedModel(model: AiModel) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model.id).apply()
        _selectedModel.value = model
        _providerStatus.value = computeProviderStatus()
    }

    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
        _providerStatus.value = computeProviderStatus()
    }

    /**
     * Retrieves the effective API key.
     * Priority:
     * 1. Manual user custom API key (if set and not blank)
     * 2. BuildConfig.GEMINI_API_KEY (if non-blank and not placeholder "MY_GEMINI_API_KEY")
     */
    fun getEffectiveApiKey(): String? {
        val customKey = getCustomApiKey().trim()
        if (customKey.isNotEmpty()) {
            return customKey
        }
        val envKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()?.trim() ?: ""
        if (envKey.isNotEmpty() && !envKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            return envKey
        }
        return null
    }

    fun computeProviderStatus(): ProviderStatus {
        val customKey = getCustomApiKey().trim()
        val envKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()?.trim() ?: ""

        val (isLinked, source, rawKey) = when {
            customKey.isNotEmpty() -> Triple(true, ApiKeySource.MANUAL_INPUT, customKey)
            envKey.isNotEmpty() && !envKey.equals("MY_GEMINI_API_KEY", ignoreCase = true) ->
                Triple(true, ApiKeySource.AI_STUDIO_SECRETS, envKey)
            else -> Triple(false, ApiKeySource.NONE, "")
        }

        val masked = if (rawKey.length > 8) {
            "${rawKey.take(4)}••••••••${rawKey.takeLast(4)}"
        } else if (rawKey.isNotEmpty()) {
            "••••••••"
        } else {
            ""
        }

        return ProviderStatus(
            isLinked = isLinked,
            source = source,
            activeModel = _selectedModel.value,
            providerName = "Google Gemini",
            maskedKey = masked
        )
    }

    fun getCloudflareAccountId(): String {
        return prefs.getString(KEY_CLOUDFLARE_ACCOUNT_ID, "") ?: ""
    }

    fun setCloudflareAccountId(id: String) {
        prefs.edit().putString(KEY_CLOUDFLARE_ACCOUNT_ID, id.trim()).apply()
    }

    fun getCloudflareApiToken(): String {
        return prefs.getString(KEY_CLOUDFLARE_API_TOKEN, "") ?: ""
    }

    fun setCloudflareApiToken(token: String) {
        prefs.edit().putString(KEY_CLOUDFLARE_API_TOKEN, token.trim()).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_SELECTED_MODEL = "key_selected_model"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CLOUDFLARE_ACCOUNT_ID = "key_cloudflare_account_id"
        private const val KEY_CLOUDFLARE_API_TOKEN = "key_cloudflare_api_token"
    }
}
