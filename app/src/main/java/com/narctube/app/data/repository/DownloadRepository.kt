package com.narctube.app.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.narctube.app.data.local.AppDatabase
import com.narctube.app.data.model.DownloadItem
import com.narctube.app.data.model.DownloadStatus
import com.narctube.app.data.model.VideoDetails
import com.narctube.app.data.model.VideoFormat
import com.narctube.app.service.DownloadService
import com.narctube.app.util.Constants
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).downloadDao()

    fun observeDownloads(): Flow<List<DownloadItem>> = dao.observeAll()

    suspend fun deleteDownload(item: DownloadItem) = dao.delete(item)

    /** Inserts a new queued download row and kicks off [DownloadService] to actually fetch it. */
    suspend fun enqueueDownload(video: VideoDetails, format: VideoFormat, sourceUrl: String): Long {
        val item = DownloadItem(
            videoUrl = sourceUrl,
            title = video.title,
            thumbnailUrl = video.thumbnailUrl,
            formatId = format.formatId,
            formatLabel = format.label,
            isAudioOnly = format.isAudioOnly,
            status = DownloadStatus.QUEUED
        )
        val id = dao.insert(item)
        startDownload(id, sourceUrl, format)
        return id
    }

    suspend fun retryDownload(item: DownloadItem) {
        dao.update(item.copy(status = DownloadStatus.QUEUED, progress = 0, errorMessage = null))
        startDownload(
            item.id,
            item.videoUrl,
            VideoFormat(item.formatId, item.formatLabel, if (item.isAudioOnly) "mp3" else "mp4", null, item.isAudioOnly)
        )
    }

    private fun startDownload(id: Long, url: String, format: VideoFormat) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = Constants.ACTION_START_DOWNLOAD
            putExtra(Constants.EXTRA_DOWNLOAD_ID, id)
            putExtra(Constants.EXTRA_URL, url)
            putExtra(Constants.EXTRA_FORMAT_ID, format.formatId)
            putExtra(Constants.EXTRA_IS_AUDIO, format.isAudioOnly)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun cancelDownload(id: Long) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = Constants.ACTION_CANCEL_DOWNLOAD
            putExtra(Constants.EXTRA_DOWNLOAD_ID, id)
        }
        context.startService(intent)
    }
}
