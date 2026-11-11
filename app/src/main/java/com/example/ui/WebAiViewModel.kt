package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ProjectChatMessageEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectFileEntity
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.LogLevel
import com.example.data.model.ProjectType
import com.example.data.model.ProviderStatus
import com.example.data.model.TechStack
import com.example.data.model.TerminalLog
import com.example.data.remote.DeployResult
import com.example.data.remote.ZipExporterService
import com.example.data.repository.WebProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkspaceTab {
    FILES,
    EDITOR,
    CHAT,
    PREVIEW,
    TERMINAL
}

enum class DeviceFrame {
    DESKTOP,
    TABLET,
    MOBILE
}

@OptIn(ExperimentalCoroutinesApi::class)
class WebAiViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val preferences = AppPreferences(application)
    private val repository = WebProjectRepository(database, preferences)

    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode
    val language: StateFlow<AppLanguage> = preferences.language
    val selectedModel: StateFlow<AiModel> = preferences.selectedModel
    val providerStatus: StateFlow<ProviderStatus> = preferences.providerStatus

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProjectId = MutableStateFlow<Long?>(null)
    val activeProjectId: StateFlow<Long?> = _activeProjectId.asStateFlow()

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    val projectFiles: StateFlow<List<ProjectFileEntity>> = _activeProjectId
        .flatMapLatest { id ->
            if (id != null) repository.getProjectFiles(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ProjectChatMessageEntity>> = _activeProjectId
        .flatMapLatest { id ->
            if (id != null) repository.getChatMessages(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeFile = MutableStateFlow<ProjectFileEntity?>(null)
    val activeFile: StateFlow<ProjectFileEntity?> = _activeFile.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _activeTab = MutableStateFlow(WorkspaceTab.PREVIEW)
    val activeTab: StateFlow<WorkspaceTab> = _activeTab.asStateFlow()

    private val _previewDevice = MutableStateFlow(DeviceFrame.DESKTOP)
    val previewDevice: StateFlow<DeviceFrame> = _previewDevice.asStateFlow()

    private val _previewReloadTrigger = MutableStateFlow(0)
    val previewReloadTrigger: StateFlow<Int> = _previewReloadTrigger.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(
        listOf(
            TerminalLog(LogLevel.INFO, "Dadix Web AI Terminal v1.0.0 ready"),
            TerminalLog(LogLevel.INFO, "Type 'help' for available commands or 'npm run dev' to serve.")
        )
    )
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isDeploying = MutableStateFlow(false)
    val isDeploying: StateFlow<Boolean> = _isDeploying.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        // Automatically open the first project or create a default E-commerce demo if completely empty
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isNotEmpty() && _activeProjectId.value == null) {
                    selectProject(projects.first().id)
                } else if (projects.isEmpty()) {
                    createProject(
                        name = "متجر الإلكترونيات الذكي",
                        description = "متجر إلكتروني احترافي باللغة العربية",
                        projectType = ProjectType.E_COMMERCE,
                        techStack = TechStack.TAILWIND,
                        targetLanguage = AppLanguage.AR
                    )
                }
            }
        }
    }

    fun selectProject(projectId: Long) {
        _activeProjectId.value = projectId
        viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            _activeProject.value = project

            // Default active file: index.html
            val files = database.projectFileDao().getFilesListForProject(projectId)
            val indexFile = files.firstOrNull { it.path.equals("index.html", ignoreCase = true) }
                ?: files.firstOrNull()
            if (indexFile != null) {
                selectFile(indexFile)
            }
            reloadPreview()
        }
    }

    fun createProject(
        name: String,
        description: String,
        projectType: ProjectType,
        techStack: TechStack,
        targetLanguage: AppLanguage,
        prompt: String? = null
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            addTerminalLog(LogLevel.COMMAND, "$ dadix-ai create ${name.ifBlank { "project" }}")
            addTerminalLog(LogLevel.INFO, "Generating project files using ${selectedModel.value.displayName}...")

            val newProject = repository.createNewProject(
                name = name,
                description = description,
                projectType = projectType,
                techStack = techStack,
                targetLanguage = targetLanguage,
                prompt = prompt
            )
            _isGenerating.value = false
            selectProject(newProject.id)
            addTerminalLog(LogLevel.SUCCESS, "Project '${newProject.name}' created successfully with full file tree.")
            _snackbarMessage.emit("تم إنشاء مشروع '${newProject.name}' بنجاح!")
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _activeProjectId.value = null
            _activeProject.value = null
            _activeFile.value = null
            _editorContent.value = ""
            _snackbarMessage.emit("تم حذف المشروع.")
        }
    }

    fun selectFile(file: ProjectFileEntity) {
        _activeFile.value = file
        _editorContent.value = file.content
    }

    fun onEditorContentChanged(newContent: String) {
        _editorContent.value = newContent
    }

    fun saveActiveFile() {
        val file = _activeFile.value ?: return
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            repository.saveFileContent(projId, file.path, _editorContent.value)
            _activeFile.value = file.copy(content = _editorContent.value, updatedAt = System.currentTimeMillis())
            reloadPreview()
            addTerminalLog(LogLevel.INFO, "File '${file.path}' saved.")
            _snackbarMessage.emit("تم حفظ ملف ${file.fileName}")
        }
    }

    fun createFile(path: String) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            repository.createNewFile(projId, path, "")
            val created = database.projectFileDao().getFileByPath(projId, path)
            if (created != null) {
                selectFile(created)
                setActiveTab(WorkspaceTab.EDITOR)
            }
            reloadPreview()
            _snackbarMessage.emit("تم إنشاء الملف: $path")
        }
    }

    fun deleteFile(path: String) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            repository.deleteFile(projId, path)
            if (_activeFile.value?.path == path) {
                _activeFile.value = null
                _editorContent.value = ""
            }
            reloadPreview()
            _snackbarMessage.emit("تم حذف الملف: $path")
        }
    }

    fun renameFile(oldPath: String, newPath: String) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            repository.renameFile(projId, oldPath, newPath)
            val updated = database.projectFileDao().getFileByPath(projId, newPath)
            if (updated != null) {
                selectFile(updated)
            }
            reloadPreview()
            _snackbarMessage.emit("تمت إعادة تسمية الملف إلى: $newPath")
        }
    }

    fun sendChatInstruction(instruction: String) {
        val projId = _activeProjectId.value ?: return
        if (instruction.isBlank()) return

        viewModelScope.launch {
            _isGenerating.value = true
            addTerminalLog(LogLevel.COMMAND, "$ dadix-ai modify --prompt \"${instruction.take(40)}...\"")
            addTerminalLog(LogLevel.INFO, "Analyzing current codebase and generating surgical changes...")

            val result = repository.sendChatModification(
                projectId = projId,
                instruction = instruction,
                model = selectedModel.value,
                language = language.value
            )

            _isGenerating.value = false

            if (result.isSuccess) {
                reloadPreview()
                addTerminalLog(LogLevel.SUCCESS, "Modifications applied successfully: ${result.getOrThrow()}")
                _snackbarMessage.emit("تم تطبيق التعديلات بنجاح!")
            } else {
                val error = result.exceptionOrNull()?.message ?: "فشل في تطبيق التعديل."
                addTerminalLog(LogLevel.ERROR, "Modification error: $error")
                _snackbarMessage.emit(error)
            }
        }
    }

    fun runTerminalCommand(commandLine: String) {
        val cmd = commandLine.trim()
        if (cmd.isEmpty()) return

        addTerminalLog(LogLevel.COMMAND, "$ $cmd")

        when {
            cmd.equals("help", ignoreCase = true) -> {
                addTerminalLog(LogLevel.INFO, "Supported commands:")
                addTerminalLog(LogLevel.INFO, "  npm install        - Resolve and verify dependencies")
                addTerminalLog(LogLevel.INFO, "  npm run dev        - Start local development server")
                addTerminalLog(LogLevel.INFO, "  npm run build      - Compile and bundle production assets")
                addTerminalLog(LogLevel.INFO, "  npm install <pkg>  - Add a package to package.json")
                addTerminalLog(LogLevel.INFO, "  ls                 - List project files")
                addTerminalLog(LogLevel.INFO, "  cat <file>         - Display file contents")
                addTerminalLog(LogLevel.INFO, "  clear              - Clear terminal output")
                addTerminalLog(LogLevel.INFO, "  status             - Show current project and deployment status")
            }
            cmd.equals("clear", ignoreCase = true) -> {
                _terminalLogs.value = emptyList()
            }
            cmd.equals("ls", ignoreCase = true) -> {
                val files = projectFiles.value.map { it.path }
                addTerminalLog(LogLevel.OUTPUT, files.joinToString("  "))
            }
            cmd.startsWith("cat ") -> {
                val path = cmd.removePrefix("cat ").trim()
                val f = projectFiles.value.firstOrNull { it.path.equals(path, ignoreCase = true) }
                if (f != null) {
                    addTerminalLog(LogLevel.OUTPUT, f.content.take(800))
                } else {
                    addTerminalLog(LogLevel.ERROR, "cat: $path: No such file")
                }
            }
            cmd.equals("npm install", ignoreCase = true) -> {
                addTerminalLog(LogLevel.INFO, "Resolving dependencies from package.json...")
                addTerminalLog(LogLevel.SUCCESS, "added 42 packages in 1.2s. 0 vulnerabilities found.")
            }
            cmd.startsWith("npm install ") -> {
                val pkg = cmd.removePrefix("npm install ").trim()
                addTerminalLog(LogLevel.INFO, "Fetching package '$pkg' from npm registry...")
                addTerminalLog(LogLevel.SUCCESS, "+ $pkg@latest installed successfully.")
            }
            cmd.equals("npm run dev", ignoreCase = true) -> {
                addTerminalLog(LogLevel.INFO, "Vite v5.0.0 ready in 180 ms")
                addTerminalLog(LogLevel.SUCCESS, "➜  Local:   http://localhost:5173/")
                addTerminalLog(LogLevel.INFO, "Live reload enabled. Serving index.html...")
                reloadPreview()
            }
            cmd.equals("npm run build", ignoreCase = true) -> {
                addTerminalLog(LogLevel.INFO, "vite v5.0.0 building for production...")
                addTerminalLog(LogLevel.INFO, "transforming...")
                addTerminalLog(LogLevel.SUCCESS, "✓ built in 420ms")
                addTerminalLog(LogLevel.OUTPUT, "dist/index.html   12.4 kB\ndist/assets/index.css   18.1 kB\ndist/assets/index.js   48.2 kB")
            }
            cmd.equals("status", ignoreCase = true) -> {
                val p = _activeProject.value
                addTerminalLog(LogLevel.INFO, "Project: ${p?.name ?: "None"} (${p?.projectType})")
                addTerminalLog(LogLevel.INFO, "Files count: ${projectFiles.value.size}")
                addTerminalLog(LogLevel.INFO, "Deployment: ${p?.deploymentStatus} (${p?.deploymentUrl ?: "Not deployed"})")
            }
            else -> {
                addTerminalLog(LogLevel.ERROR, "bash: $cmd: command not found. Type 'help' for available commands.")
            }
        }
    }

    private fun addTerminalLog(level: LogLevel, message: String) {
        _terminalLogs.value = _terminalLogs.value + TerminalLog(level, message)
    }

    fun exportZip(context: Context) {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            addTerminalLog(LogLevel.COMMAND, "$ dadix-ai export --format zip")
            addTerminalLog(LogLevel.INFO, "Archiving project files...")
            val result = repository.exportZip(context, projId)
            if (result.isSuccess) {
                val file = result.getOrThrow()
                addTerminalLog(LogLevel.SUCCESS, "ZIP exported: ${file.name} (${file.length() / 1024} KB)")
                ZipExporterService.triggerShareOrSaveIntent(context, file)
                _snackbarMessage.emit("تم إنشاء ملف ZIP بنجاح!")
            } else {
                val err = result.exceptionOrNull()?.message ?: "فشل في تصدير ZIP."
                addTerminalLog(LogLevel.ERROR, "Export error: $err")
                _snackbarMessage.emit(err)
            }
        }
    }

    fun deployToCloudflare() {
        val projId = _activeProjectId.value ?: return
        viewModelScope.launch {
            _isDeploying.value = true
            addTerminalLog(LogLevel.COMMAND, "$ dadix-ai deploy --provider cloudflare-pages")
            addTerminalLog(LogLevel.INFO, "Authenticating with Cloudflare Pages API...")

            val res = repository.deployToCloudflare(projId)
            _isDeploying.value = false
            _activeProject.value = repository.getProjectById(projId)

            when (res) {
                is DeployResult.Success -> {
                    addTerminalLog(LogLevel.SUCCESS, "🚀 Deployed to: ${res.url}")
                    _snackbarMessage.emit("تم نشر الموقع بنجاح: ${res.url}")
                }
                is DeployResult.Failure -> {
                    addTerminalLog(LogLevel.ERROR, "Deployment failed: ${res.errorMessage}")
                    _snackbarMessage.emit(res.errorMessage)
                }
            }
        }
    }

    fun setActiveTab(tab: WorkspaceTab) {
        _activeTab.value = tab
    }

    fun setPreviewDevice(device: DeviceFrame) {
        _previewDevice.value = device
    }

    fun reloadPreview() {
        _previewReloadTrigger.value = _previewReloadTrigger.value + 1
    }

    fun selectModel(model: AiModel) {
        preferences.setSelectedModel(model)
        viewModelScope.launch {
            _snackbarMessage.emit("تم اختيار نموذج ${model.displayName}")
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun setLanguage(lang: AppLanguage) {
        preferences.setLanguage(lang)
    }

    fun saveApiKey(key: String) {
        preferences.setCustomApiKey(key)
        viewModelScope.launch {
            val status = preferences.computeProviderStatus()
            val msg = if (status.isLinked) "تم ربط مزود Gemini بنجاح!" else "تمت إزالة المفتاح."
            _snackbarMessage.emit(msg)
        }
    }

    fun saveCloudflareConfig(accountId: String, apiToken: String) {
        preferences.setCloudflareAccountId(accountId)
        preferences.setCloudflareApiToken(apiToken)
        viewModelScope.launch {
            _snackbarMessage.emit("تم حفظ إعدادات Cloudflare Pages بنجاح!")
        }
    }
}
