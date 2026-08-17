package com.narctube.app.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "narctube_downloads"
    const val DOWNLOAD_FOLDER_NAME = "NarcTube"

    // Intent extras used to start DownloadService
    const val EXTRA_DOWNLOAD_ID = "extra_download_id"
    const val EXTRA_URL = "extra_url"
    const val EXTRA_FORMAT_ID = "extra_format_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_IS_AUDIO = "extra_is_audio"

    // Actions for DownloadService
    const val ACTION_START_DOWNLOAD = "com.narctube.app.action.START_DOWNLOAD"
    const val ACTION_CANCEL_DOWNLOAD = "com.narctube.app.action.CANCEL_DOWNLOAD"

    // Broadcast used by the service to report progress back to the UI layer
    const val BROADCAST_PROGRESS = "com.narctube.app.broadcast.PROGRESS"
    const val EXTRA_PROGRESS = "extra_progress"
    const val EXTRA_STATUS = "extra_status"
    const val EXTRA_ERROR = "extra_error"
    const val EXTRA_FILE_PATH = "extra_file_path"

    val YOUTUBE_URL_REGEX = Regex(
        "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|shorts/|live/)|youtu\\.be/).+"
    )
}
