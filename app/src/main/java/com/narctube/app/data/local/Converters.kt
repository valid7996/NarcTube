package com.narctube.app.data.local

import androidx.room.TypeConverter
import com.narctube.app.data.model.DownloadStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): DownloadStatus =
        runCatching { DownloadStatus.valueOf(value) }.getOrDefault(DownloadStatus.FAILED)
}
