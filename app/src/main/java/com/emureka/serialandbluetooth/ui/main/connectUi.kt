package com.emureka.serialandbluetooth.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.emureka.serialandbluetooth.communication.SerialCommunication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Connect(
    serial: SerialCommunication,
    scaffoldState: ScaffoldState,
    activity: ComponentActivity
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        UsbInfo(serial = serial)

        Button(
            onClick = {
                if (!serial.openDevice(activity)) {
                    CoroutineScope(Dispatchers.IO).launch {
                        scaffoldState.snackbarHostState.showSnackbar("No devices found!", "OK", SnackbarDuration.Short)
                    }
                }
            },
            modifier = Modifier
                .width(160.dp)
                .height(40.dp)

        ) {
            Text(text = "Connect via USB")
        }
    }
}

@Composable
fun UsbInfo(
    serial: SerialCommunication
) {

    var deviceName by remember {
        mutableStateOf(serial.getDeviceName())
    }

    var productName by remember {
        mutableStateOf(serial.getProductName())
    }

    var serialDes by remember {
        mutableStateOf(serial.getSerial())
    }

    LaunchedEffect(key1 = deviceName) {
        while(true) {
            deviceName = serial.getDeviceName()
            productName = serial.getProductName()
            serialDes = serial.getSerial()
            delay(500)
        }
    }

    Row(
        modifier = Modifier.padding(15.dp)
    ) {
        Column() {
            Row {
                Text(text = "Device Name: ")
                Text(
                    fontStyle = FontStyle.Italic,
                    text = deviceName
                )
            }

            Row {
                Text("Product Name: ")
                Text(
                    fontStyle = FontStyle.Italic,
                    text = productName
                )
            }

            Row {
                Text("Serial: ")
                Text(
                    fontStyle = FontStyle.Italic,
                    text = serialDes
                )
            }

        }

    }
}