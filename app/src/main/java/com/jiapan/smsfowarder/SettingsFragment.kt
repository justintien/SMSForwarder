package com.jiapan.smsfowarder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

class SettingsFragment : Fragment() {

    private lateinit var editTextWebhook: EditText
    private lateinit var editTextReceiver: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonReset: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // 初始化元件
        editTextWebhook = view.findViewById(R.id.editText_webhook)
        editTextReceiver = view.findViewById(R.id.editText_receiver)
        buttonSave = view.findViewById(R.id.button_save)
        buttonReset = view.findViewById(R.id.button_reset)

        // 載入現有設定
        loadSettings()

        // 綁定儲存按鈕事件
        buttonSave.setOnClickListener {
            saveSettings()
        }

        // 綁定重設按鈕事件
        buttonReset.setOnClickListener {
            resetSettings()
        }

        return view
    }

    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val webhook = prefs.getString("discord_webhook", "")
        val receiver = prefs.getString("receiver_mobile", "")

        editTextWebhook.setText(webhook)
        editTextReceiver.setText(receiver)
    }

    private fun saveSettings() {
        val webhook = editTextWebhook.text.toString().trim()
        val receiver = editTextReceiver.text.toString().trim()

        if (webhook.isEmpty()) {
            editTextWebhook.error = "請輸入Discord Webhook URL"
            editTextWebhook.requestFocus()
            return
        }

        if (receiver.isEmpty()) {
            editTextReceiver.error = "請輸入接收者號碼"
            editTextReceiver.requestFocus()
            return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        with(prefs.edit()) {
            putString("discord_webhook", webhook)
            putString("receiver_mobile", receiver)
            apply()
        }

        Toast.makeText(requireContext(), "設定已儲存", Toast.LENGTH_SHORT).show()
    }

    private fun resetSettings() {
        editTextWebhook.setText("")
        editTextReceiver.setText("")
        Toast.makeText(requireContext(), "已重設設定", Toast.LENGTH_SHORT).show()
    }
}