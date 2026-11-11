package com.example.data.model

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val flagEmoji: String,
    val isRtl: Boolean
) {
    AR(code = "ar", nativeName = "العربية", flagEmoji = "🇸🇦", isRtl = true),
    EN(code = "en", nativeName = "English", flagEmoji = "🇬🇧", isRtl = false),
    FR(code = "fr", nativeName = "Français", flagEmoji = "🇫🇷", isRtl = false);

    companion object {
        val DEFAULT = AR

        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
