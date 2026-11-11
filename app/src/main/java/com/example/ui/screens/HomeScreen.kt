package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.ProviderStatus
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.components.ProviderNoticeCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickPromptItem(
    val icon: ImageVector,
    val titleAr: String,
    val titleEn: String,
    val titleFr: String,
    val promptAr: String,
    val promptEn: String,
    val promptFr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    conversations: List<ConversationEntity>,
    selectedModel: AiModel,
    providerStatus: ProviderStatus,
    language: AppLanguage,
    customApiKey: String,
    onOpenDrawer: () -> Unit,
    onStartNewChat: () -> Unit,
    onOpenConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onSendQuickPrompt: (String) -> Unit,
    onSelectModel: (AiModel) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showModelDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<Long?>(null) }

    val quickPrompts = remember {
        listOf(
            QuickPromptItem(
                icon = Icons.Default.Psychology,
                titleAr = "تبسيط الحوسبة",
                titleEn = "Cloud Basics",
                titleFr = "Le Cloud",
                promptAr = "اشرح مفهوم الحوسبة السحابية وكيف تعمل بأسلوب مبسط ومناسب للمبتدئين.",
                promptEn = "Explain cloud computing and how it works in simple beginner-friendly terms.",
                promptFr = "Explique l'informatique en nuage (cloud computing) de façon simple pour débutants."
            ),
            QuickPromptItem(
                icon = Icons.Default.Code,
                titleAr = "كود Kotlin",
                titleEn = "Kotlin Code",
                titleFr = "Code Kotlin",
                promptAr = "اكتب لي دالة في Kotlin لتصفية وترتيب قائمة مستخدمين حسب العمر مع شرح الخطوات.",
                promptEn = "Write a Kotlin function to filter and sort a list of users by age with step-by-step comments.",
                promptFr = "Écris une fonction Kotlin pour filtrer et trier une liste d'utilisateurs par âge avec explications."
            ),
            QuickPromptItem(
                icon = Icons.Default.Email,
                titleAr = "بريد مهني",
                titleEn = "Follow-up Email",
                titleFr = "Email Pro",
                promptAr = "صغ بريداً إلكترونياً رسمياً ومهنياً لمتابعة نتائج اجتماع شراكة عمل حديث.",
                promptEn = "Draft a polite, professional follow-up email after a strategic business partnership meeting.",
                promptFr = "Rédige un email professionnel et courtois pour faire le suivi d'une réunion de partenariat."
            ),
            QuickPromptItem(
                icon = Icons.Default.AutoAwesome,
                titleAr = "قواعد UX",
                titleEn = "UX Rules",
                titleFr = "Règles UX",
                promptAr = "لخص أهم 5 قواعد في تصميم تجربة المستخدم لتطبيقات الهواتف الذكية مع أمثلة عملية.",
                promptEn = "Summarize top 5 UX design golden rules for mobile applications with practical examples.",
                promptFr = "Résume les 5 règles d'or du design d'interface mobile avec des exemples concrets."
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(CyberCyan, ElectricViolet))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Dadix AI",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dadix AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("home_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer"
                        )
                    }
                },
                actions = {
                    // Active Model selector chip button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showModelDialog = true }
                            .padding(end = 6.dp)
                            .testTag("home_model_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedModel.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Settings button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
            // 1. Provider Notice Card
            item {
                ProviderNoticeCard(
                    providerStatus = providerStatus,
                    language = language,
                    onConfigureClicked = { showApiKeyDialog = true }
                )
            }

            // 2. Hero Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(CyberCyan, ElectricViolet))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val heroTitle = when (language) {
                            AppLanguage.AR -> "مرحباً بك في Dadix AI"
                            AppLanguage.FR -> "Bienvenue sur Dadix AI"
                            AppLanguage.EN -> "Welcome to Dadix AI"
                        }
                        Text(
                            text = heroTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val heroSubtitle = when (language) {
                            AppLanguage.AR -> "مساعدك الذكي لإنجاز المهام، البرمجة، وصياغة المحتوى بأحدث نماذج Gemini مع حفظ محلي تام لبياناتك."
                            AppLanguage.FR -> "Votre assistant IA intelligent pour la productivité et le code, avec stockage local sécurisé."
                            AppLanguage.EN -> "Your intelligent assistant for tasks, coding and writing, with 100% private local storage."
                        }
                        Text(
                            text = heroSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Start New Chat button
                        Button(
                            onClick = onStartNewChat,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("home_new_chat_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            val btnText = when (language) {
                                AppLanguage.AR -> "بدء محادثة جديدة"
                                AppLanguage.FR -> "Démarrer une discussion"
                                AppLanguage.EN -> "Start New Conversation"
                            }
                            Text(text = btnText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // 3. Active Model Showcase Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showModelDialog = true }
                        .testTag("home_active_model_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedModel.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedModel.speedTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = selectedModel.getBadge(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            val changeText = when (language) {
                                AppLanguage.AR -> "تغيير"
                                AppLanguage.FR -> "Changer"
                                AppLanguage.EN -> "Change"
                            }
                            Text(
                                text = changeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Quick Prompts Section
            item {
                val quickPromptsHeader = when (language) {
                    AppLanguage.AR -> "أفكار مقترحة للبدء"
                    AppLanguage.FR -> "Suggestions de démarrage"
                    AppLanguage.EN -> "Starter Suggestions"
                }
                Text(
                    text = quickPromptsHeader,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickPrompts) { item ->
                        val itemTitle = when (language) {
                            AppLanguage.AR -> item.titleAr
                            AppLanguage.FR -> item.titleFr
                            AppLanguage.EN -> item.titleEn
                        }
                        val itemPrompt = when (language) {
                            AppLanguage.AR -> item.promptAr
                            AppLanguage.FR -> item.promptFr
                            AppLanguage.EN -> item.promptEn
                        }

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSendQuickPrompt(itemPrompt) }
                                .testTag("quick_prompt_${item.titleEn}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = itemTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = itemPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 5. Recent Conversations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val recentHeader = when (language) {
                        AppLanguage.AR -> "المحادثات الأخيرة"
                        AppLanguage.FR -> "Discussions récentes"
                        AppLanguage.EN -> "Recent Conversations"
                    }
                    Text(
                        text = recentHeader,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (conversations.isNotEmpty()) {
                        Text(
                            text = "${conversations.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (conversations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val emptyText = when (language) {
                                AppLanguage.AR -> "لا توجد محادثات مخزنة محلياً بعد."
                                AppLanguage.FR -> "Aucune conversation enregistrée localement pour le moment."
                                AppLanguage.EN -> "No locally stored conversations yet."
                            }
                            Text(
                                text = emptyText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(conversations.take(6), key = { it.id }) { conv ->
                    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                    val formattedDate = remember(conv.updatedAt) {
                        dateFormat.format(Date(conv.updatedAt))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenConversation(conv.id) }
                            .testTag("recent_chat_card_${conv.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (conv.snippet.isNotBlank()) {
                                    Text(
                                        text = conv.snippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { conversationToDelete = conv.id },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs
    if (showModelDialog) {
        ModelSelectorDialog(
            selectedModel = selectedModel,
            language = language,
            onModelSelected = onSelectModel,
            onDismissRequest = { showModelDialog = false }
        )
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

    conversationToDelete?.let { id ->
        val confirmTitle = when (language) {
            AppLanguage.AR -> "حذف المحادثة"
            AppLanguage.FR -> "Supprimer la discussion"
            AppLanguage.EN -> "Delete Conversation"
        }
        val confirmMsg = when (language) {
            AppLanguage.AR -> "هل أنت متأكد من حذف هذه المحادثة بالكامل من هاتفك؟"
            AppLanguage.FR -> "Voulez-vous supprimer cette discussion de votre appareil ?"
            AppLanguage.EN -> "Are you sure you want to delete this conversation from your phone?"
        }
        DeleteConfirmDialog(
            title = confirmTitle,
            message = confirmMsg,
            language = language,
            onConfirm = {
                onDeleteConversation(id)
                conversationToDelete = null
            },
            onDismissRequest = { conversationToDelete = null }
        )
    }
}
