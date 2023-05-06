package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.R
import com.emureka.serialandbluetooth.mediapipe.PoseTracking
import kotlinx.atomicfu.AtomicInt
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Bird : Service() {
    private lateinit var bird: MediaPlayer
    private lateinit var serviceScope: CoroutineScope
    private var isSoundOn = AtomicBoolean(false)
    private var emuState = AtomicInteger(0)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        bird = MediaPlayer.create(this, R.raw.bird)
        serviceScope = CoroutineScope(Dispatchers.IO)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val dataStore = MyDataStore.getInstance(this@Bird)

        // coroutine for collect isSoundOn
        serviceScope.launch {
            dataStore.settingsFlow.collect {
                this@Bird.isSoundOn.set(it.isSoundOn)
            }
        }

        // coroutine for collect emuState
        serviceScope.launch {
            dataStore.settingsFlow.collect {
                this@Bird.emuState.set(it.emuState)
                Log.d("Bird_", "${this@Bird.emuState} ${it.emuState}")
            }
        }

        // bird chirping
        serviceScope.launch {
            var cuml = 0
            while (true) {
                if(!PoseTracking.isReset()) {
                    delay(200)
                    continue
                };
                PoseTracking.update_current_state(dataStore)
//                Log.d("Bird", "$isSoundOn $emuState")
//                Log.d("Bird"," " + PoseTracking.currStat)
                if(isSoundOn.get() && (emuState.get() != 0) && PoseTracking.isReset()) {
                    cuml += 1
                    if(cuml>=5) {
                        bird.start()
                        delay(1000)
                    }
                }
                else{
                    cuml = 0
                }
                delay(450)
            }
        }
        return START_STICKY
    }
}