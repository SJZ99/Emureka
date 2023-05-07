package com.emureka.serialandbluetooth

import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothDevice
import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothMessage

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList()
)
