package com.jiapan.smsfowarder.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Query("SELECT * FROM sms_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SmsRecord>>

    /**
     * Full-text-ish search over sender and body. Pass the raw query; the DAO wraps it with %.
     * When [starredOnly] is true, only starred records are returned.
     */
    @Query(
        """
        SELECT * FROM sms_records
        WHERE (sender LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%')
          AND (:starredOnly = 0 OR starred = 1)
        ORDER BY timestamp DESC
        """
    )
    fun search(query: String, starredOnly: Boolean): Flow<List<SmsRecord>>

    @Insert
    suspend fun insert(record: SmsRecord): Long

    @Query("UPDATE sms_records SET starred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Delete
    suspend fun delete(record: SmsRecord)

    @Query("DELETE FROM sms_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
