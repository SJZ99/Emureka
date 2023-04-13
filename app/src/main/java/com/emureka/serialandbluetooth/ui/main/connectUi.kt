package com.emureka.serialandbluetooth.ui.main

import android.hardware.usb.UsbAccessory
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.emureka.serialandbluetooth.communication.SerialCommunication

@Composable
fun Connect(
    serial: SerialCommunication,
    scaffoldState: ScaffoldState,
) {
    val list = serial.getAccessoryList()
    if(list.isNotEmpty()) {
        // show devices list
        DevicesList(list = list, serial = serial)
    } else {
        // show snack-bar to notify no device found
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                "No devices found!",
                "OK",
                SnackbarDuration.Long
            )
        }
    }
}

@Composable
fun DevicesList(
    list: List<UsbAccessory>,
    serial: SerialCommunication,
) {
    list.forEach {
        DevicesListItem(
            name = it.model,
            description = (it.description ?: it.model)
        ) {
            serial.requestUsbPermission(it)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DevicesListItem(name: String, description: String, onClick: () -> Unit = {}) {
    ListItem(
        secondaryText = { Text(description) },
        text = { Text(name) },
        modifier = Modifier.clickable(onClick = onClick)
    )

    Divider()
}