package com.jiapan.smsforwarder.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jiapan.smsforwarder.data.db.AppDatabase
import com.jiapan.smsforwarder.data.db.SmsDao
import com.jiapan.smsforwarder.data.db.SmsRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val dao: SmsDao = AppDatabase.getInstance(app).smsDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _starredOnly = MutableStateFlow(false)
    val starredOnly: StateFlow<Boolean> = _starredOnly.asStateFlow()

    val records: StateFlow<List<SmsRecord>> =
        combine(_query, _starredOnly) { query, starredOnly -> query to starredOnly }
            .flatMapLatest { (query, starredOnly) ->
                if (query.isBlank() && !starredOnly) {
                    dao.observeAll()
                } else {
                    dao.search(query.trim(), starredOnly)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleStarredOnly() {
        _starredOnly.value = !_starredOnly.value
    }

    fun toggleStar(record: SmsRecord) {
        viewModelScope.launch { dao.setStarred(record.id, !record.starred) }
    }

    fun delete(record: SmsRecord) {
        viewModelScope.launch { dao.delete(record) }
    }
}
