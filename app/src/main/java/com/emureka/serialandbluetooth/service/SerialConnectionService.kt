package com.emureka.serialandbluetooth.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class SerialConnectionService : Service() {

    lateinit var usbManager: UsbManager
    lateinit var inputStream: FileInputStream
    lateinit var outputStream: FileOutputStream
    lateinit var scope: CoroutineScope

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager
        scope = CoroutineScope(Dispatchers.IO)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val accessory = intent?.getParcelableExtra("Accessory") as UsbAccessory?
        val fileDescriptor: ParcelFileDescriptor? = usbManager.openAccessory(accessory)

        fileDescriptor?.let {
            val fd: FileDescriptor = it.fileDescriptor
            inputStream = FileInputStream(fd)
            outputStream = FileOutputStream(fd)

            // communication here
            scope.launch {

            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        inputStream.close()
        outputStream.close()
    }

}