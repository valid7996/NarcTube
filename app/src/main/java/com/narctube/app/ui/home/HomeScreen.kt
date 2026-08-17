package com.narctube.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narctube.app.R
import com.narctube.app.ui.components.QualityChipRow
import com.narctube.app.ui.components.VideoInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String?,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sharedUrl) {
        sharedUrl?.let { viewModel.consumeSharedUrl(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val text = when (message) {
                "download_started" -> "دانلود به صف اضافه شد"
                else -> message
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_title)) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.url,
                    onValueChange = viewModel::onUrlChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.home_url_hint)) },
                    singleLine = true,
                    isError = state.error == "invalid_url"
                )
                IconButton(onClick = {
                    clipboardManager.getText()?.text?.let { viewModel.onUrlChange(it) }
                }) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = stringResource(R.string.home_paste_button))
                }
            }

            if (state.error == "invalid_url") {
                Text(
                    stringResource(R.string.home_error_invalid_url),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (state.error == "fetch_failed") {
                Text(
                    stringResource(R.string.home_error_fetch_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Button(
                onClick = { viewModel.fetchInfo() },
                enabled = state.url.isNotBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.home_fetch_button))
            }

            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.video?.let { video ->
                VideoInfoCard(video)

                Text(stringResource(R.string.quality_section_title), style = MaterialTheme.typography.titleMedium)
                QualityChipRow(
                    formats = video.formats,
                    selected = state.selectedFormat,
                    onSelect = viewModel::selectFormat
                )

                Button(
                    onClick = { viewModel.startDownload() },
                    enabled = state.selectedFormat != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.download_button))
                }
            }
        }
    }
}
