package com.narctube.app.data.repository

import com.narctube.app.data.model.VideoDetails
import com.narctube.app.data.model.VideoFormat
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper around youtubedl-android's `getInfo` (equivalent to yt-dlp's `--dump-json`).
 *
 * NOTE ON LIBRARY VERSIONS: youtubedl-android maps yt-dlp's JSON output onto Kotlin data
 * classes under `com.yausername.youtubedl_android.mapper`. The exact property names on
 * `VideoInfo` / the per-format objects have shifted slightly between library releases.
 * The property names used below (title, thumbnail, duration, uploader, formats, formatId,
 * ext, height, vcodec, fileSize/fileSizeApproximate) match the mapper as of youtubedl-android
 * 0.18.1. If you bump the library version and get "unresolved reference" errors here,
 * open the `mapper` package sources (Android Studio -> "Go to declaration" on `VideoInfo`)
 * and adjust the field names to match - the overall flow below won't need to change.
 */
class YoutubeRepository {

    suspend fun fetchVideoDetails(url: String): Result<VideoDetails> = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url)
            val info = YoutubeDL.getInstance().getInfo(request)

            val formats = buildFormatList(info)
            Result.success(
                VideoDetails(
                    id = info.id.orEmpty(),
                    title = info.title?.takeIf { it.isNotBlank() } ?: "Untitled",
                    thumbnailUrl = info.thumbnail,
                    durationSeconds = info.duration.toLongOrZero(),
                    uploader = info.uploader,
                    formats = formats
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Collapses yt-dlp's raw format list into one chip per resolution (best available audio
     * is merged in automatically via the `+bestaudio` selector) plus a single "MP3" option.
     */
    private fun buildFormatList(info: com.yausername.youtubedl_android.mapper.VideoInfo): List<VideoFormat> {
        val rawFormats = info.formats.orEmpty()

        val videoFormats = rawFormats
            .filter { f -> f.vcodec != null && f.vcodec != "none" && f.height.toLongOrZero() > 0 }
            .distinctBy { it.height }
            .sortedByDescending { it.height.toLongOrZero() }
            .map { f ->
                VideoFormat(
                    // Merge the chosen video-only stream with the best available audio track,
                    // falling back to a pre-muxed format if merging isn't possible.
                    formatId = "${f.formatId}+bestaudio/best",
                    label = "${f.height.toLongOrZero()}p",
                    ext = "mp4",
                    fileSizeApprox = f.fileSize.toLongOrZero().takeIf { it > 0 }
                        ?: f.fileSizeApproximate.toLongOrZero().takeIf { it > 0 },
                    isAudioOnly = false
                )
            }

        val audioOnly = VideoFormat(
            formatId = "bestaudio/best",
            label = "MP3",
            ext = "mp3",
            fileSizeApprox = null,
            isAudioOnly = true
        )

        return videoFormats + audioOnly
    }
}

/**
 * The youtubedl-android mapper classes represent numeric fields (height, filesize, duration...)
 * with a type that has varied across library releases (Int?, Long?, Double?...). Converting
 * through [Number] here means this code keeps compiling regardless of which numeric type the
 * installed version actually uses.
 */
private fun Any?.toLongOrZero(): Long = (this as? Number)?.toLong() ?: 0L
