package com.narctube.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min

data class NarcTubeUiState(
    val url: String = "",
    val downloadType: String = "Video",
    val quality: String = "720p",
    val status: String = "Ready to download",
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String = "",
    val lastSavedFileName: String = ""
)

class NarcTubeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NarcTubeUiState())
    val uiState: StateFlow<NarcTubeUiState> = _uiState.asStateFlow()

    fun onUrlChanged(newUrl: String) {
        _uiState.value = _uiState.value.copy(url = newUrl, errorMessage = "")
    }

    fun onTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(downloadType = type, errorMessage = "")
    }

    fun onQualityChanged(quality: String) {
        _uiState.value = _uiState.value.copy(quality = quality, errorMessage = "")
    }

    fun reset() {
        _uiState.value = NarcTubeUiState()
    }

    fun startDownload() {
        val currentUrl = _uiState.value.url.trim()
        if (currentUrl.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid YouTube link.")
            return
        }

        if (!currentUrl.contains("youtube.com") && !currentUrl.contains("youtu.be")) {
            _uiState.value = _uiState.value.copy(errorMessage = "This is not a valid YouTube URL.")
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            status = "Preparing download...",
            progress = 0f,
            errorMessage = "",
            lastSavedFileName = ""
        )

        val simulatedProgress = listOf(0.15f, 0.35f, 0.65f, 0.9f, 1f)
        var step = 0

        val thread = Thread {
            try {
                while (step < simulatedProgress.size) {
                    Thread.sleep(500)
                    val nextProgress = simulatedProgress[step]
                    _uiState.value = _uiState.value.copy(
                        progress = nextProgress,
                        status = if (nextProgress < 1f) {
                            "Downloading ${_uiState.value.downloadType.lowercase()}..."
                        } else {
                            "Download completed"
                        }
                    )
                    step += 1
                }

                val fileName = if (_uiState.value.downloadType == "Audio") {
                    "narctube_audio_${_uiState.value.quality}.mp3"
                } else {
                    "narctube_video_${_uiState.value.quality}.mp4"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "Completed successfully",
                    progress = 1f,
                    lastSavedFileName = fileName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "Download failed",
                    errorMessage = e.message ?: "Unexpected error occurred"
                )
            }
        }

        thread.start()
    }
}
