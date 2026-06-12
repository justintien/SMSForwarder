package com.jiapan.smsfowarder.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookDao {

    @Query("SELECT * FROM webhooks ORDER BY id ASC")
    fun observeAll(): Flow<List<Webhook>>

    /** Snapshot of enabled webhooks, used by the SMS receiver when forwarding. */
    @Query("SELECT * FROM webhooks WHERE enabled = 1")
    suspend fun getEnabled(): List<Webhook>

    @Upsert
    suspend fun upsert(webhook: Webhook): Long

    @Query("UPDATE webhooks SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Delete
    suspend fun delete(webhook: Webhook)
}
