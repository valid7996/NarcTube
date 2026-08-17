package com.narctube.app.ui.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.narctube.app.data.model.DownloadItem
import com.narctube.app.data.repository.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DownloadRepository(application)

    val downloads: StateFlow<List<DownloadItem>> = repository.observeDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancel(item: DownloadItem) {
        repository.cancelDownload(item.id)
    }

    fun retry(item: DownloadItem) {
        viewModelScope.launch { repository.retryDownload(item) }
    }

    fun delete(item: DownloadItem) {
        viewModelScope.launch { repository.deleteDownload(item) }
    }
}
