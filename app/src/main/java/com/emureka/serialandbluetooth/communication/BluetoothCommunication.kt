package com.emureka.serialandbluetooth.communication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothController
import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothCommunication private constructor (
    context: Context,
    private val btController: BluetoothController
) {

    private lateinit var bluetoothManager: BluetoothManager
//    by lazy {
//        applicationContext.getSystemService(BluetoothManager::class.java)
//    }
    private lateinit var bluetoothAdapter: BluetoothAdapter
//    by lazy {
//        bluetoothManager?.adapter
//    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    companion object {
        @Volatile private var instance: BluetoothCommunication? = null
        fun getInstance(context: Context): BluetoothCommunication? {
            return instance
        }
        fun getInstance(context: Context, btController: BluetoothController): BluetoothCommunication {
            return instance ?: synchronized(this) {
                instance ?: BluetoothCommunication(context, btController).also { instance = it }
            }
        }
    }

    fun onCreate(activity: ComponentActivity) {
        bluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        val enableBluetoothLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }

        val permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if(canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }
    }

    suspend fun trySend(message: String): BluetoothMessage? {
        return btController.trySendMessage(message)
    }
}