package com.narctube.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.narctube.app.R
import com.narctube.app.data.local.AppDatabase
import com.narctube.app.data.model.DownloadStatus
import com.narctube.app.util.Constants
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Runs each download as a coroutine backed by youtubedl-android's blocking `execute()` call.
 *
 * NOTE ON LIBRARY API: `YoutubeDLRequest.addOption` and `YoutubeDL.execute`'s parameter order
 * (request / processId / progress callback) are used here per the public README examples for
 * youtubedl-android. As of 0.18.1 the progress callback takes three arguments -
 * (progress: Float, etaSeconds: Long, rawOutputLine: String) - not two; the third is the raw
 * yt-dlp output line for that tick, which this app doesn't currently need. If a future library
 * version changes these signatures, the compiler errors will point at this file only - nothing
 * else in the app depends on the exact shape of this API.
 */
@SuppressLint("MissingPermission")
class DownloadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = mutableMapOf<Long, Job>()
    private val processIds = mutableMapOf<Long, String>()

    private val dao by lazy { AppDatabase.getInstance(applicationContext).downloadDao() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_DOWNLOAD -> {
                val id = intent.getLongExtra(Constants.EXTRA_DOWNLOAD_ID, -1L)
                val url = intent.getStringExtra(Constants.EXTRA_URL).orEmpty()
                val formatId = intent.getStringExtra(Constants.EXTRA_FORMAT_ID).orEmpty()
                val isAudio = intent.getBooleanExtra(Constants.EXTRA_IS_AUDIO, false)
                if (id != -1L && url.isNotBlank()) {
                    startDownload(id, url, formatId, isAudio)
                }
            }
            Constants.ACTION_CANCEL_DOWNLOAD -> {
                val id = intent.getLongExtra(Constants.EXTRA_DOWNLOAD_ID, -1L)
                cancelDownload(id)
            }
        }
        return START_NOT_STICKY
    }

    private fun notificationId(downloadId: Long) = 1000 + downloadId.toInt()

    private fun startDownload(id: Long, url: String, formatId: String, isAudio: Boolean) {
        val processId = "narctube_download_$id"
        processIds[id] = processId
        val notifId = notificationId(id)
        val isFirstActiveDownload = activeJobs.isEmpty()

        postOrPromoteNotification(notifId, buildProgressNotification(0, isAudio), isFirstActiveDownload)

        val job = serviceScope.launch {
            try {
                dao.getById(id)?.let { dao.update(it.copy(status = DownloadStatus.DOWNLOADING)) }

                val downloadDir = File(publicDownloadsDir(), Constants.DOWNLOAD_FOLDER_NAME).apply { mkdirs() }

                val request = YoutubeDLRequest(url)
                request.addOption("-o", File(downloadDir, "%(title).100s.%(ext)s").absolutePath)
                request.addOption("--no-mtime")
                request.addOption("-f", formatId)

                if (isAudio) {
                    request.addOption("-x")
                    request.addOption("--audio-format", "mp3")
                    request.addOption("--audio-quality", "0")
                } else {
                    request.addOption("--merge-output-format", "mp4")
                }

                var lastPersistedProgress = -1

                val response = YoutubeDL.getInstance().execute(
                    request = request,
                    processId = processId
                ) { progress, _, _ ->
                    val rounded = progress.toInt().coerceIn(0, 100)
                    if (rounded != lastPersistedProgress) {
                        lastPersistedProgress = rounded
                        NotificationManagerCompat.from(this@DownloadService)
                            .notify(notifId, buildProgressNotification(rounded, isAudio))
                        serviceScope.launch {
                            dao.getById(id)?.let {
                                dao.update(it.copy(progress = rounded, status = DownloadStatus.DOWNLOADING))
                            }
                        }
                    }
                }

                val filePath = extractDestinationPath(response.out)

                dao.getById(id)?.let {
                    dao.update(it.copy(status = DownloadStatus.COMPLETED, progress = 100, filePath = filePath))
                }
                NotificationManagerCompat.from(this@DownloadService)
                    .notify(notifId, buildCompletedNotification())
            } catch (e: Exception) {
                val cancelled = e.message?.contains("cancel", ignoreCase = true) == true
                dao.getById(id)?.let {
                    dao.update(
                        it.copy(
                            status = if (cancelled) DownloadStatus.CANCELLED else DownloadStatus.FAILED,
                            errorMessage = e.message
                        )
                    )
                }
                NotificationManagerCompat.from(this@DownloadService)
                    .notify(notifId, buildFailedNotification(cancelled))
            } finally {
                activeJobs.remove(id)
                processIds.remove(id)
                stopIfIdle()
            }
        }
        activeJobs[id] = job
    }

    private fun cancelDownload(id: Long) {
        processIds[id]?.let { pid -> runCatching { YoutubeDL.getInstance().destroyProcessById(pid) } }
        activeJobs[id]?.cancel()
    }

    private fun stopIfIdle() {
        if (activeJobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun postOrPromoteNotification(notifId: Int, notification: Notification, promoteToForeground: Boolean) {
        if (promoteToForeground) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
            ServiceCompat.startForeground(this, notifId, notification, type)
        } else {
            NotificationManagerCompat.from(this).notify(notifId, notification)
        }
    }

    /** yt-dlp prints "Destination: /path/to/file.ext" (and "[ExtractAudio] Destination: ..." for -x). */
    private fun extractDestinationPath(output: String?): String? {
        if (output.isNullOrBlank()) return null
        return Regex("Destination:\\s*(.+)").findAll(output)
            .map { it.groupValues[1].trim() }
            .lastOrNull()
    }

    @Suppress("DEPRECATION")
    private fun publicDownloadsDir(): File =
        android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)

    private fun buildProgressNotification(progress: Int, isAudio: Boolean): Notification {
        val title = getString(
            if (isAudio) R.string.download_status_converting else R.string.download_status_downloading
        )
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun buildCompletedNotification(): Notification =
        NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(getString(R.string.download_status_completed))
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

    private fun buildFailedNotification(cancelled: Boolean): Notification {
        val title = getString(
            if (cancelled) R.string.download_status_cancelled else R.string.download_status_failed
        )
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
