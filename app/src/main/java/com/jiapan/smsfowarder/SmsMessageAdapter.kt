package com.jiapan.smsfowarder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsMessageAdapter(
    private val onStarClick: (SmsMessage) -> Unit
) : RecyclerView.Adapter<SmsMessageAdapter.SmsMessageViewHolder>() {

    private var smsMessages = listOf<SmsMessage>()

    class SmsMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderTextView: TextView = view.findViewById(R.id.textView_sender)
        val messageTextView: TextView = view.findViewById(R.id.textView_message)
        val timeTextView: TextView = view.findViewById(R.id.textView_time)
        val starButton: ImageButton = view.findViewById(R.id.button_star)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms_message, parent, false)
        return SmsMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsMessageViewHolder, position: Int) {
        val smsMessage = smsMessages[position]
        holder.senderTextView.text = smsMessage.sender
        holder.messageTextView.text = smsMessage.message

        // 格式化時間
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.timeTextView.text = dateFormat.format(smsMessage.timestamp)

        // 設置星號按鈕狀態
        holder.starButton.setImageResource(
            if (smsMessage.isStarred) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
        )

        // 設置星號按鈕點擊事件
        holder.starButton.setOnClickListener {
            onStarClick(smsMessage)
        }
    }

    override fun getItemCount(): Int = smsMessages.size

    fun updateMessages(newMessages: List<SmsMessage>) {
        this.smsMessages = newMessages
        notifyDataSetChanged()
    }
}