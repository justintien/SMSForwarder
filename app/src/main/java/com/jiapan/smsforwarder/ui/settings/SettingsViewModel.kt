package com.jiapan.smsforwarder.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jiapan.smsforwarder.data.db.AppDatabase
import com.jiapan.smsforwarder.data.db.Webhook
import com.jiapan.smsforwarder.data.db.WebhookDao
import com.jiapan.smsforwarder.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val webhookDao: WebhookDao = AppDatabase.getInstance(app).webhookDao()
    private val settings = SettingsRepository(app)

    val receiverMobile: StateFlow<String> = settings.receiverMobile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val webhooks: StateFlow<List<Webhook>> = webhookDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setReceiverMobile(value: String) {
        viewModelScope.launch { settings.setReceiverMobile(value) }
    }

    fun addWebhook(label: String, url: String) {
        viewModelScope.launch {
            webhookDao.upsert(Webhook(label = label.trim(), url = url.trim(), enabled = true))
        }
    }

    fun updateWebhook(webhook: Webhook, label: String, url: String) {
        viewModelScope.launch {
            webhookDao.upsert(webhook.copy(label = label.trim(), url = url.trim()))
        }
    }

    fun setEnabled(webhook: Webhook, enabled: Boolean) {
        viewModelScope.launch { webhookDao.setEnabled(webhook.id, enabled) }
    }

    fun deleteWebhook(webhook: Webhook) {
        viewModelScope.launch { webhookDao.delete(webhook) }
    }
}
