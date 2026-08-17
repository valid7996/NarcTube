package com.narctube.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoUrl: String,
    val title: String,
    val thumbnailUrl: String?,
    val formatId: String,
    val formatLabel: String,
    val isAudioOnly: Boolean,
    val filePath: String? = null,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
