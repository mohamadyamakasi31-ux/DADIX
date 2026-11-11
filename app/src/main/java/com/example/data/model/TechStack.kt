package com.example.data.model

enum class TechStack(
    val id: String,
    val displayName: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val subtitleFr: String,
    val icon: String
) {
    HTML_CSS_JS(
        id = "vanilla",
        displayName = "HTML5 + CSS3 + Vanilla JS",
        subtitleAr = "خفيف، مباشر، وسريع التعديل والتشغيل",
        subtitleEn = "Lightweight, native and instant preview",
        subtitleFr = "Léger, natif et rendu instantané",
        icon = "🌐"
    ),
    TAILWIND(
        id = "tailwind",
        displayName = "HTML5 + Tailwind CSS + Lucide",
        subtitleAr = "تصميم احترافي فائق التجاوب والأناقة",
        subtitleEn = "Modern utility-first CSS styling",
        subtitleFr = "Design utilitaire ultra moderne",
        icon = "🎨"
    ),
    REACT(
        id = "react",
        displayName = "React 18 + Tailwind",
        subtitleAr = "مكونات تفاعلية، إدارة حالة متقدمة وJSX",
        subtitleEn = "Interactive component architecture",
        subtitleFr = "Architecture composants réactive",
        icon = "⚛️"
    ),
    VITE(
        id = "vite",
        displayName = "Vite + TypeScript / Modern JS",
        subtitleAr = "هيكل بناء حديث مع package.json كامل",
        subtitleEn = "Modern build tooling with package.json",
        subtitleFr = "Outillage moderne avec package.json",
        icon = "⚡"
    );

    fun getSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> subtitleAr
        AppLanguage.FR -> subtitleFr
        AppLanguage.EN -> subtitleEn
    }

    companion object {
        fun fromId(id: String?): TechStack {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: TAILWIND
        }
    }
}
