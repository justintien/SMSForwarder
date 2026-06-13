package com.jiapan.smsfowarder.data

import android.util.Log
import com.jiapan.smsfowarder.data.db.Webhook
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Forwards an SMS to one or more Discord webhooks. Uses synchronous OkHttp calls,
 * so it must be invoked off the main thread (e.g. inside a WorkManager worker).
 */
object DiscordSender {

    private const val TAG = "DiscordSender"

    // Bounded timeouts so a slow/unreachable webhook fails fast instead of blocking
    // for the OkHttp default (~30s) and starving the surrounding background work.
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()
    private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

    /**
     * Sends to every webhook. Returns true only if all enabled webhooks accepted the
     * message, so the caller (the worker) can decide whether to retry.
     */
    fun send(
        webhooks: List<Webhook>,
        receiverMobile: String,
        sender: String,
        message: String
    ): Boolean {
        if (webhooks.isEmpty()) {
            Log.w(TAG, "No enabled webhook configured; skipping forward.")
            return true
        }

        val content = buildString {
            append("\n\n【SMS Forwarder】")
            append("\n接收者: ").append(receiverMobile)
            append("\n發送者: ").append(sender)
            append("\n內容: ").append(message)
        }
        // JSONObject handles escaping of quotes / newlines / backslashes correctly.
        val body = JSONObject().put("content", content).toString()
            .toRequestBody(JSON)

        var allSucceeded = true
        for (webhook in webhooks) {
            val request = Request.Builder()
                .url(webhook.url)
                .post(body)
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Sent to '${webhook.label}' (${response.code})")
                    } else {
                        Log.e(TAG, "Failed to send to '${webhook.label}': HTTP ${response.code}")
                        allSucceeded = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending to '${webhook.label}'", e)
                allSucceeded = false
            }
        }
        return allSucceeded
    }
}
