package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectChatMessageEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectFileEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.ProviderStatus
import com.example.data.model.TerminalLog
import com.example.ui.DeviceFrame
import com.example.ui.WorkspaceTab
import com.example.ui.components.AiChatPane
import com.example.ui.components.CodeEditorPane
import com.example.ui.components.FileExplorerPane
import com.example.ui.components.LivePreviewPane
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.TerminalPane
import com.example.ui.components.WebSettingsDialog
import com.example.ui.theme.DadixCyan
import com.example.ui.theme.DadixEmerald
import com.example.ui.theme.DadixPurple
import com.example.ui.theme.DadixPurpleDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DadixWebAiScreen(
    projects: List<ProjectEntity>,
    activeProject: ProjectEntity?,
    files: List<ProjectFileEntity>,
    activeFile: ProjectFileEntity?,
    editorContent: String,
    chatMessages: List<ProjectChatMessageEntity>,
    terminalLogs: List<TerminalLog>,
    isGenerating: Boolean,
    isDeploying: Boolean,
    activeTab: WorkspaceTab,
    previewDevice: DeviceFrame,
    previewReloadTrigger: Int,
    selectedModel: AiModel,
    providerStatus: ProviderStatus,
    themeMode: AppThemeMode,
    language: AppLanguage,
    customApiKey: String,
    cfAccount: String,
    cfToken: String,
    onSelectProject: (Long) -> Unit,
    onCreateProject: (name: String, desc: String, type: com.example.data.model.ProjectType, stack: com.example.data.model.TechStack, lang: AppLanguage, prompt: String) -> Unit,
    onSelectFile: (ProjectFileEntity) -> Unit,
    onEditorContentChange: (String) -> Unit,
    onSaveFile: () -> Unit,
    onCreateFile: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRenameFile: (String, String) -> Unit,
    onSendChatInstruction: (String) -> Unit,
    onExecuteTerminalCommand: (String) -> Unit,
    onExportZip: () -> Unit,
    onDeployCloudflare: () -> Unit,
    onSetActiveTab: (WorkspaceTab) -> Unit,
    onSetPreviewDevice: (DeviceFrame) -> Unit,
    onReloadPreview: () -> Unit,
    onSelectModel: (AiModel) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveCloudflareConfig: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showProjectDropdown by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dadix Web AI Logo Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(DadixPurple, DadixCyan))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Dadix Web AI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Dadix Web AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "AI Website IDE",
                                style = MaterialTheme.typography.labelSmall,
                                color = DadixPurple,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Project Selector Pill
                        Box {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showProjectDropdown = true }
                                    .testTag("project_selector_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = activeProject?.name ?: "اختر مشروعاً",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showProjectDropdown,
                                onDismissRequest = { showProjectDropdown = false }
                            ) {
                                projects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text(proj.name, fontWeight = if (proj.id == activeProject?.id) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            showProjectDropdown = false
                                            onSelectProject(proj.id)
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ مشروع جديد بالذكاء الاصطناعي", color = DadixPurple, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = DadixPurple) },
                                    onClick = {
                                        showProjectDropdown = false
                                        showNewProjectDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Export ZIP button
                    IconButton(
                        onClick = onExportZip,
                        modifier = Modifier.testTag("main_export_zip_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download ZIP",
                            tint = DadixCyan
                        )
                    }

                    // Deploy to Cloudflare button
                    Button(
                        onClick = onDeployCloudflare,
                        enabled = !isDeploying,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeProject?.deploymentStatus == "DEPLOYED") DadixEmerald else DadixPurple,
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("main_deploy_btn")
                    ) {
                        if (isDeploying) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (activeProject?.deploymentStatus == "DEPLOYED") "منشور" else "نشر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Settings button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("main_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Workspace Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DadixPurple,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (activeTab.ordinal < tabPositions.size) {
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = DadixPurple
                        )
                    }
                }
            ) {
                Tab(
                    selected = activeTab == WorkspaceTab.PREVIEW,
                    onClick = { onSetActiveTab(WorkspaceTab.PREVIEW) },
                    text = { Text("المعاينة الحية", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == WorkspaceTab.EDITOR,
                    onClick = { onSetActiveTab(WorkspaceTab.EDITOR) },
                    text = { Text("محرر الكود", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == WorkspaceTab.CHAT,
                    onClick = { onSetActiveTab(WorkspaceTab.CHAT) },
                    text = { Text("مساعد الذكاء الاصطناعي", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == WorkspaceTab.FILES,
                    onClick = { onSetActiveTab(WorkspaceTab.FILES) },
                    text = { Text("الملفات (${files.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == WorkspaceTab.TERMINAL,
                    onClick = { onSetActiveTab(WorkspaceTab.TERMINAL) },
                    text = { Text("الطرفية", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Deployment banner if deployed
            if (activeProject?.deploymentStatus == "DEPLOYED" && !activeProject.deploymentUrl.isNullOrBlank()) {
                Surface(
                    color = DadixEmerald.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DadixEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "الموقع منشور على Cloudflare: ${activeProject.deploymentUrl}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DadixEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Workspace Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    WorkspaceTab.PREVIEW -> {
                        LivePreviewPane(
                            files = files,
                            deviceFrame = previewDevice,
                            reloadTrigger = previewReloadTrigger,
                            onDeviceFrameChange = onSetPreviewDevice,
                            onReload = onReloadPreview
                        )
                    }
                    WorkspaceTab.EDITOR -> {
                        CodeEditorPane(
                            activeFile = activeFile,
                            editorContent = editorContent,
                            language = language,
                            onContentChange = onEditorContentChange,
                            onSave = onSaveFile
                        )
                    }
                    WorkspaceTab.CHAT -> {
                        AiChatPane(
                            messages = chatMessages,
                            isGenerating = isGenerating,
                            selectedModel = selectedModel,
                            language = language,
                            onSendMessage = onSendChatInstruction
                        )
                    }
                    WorkspaceTab.FILES -> {
                        FileExplorerPane(
                            files = files,
                            activeFile = activeFile,
                            language = language,
                            onFileSelected = { file ->
                                onSelectFile(file)
                                onSetActiveTab(WorkspaceTab.EDITOR)
                            },
                            onCreateFile = onCreateFile,
                            onDeleteFile = onDeleteFile,
                            onRenameFile = onRenameFile
                        )
                    }
                    WorkspaceTab.TERMINAL -> {
                        TerminalPane(
                            logs = terminalLogs,
                            onExecuteCommand = onExecuteTerminalCommand
                        )
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            language = language,
            onDismissRequest = { showNewProjectDialog = false },
            onCreateProject = { name, desc, type, stack, lang, prompt ->
                onCreateProject(name, desc, type, stack, lang, prompt)
                onSetActiveTab(WorkspaceTab.PREVIEW)
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        WebSettingsDialog(
            selectedModel = selectedModel,
            providerStatus = providerStatus,
            themeMode = themeMode,
            language = language,
            currentApiKey = customApiKey,
            currentCfAccount = cfAccount,
            currentCfToken = cfToken,
            onSelectModel = onSelectModel,
            onSetThemeMode = onSetThemeMode,
            onSetLanguage = onSetLanguage,
            onSaveApiKey = onSaveApiKey,
            onSaveCloudflare = onSaveCloudflareConfig,
            onDismissRequest = { showSettingsDialog = false }
        )
    }
}
