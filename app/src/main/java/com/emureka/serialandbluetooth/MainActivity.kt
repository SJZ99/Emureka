package com.emureka.serialandbluetooth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emureka.serialandbluetooth.communication.SerialCommunication
import com.emureka.serialandbluetooth.service.Bird
import com.emureka.serialandbluetooth.ui.main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.*

class MainActivity : ComponentActivity() {

    private lateinit var dataStore: MyDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serial = SerialCommunication(this)


        dataStore = MyDataStore.getInstance(this)
        setContent {

            val setting = dataStore.settingsFlow.collectAsState(initial = Setting())

            var content by remember { mutableStateOf(Content.MAIN) }

            MyScaffold (
                darkTheme = setting.value.isDark,
                onDarkThemeChange = { dataStore.updateIsDark(it)  },
                soundEnable = setting.value.isSoundOn,
                onSoundEnableChange = { dataStore.updateIsSoundOn(it) },
                onContentChange = { content = it },
            ) { scaffoldState ->

                when(content) {
                    // Home
                    Content.MAIN -> {
                        Button(onClick = { dataStore.updateIsActive(!setting.value.isEmuActive) }) {
                            Text("EMUUUUU")
                        }
                        MainUi(isActive = setting.value.isEmuActive)
                    }

                    // Connect (USB and bluetooth)
                    Content.CONNECT -> {
                        Connect(serial, scaffoldState)
                    }

                    // System state
                    Content.CAMERA -> {
                        Text(text = "Camera view")
                    }
                }
            }
        }

        startService(Intent(this, Bird::class.java))
    }
}