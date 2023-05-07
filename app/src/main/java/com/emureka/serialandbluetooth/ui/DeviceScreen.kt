package com.emureka.serialandbluetooth.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emureka.serialandbluetooth.bluetoothchat.domain.chat.BluetoothDevice
import com.emureka.serialandbluetooth.BluetoothUiState

@Composable
fun DeviceScreen(
    state: BluetoothUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
//    onStartServer: () -> Unit,
    onDeviceClick: (BluetoothDevice, Context) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
            }
//            Button(onClick = onStartServer) {
//                Text(text = "Start")
//            }
        }
        BluetoothDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            onClick = onDeviceClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

    }
}

@Composable
fun BluetoothDeviceList(
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice, Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.DarkGray)
            .verticalScroll(rememberScrollState())
    ) {
        LazyColumn(
            modifier = modifier

        ) {
            item {
                Text(
                    text = "Paired Devices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(pairedDevices) { device ->
                Text(
                    text = device.name ?: "(No name)",

                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(device, context) }
                        .padding(16.dp)
                )
            }

            item {
                Text(
                    text = "Scanned Devices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(scannedDevices) { device ->
                Text(
                    text = device.name ?: "(null)",
                    modifier = if(device.name==null)
                        Modifier
                            .height(0.dp)
                    else
                        Modifier
                            .fillMaxWidth()
                            .clickable { onClick(device, context) }
                            .padding(16.dp)
                )
            }
        }
    }

}