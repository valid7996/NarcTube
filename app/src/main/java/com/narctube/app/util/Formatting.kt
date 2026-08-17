package com.narctube.app.util

import java.util.Locale
import kotlin.math.roundToInt

/** Formats a duration in seconds as `H:MM:SS` or `M:SS`. */
fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "--:--"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

/** Formats a byte count as a human readable size, e.g. `42.3 MB`. */
fun formatFileSize(bytes: Long?): String? {
    if (bytes == null || bytes <= 0) return null
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.lastIndex) {
        size /= 1024
        unitIndex++
    }
    val rounded = (size * 10).roundToInt() / 10.0
    return "$rounded ${units[unitIndex]}"
}
