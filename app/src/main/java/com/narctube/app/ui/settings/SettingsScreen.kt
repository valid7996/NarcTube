package com.narctube.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.narctube.app.BuildConfig
import com.narctube.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_download_location)) },
                leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about)) },
                supportingContent = { Text("NarcTube v${BuildConfig.VERSION_NAME}") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) }
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_legal_notice_title)) },
                        leadingContent = { Icon(Icons.Filled.Gavel, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Text(
                        stringResource(R.string.settings_legal_notice_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
