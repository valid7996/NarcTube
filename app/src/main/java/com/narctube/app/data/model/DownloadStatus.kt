package com.narctube.app.data.model

enum class DownloadStatus {
    QUEUED,
    FETCHING_INFO,
    DOWNLOADING,
    CONVERTING,
    COMPLETED,
    FAILED,
    CANCELLED
}
