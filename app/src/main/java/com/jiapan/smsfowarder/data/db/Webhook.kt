package com.jiapan.smsfowarder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A Discord webhook destination. Multiple may be configured; every enabled one
 * receives a copy of each incoming SMS.
 */
@Entity(tableName = "webhooks")
data class Webhook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val url: String,
    val enabled: Boolean = true
)
