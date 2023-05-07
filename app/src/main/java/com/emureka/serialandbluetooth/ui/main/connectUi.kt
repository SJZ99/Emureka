package com.emureka.serialandbluetooth.ui.main

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.emureka.serialandbluetooth.BluetoothViewModel
import com.emureka.serialandbluetooth.bluetoothchat.presentation.components.ChatScreen
import com.emureka.serialandbluetooth.ui.DeviceScreen
import com.emureka.serialandbluetooth.communication.SerialCommunication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Connect(
    serial: SerialCommunication,
    scaffoldState: ScaffoldState,
    activity: ComponentActivity,
    viewModel: BluetoothViewModel,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        Usb(
            serial = serial,
            scaffoldState = scaffoldState,
            activity = activity
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(300.dp)
        ) {
            Bluetooth(
                viewModel = viewModel,
                activity = activity,
            )
        }


    }
}

@Composable
fun Bluetooth(
    viewModel: BluetoothViewModel,
    activity: ComponentActivity,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { message ->
            Toast.makeText(
                activity,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(key1 = state.isConnected) {
        if(state.isConnected) {
            Toast.makeText(
                activity,
                "You're connected!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Surface(
        color = MaterialTheme.colors.background
    ) {
        when {
            state.isConnecting -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(text = "Connecting...")
                }
            }
            state.isConnected -> {
                ChatScreen(
                    state = state,
                    onDisconnect = viewModel::disconnectFromDevice,
                    onSendMessage = viewModel::sendMessage
                )
            }
            else -> {
                DeviceScreen(
                    state = state,
                    onStartScan = viewModel::startScan,
                    onStopScan = viewModel::stopScan,
                    onDeviceClick = viewModel::connectToDevice,
//                    onStartServer = viewModel::waitForIncomingConnections
                )
            }
        }
    }
}

@Composable
fun Usb(
    serial: SerialCommunication,
    scaffoldState: ScaffoldState,
    activity: ComponentActivity,
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