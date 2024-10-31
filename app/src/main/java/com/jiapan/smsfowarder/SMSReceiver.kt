package com.jiapan.smsfowarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class SmsReceiver  : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showToast(context, "接收到廣播訊息！\nintent.action: " + intent.action + " " + intent.extras)

        // 檢查是否是 SMS 接收動作
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                for (pdu in pdus) {
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = smsMessage.displayOriginatingAddress
                    val messageBody = smsMessage.messageBody

                    Log.e("SmsReceiver", "SMS received from: $sender, message: $messageBody")
                    showToast(context, "SMS received from: $sender, message: $messageBody")

                    // 發送至 Discord Webhook
                    sendToDiscordWebhook(context, sender, messageBody)

                    sendLocalBroadcast(context, sender, messageBody)
                }
            }
        }
    }

    private fun sendToDiscordWebhook(context: Context, sender: String, message: String) {
        val webhookUrl = context.getString(R.string.discord_webhook)

        // 設定 JSON 格式的資料
        val json = """
            {
                "content": "\n\n【SMS Forwarder】\n接收者: ${context.getString(R.string.receiver_mobile)} \n發送者: ${sender} \n內容: ${message}"
            }
        """.trimIndent()

        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(webhookUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SmsReceiver", "Failed to send message to Discord", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("SmsReceiver", "Message sent to Discord successfully")
            }
        })
    }

    // 顯示 Toast 訊息
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 在 SmsReceiver 中
    private fun sendLocalBroadcast(context: Context, sender: String, message: String) {
        val intent = Intent("SMS_RECEIVED")
        intent.putExtra("sender", sender)
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
