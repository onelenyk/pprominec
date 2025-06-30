package dev.onelenyk.pprominec.presentation.components.main

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Enum for supported file types
 */
enum class SupportedFileType(val extension: String) {
    MBTILES("mbtiles"),
    NOT_SUPPORTED(""),
    ;

    companion object {
        fun fromExtension(extension: String): SupportedFileType {
            return SupportedFileType.entries.find { it.extension == extension } ?: NOT_SUPPORTED
        }
    }
}

data class FileInfo(
    val uid: String,
    val type: SupportedFileType,
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
)

/**
 * Extension function to convert File to FileInfo with provided UID
 */
fun File.toFileInfo(uid: String, supportedFileType: SupportedFileType): FileInfo {
    return FileInfo(
        uid = uid,
        type = supportedFileType,
        name = name,
        path = absolutePath,
        size = length(),
        isDirectory = isDirectory,
        lastModified = lastModified(),
    )
}

/**
 * Extension function to create filename with UID, extra word, and extension
 */
fun String.createFileName(extraWord: String = "", extension: String = ""): String {
    return buildString {
        append(this@createFileName)

        if (extraWord.isNotEmpty()) {
            append("_")
            append(extraWord)
        }

        if (extension.isNotEmpty()) {
            append(".")
            append(extension)
        }
    }
}

/**
 * Generate a unique ID for a file
 */
private fun generateUid(): String {
    return UUID.randomUUID().toString()
}

class FileStorage(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) {
    private val mapFilesDir = File(context.filesDir, "osmdroid")
    private val _filesFlow = MutableStateFlow<List<FileInfo>>(emptyList())

    init {
        if (!mapFilesDir.exists()) {
            mapFilesDir.mkdirs()
        }
        // Initial load
        coroutineScope.launch {
            refreshFilesInternal()
        }
    }

    /**
     * Flow that emits the list of files whenever the directory changes
     */
    val files: Flow<List<FileInfo>> = _filesFlow.asStateFlow()

    /**
     * Manually refresh the files list
     */
    suspend fun refreshFiles(): Result<Unit> = withContext(coroutineScope.coroutineContext) {
        try {
            refreshFilesInternal()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Internal function to refresh files and update the flow
     */
    private suspend fun refreshFilesInternal() {
        val fileList = listFilesInternal()
        _filesFlow.value = fileList
    }

    /**
     * List all files in the map files directory (internal implementation)
     */
    private suspend fun listFilesInternal(): List<FileInfo> = withContext(coroutineScope.coroutineContext) {
        mapFilesDir.listFiles()?.map { file ->
            val uid = file.nameWithoutExtension
            val type = SupportedFileType.fromExtension(file.extension)
            file.toFileInfo(uid, type)
        } ?: emptyList()
    }

    /**
     * List all files in the map files directory
     */
    suspend fun listFiles(): List<FileInfo> = withContext(coroutineScope.coroutineContext) {
        listFilesInternal()
    }

    suspend fun saveFileFromUri(uri: Uri): Result<FileInfo> = withContext(coroutineScope.coroutineContext) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open input stream"))

            // Extract file extension from URI
            val fileName = uri.lastPathSegment ?: return@withContext Result.failure(Exception("Invalid file name"))
            val fileExtension = if (fileName.contains(".")) {
                fileName.substringAfterLast(".", "")
            } else {
                ""
            }

            // Determine supported file type from extension
            val supportedType = SupportedFileType.fromExtension(fileExtension)
            if (supportedType == SupportedFileType.NOT_SUPPORTED) {
                return@withContext Result.failure(Exception("Unsupported file type: $fileExtension"))
            }

            val uid = generateUid()
            val fileNameWithUid = uid.createFileName(extension = supportedType.extension)
            val destFile = File(mapFilesDir, fileNameWithUid)

            inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val fileInfo = destFile.toFileInfo(uid, supportedType)

            // Refresh the files flow after adding a new file
            refreshFilesInternal()

            Result.success(fileInfo)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a file by its UID
     */
    suspend fun deleteFile(uid: String): Result<Unit> = withContext(coroutineScope.coroutineContext) {
        try {
            // Find the file with the given UID
            val fileToDelete = mapFilesDir.listFiles()?.find { file ->
                file.nameWithoutExtension == uid
            }

            if (fileToDelete == null) {
                return@withContext Result.failure(Exception("File with UID $uid not found"))
            }

            if (fileToDelete.delete()) {
                // Refresh the files flow after deleting a file
                refreshFilesInternal()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete file: ${fileToDelete.name}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all files in the map files directory
     */
    suspend fun clearFolder(): Result<Unit> = withContext(coroutineScope.coroutineContext) {
        try {
            val files = mapFilesDir.listFiles()
            if (files == null || files.isEmpty()) {
                return@withContext Result.success(Unit)
            }

            var deletedCount = 0
            var failedCount = 0

            files.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                } else {
                    failedCount++
                }
            }

            if (failedCount > 0) {
                Result.failure(Exception("Failed to delete $failedCount out of ${files.size} files"))
            } else {
                // Refresh the files flow after clearing all files
                refreshFilesInternal()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
