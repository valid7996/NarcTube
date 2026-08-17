package com.narctube.app.ui.downloads

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narctube.app.R
import com.narctube.app.data.model.DownloadItem
import com.narctube.app.ui.components.DownloadListItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = viewModel()) {
    val downloads by viewModel.downloads.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.downloads_title)) }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (downloads.isEmpty()) {
                Text(
                    stringResource(R.string.downloads_empty),
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(downloads, key = { it.id }) { item ->
                        DownloadListItem(
                            item = item,
                            onCancel = { viewModel.cancel(item) },
                            onRetry = { viewModel.retry(item) },
                            onDelete = { viewModel.delete(item) },
                            onOpen = { openDownload(item, context) }
                        )
                    }
                }
            }
        }
    }
}

private fun openDownload(item: DownloadItem, context: android.content.Context) {
    val path = item.filePath ?: return
    val file = File(path)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val mimeType = if (item.isAudioOnly) "audio/mpeg" else "video/mp4"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
