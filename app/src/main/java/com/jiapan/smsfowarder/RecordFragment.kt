package com.jiapan.smsfowarder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Date

class RecordFragment : Fragment() {

    private lateinit var smsMessageAdapter: SmsMessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: SmsMessageDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record, container, false)

        // 初始化資料庫
        database = SmsMessageDatabase.getDatabase(requireContext())

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 初始化Adapter
        smsMessageAdapter = SmsMessageAdapter { smsMessage ->
            // 點擊事件處理
            toggleStarStatus(smsMessage)
        }
        recyclerView.adapter = smsMessageAdapter

        // 加載資料
        loadSmsMessages()

        // 綁定刷新按鈕
        val refreshButton = view.findViewById<Button>(R.id.button_refresh)
        refreshButton.setOnClickListener {
            loadSmsMessages()
        }

        return view
    }

    private fun loadSmsMessages() {
        lifecycleScope.launch {
            try {
                val smsMessages = database.smsMessageDao().getAllSmsMessages()
                smsMessageAdapter.updateMessages(smsMessages)
            } catch (e: Exception) {
                // 處理錯誤
                val errorText = view?.findViewById<TextView>(R.id.textView_error)
                errorText?.text = "載入資料時發生錯誤"
                errorText?.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleStarStatus(smsMessage: SmsMessage) {
        lifecycleScope.launch {
            try {
                database.smsMessageDao().updateStarStatus(smsMessage.id, !smsMessage.isStarred)
                loadSmsMessages() // 重新載入資料
            } catch (e: Exception) {
                // 處理錯誤
            }
        }
    }
}