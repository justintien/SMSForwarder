package com.jiapan.smsforwarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single received SMS, persisted locally so the history survives app restarts.
 */
@Entity(tableName = "sms_records")
data class SmsRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val starred: Boolean = false
)
