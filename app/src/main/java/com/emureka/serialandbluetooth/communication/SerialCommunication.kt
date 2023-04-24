package com.emureka.serialandbluetooth.communication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.getSystemService
import com.emureka.serialandbluetooth.service.SerialConnectionService
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SerialCommunication(
    private val activity: ComponentActivity
) {
    companion object {
        const val ACTION_USB_PERMISSION = "com.emureka.serialandbluetooth.USB_PERMISSION"
    }

    private val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
    private lateinit var port: UsbSerialPort
    private lateinit var driver: UsbSerialDriver

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val accessory: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        accessory?.apply {
                            val connection = usbManager.openDevice(accessory)
                            Log.i("USB", "opened1")

                            port = driver.ports[0] // Most devices have just one port (port 0)

                            port.open(connection)
                            Log.i("USB", "opened2")

                            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                            Log.i("USB", "set")

                            CoroutineScope(Dispatchers.IO).launch {
                                while (true) {
                                    write("Hello")
                                    Log.i("USB", "send")
                                    delay(800)
                                }
                            }
                        }
                    } else {
                        Log.d("USB", "permission denied for accessory $accessory")
                    }
                }
            }
        }
    }

    fun openDevice() {
        // Find all available drivers from attached devices.
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        driver = availableDrivers[0]

        requestUsbPermission(driver.device)
        return
    }

    fun write(text: String) {
        port.write(text.toByteArray(), 0);
    }

    private fun requestUsbPermission(accessory: UsbDevice) {
        val usbIntent = Intent(ACTION_USB_PERMISSION)

        val permissionIntent: PendingIntent? = PendingIntent.getBroadcast(activity, 1, usbIntent, PendingIntent.FLAG_MUTABLE)
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        activity.registerReceiver(usbReceiver, filter)

        usbManager.requestPermission(accessory, permissionIntent)
    }

}