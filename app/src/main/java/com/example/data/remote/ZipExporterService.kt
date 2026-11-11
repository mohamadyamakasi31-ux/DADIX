package com.example.data.remote

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporterService {

    suspend fun exportProjectToZip(
        context: Context,
        projectName: String,
        files: List<ProjectFileEntity>
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sanitizedName = projectName.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val zipFile = File(outputDir, "${sanitizedName}_dadix.zip")

            if (zipFile.exists()) {
                zipFile.delete()
            }

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in files) {
                    if (file.isFolder) continue
                    val entryName = file.path.removePrefix("/")
                    val zipEntry = ZipEntry(entryName)
                    zos.putNextEntry(zipEntry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }

            Result.success(zipFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun triggerShareOrSaveIntent(context: Context, zipFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "تصدير مشروع ${zipFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "تنزيل أو مشاركة ملف ZIP").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
