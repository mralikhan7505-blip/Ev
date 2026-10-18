package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiMode
import com.example.data.model.AiResponseResult
import com.example.data.model.SearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiApiClient"
private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        prompt: String,
        mode: AiMode,
        systemDirectives: String = "",
        customApiKey: String? = null
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() }
            ?: runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()
            ?: ""

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AiResponseResult(
                text = "⚠️ Gemini API key is missing or not configured. Please ensure your GEMINI_API_KEY is configured in the Secrets panel in AI Studio or enter it in Ev's Settings to enable live AI responses.",
                thinkingProcess = "Notice: API key check failed. Provide a valid Gemini API key in Secrets to activate full intelligence."
            )
        }

        val modelName = when (mode) {
            AiMode.HIGH_THINKING -> "gemini-4-argon"
            AiMode.GOOGLE_SEARCH -> "gemini-3.8-flash"
            AiMode.STANDARD -> "gemini-3.8-flash"
        }

        val requestUrl = "$BASE_URL/$modelName:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            // System instructions incorporating Ev's core persona and active learned memories
            val fullSystemPrompt = buildString {
                append("You are Ev, an intelligent, curious, and empathetic personal AI assistant that learns with the user. ")
                append("Your mission is to help the user learn new concepts, solve difficult problems, and execute tasks using what you learned together. ")
                append("Be supportive, articulate, and structured. ")
                if (systemDirectives.isNotBlank()) {
                    append("\n\n[Active Learned Memories & User Preferences]:\n")
                    append(systemDirectives)
                    append("\nAlways respect and incorporate these active preferences and rules into your output.")
                }
            }

            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", fullSystemPrompt)
                    })
                })
            })

            // Contents array
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })

            // High Thinking Mode configuration
            // Requirement: You MUST add thinking mode to the app where relevant to handle users' most complex queries.
            // You MUST use the gemini-4-argon model and set `thinkingLevel` to `ThinkingLevel.HIGH`. Do not set `maxOutputTokens`.
            if (mode == AiMode.HIGH_THINKING) {
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                    // Crucial: DO NOT SET maxOutputTokens as specified in feature instructions
                })
            }

            // Google Search Grounding tool configuration
            // Requirement: You MUST add Search Grounding to the app where relevant to get up to date and accurate information.
            // Use gemini-3.8-flash (with googleSearch tool)
            if (mode == AiMode.GOOGLE_SEARCH) {
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
            }
        }

        val request = Request.Builder()
            .url(requestUrl)
            .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $responseBody")
                val errorMsg = runCatching {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP ${response.code}: $responseBody"

                return@withContext AiResponseResult(
                    text = "Ev encountered an issue with the AI request: $errorMsg",
                    thinkingProcess = "Error code: ${response.code}. Request to $modelName failed."
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext AiResponseResult(text = "Ev did not receive a response candidate. Please try again.")
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            val thinkingBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val isThought = part.optBoolean("thought", false)
                    val textVal = part.optString("text", "")
                    if (isThought) {
                        thinkingBuilder.append(textVal).append("\n")
                    } else {
                        textBuilder.append(textVal)
                    }
                }
            }

            // Search Grounding Metadata extraction
            val sourcesList = mutableListOf<SearchSource>()
            val searchQueries = mutableListOf<String>()

            val groundingMetadata = candidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                // Web Search Queries
                val queriesArray = groundingMetadata.optJSONArray("webSearchQueries")
                if (queriesArray != null) {
                    for (i in 0 until queriesArray.length()) {
                        searchQueries.add(queriesArray.optString(i))
                    }
                }

                // Grounding Chunks (sources)
                val chunksArray = groundingMetadata.optJSONArray("groundingChunks")
                if (chunksArray != null) {
                    for (i in 0 until chunksArray.length()) {
                        val chunk = chunksArray.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title").ifBlank { uri }
                            if (uri.isNotBlank()) {
                                sourcesList.add(SearchSource(title = title, url = uri))
                            }
                        }
                    }
                }
            }

            val finalText = textBuilder.toString().trim()
            val finalThinking = thinkingBuilder.toString().trim().takeIf { it.isNotBlank() }

            return@withContext AiResponseResult(
                text = if (finalText.isNotBlank()) finalText else "Ev completed processing with empty response body.",
                thinkingProcess = finalThinking,
                searchSources = sourcesList.distinctBy { it.url },
                searchQueries = searchQueries
            )
        } catch (e: Exception) {
            Log.e(TAG, "Request to Gemini API failed", e)
            return@withContext AiResponseResult(
                text = "Could not connect to Ev's neural service: ${e.localizedMessage ?: "Network error"}. Please check your internet connection.",
                thinkingProcess = "Network exception: ${e.message}"
            )
        }
    }
}
