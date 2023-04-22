package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.R
import com.emureka.serialandbluetooth.Setting
import com.emureka.serialandbluetooth.communication.SerialCommunication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceScope: CoroutineScope
    private var isSoundOn = false
    private lateinit var serial: SerialCommunication

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bird = MediaPlayer.create(this, R.raw.bird)
        serviceScope = CoroutineScope(Dispatchers.IO)
        serial = SerialCommunication(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceScope.launch {
            val dataStore = MyDataStore.getInstance(this@Bird)
            dataStore.settingsFlow.collect {
                this@Bird.isSoundOn = it.isSoundOn
//                Log.i("USB", "${this@Bird.isSoundOn}")
            }
            serial.openDevice()
            while(true) {
                serial.write("Hello\n")
                Log.i("USB", serial.read());

            }
        }
        return START_STICKY
    }
}