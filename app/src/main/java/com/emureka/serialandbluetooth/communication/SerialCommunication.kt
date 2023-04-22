package com.emureka.serialandbluetooth.communication

import android.content.*
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.activity.ComponentActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.charset.Charset

class SerialCommunication(
    context: Context
) {
    companion object {
        const val ACTION_USB_PERMISSION = "com.emureka.serialandbluetooth.USB_PERMISSION"
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private lateinit var port: UsbSerialPort
    fun openDevice() {
        val availableDrivers: List<UsbSerialDriver> =
            UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        val driver: UsbSerialDriver = availableDrivers[0]
        val connection: UsbDeviceConnection = usbManager.openDevice(driver.device)
            ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return

        port = driver.ports[0] // Most devices have just one port (port 0)

        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
    }

    fun write(text: String) {
        port.write(text.toByteArray(Charsets.UTF_8), 1000)
    }

    fun read(): String {
        val arr = ByteArray(1024)
        port.read(arr, 1000)
        return arr.toString()
    }


}