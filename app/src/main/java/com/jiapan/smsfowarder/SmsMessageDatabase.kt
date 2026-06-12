package com.jiapan.smsfowarder

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [SmsMessage::class],
    version = 1,
    exportSchema = false
)
abstract class SmsMessageDatabase : RoomDatabase() {
    abstract fun smsMessageDao(): SmsMessageDao

    companion object {
        @Volatile
        private var INSTANCE: SmsMessageDatabase? = null

        fun getDatabase(context: Context): SmsMessageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsMessageDatabase::class.java,
                    "sms_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}