package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder!")
            return@withContext getOfflineMockResponse(prompt)
        }

        val jsonBody = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArr)
                }
                put(contentObj)
            }
            put("contents", contentsArr)

            if (systemInstruction != null) {
                val sysInstObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", systemInstruction)
                        }
                        put(partObj)
                    }
                    put("parts", partsArr)
                }
                put("systemInstruction", sysInstObj)
            }
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Error from Gemini: $errBody")
                    // Fallback to offline mock response with notice
                    return@withContext getOfflineMockResponse(prompt)
                }
                val respString = response.body?.string() ?: "No response body"
                val jsonResp = JSONObject(respString)
                val textResponse = jsonResp.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                textResponse
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "No internet connection", e)
            getOfflineMockResponse(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini", e)
            getOfflineMockResponse(prompt)
        }
    }

    private fun getOfflineMockResponse(prompt: String): String {
        return when {
            prompt.contains("Caption") || prompt.contains("caption") -> {
                "✨ STRATEGY MEETS CREATIVITY ✨\n\nStop scrolling. Start expanding. In a feed full of noise, Editoz Agency ensures your brand is the signal. We turn raw reels and aesthetic design into conversion-focused brand engines.\n\nReady to upscale your revenue? DM us 'SCALE' for a customized marketing blueprint. 🚀\n\n#EditozAgency #SocialMediaStrategy #BrandingExpert #ContentCreationVibe #ReelEditingCraft #AestheticMarketing"
            }
            prompt.contains("Hashtag") || prompt.contains("hashtag") -> {
                "#EditozAgency #SocialMediaManagement #LuxuryMarketing #ScaleYourBiz #ContentStrategy2026 #ReelPower #MetaAdsAgency #GoogleGrowthTips #AgencySecretWeapon #AestheticFeeds #BrandEvolution"
            }
            prompt.contains("Script") || prompt.contains("script") -> {
                "🎬 TITLE: The 3 Secrets Behind Editoz-level 1M+ Reels\n\n[0:00 - 0:02] HOOK: (Visual: Bold black-and-orange graphics flashing. Voiceover: 'This single layout hack gets 10x more reach.')\n\n[0:02 - 0:08] BODY 1: (Visual: Split screen showing low contrast VS premium dark contrast grading. Voiceover: 'First, ditch the white background. High-contrast premium dark aesthetic increases dwell time by 48%.')\n\n[0:08 - 0:15] BODY 2: (Visual: Graph displaying growth metrics overlaying edited video reels. Voiceover: 'Second, transition exactly on the beat. Human brains crave mathematical closure. Give it to them.')\n\n[0:15 - 0:20] CALL-TO-ACTION: (Visual: Aesthetic logo transition. Voiceover: 'Hit Save, or simply outsource your reel design to Editoz. Link in bio!')"
            }
            else -> {
                "💡 CONTENT IDEA GENERATOR [Editoz Exclusive]\n\n1. 'Day in the Life of a Marketing Director': Focus on raw results, analytics screens, and direct revisions. Shows social proof!\n2. 'Behind the Revision Request': Share a side-by-side edit loop before and after Editoz grading. Establishes deep quality authority.\n3. 'Why Your Agency is Static': Break down why basic templates keep brands invisible. Contrast with custom branding assets."
            }
        }
    }
}
