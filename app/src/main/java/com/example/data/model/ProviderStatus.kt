package com.example.data.model

enum class ApiKeySource {
    AI_STUDIO_SECRETS,
    MANUAL_INPUT,
    NONE
}

data class ProviderStatus(
    val isLinked: Boolean,
    val source: ApiKeySource,
    val activeModel: AiModel,
    val providerName: String = "Google Gemini",
    val maskedKey: String = ""
)
