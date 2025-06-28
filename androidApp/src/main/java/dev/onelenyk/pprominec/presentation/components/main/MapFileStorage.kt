package dev.onelenyk.pprominec.presentation.components.main

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class MapFileStorage(private val context: Context) {
    private val mapFilesDir = File(context.filesDir, "map_files")

    private suspend fun getOrCreateMapFilesDir(): File =
        withContext(Dispatchers.IO) {
            if (!mapFilesDir.exists()) {
                mapFilesDir.mkdirs()
            }
            mapFilesDir
        }

    // Copy from InputStream to storage with a given file name
    suspend fun copyInputStreamToStorage(
        inputStream: InputStream,
        fileName: String,
    ): File =
        withContext(Dispatchers.IO) {
            val file = File(getOrCreateMapFilesDir(), fileName)
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        }

    suspend fun listStoredFiles(): List<File> =
        withContext(Dispatchers.IO) {
            getOrCreateMapFilesDir().listFiles()?.toList() ?: emptyList()
        }

    // Copy from Uri to storage with a given file name
    suspend fun copyUriToStorage(
        uri: Uri,
        fileName: String,
    ): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        return copyInputStreamToStorage(inputStream, fileName)
    }

    // Remove a file from storage by name
    suspend fun removeFromStorage(fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            File(getOrCreateMapFilesDir(), fileName).delete()
        }

    // Clear all files in the storage directory
    suspend fun clearStorage() =
        withContext(Dispatchers.IO) {
            getOrCreateMapFilesDir().listFiles()?.forEach { it.delete() }
        }
}
