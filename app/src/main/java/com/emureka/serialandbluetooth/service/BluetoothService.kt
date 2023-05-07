package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.emureka.serialandbluetooth.communication.BluetoothCommunication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BluetoothService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val bt = BluetoothCommunication.getInstance(this)!!
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                val msg = "Hello"
                bt.trySend(msg)

                delay(500)
            }
        }
        return START_STICKY
    }
}