package com.narctube.app.data.model

/**
 * One selectable download option shown as a chip in the UI.
 *
 * [formatId] is passed straight through to yt-dlp's `-f` selector, so it can be either a
 * concrete format id (e.g. "137") or a selector expression (e.g. "137+bestaudio/best").
 */
data class VideoFormat(
    val formatId: String,
    val label: String,
    val ext: String,
    val fileSizeApprox: Long?,
    val isAudioOnly: Boolean
)

data class VideoDetails(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val uploader: String?,
    val formats: List<VideoFormat>
)
