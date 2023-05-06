package com.emureka.serialandbluetooth.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
    var input by remember {
        mutableStateOf("")
    }

    LaunchedEffect(true) {
        while (true) {
            if(serial.isConnect()) {
                serial.read()
                input = String(serial.input)
            }
            delay(400);

        }
    }

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

        Text(input)
    }
}

@Composable
fun UsbInfo(
    serial: SerialCommunication
) {

    var deviceName by remember {
        mutableStateOf("Not found")
    }

    var productName by remember {
        mutableStateOf("Not found")
    }

    var manufacturer by remember {
        mutableStateOf("Not found")
    }

    LaunchedEffect(true) {
        while(true) {
            deviceName = serial.getDeviceName()
            productName = serial.getProductName()
            manufacturer = serial.getManufacturer()
            delay(300)
        }
    }

    Row(
        modifier = Modifier
            .padding(15.dp)
            .border(BorderStroke(3.dp, MaterialTheme.colors.onBackground))
            .padding(15.dp)
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
                Text("Manufacturer: ")
                Text(
                    fontStyle = FontStyle.Italic,
                    text = manufacturer
                )
            }

            Row {
                Text("Product Name: ")
                Text(
                    fontStyle = FontStyle.Italic,
                    text = productName
                )
            }

        }
    }
}