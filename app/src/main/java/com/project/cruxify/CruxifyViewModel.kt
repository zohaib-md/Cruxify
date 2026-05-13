
//This file will hold the state of our UI and manage the
// asynchronous call to the Gemini model so it doesn't freeze the screen while thinking.
package com.project.cruxify


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define the four possible states of our screen
sealed interface CruxUiState {
    object Idle : CruxUiState
    object Loading : CruxUiState
    data class Success(val cruxResult: String) : CruxUiState
    data class Error(val errorMessage: String) : CruxUiState
}

class CruxifyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CruxUiState>(CruxUiState.Idle)
    val uiState: StateFlow<CruxUiState> = _uiState.asStateFlow()

    // Initialize the Gemini 2.5 Flash model securely using BuildConfig
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun resetState() {
        _uiState.value = CruxUiState.Idle
    }

    fun getTheCrux(youtubeUrl: String, customPrompt: String = "") {
        if (youtubeUrl.isBlank()) return

        _uiState.value = CruxUiState.Loading

        viewModelScope.launch {
            try {
                // The Master Prompt (text instruction)
                val promptText = """
                    You are Cruxify, an AI specialized in extracting the essential core of video content.
                    Watch and analyze the provided video carefully.
                    
                    Provide:
                    1. **🎯 The Crux**: A one-sentence main takeaway.
                    2. **📋 Structured Summary**: A detailed summary using bullet points.
                    3. **⏱️ Key Timestamps**: Important moments with timestamps (MM:SS format) if applicable.
                    
                    User Context: ${customPrompt.ifBlank { "None" }}
                    
                    Format your response in clean Markdown.
                """.trimIndent()

                // Build multimodal content: video file + text prompt
                // This tells Gemini to actually WATCH the video, not just guess from the URL
                val inputContent = content {
                    fileData(uri = youtubeUrl, mimeType = "video/*")
                    text(promptText)
                }

                val response = generativeModel.generateContent(inputContent)

                response.text?.let { result ->
                    _uiState.value = CruxUiState.Success(result)
                } ?: run {
                    _uiState.value = CruxUiState.Error("Received an empty response.")
                }
            } catch (e: Exception) {
                _uiState.value = CruxUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}