package com.narctube.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import com.narctube.app.ui.navigation.NarcTubeNavHost
import com.narctube.app.ui.theme.NarcTubeTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            // Computed once per composition; HomeViewModel.consumeSharedUrl guards against
            // re-triggering a fetch if this same value is seen again on recomposition.
            val sharedUrl = remember { extractSharedUrl(intent) }

            NarcTubeTheme {
                NarcTubeNavHost(sharedUrl = sharedUrl)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
