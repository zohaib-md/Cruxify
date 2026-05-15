package com.project.cruxify.data

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object YouTubeTranscriptFetcher {

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val videoIdPatterns = listOf(
        Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([\\w-]{11})"),
        Pattern.compile("youtube\\.com/shorts/([\\w-]{11})"),
    )

    fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        for (pattern in videoIdPatterns) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) return matcher.group(1)
        }
        return if (trimmed.length == 11 && trimmed.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
            trimmed
        } else {
            null
        }
    }

    suspend fun fetchTranscript(youtubeUrl: String): String = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(youtubeUrl)
            ?: throw IllegalArgumentException("Invalid YouTube URL")

        val watchHtml = fetchWatchPage(videoId)
        val playerJson = extractPlayerResponse(watchHtml)
            ?: throw IllegalStateException(
                "Could not read video metadata. The video may be private or unavailable."
            )

        val captionUrl = findCaptionTrackUrl(playerJson)
            ?: throw IllegalStateException(
                "No captions found for this video. Enable subtitles on the video and try again."
            )

        parseTranscriptXml(fetchUrl(captionUrl))
    }

    private fun fetchWatchPage(videoId: String): String {
        val request = Request.Builder()
            .url("https://www.youtube.com/watch?v=$videoId")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to load video page (${response.code})")
            }
            response.body?.string().orEmpty()
        }
    }

    private fun extractPlayerResponse(html: String): JSONObject? {
        val markers = listOf("ytInitialPlayerResponse = ", "var ytInitialPlayerResponse = ")
        for (marker in markers) {
            val start = html.indexOf(marker)
            if (start < 0) continue
            val jsonStart = start + marker.length
            val jsonEnd = findJsonObjectEnd(html, jsonStart)
            if (jsonEnd > jsonStart) {
                return runCatching { JSONObject(html.substring(jsonStart, jsonEnd)) }.getOrNull()
            }
        }
        return null
    }

    private fun findJsonObjectEnd(text: String, start: Int): Int {
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val char = text[i]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else when (char) {
                    '\\' -> escaped = true
                    '"' -> inString = false
                }
                continue
            }
            when (char) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i + 1
                }
            }
        }
        return -1
    }

    private fun findCaptionTrackUrl(player: JSONObject): String? {
        val tracks = player
            .optJSONObject("captions")
            ?.optJSONObject("playerCaptionsTracklistRenderer")
            ?.optJSONArray("captionTracks")
            ?: return null

        if (tracks.length() == 0) return null

        var fallback: String? = null
        for (i in 0 until tracks.length()) {
            val track = tracks.getJSONObject(i)
            val url = track.optString("baseUrl").takeIf { it.isNotBlank() } ?: continue
            fallback = fallback ?: url
            if (track.optString("languageCode").startsWith("en")) return url
        }
        return fallback
    }

    private fun fetchUrl(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            .build()
        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to load captions (${response.code})")
            }
            response.body?.string().orEmpty()
        }
    }

    private fun parseTranscriptXml(xml: String): String {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(xml.reader())

        val segments = mutableListOf<String>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "text") {
                val startMs = (parser.getAttributeValue(null, "start")?.toDoubleOrNull() ?: 0.0) * 1000
                parser.next()
                val rawText = parser.text?.trim().orEmpty()
                if (rawText.isNotBlank()) {
                    val text = Html.fromHtml(rawText, Html.FROM_HTML_MODE_LEGACY).toString()
                    segments.add("[${formatTimestamp(startMs.toLong())}] $text")
                }
            }
            event = parser.next()
        }

        if (segments.isEmpty()) {
            throw IllegalStateException("Captions were empty for this video.")
        }
        return segments.joinToString("\n")
    }

    private fun formatTimestamp(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
