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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceScope: CoroutineScope
    private var isSoundOn = false

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
            dataStore.settingsFlow.collect {
                this@Bird.isSoundOn = it.isSoundOn
//                Log.i("USB", "${this@Bird.isSoundOn}")
            }

            while(true) {
                // if camera say yes then chirp
//                if(this@Bird.isSoundOn) {
                    bird.start()
                    delay(7000)
//                }
            }
        }
        return START_STICKY
    }
}