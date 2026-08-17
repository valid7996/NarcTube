package com.narctube.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.narctube.app.data.model.VideoDetails
import com.narctube.app.data.model.VideoFormat
import com.narctube.app.data.repository.DownloadRepository
import com.narctube.app.data.repository.YoutubeRepository
import com.narctube.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val video: VideoDetails? = null,
    val selectedFormat: VideoFormat? = null,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val youtubeRepository = YoutubeRepository()
    private val downloadRepository = DownloadRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onUrlChange(newUrl: String) {
        _uiState.value = _uiState.value.copy(url = newUrl, error = null)
    }

    fun fetchInfo() {
        val url = _uiState.value.url.trim()
        if (!Constants.YOUTUBE_URL_REGEX.matches(url)) {
            _uiState.value = _uiState.value.copy(error = "invalid_url")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, video = null, selectedFormat = null)

        viewModelScope.launch {
            val result = youtubeRepository.fetchVideoDetails(url)
            result.onSuccess { details ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    video = details,
                    selectedFormat = details.formats.firstOrNull()
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "fetch_failed")
            }
        }
    }

    fun selectFormat(format: VideoFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun startDownload() {
        val state = _uiState.value
        val video = state.video ?: return
        val format = state.selectedFormat ?: return

        viewModelScope.launch {
            downloadRepository.enqueueDownload(video, format, state.url.trim())
            _messages.send("download_started")
            _uiState.value = HomeUiState()
        }
    }

    fun consumeSharedUrl(sharedUrl: String) {
        if (sharedUrl.isNotBlank() && _uiState.value.url != sharedUrl) {
            _uiState.value = _uiState.value.copy(url = sharedUrl)
            fetchInfo()
        }
    }
}
