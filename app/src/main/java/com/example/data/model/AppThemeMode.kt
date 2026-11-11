package com.example.data.model

enum class AppThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    fun getDisplayName(lang: AppLanguage): String = when (this) {
        SYSTEM -> when (lang) {
            AppLanguage.AR -> "تلقائي (حسب النظام)"
            AppLanguage.FR -> "Système"
            AppLanguage.EN -> "System Default"
        }
        LIGHT -> when (lang) {
            AppLanguage.AR -> "الوضع الفاتح"
            AppLanguage.FR -> "Mode clair"
            AppLanguage.EN -> "Light Mode"
        }
        DARK -> when (lang) {
            AppLanguage.AR -> "الوضع الداكن"
            AppLanguage.FR -> "Mode sombre"
            AppLanguage.EN -> "Dark Mode"
        }
    }

    companion object {
        fun fromCode(code: String?): AppThemeMode {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}
