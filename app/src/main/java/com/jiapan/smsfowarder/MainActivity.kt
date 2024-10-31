package com.jiapan.smsfowarder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.jiapan.smsfowarder.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        Toast.makeText(applicationContext, "onCreate", Toast.LENGTH_LONG).show()
//        Toast.makeText(applicationContext, "onCreate2", Toast.LENGTH_LONG).show()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        requestSmsPermission()
//        requestPermissions(
//            arrayOf(
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.INTERNET,
//                Manifest.permission.READ_CONTACTS
//            ), 0
//        )

        LocalBroadcastManager.getInstance(this).registerReceiver(
            smsBroadcastReceiver, IntentFilter("SMS_RECEIVED")
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(smsBroadcastReceiver)
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog.
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }

    private val requestPermissionLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

        if (isGranted) {
            Toast.makeText(applicationContext, "SMS permission granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "SMS permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private val smsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sender = intent?.getStringExtra("sender")
            val message = intent?.getStringExtra("message")

            if (sender != null && message != null) {
                addSmsToList(sender, message)
            }
        }
    }

    private val smsList = ArrayList<SmsMessageData>()

    fun addSmsToList(sender: String, message: String) {
        smsList.add(SmsMessageData(sender, message))

        // 更新 TextView 的文字
        val textView: TextView = findViewById(R.id.textview_first)
        textView.text = smsList.joinToString(separator = "\n") { smsMessage ->
            smsMessage.sender + ": " + smsMessage.message
        }
    }

}