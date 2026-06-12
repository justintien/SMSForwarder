package com.jiapan.smsfowarder

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sender: String,
    val message: String,
    val timestamp: Date,
    val isStarred: Boolean = false
)