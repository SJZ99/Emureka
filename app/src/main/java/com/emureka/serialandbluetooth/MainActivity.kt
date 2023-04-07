package com.emureka.serialandbluetooth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.emureka.serialandbluetooth.communication.SerialCommunication
import com.emureka.serialandbluetooth.ui.main.MainUi

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serial = SerialCommunication(this)
        val list = serial.getAccessoryList()

        if(list.isNotEmpty()) {
            serial.requestUsbPermission(list[0])
        }
        else Log.i("USB", "Devices Not found!")

        setContent {
            var darkTheme by remember {
                mutableStateOf(true)
            }

            var soundEnable by remember {
                mutableStateOf(false)
            }

            MainUi(
                darkTheme = darkTheme,
                onDarkThemeChange = {darkTheme = it},
                soundEnable = soundEnable,
                onSoundEnableChange = {soundEnable = it})
        }
    }
}