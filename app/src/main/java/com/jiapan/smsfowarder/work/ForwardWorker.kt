package com.jiapan.smsfowarder.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jiapan.smsfowarder.data.DiscordSender
import com.jiapan.smsfowarder.data.db.AppDatabase
import com.jiapan.smsfowarder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first

/**
 * Forwards a single already-persisted SMS to the configured Discord webhooks.
 *
 * The SMS is saved to the database by the receiver *before* this worker is enqueued,
 * so forwarding here can be retried freely (with WorkManager's backoff) without any
 * risk of losing the message. This is what makes delivery survive a slow network,
 * Doze, or the app process being killed mid-forward.
 */
class ForwardWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.success()
        val body = inputData.getString(KEY_BODY) ?: ""

        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val webhooks = db.webhookDao().getEnabled()
            val receiver = SettingsRepository(applicationContext).receiverMobile.first()

            val allSucceeded = DiscordSender.send(webhooks, receiver, sender, body)
            when {
                allSucceeded -> Result.success()
                runAttemptCount >= MAX_ATTEMPTS -> {
                    Log.e(TAG, "Giving up forwarding from $sender after $runAttemptCount attempts")
                    Result.failure()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding SMS from $sender", e)
            if (runAttemptCount >= MAX_ATTEMPTS) Result.failure() else Result.retry()
        }
    }

    companion object {
        private const val TAG = "ForwardWorker"
        private const val MAX_ATTEMPTS = 5

        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
    }
}
