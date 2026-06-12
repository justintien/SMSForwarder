package com.jiapan.smsfowarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.jiapan.smsfowarder.data.DiscordSender
import com.jiapan.smsfowarder.data.db.AppDatabase
import com.jiapan.smsfowarder.data.db.SmsRecord
import com.jiapan.smsfowarder.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        // A multipart SMS arrives as several segments in one broadcast; merge them.
        val sender = messages.first().displayOriginatingAddress ?: "Unknown"
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val timestamp = messages.first().timestampMillis

        Log.d(TAG, "SMS from $sender: $body")

        // Keep the receiver alive while we persist + forward off the main thread.
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        scope.launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                db.smsDao().insert(
                    SmsRecord(sender = sender, body = body, timestamp = timestamp)
                )

                val webhooks = db.webhookDao().getEnabled()
                val receiver = SettingsRepository(appContext).receiverMobile.first()
                DiscordSender.send(webhooks, receiver, sender, body)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle incoming SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
