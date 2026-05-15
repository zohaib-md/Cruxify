package com.project.cruxify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.cruxify.data.AnthropicClient
import com.project.cruxify.data.YouTubeTranscriptFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CruxUiState {
    object Idle : CruxUiState
    object Loading : CruxUiState
    data class Success(val cruxResult: String) : CruxUiState
    data class Error(val errorMessage: String) : CruxUiState
}

class CruxifyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CruxUiState>(CruxUiState.Idle)
    val uiState: StateFlow<CruxUiState> = _uiState.asStateFlow()

    private val anthropic = AnthropicClient()

    fun resetState() {
        _uiState.value = CruxUiState.Idle
    }

    fun getTheCrux(youtubeUrl: String, customPrompt: String = "") {
        if (youtubeUrl.isBlank()) return

        _uiState.value = CruxUiState.Loading

        viewModelScope.launch {
            try {
                val videoId = YouTubeTranscriptFetcher.extractVideoId(youtubeUrl)
                    ?: throw IllegalArgumentException("Invalid YouTube URL")

                val transcript = YouTubeTranscriptFetcher.fetchTranscript(youtubeUrl)

                val systemPrompt = """
                    You are Cruxify, an AI specialized in extracting the essential core of video content.
                    You receive a timestamped transcript from a YouTube video. Base your answer only on that transcript.

                    Provide:
                    1. **🎯 The Crux**: A one-sentence main takeaway.
                    2. **📋 Structured Summary**: A detailed summary using bullet points.
                    3. **⏱️ Key Timestamps**: Important moments with timestamps (MM:SS format) when applicable.

                    Format your response in clean Markdown.
                """.trimIndent()

                val userMessage = """
                    YouTube URL: https://www.youtube.com/watch?v=$videoId

                    TRANSCRIPT:
                    $transcript

                    User Context: ${customPrompt.ifBlank { "None" }}
                """.trimIndent()

                val result = anthropic.createMessage(systemPrompt, userMessage)
                _uiState.value = CruxUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = CruxUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
