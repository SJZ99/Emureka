package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.emureka.serialandbluetooth.communication.SerialCommunication
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class SerialConnectionService : Service() {

    lateinit var usbManager: UsbManager
    lateinit var scope: CoroutineScope

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager
        scope = CoroutineScope(Dispatchers.IO)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

//        val serial = intent?.getParcelableExtra("Serial") as SerialCommunication?

        scope.launch {

        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}