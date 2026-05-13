package com.project.cruxify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.cruxify.ui.theme.*
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun CruxifyScreen(
    initialUrl: String = "",
    viewModel: CruxifyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var urlInput by remember { mutableStateOf(initialUrl) }
    var promptInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(initialUrl) {
        if (initialUrl.isNotBlank()) {
            viewModel.getTheCrux(initialUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CruxBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ── Header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SectionLabel("VIDEO SUMMARIZER")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cruxify",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = CruxTextPrimary
                )
            }
            // Logo badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CruxPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cx",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── URL Input Card ─────────────────────────────────────────
        DarkCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("YOUTUBE LINK")
                    // Paste button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                    as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                urlInput = clip.getItemAt(0).text?.toString() ?: ""
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ContentPaste,
                            contentDescription = "Paste",
                            tint = CruxPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = {
                        Text(
                            "https://youtube.com/watch?v=...",
                            color = CruxTextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CruxTextPrimary,
                        unfocusedTextColor = CruxTextPrimary,
                        cursorColor = CruxPurple,
                        focusedBorderColor = CruxPurple,
                        unfocusedBorderColor = CruxBorder,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Custom Prompt Card ─────────────────────────────────────
        DarkCard {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionLabel("CUSTOM PROMPT")
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Optional — guide the AI's focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = CruxTextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            "e.g., Focus on the coding examples...",
                            color = CruxTextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CruxTextPrimary,
                        unfocusedTextColor = CruxTextPrimary,
                        cursorColor = CruxPurple,
                        focusedBorderColor = CruxPurple,
                        unfocusedBorderColor = CruxBorder,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── CTA Button ─────────────────────────────────────────────
        Button(
            onClick = { viewModel.getTheCrux(urlInput, promptInput) },
            enabled = urlInput.isNotBlank() && uiState !is CruxUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CruxPurple,
                contentColor = Color.White,
                disabledContainerColor = CruxSurfaceHigh,
                disabledContentColor = CruxTextMuted
            )
        ) {
            Text(
                text = if (uiState is CruxUiState.Loading) "Analyzing..." else "Find the Crux",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Result Area ────────────────────────────────────────────
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "stateTransition"
        ) { state ->
            when (state) {
                is CruxUiState.Idle -> IdleHint()
                is CruxUiState.Loading -> LoadingIndicator()
                is CruxUiState.Success -> ResultCard(
                    result = state.cruxResult,
                    context = context,
                    onNewSummary = {
                        urlInput = ""
                        promptInput = ""
                        viewModel.resetState()
                    }
                )
                is CruxUiState.Error -> ErrorCard(
                    message = state.errorMessage,
                    onRetry = { viewModel.getTheCrux(urlInput, promptInput) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Reusable Components ───────────────────────────────────────────

/** Uppercase spaced label — matches the reference design's section headers */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            fontSize = 11.sp
        ),
        color = CruxTextMuted
    )
}

/** Dark card with subtle border — the core design element */
@Composable
private fun DarkCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CruxSurfaceCard)
            .border(1.dp, CruxBorder, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun IdleHint() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        Text("💡", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Paste a link or share a video\nfrom YouTube to begin",
            style = MaterialTheme.typography.bodyMedium,
            color = CruxTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = CruxPurple.copy(alpha = alpha),
            strokeWidth = 3.dp,
            trackColor = CruxSurfaceHigh
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Cruxifying the video...",
            style = MaterialTheme.typography.bodyMedium,
            color = CruxTextSecondary.copy(alpha = alpha)
        )
    }
}

@Composable
private fun ResultCard(
    result: String,
    context: Context,
    onNewSummary: () -> Unit
) {
    Column {
        DarkCard {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CruxSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SectionLabel("THE CRUX")
                    }
                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                        as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Cruxify Summary", result)
                                )
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = CruxTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, result)
                                    putExtra(Intent.EXTRA_SUBJECT, "Cruxify Summary")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share summary"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = "Share",
                                tint = CruxTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CruxBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Markdown result
                MarkdownText(
                    markdown = result,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CruxTextPrimary,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // New Summary button
        OutlinedButton(
            onClick = onNewSummary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CruxPurple
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(CruxBorderLight)
            )
        ) {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "New Summary",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    DarkCard(
        modifier = Modifier.border(1.dp, CruxError.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = CruxError
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = CruxTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CruxError.copy(alpha = 0.15f),
                    contentColor = CruxError
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}