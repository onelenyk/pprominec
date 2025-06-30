package dev.onelenyk.pprominec.presentation.components.main

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun Uri.validateFileType(): Result<Unit> {
    val fileName = lastPathSegment ?: return Result.failure(Exception("Invalid file name"))

    val fileExtension = if (fileName.contains(".")) {
        fileName.substringAfterLast(".", "")
    } else {
        ""
    }

    val supportedType = SupportedFileType.fromExtension(fileExtension)
    return if (supportedType == SupportedFileType.NOT_SUPPORTED) {
        Result.failure(Exception("Unsupported file type: $fileExtension"))
    } else {
        Result.success(Unit)
    }
}

class FileManager(
    private val context: Context,
    private val fileStorage: FileStorage,
    private val coroutineScope: CoroutineScope,
) {
    val files: Flow<List<FileInfo>> = fileStorage.files

    suspend fun processSelectedFile(uri: Uri): Result<FileInfo> {
        return try {
            // Validate file type using extension function
            uri.validateFileType().getOrThrow()

            // Process the file using the new saveFileFromUri function
            fileStorage.saveFileFromUri(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(uid: String): Result<Unit> {
        return try {
            fileStorage.deleteFile(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearFolder(): Result<Unit> {
        return try {
            fileStorage.clearFolder()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshFiles(): Result<Unit> {
        return try {
            fileStorage.refreshFiles()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
