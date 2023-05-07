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
    val input = ByteArray(1024)

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

    private val detachedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    port?.close()
                    port = null
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

    fun write(command: String) {
        port?.write(command.toByteArray(), 0);
    }

    fun read() {
        port?.read(input, 0)
    }

    fun isConnect(): Boolean {
        return port != null
    }

    fun getManufacturer(): String {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if(availableDrivers.isEmpty()) {
            return "Not found"
        }
        return availableDrivers[0]?.device?.manufacturerName ?: "Not found"
    }

    fun getDeviceName(): String {

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (availableDrivers.isEmpty()) {
            return "Not found";
        }
//        availableDrivers[0]?.device?.deviceName
        return availableDrivers[0]?.device?.deviceName ?: "Not found"
//        return "Not found"
    }


    fun getProductName(): String {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if(availableDrivers.isEmpty()) {
            return "Not found";
        }
        return availableDrivers[0]?.device?.productName ?: "Not found"
    }

    private fun requestUsbPermission(accessory: UsbDevice, activity: ComponentActivity) {
        val usbIntent = Intent(ACTION_USB_PERMISSION)

        val permissionIntent: PendingIntent? = PendingIntent.getBroadcast(activity, 1, usbIntent, PendingIntent.FLAG_MUTABLE)
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        activity.registerReceiver(usbReceiver, filter)
        activity.registerReceiver(detachedReceiver, IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED))

        usbManager.requestPermission(accessory, permissionIntent)
    }

    fun close() {
        port?.close()
        instance = null
    }

}