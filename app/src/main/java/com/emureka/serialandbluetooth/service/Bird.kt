package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.datastore.preferences.core.intPreferencesKey
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.R
import com.emureka.serialandbluetooth.Setting
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceScope: CoroutineScope

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bird = MediaPlayer.create(this, R.raw.bird)
        serviceScope = CoroutineScope(Dispatchers.IO)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceScope.launch {
            val dataStore = MyDataStore.getInstance(this@Bird)
            while(true) {
                // if camera say yes then chirp

            }
        }
        return START_STICKY
    }
}