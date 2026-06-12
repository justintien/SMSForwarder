package com.jiapan.smsfowarder.data

import android.util.Log
import com.jiapan.smsfowarder.data.db.Webhook
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Forwards an SMS to one or more Discord webhooks. Uses synchronous OkHttp calls,
 * so it must be invoked off the main thread (e.g. inside the receiver's goAsync scope).
 */
object DiscordSender {

    private const val TAG = "DiscordSender"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

    fun send(
        webhooks: List<Webhook>,
        receiverMobile: String,
        sender: String,
        message: String
    ) {
        if (webhooks.isEmpty()) {
            Log.w(TAG, "No enabled webhook configured; skipping forward.")
            return
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
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending to '${webhook.label}'", e)
            }
        }
    }
}
