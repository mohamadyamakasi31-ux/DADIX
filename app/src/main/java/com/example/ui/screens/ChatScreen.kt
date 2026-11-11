package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.ProviderStatus
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversation: ConversationEntity?,
    messages: List<MessageEntity>,
    isGenerating: Boolean,
    inputMessage: String,
    selectedModel: AiModel,
    providerStatus: ProviderStatus,
    language: AppLanguage,
    customApiKey: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onSelectModel: (AiModel) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConvDialog by remember { mutableStateOf(false) }

    // Auto scroll to bottom when new messages arrive or when generating starts
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation?.title ?: "Dadix AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showModelDialog = true }
                                    .testTag("chat_model_pill")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedModel.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("chat_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                val changeModelText = when (language) {
                                    AppLanguage.AR -> "تغيير النموذج (${selectedModel.displayName})"
                                    AppLanguage.FR -> "Changer le modèle (${selectedModel.displayName})"
                                    AppLanguage.EN -> "Switch Model (${selectedModel.displayName})"
                                }
                                Text(changeModelText)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Tune, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                showModelDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                val providerText = when (language) {
                                    AppLanguage.AR -> "إعداد مزود الذكاء الاصطناعي"
                                    AppLanguage.FR -> "Fournisseur d'IA"
                                    AppLanguage.EN -> "AI Provider Settings"
                                }
                                Text(providerText)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                showApiKeyDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                val deleteText = when (language) {
                                    AppLanguage.AR -> "حذف هذه المحادثة"
                                    AppLanguage.FR -> "Supprimer la discussion"
                                    AppLanguage.EN -> "Delete this conversation"
                                }
                                Text(deleteText, color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteConvDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Small warning if provider is not linked
                    if (!providerStatus.isLinked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clickable { showApiKeyDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val unlinkedNotice = when (language) {
                                    AppLanguage.AR -> "المزود غير مرتبط. اضغط لإدخال مفتاح Gemini API وتفعيل الردود الحقيقية."
                                    AppLanguage.FR -> "Fournisseur non associé. Cliquez pour ajouter votre clé Gemini."
                                    AppLanguage.EN -> "Provider unlinked. Tap to configure Gemini API key."
                                }
                                Text(
                                    text = unlinkedNotice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val inputPlaceholder = when (language) {
                            AppLanguage.AR -> "اكتب رسالتك لـ Dadix AI..."
                            AppLanguage.FR -> "Écrivez votre message à Dadix AI..."
                            AppLanguage.EN -> "Type your message to Dadix AI..."
                        }

                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = onInputChanged,
                            placeholder = {
                                Text(
                                    text = inputPlaceholder,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            maxLines = 5,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = onSendMessage,
                            enabled = inputMessage.isNotBlank() && !isGenerating,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("chat_send_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty() && !isGenerating) {
                // Empty state inside chat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
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
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        val readyTitle = when (language) {
                            AppLanguage.AR -> "Dadix AI جاهز للإجابة"
                            AppLanguage.FR -> "Dadix AI est prêt"
                            AppLanguage.EN -> "Dadix AI is ready"
                        }
                        Text(
                            text = readyTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val readyDesc = when (language) {
                            AppLanguage.AR -> "النموذج النشط: ${selectedModel.displayName}\nاكتب سؤالك، طلبك البرمجي، أو فكرتك في الحقل أدناه."
                            AppLanguage.FR -> "Modèle actif : ${selectedModel.displayName}\nPosez votre question ou demandez du code ci-dessous."
                            AppLanguage.EN -> "Active model: ${selectedModel.displayName}\nType your question, code request or task below."
                        }
                        Text(
                            text = readyDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            language = language,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Dadix AI Message", text)
                                clipboard.setPrimaryClip(clip)
                                val copiedMsg = when (language) {
                                    AppLanguage.AR -> "تم نسخ الرسالة إلى الحافظة"
                                    AppLanguage.FR -> "Message copié dans le presse-papiers"
                                    AppLanguage.EN -> "Message copied to clipboard"
                                }
                                onShowToast(copiedMsg)
                            },
                            onDelete = { messageToDelete = msg.id },
                            onConfigureKey = { showApiKeyDialog = true }
                        )
                    }

                    if (isGenerating) {
                        item {
                            ThinkingBubble(
                                model = selectedModel,
                                language = language
                            )
                        }
                    }
                }
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

    messageToDelete?.let { msgId ->
        val confirmTitle = when (language) {
            AppLanguage.AR -> "حذف الرسالة"
            AppLanguage.FR -> "Supprimer le message"
            AppLanguage.EN -> "Delete Message"
        }
        val confirmMsg = when (language) {
            AppLanguage.AR -> "هل أنت متأكد من حذف هذه الرسالة؟"
            AppLanguage.FR -> "Voulez-vous supprimer ce message ?"
            AppLanguage.EN -> "Are you sure you want to delete this message?"
        }
        DeleteConfirmDialog(
            title = confirmTitle,
            message = confirmMsg,
            language = language,
            onConfirm = {
                onDeleteMessage(msgId)
                messageToDelete = null
            },
            onDismissRequest = { messageToDelete = null }
        )
    }

    if (showDeleteConvDialog) {
        conversation?.let { conv ->
            val confirmTitle = when (language) {
                AppLanguage.AR -> "حذف المحادثة"
                AppLanguage.FR -> "Supprimer la discussion"
                AppLanguage.EN -> "Delete Conversation"
            }
            val confirmMsg = when (language) {
                AppLanguage.AR -> "هل تريد بالتأكيد حذف هذه المحادثة بالكامل؟"
                AppLanguage.FR -> "Voulez-vous supprimer cette discussion définitivement ?"
                AppLanguage.EN -> "Do you want to permanently delete this conversation?"
            }
            DeleteConfirmDialog(
                title = confirmTitle,
                message = confirmMsg,
                language = language,
                onConfirm = {
                    onDeleteConversation(conv.id)
                    showDeleteConvDialog = false
                },
                onDismissRequest = { showDeleteConvDialog = false }
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    language: AppLanguage,
    onCopy: (String) -> Unit,
    onDelete: () -> Unit,
    onConfigureKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val isError = message.isError

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) {
        timeFormat.format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isError) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Warning else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f, fill = false)) {
                // Header label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = if (isUser) {
                            when (language) {
                                AppLanguage.AR -> "أنت"
                                AppLanguage.FR -> "Vous"
                                AppLanguage.EN -> "You"
                            }
                        } else {
                            "Dadix AI"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isUser && message.modelUsed != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${message.modelUsed}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }

                // Bubble Card
                val bubbleColor = when {
                    isUser -> MaterialTheme.colorScheme.primary
                    isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }

                val textColor = when {
                    isUser -> MaterialTheme.colorScheme.onPrimary
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val bubbleShape = if (isUser) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                }

                Surface(
                    shape = bubbleShape,
                    color = bubbleColor,
                    border = if (isError) CardDefaults.outlinedCardBorder() else null,
                    modifier = Modifier.widthIn(min = 60.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SelectionContainer {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                lineHeight = 21.sp
                            )
                        }

                        if (isError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onConfigureKey,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val configText = when (language) {
                                    AppLanguage.AR -> "إعداد مفتاح Gemini"
                                    AppLanguage.FR -> "Configurer la clé"
                                    AppLanguage.EN -> "Configure Key"
                                }
                                Text(configText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bubble actions: Copy & Delete
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    IconButton(
                        onClick = { onCopy(message.content) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble(
    model: AiModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
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
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                val thinkingText = when (language) {
                    AppLanguage.AR -> "Dadix AI يفكّر بالاستجابة…"
                    AppLanguage.FR -> "Dadix AI réfléchit…"
                    AppLanguage.EN -> "Dadix AI is thinking…"
                }
                Text(
                    text = thinkingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha))
                )
            }
        }
    }
}
