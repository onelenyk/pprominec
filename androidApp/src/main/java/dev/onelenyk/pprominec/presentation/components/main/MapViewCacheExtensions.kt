package dev.onelenyk.pprominec.presentation.components.main

import android.widget.Toast
import kotlinx.coroutines.suspendCancellableCoroutine
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extension function to download all map tiles within a given geographic area
 * for offline use, as a suspend function.
 *
 * @param boundingBox The rectangular area to download.
 * @param zoomMin The minimum zoom level to download.
 * @param zoomMax The maximum zoom level to download.
 */
suspend fun MapView.downloadAndCacheRegionSuspend(
    boundingBox: BoundingBox,
    zoomMin: Int,
    zoomMax: Int,
): Result<Unit> = suspendCancellableCoroutine { cont ->
    val context = this.context
    val cacheManager = CacheManager(this)

    cacheManager.downloadAreaAsync(
        context,
        boundingBox,
        zoomMin,
        zoomMax,
        object : CacheManager.CacheManagerCallback {
            override fun onTaskComplete() {
                Toast.makeText(
                    context,
                    "Cache download completed for the selected area.",
                    Toast.LENGTH_SHORT,
                ).show()
                cont.resume(Result.success(Unit))
            }

            override fun onTaskFailed(errors: Int) {
                Toast.makeText(
                    context,
                    "Cache download failed ($errors errors).",
                    Toast.LENGTH_LONG,
                ).show()
                cont.resume(Result.failure(Exception("Cache download failed with $errors errors")))
            }

            override fun updateProgress(
                progress: Int,
                currentZoomLevel: Int,
                zoomMin: Int,
                zoomMax: Int,
            ) {
                // Optionally, update UI with progress
            }

            override fun downloadStarted() {
                Toast.makeText(
                    context,
                    "Cache download started for the selected area.",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            override fun setPossibleTilesInArea(total: Int) {}
            fun onTaskCancelled() {
                Toast.makeText(context, "Cache download cancelled.", Toast.LENGTH_SHORT).show()
                cont.resumeWithException(Exception("Cache download cancelled"))
            }
        },
    )
}

/**
 * Extension function to calculate and display the current cache usage as a suspend function.
 * Returns the cache size in MB.
 */
suspend fun MapView.getCacheUsageMB(): Double = suspendCancellableCoroutine { cont ->
    val cacheManager = CacheManager(this)
    Thread {
        val cacheSizeBytes = cacheManager.currentCacheUsage()
        val cacheSizeMB = if (cacheSizeBytes > 0) cacheSizeBytes / (1024.0 * 1024.0) else 0.0
        cont.resume(cacheSizeMB)
    }.start()
}
