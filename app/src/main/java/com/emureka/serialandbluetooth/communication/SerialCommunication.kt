package com.emureka.serialandbluetooth.communication

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.activity.ComponentActivity

class SerialCommunication(
    private val activity: ComponentActivity
) {
    companion object {
        const val ACTION_USB_PERMISSION = "com.emureka.serialandbluetooth.USB_PERMISSION"
    }

    private val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val accessory: UsbAccessory? = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        accessory?.apply {
                            // set up device here!!!!!!!!!!!!!!!!!!!!!!
                        }
                    } else {
                        Log.d(ContentValues.TAG, "permission denied for accessory $accessory")
                    }
                }
            }
        }
    }

    fun getAccessoryList(): List<UsbAccessory> {
        val list = mutableListOf<UsbAccessory>()
        val array = usbManager.accessoryList

        // from java, need check null
        if(array != null && array.isNotEmpty()) {
            array.forEach {
                list.add(it)
            }
        }
        return list.toList()
    }

    fun requestUsbPermission(accessory: UsbAccessory) {
        val usbIntent = Intent(ACTION_USB_PERMISSION)

        val permissionIntent: PendingIntent? = PendingIntent.getBroadcast(activity, 1, usbIntent, PendingIntent.FLAG_IMMUTABLE)
        val filter = IntentFilter(ACTION_USB_PERMISSION)

        activity.registerReceiver(usbReceiver, filter)

        usbManager.requestPermission(accessory, permissionIntent)
    }
}