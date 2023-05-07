package com.emureka.serialandbluetooth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.emureka.serialandbluetooth.bluetoothchat.data.chat.AndroidBluetoothController
import com.emureka.serialandbluetooth.communication.BluetoothCommunication
import com.emureka.serialandbluetooth.communication.SerialCommunication
import com.emureka.serialandbluetooth.mediapipe.PoseTracking
import com.emureka.serialandbluetooth.service.Bird
import com.emureka.serialandbluetooth.ui.main.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var pose : PoseTracking
    private lateinit var dataStore: MyDataStore
    private lateinit var surface: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serial = SerialCommunication.getInstance(this)
        val btController = AndroidBluetoothController(this)
        val bt = BluetoothCommunication.getInstance(this, btController)

        pose = PoseTracking(this)
        surface = SurfaceView(this)
        dataStore = MyDataStore.getInstance(this)

        startService(Intent(this, Bird::class.java))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bt.onCreate(this)

        setContent {

            val setting = dataStore.settingsFlow.collectAsState(initial = Setting())

            var content by remember { mutableStateOf(Content.MAIN) }

            val viewModel = viewModels<BluetoothViewModel>(factoryProducer = { BluetoothViewModelFactory(btController) })

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
                        LaunchedEffect(true) {
                            if(!PoseTracking.isReset()) {
                                dataStore.updateEmuState(0)
                            }
                        }
                        MainUi(emuState = setting.value.emuState)
                    }

                    // Connect (USB and bluetooth)
                    Content.CONNECT -> {
                        Connect(
                            serial = serial,
                            scaffoldState = scaffoldState,
                            activity = this@MainActivity,
                            viewModel = viewModel.value
                        )
                    }

                    // Camera
                    Content.CAMERA -> {
                        LaunchedEffect(key1 = content) {
                            pose.requestPermission()
                            pose.onPause()
                            delay(50)
                            pose.onResume()
                        }
                        CameraView(surface = surface, scaffoldState)
                    }
                }
            }
        }
//        setContentView(R.layout.activity_main)
        pose.onCreate(surface)

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        print(permissions[0])
        print("**************************")
        pose.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onPause(){
        super.onPause()
        pose.onPause()
    }
    override fun onResume(){
        super.onResume()
        pose.onResume()
    }

}