package com.jiapan.smsfowarder.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Persists user-editable settings that are not list-shaped (currently just the
 * receiver mobile number) via Jetpack DataStore.
 */
class SettingsRepository(private val context: Context) {

    val receiverMobile: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[RECEIVER_MOBILE] ?: "" }

    suspend fun setReceiverMobile(value: String) {
        context.dataStore.edit { prefs -> prefs[RECEIVER_MOBILE] = value }
    }

    companion object {
        private val RECEIVER_MOBILE = stringPreferencesKey("receiver_mobile")
    }
}
