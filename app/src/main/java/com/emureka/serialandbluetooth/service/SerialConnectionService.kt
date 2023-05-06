package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.communication.SerialCommunication
import com.emureka.serialandbluetooth.mediapipe.PoseTracking
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class SerialConnectionService : Service() {

    private lateinit var usbManager: UsbManager
    private lateinit var scope: CoroutineScope
    private lateinit var serial: SerialCommunication
    private val dataStore = MyDataStore.getInstance(this)
    private var emuState = 0

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager
        scope = CoroutineScope(Dispatchers.IO)
        serial = SerialCommunication.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        scope.launch {
            dataStore.settingsFlow.collect {
                this@SerialConnectionService.emuState = it.emuState
            }
        }

        scope.launch {
            while (true) {

                serial.write("$emuState ${PoseTracking.poseOffset[0]} ${PoseTracking.poseOffset[1]} ${PoseTracking.poseOffset[2]}\n")
                delay(500)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serial.close()
    }

}