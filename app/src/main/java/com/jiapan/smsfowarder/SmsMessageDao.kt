package com.jiapan.smsfowarder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import java.util.Date

@Dao
interface SmsMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessage(smsMessage: SmsMessage)

    @Update
    suspend fun updateSmsMessage(smsMessage: SmsMessage)

    @Delete
    suspend fun deleteSmsMessage(smsMessage: SmsMessage)

    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteSmsMessageById(id: Long)

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    suspend fun getAllSmsMessages(): List<SmsMessage>

    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getSmsMessageById(id: Long): SmsMessage?

    @Query("SELECT * FROM sms_messages WHERE isStarred = 1 ORDER BY timestamp DESC")
    suspend fun getStarredSmsMessages(): List<SmsMessage>

    @Query("UPDATE sms_messages SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarStatus(id: Long, isStarred: Boolean)
}