package com.example.data.model

enum class AiModel(
    val id: String,
    val displayName: String,
    val badgeAr: String,
    val badgeEn: String,
    val badgeFr: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val descriptionFr: String,
    val speedTag: String,
    val isDefault: Boolean = false
) {
    GEMINI_2_5_FLASH(
        id = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash",
        badgeAr = "الافتراضي • فائق السرعة",
        badgeEn = "Default • Ultra Fast",
        badgeFr = "Par défaut • Ultra Rapide",
        descriptionAr = "النموذج الأحدث والأنسب للمحادثات اليومية، صياغة النصوص، والبرمجة مع سرعة استجابة مذهلة.",
        descriptionEn = "Latest cutting-edge model ideal for everyday chats, coding, and creative tasks with ultra-fast latency.",
        descriptionFr = "Dernier modèle idéal pour les discussions quotidiennes, le code et les tâches avec une latence ultra-faible.",
        speedTag = "⚡⚡⚡",
        isDefault = true
    ),
    GEMINI_2_5_PRO(
        id = "gemini-2.5-pro",
        displayName = "Gemini 2.5 Pro",
        badgeAr = "تفكير عميق • مهام معقدة",
        badgeEn = "Deep Reasoning • Complex",
        badgeFr = "Raisonnement avancé • Complexe",
        descriptionAr = "قوة استدلال فائقة لحل المشكلات المعقدة والتحليل المنطقي والرياضي المتقدم.",
        descriptionEn = "Maximum reasoning power for complex math, structured reasoning, and high-precision problem solving.",
        descriptionFr = "Puissance de raisonnement maximale pour les problèmes complexes et les analyses de haute précision.",
        speedTag = "⚡⚡",
        isDefault = false
    ),
    GEMINI_1_5_FLASH(
        id = "gemini-1.5-flash",
        displayName = "Gemini 1.5 Flash",
        badgeAr = "خفيف • اقتصادي",
        badgeEn = "Lightweight • Efficient",
        badgeFr = "Léger • Économe",
        descriptionAr = "أداء رشيق وموفر، ممتاز للأجهزة ذات الاتصال المحدود والاستفسارات المقتضبة.",
        descriptionEn = "Fast, lightweight model tuned for quick questions and high resource efficiency.",
        descriptionFr = "Modèle rapide et léger optimisé pour les réponses brèves et l'efficacité des données.",
        speedTag = "⚡⚡⚡",
        isDefault = false
    ),
    GEMINI_1_5_PRO(
        id = "gemini-1.5-pro",
        displayName = "Gemini 1.5 Pro",
        badgeAr = "سياق ضخم • نصوص طويلة",
        badgeEn = "Massive Context • Long Text",
        badgeFr = "Contexte étendu • Textes longs",
        descriptionAr = "نافذة سياق عملاقة تصل حتى مليون رمز، مثالية لتلخيص المستندات الكبيرة ومراجعة الشيفرات الطويلة.",
        descriptionEn = "Vast context window capable of deep analysis across long code repositories and large texts.",
        descriptionFr = "Fenêtre contextuelle énorme permettant l'analyse approfondie de longs documents et codes.",
        speedTag = "⚡",
        isDefault = false
    );

    fun getBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> badgeAr
        AppLanguage.FR -> badgeFr
        AppLanguage.EN -> badgeEn
    }

    fun getDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> descriptionAr
        AppLanguage.FR -> descriptionFr
        AppLanguage.EN -> descriptionEn
    }

    companion object {
        val ALL: List<AiModel> = entries

        fun fromId(id: String?): AiModel {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI_2_5_FLASH
        }
    }
}
