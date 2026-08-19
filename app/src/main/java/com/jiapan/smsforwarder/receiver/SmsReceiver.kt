package com.jiapan.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jiapan.smsforwarder.data.db.AppDatabase
import com.jiapan.smsforwarder.data.db.SmsRecord
import com.jiapan.smsforwarder.work.ForwardWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

        // Persist quickly, then hand forwarding off to WorkManager and finish well within
        // the BroadcastReceiver time limit. Doing the network call here (the old behaviour)
        // could blow past that ~10s window on a slow webhook and get the whole process
        // killed, dropping messages entirely — neither saved nor forwarded.
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        scope.launch {
            try {
                AppDatabase.getInstance(appContext).smsDao().insert(
                    SmsRecord(sender = sender, body = body, timestamp = timestamp)
                )
                enqueueForward(appContext, sender, body)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist/enqueue incoming SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun enqueueForward(context: Context, sender: String, body: String) {
        val request = OneTimeWorkRequestBuilder<ForwardWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ForwardWorker.KEY_SENDER, sender)
                    .putString(ForwardWorker.KEY_BODY, body)
                    .build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
