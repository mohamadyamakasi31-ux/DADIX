package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WebAiViewModel
import com.example.ui.screens.DadixWebAiScreen
import com.example.ui.theme.DadixAiTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: WebAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()
            val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
            val providerStatus by viewModel.providerStatus.collectAsStateWithLifecycle()

            val projects by viewModel.allProjects.collectAsStateWithLifecycle()
            val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
            val files by viewModel.projectFiles.collectAsStateWithLifecycle()
            val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
            val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
            val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
            val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
            val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
            val isDeploying by viewModel.isDeploying.collectAsStateWithLifecycle()
            val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
            val previewDevice by viewModel.previewDevice.collectAsStateWithLifecycle()
            val previewReloadTrigger by viewModel.previewReloadTrigger.collectAsStateWithLifecycle()

            val customApiKey = viewModel.preferences.getCustomApiKey()
            val cfAccount = viewModel.preferences.getCloudflareAccountId()
            val cfToken = viewModel.preferences.getCloudflareApiToken()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.snackbarMessage.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                DadixAiTheme(themeMode = themeMode) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { _ ->
                        DadixWebAiScreen(
                            projects = projects,
                            activeProject = activeProject,
                            files = files,
                            activeFile = activeFile,
                            editorContent = editorContent,
                            chatMessages = chatMessages,
                            terminalLogs = terminalLogs,
                            isGenerating = isGenerating,
                            isDeploying = isDeploying,
                            activeTab = activeTab,
                            previewDevice = previewDevice,
                            previewReloadTrigger = previewReloadTrigger,
                            selectedModel = selectedModel,
                            providerStatus = providerStatus,
                            themeMode = themeMode,
                            language = language,
                            customApiKey = customApiKey,
                            cfAccount = cfAccount,
                            cfToken = cfToken,
                            onSelectProject = { id -> viewModel.selectProject(id) },
                            onCreateProject = { name, desc, type, stack, lang, prompt ->
                                viewModel.createProject(name, desc, type, stack, lang, prompt)
                            },
                            onSelectFile = { file -> viewModel.selectFile(file) },
                            onEditorContentChange = { text -> viewModel.onEditorContentChanged(text) },
                            onSaveFile = { viewModel.saveActiveFile() },
                            onCreateFile = { path -> viewModel.createFile(path) },
                            onDeleteFile = { path -> viewModel.deleteFile(path) },
                            onRenameFile = { old, new -> viewModel.renameFile(old, new) },
                            onSendChatInstruction = { inst -> viewModel.sendChatInstruction(inst) },
                            onExecuteTerminalCommand = { cmd -> viewModel.runTerminalCommand(cmd) },
                            onExportZip = { viewModel.exportZip(this@MainActivity) },
                            onDeployCloudflare = { viewModel.deployToCloudflare() },
                            onSetActiveTab = { tab -> viewModel.setActiveTab(tab) },
                            onSetPreviewDevice = { dev -> viewModel.setPreviewDevice(dev) },
                            onReloadPreview = { viewModel.reloadPreview() },
                            onSelectModel = { model -> viewModel.selectModel(model) },
                            onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                            onSaveApiKey = { key -> viewModel.saveApiKey(key) },
                            onSaveCloudflareConfig = { acc, tok -> viewModel.saveCloudflareConfig(acc, tok) }
                        )
                    }
                }
            }
        }
    }
}
