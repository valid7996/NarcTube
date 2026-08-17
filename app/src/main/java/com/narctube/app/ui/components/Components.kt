package com.narctube.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.narctube.app.data.model.DownloadItem
import com.narctube.app.data.model.DownloadStatus
import com.narctube.app.data.model.VideoDetails
import com.narctube.app.data.model.VideoFormat
import com.narctube.app.util.formatDuration
import com.narctube.app.util.formatFileSize

@Composable
fun VideoInfoCard(video: VideoDetails, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatDuration(video.durationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                video.uploader?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QualityChipRow(
    formats: List<VideoFormat>,
    selected: VideoFormat?,
    onSelect: (VideoFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(formats) { format ->
            val isSelected = selected?.formatId == format.formatId
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(format) },
                label = {
                    Column {
                        Text(format.label)
                        formatFileSize(format.fileSizeApprox)?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                leadingIcon = if (format.isAudioOnly) {
                    { Icon(Icons.Filled.Audiotrack, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun DownloadListItem(
    item: DownloadItem,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
                Text(
                    item.formatLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                when (item.status) {
                    DownloadStatus.DOWNLOADING, DownloadStatus.CONVERTING -> {
                        LinearProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    }
                    DownloadStatus.QUEUED, DownloadStatus.FETCHING_INFO -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        }
                    }
                    DownloadStatus.FAILED -> {
                        Text(
                            item.errorMessage ?: "خطا در دانلود",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> Unit
                }
            }

            when (item.status) {
                DownloadStatus.DOWNLOADING, DownloadStatus.CONVERTING, DownloadStatus.QUEUED, DownloadStatus.FETCHING_INFO -> {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Cancel, contentDescription = "لغو")
                    }
                }
                DownloadStatus.FAILED, DownloadStatus.CANCELLED -> {
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Filled.Refresh, contentDescription = "تلاش مجدد")
                    }
                }
                DownloadStatus.COMPLETED -> {
                    IconButton(onClick = onOpen) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "باز کردن")
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف")
            }
        }
    }
}
