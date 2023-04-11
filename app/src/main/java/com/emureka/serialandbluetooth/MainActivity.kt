package com.emureka.serialandbluetooth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.emureka.serialandbluetooth.communication.SerialCommunication
import com.emureka.serialandbluetooth.service.Bird
import com.emureka.serialandbluetooth.ui.main.Connect
import com.emureka.serialandbluetooth.ui.main.Content
import com.emureka.serialandbluetooth.ui.main.MainUi

class MainActivity : ComponentActivity() {

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel_ID"
            val descriptionText = "channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(name, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serial = SerialCommunication(this)

        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            var soundEnable by remember { mutableStateOf(false) }

            var content by remember { mutableStateOf(Content.MAIN) }
            
            MainUi(
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                soundEnable = soundEnable,
                onSoundEnableChange = { soundEnable = it },
                onContentChange = { content = it },
            ) { scaffoldState ->
//                Button(onClick = {
//
//                }) {
//                    Text(text = "chirp")
//                }
                when(content) {
                    // Home
                    Content.MAIN -> {
                        Text(text = "Home")
                    }
                    
                    // Connect (USB and bluetooth)
                    Content.CONNECT -> {
                        Connect(serial, scaffoldState)
                    }
                    
                    // System state
                    Content.CAMERA -> {
                        Text(text = "Camera view")
                    }
                }
            }
        }
        createNotificationChannel()
        startService(Intent(this, Bird::class.java))

        Log.i("USB", "ok")
    }
}