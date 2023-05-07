package com.emureka.serialandbluetooth.bluetoothchat.domain.chat
data class BluetoothMessage(
    val message: String

 //   val senderName: String,
 //   val isFromLocalUser: Boolean
)

fun String.toBluetoothMessage(): BluetoothMessage {
    //   val name = substringBeforeLast("#")
    val message = substringAfter("#")
    return BluetoothMessage(
        message = message
        //       senderName = name,
        //     isFromLocalUser = isFromLocalUser
    )
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return " $message".encodeToByteArray()
}

