package com.project.cruxify

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.cruxify.CruxUiState
import com.project.cruxify.CruxifyViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun CruxifyScreen(
    initialUrl: String = "",
    viewModel: CruxifyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var urlInput by remember { mutableStateOf(initialUrl) }
    var promptInput by remember { mutableStateOf("") }

    // If an initial URL was passed in via Share Intent, trigger the summary immediately
    LaunchedEffect(initialUrl) {
        if (initialUrl.isNotBlank()) {
            viewModel.getTheCrux(initialUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cruxify",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("YouTube URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Custom Prompt (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.getTheCrux(urlInput, promptInput) },
            enabled = urlInput.isNotBlank() && uiState !is CruxUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Find the Crux")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // State-driven UI updates
        when (val state = uiState) {
            is CruxUiState.Idle -> Text("Paste a link or share a video from YouTube to begin.")
            is CruxUiState.Loading -> CircularProgressIndicator()
            is CruxUiState.Success -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    MarkdownText(
                        markdown = state.cruxResult,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            is CruxUiState.Error -> Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
        }
    }
}