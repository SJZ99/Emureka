package com.emureka.serialandbluetooth.communication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.activity.ComponentActivity
import com.emureka.serialandbluetooth.service.SerialConnectionService
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber


class SerialCommunication private constructor (
    context: Context
) {
    companion object {
        const val ACTION_USB_PERMISSION = "com.emureka.serialandbluetooth.USB_PERMISSION"

        @Volatile private var instance: SerialCommunication? = null
        fun getInstance(context: Context): SerialCommunication {
            return instance ?: synchronized(this) {
                instance ?: SerialCommunication(context).also { instance = it }
            }
        }
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private lateinit var driver: UsbSerialDriver

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val accessory: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        accessory?.apply {
                            val connection = usbManager.openDevice(accessory)

                            port = driver.ports[0] // Most devices have just one port (port 0)

                            port?.open(connection)

                            port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)


                            context.startService(Intent(context, SerialConnectionService::class.java))
                        }
                    } else {
                        Log.d("USB", "permission denied for accessory $accessory")
                    }
                }
            }
        }
    }

    fun openDevice(activity: ComponentActivity): Boolean {
        // Find all available drivers from attached devices.
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            return false
        }

        // Open a connection to the first available driver.
        driver = availableDrivers[0]

        requestUsbPermission(driver.device, activity)
        return true
    }

    fun write(text: String) {
        port?.write(text.toByteArray(), 0);
    }

    fun getSerial(): String {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if(availableDrivers.isEmpty()) {
            return "";
        }
        return availableDrivers[0].device.serialNumber ?: ""
    }

    fun getDeviceName(): String {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if(availableDrivers.isEmpty()) {
            return "";
        }
        return availableDrivers[0]?.device?.deviceName ?: "Not found"
    }

    fun getProductName(): String {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if(availableDrivers.isEmpty()) {
            return "";
        }
        return availableDrivers[0].device.productName ?: ""
    }

    private fun requestUsbPermission(accessory: UsbDevice, activity: ComponentActivity) {
        val usbIntent = Intent(ACTION_USB_PERMISSION)

        val permissionIntent: PendingIntent? = PendingIntent.getBroadcast(activity, 1, usbIntent, PendingIntent.FLAG_MUTABLE)
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        activity.registerReceiver(usbReceiver, filter)

        usbManager.requestPermission(accessory, permissionIntent)
    }

    fun close() {
        port?.close()
        instance = null
    }

}