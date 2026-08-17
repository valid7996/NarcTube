package com.narctube.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.narctube.app.util.Constants
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application entry point.
 *
 * yt-dlp (bundled inside youtubedl-android) and its ffmpeg companion library both ship
 * native binaries that need to be unpacked to the app's private storage once per install.
 * This is a somewhat heavy, blocking operation, so it's done off the main thread here and
 * awaited lazily by anything that actually needs to fetch or download (see [YoutubeRepository]).
 */
class NarcTubeApplication : Application() {

    companion object {
        private const val TAG = "NarcTubeApplication"

        @Volatile
        var isYoutubeDLReady: Boolean = false
            private set
    }

    val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeYoutubeDL()
    }

    private fun initializeYoutubeDL() {
        applicationScope.launch {
            try {
                YoutubeDL.getInstance().init(this@NarcTubeApplication)
                FFmpeg.getInstance().init(this@NarcTubeApplication)
                isYoutubeDLReady = true
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "failed to initialize youtubedl-android", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
