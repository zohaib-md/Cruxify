package com.project.cruxify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.project.cruxify.ui.theme.CruxBackground
import com.project.cruxify.ui.theme.CruxifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Catch the YouTube link if the app was opened via the Share menu
        var sharedUrl = ""
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        }

        setContent {
            CruxifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CruxBackground
                ) {
                    CruxifyScreen(initialUrl = sharedUrl)
                }
            }
        }
    }

    // Handle new share intents when the app is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val url = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (url.isNotBlank()) {
                // Recreate to pass the new URL through
                recreate()
            }
        }
    }
}