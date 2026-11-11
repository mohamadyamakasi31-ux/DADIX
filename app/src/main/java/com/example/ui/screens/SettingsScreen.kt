package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeySource
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.ProviderStatus
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.DeleteConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedModel: AiModel,
    providerStatus: ProviderStatus,
    themeMode: AppThemeMode,
    language: AppLanguage,
    customApiKey: String,
    conversationCount: Int,
    onSelectModel: (AiModel) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearAllConversations: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val screenTitle = when (language) {
        AppLanguage.AR -> "الإعدادات والمزود"
        AppLanguage.FR -> "Paramètres & Fournisseur"
        AppLanguage.EN -> "Settings & Provider"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. Prominent Selected Model Section (User Requirement)
            // ==========================================
            item {
                val modelHeader = when (language) {
                    AppLanguage.AR -> "نموذج الذكاء الاصطناعي النشط"
                    AppLanguage.FR -> "Modèle d'IA actif"
                    AppLanguage.EN -> "Active AI Model"
                }
                Text(
                    text = modelHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val currentModelLabel = when (language) {
                                    AppLanguage.AR -> "النموذج المحدد للدردشة:"
                                    AppLanguage.FR -> "Modèle sélectionné pour le chat :"
                                    AppLanguage.EN -> "Selected Chat Model:"
                                }
                                Text(
                                    text = currentModelLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedModel.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedModel.speedTag,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedModel.getDescription(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Model selection list
                        val chooseModelPrompt = when (language) {
                            AppLanguage.AR -> "اختر النموذج المناسب لمهامك:"
                            AppLanguage.FR -> "Sélectionnez un modèle :"
                            AppLanguage.EN -> "Select an AI model:"
                        }
                        Text(
                            text = chooseModelPrompt,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AiModel.ALL.forEach { model ->
                                val isSelected = model == selectedModel
                                val itemBg = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                                val itemBorder = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(itemBg)
                                        .border(1.dp, itemBorder, RoundedCornerShape(10.dp))
                                        .clickable { onSelectModel(model) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("settings_model_option_${model.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = model.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = model.getBadge(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Text(
                                        text = model.speedTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. AI Provider Status Section (User Requirement)
            // ==========================================
            item {
                val providerHeader = when (language) {
                    AppLanguage.AR -> "حالة مزود الذكاء الاصطناعي"
                    AppLanguage.FR -> "État du fournisseur d'IA"
                    AppLanguage.EN -> "AI Provider Status"
                }
                Text(
                    text = providerHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                val isLinked = providerStatus.isLinked
                val statusContainerColor = if (isLinked) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = statusContainerColor),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isLinked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val linkStatusText = if (isLinked) {
                                when (language) {
                                    AppLanguage.AR -> "متصل (Google Gemini)"
                                    AppLanguage.FR -> "Connecté (Google Gemini)"
                                    AppLanguage.EN -> "Connected (Google Gemini)"
                                }
                            } else {
                                when (language) {
                                    AppLanguage.AR -> "غير متصل (لا يوجد مزود مرتبط)"
                                    AppLanguage.FR -> "Non connecté (aucun fournisseur)"
                                    AppLanguage.EN -> "Disconnected (No provider linked)"
                                }
                            }
                            Text(
                                text = linkStatusText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val explanation = if (isLinked) {
                            val sourceDesc = when (providerStatus.source) {
                                ApiKeySource.AI_STUDIO_SECRETS -> when (language) {
                                    AppLanguage.AR -> "المصدر: لوحة أسرار AI Studio (GEMINI_API_KEY)"
                                    AppLanguage.FR -> "Source : Panneau Secrets AI Studio (GEMINI_API_KEY)"
                                    AppLanguage.EN -> "Source: AI Studio Secrets (GEMINI_API_KEY)"
                                }
                                ApiKeySource.MANUAL_INPUT -> when (language) {
                                    AppLanguage.AR -> "المصدر: مفتاح مخصص تم إدخاله يدوياً"
                                    AppLanguage.FR -> "Source : Clé personnalisée manuelle"
                                    AppLanguage.EN -> "Source: Manual custom API key"
                                }
                                ApiKeySource.NONE -> ""
                            }
                            val keyInfo = if (providerStatus.maskedKey.isNotBlank()) " [${providerStatus.maskedKey}]" else ""
                            "$sourceDesc$keyInfo"
                        } else {
                            when (language) {
                                AppLanguage.AR -> "تطبيق Dadix AI تطبيق أصلي تماماً ولا يستخدم نصوصاً مصطنعة للإيهام بالعمل. لكي يجيب الذكاء الاصطناعي، يرجى ربط مفتاح API صالح."
                                AppLanguage.FR -> "Dadix AI est une vraie application qui ne génère aucune réponse factice. Pour discuter, associez une clé API valide."
                                AppLanguage.EN -> "Dadix AI is authentic and does not use fabricated responses. To enable chatting, please provide a valid API key."
                            }
                        }

                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showApiKeyDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("settings_config_provider_button")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val buttonLabel = if (isLinked) {
                                when (language) {
                                    AppLanguage.AR -> "تعديل أو فحص المفتاح"
                                    AppLanguage.FR -> "Modifier la clé"
                                    AppLanguage.EN -> "Edit / Inspect Key"
                                }
                            } else {
                                when (language) {
                                    AppLanguage.AR -> "إدخال مفتاح Gemini API الآن"
                                    AppLanguage.FR -> "Ajouter une clé Gemini"
                                    AppLanguage.EN -> "Configure Gemini API Key"
                                }
                            }
                            Text(text = buttonLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ==========================================
            // 3. Theme & Appearance
            // ==========================================
            item {
                val themeHeader = when (language) {
                    AppLanguage.AR -> "المظهر والتصميم"
                    AppLanguage.FR -> "Thème & Apparence"
                    AppLanguage.EN -> "Theme & Appearance"
                }
                Text(
                    text = themeHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(AppThemeMode.SYSTEM, Icons.Default.PhoneAndroid, "theme_system"),
                                Triple(AppThemeMode.LIGHT, Icons.Default.LightMode, "theme_light"),
                                Triple(AppThemeMode.DARK, Icons.Default.DarkMode, "theme_dark")
                            ).forEach { (mode, icon, tag) ->
                                val isSelected = mode == themeMode
                                val btnBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                val btnContent = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = btnBg,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSetThemeMode(mode) }
                                        .testTag(tag)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = btnContent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = mode.getDisplayName(language),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = btnContent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. Language Section (Arabic default, English, French)
            // ==========================================
            item {
                val langHeader = when (language) {
                    AppLanguage.AR -> "لغة التطبيق"
                    AppLanguage.FR -> "Langue de l'application"
                    AppLanguage.EN -> "Application Language"
                }
                Text(
                    text = langHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == language
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { onSetLanguage(lang) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("lang_option_${lang.code}")
                            ) {
                                Text(text = lang.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. Local Database & Chat Storage
            // ==========================================
            item {
                val storageHeader = when (language) {
                    AppLanguage.AR -> "التخزين المحلي وسجل المحادثات"
                    AppLanguage.FR -> "Stockage local & Historique"
                    AppLanguage.EN -> "Local Storage & History"
                }
                Text(
                    text = storageHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val convCountLabel = when (language) {
                                AppLanguage.AR -> "عدد المحادثات المخزنة محلياً:"
                                AppLanguage.FR -> "Discussions enregistrées localement :"
                                AppLanguage.EN -> "Locally stored conversations:"
                            }
                            Text(
                                text = "$convCountLabel $conversationCount",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val storagePrivacyNote = when (language) {
                            AppLanguage.AR -> "يتم تخزين المحادثات على قاعدة بيانات Room المدمجة في هاتفك فقط ولا ترفع إلى أي خادم خارجي."
                            AppLanguage.FR -> "Les conversations sont stockées exclusivement sur votre téléphone via Room Database."
                            AppLanguage.EN -> "Conversations are saved exclusively to your on-device Room database with zero cloud tracking."
                        }
                        Text(
                            text = storagePrivacyNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        if (conversationCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("clear_all_chats_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                val clearBtnText = when (language) {
                                    AppLanguage.AR -> "مسح كل المحادثات"
                                    AppLanguage.FR -> "Effacer toutes les discussions"
                                    AppLanguage.EN -> "Clear All Conversations"
                                }
                                Text(text = clearBtnText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. About Dadix AI
            // ==========================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dadix AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 1.0.0 • Native Android Compose",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val footerCopy = when (language) {
                            AppLanguage.AR -> "واجهة أصيلة، نماذج Gemini المتقدمة، وخصوصية تامة."
                            AppLanguage.FR -> "Expérience native, modèles Gemini avancés et confidentialité."
                            AppLanguage.EN -> "Native experience, advanced Gemini models and complete privacy."
                        }
                        Text(
                            text = footerCopy,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            providerStatus = providerStatus,
            currentCustomKey = customApiKey,
            language = language,
            onSaveKey = onSaveApiKey,
            onDismissRequest = { showApiKeyDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        val confirmTitle = when (language) {
            AppLanguage.AR -> "مسح كل المحادثات"
            AppLanguage.FR -> "Effacer tout l'historique"
            AppLanguage.EN -> "Clear All Conversations"
        }
        val confirmMsg = when (language) {
            AppLanguage.AR -> "هل أنت متأكد من مسح جميع المحادثات المخزنة محلياً؟ لا يمكن التراجع عن هذا الإجراء."
            AppLanguage.FR -> "Êtes-vous sûr de vouloir supprimer définitivement toutes les discussions ?"
            AppLanguage.EN -> "Are you sure you want to delete all locally stored conversations? This cannot be undone."
        }
        DeleteConfirmDialog(
            title = confirmTitle,
            message = confirmMsg,
            language = language,
            onConfirm = {
                onClearAllConversations()
                showClearConfirmDialog = false
            },
            onDismissRequest = { showClearConfirmDialog = false }
        )
    }
}
