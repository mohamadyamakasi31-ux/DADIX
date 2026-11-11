package com.example.data.model

enum class ProjectType(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val titleFr: String,
    val icon: String,
    val defaultPromptAr: String
) {
    E_COMMERCE(
        id = "e_commerce",
        titleAr = "متجر إلكتروني",
        titleEn = "E-Commerce",
        titleFr = "Boutique en ligne",
        icon = "🛍️",
        defaultPromptAr = "أنشئ موقعاً احترافياً لمتجر إلكترونيات باللغة العربية، يحتوي على كاتالوج منتجات، أسعار، تصفية الفئات، سلة مشتريات تفاعلية، وصفحة اتصل بنا."
    ),
    COMPANY(
        id = "company",
        titleAr = "موقع شركة ومؤسسة",
        titleEn = "Company / Corporate",
        titleFr = "Site d'entreprise",
        icon = "🏢",
        defaultPromptAr = "أنشئ موقعاً رسمياً عصرياً لشركة حلول تقنية وخدمات سحابية، يحتوي على نبذة عنا، خدماتنا، فريق العمل، شهادات العملاء، ونموذج طلب استشارة."
    ),
    PORTFOLIO(
        id = "portfolio",
        titleAr = "معرض أعمال شخصي",
        titleEn = "Personal Portfolio",
        titleFr = "Portfolio personnel",
        icon = "💼",
        defaultPromptAr = "أنشئ معرض أعمال شخصي لمطور برمجيات ومصمم واجهات، يحتوي على المشاريع، المهارات التقنية، السيرة الذاتية، ونموذج تواصل سريع مع تأثيرات حركية أنيقة."
    ),
    BLOG(
        id = "blog",
        titleAr = "مدونة ومحتوى",
        titleEn = "Blog / Publication",
        titleFr = "Blog & Magazine",
        icon = "✍️",
        defaultPromptAr = "أنشئ مدونة تقنية عصرية مع ميزة قراءة المقالات، تصنيفات، شريط بحث، مقالات مقترحة، ومساحة تعليقات."
    ),
    NEWS(
        id = "news",
        titleAr = "موقع إخباري",
        titleEn = "News Portal",
        titleFr = "Site d'actualités",
        icon = "📰",
        defaultPromptAr = "أنشئ بوابة أخبار رقمية مع شريط عاجل، تبويبات للأخبار السياسية والتكنولوجية والرياضية، وأقسام للفيديو ومعرض الصور."
    ),
    EDUCATION(
        id = "education",
        titleAr = "منصة تعليمية",
        titleEn = "E-Learning Platform",
        titleFr = "Plateforme éducative",
        icon = "🎓",
        defaultPromptAr = "أنشئ موقعاً لمنصة دورات تدريبية وتعليمية، يحتوي على بطاقات الدورات، مسارات التعلم، ملف المدرب، ومحاكي تسجيل الطلاب."
    ),
    RESTAURANT(
        id = "restaurant",
        titleAr = "موقع مطعم ومقهى",
        titleEn = "Restaurant & Cafe",
        titleFr = "Restaurant & Café",
        icon = "🍽️",
        defaultPromptAr = "أنشئ موقعاً أنيقاً لمطعم ومقهى فاخر، يحتوي على قائمة الطعام والوجبات، أسعار، صور جذابة، ونظام حجز طاولة إلكتروني."
    ),
    REAL_ESTATE(
        id = "real_estate",
        titleAr = "موقع عقارات",
        titleEn = "Real Estate",
        titleFr = "Immobilier",
        icon = "🏡",
        defaultPromptAr = "أنشئ منصة وساطة عقارية لعرض الشقق والفلل مع ميزة البحث بالموقع والسعر، صور العقارات، وتفاصيل الاتصال بالوكيل."
    ),
    DASHBOARD(
        id = "dashboard",
        titleAr = "لوحة تحكم وإدارة",
        titleEn = "Admin Dashboard",
        titleFr = "Tableau de bord",
        icon = "📊",
        defaultPromptAr = "أنشئ لوحة تحكم إدارية تفاعلية تتضمن مؤشرات أداء، رسوم بيانية تفاعلية، جداول إدارة المستخدمين، وتنبيهات النظام."
    ),
    CUSTOM(
        id = "custom",
        titleAr = "موقع مخصص",
        titleEn = "Custom Web App",
        titleFr = "Application sur-mesure",
        icon = "⚡",
        defaultPromptAr = "أنشئ تطبيق ويب تفاعلي حديث ومتجاوب بالكامل مع كافة الشاشات."
    );

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.AR -> titleAr
        AppLanguage.FR -> titleFr
        AppLanguage.EN -> titleEn
    }

    companion object {
        fun fromId(id: String?): ProjectType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: E_COMMERCE
        }
    }
}
