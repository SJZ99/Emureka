package com.emureka.serialandbluetooth.bluetoothchat.data.chat

import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothMessage

fun String.toBluetoothMessage(): BluetoothMessage {
 //   val name = substringBeforeLast("#")
    val message = substringAfter("#")

    return BluetoothMessage(
        message = message

    )
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return "#$message".encodeToByteArray()
}
