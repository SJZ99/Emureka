package com.emureka.serialandbluetooth.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.emureka.serialandbluetooth.R
import kotlinx.coroutines.*


class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceJob: Job
    private lateinit var serviceScope: CoroutineScope

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onCreate() {
        super.onCreate()
        bird = MediaPlayer.create(this, R.raw.bird)
        serviceJob = Job()
        serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceScope.launch {
            while(true) {
                bird.start()
                delay(5000)
            }
        }

        return START_STICKY
    }

}