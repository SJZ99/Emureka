package com.emureka.serialandbluetooth.ui.main

import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.emureka.serialandbluetooth.MyDataStore
import com.emureka.serialandbluetooth.Setting
import com.emureka.serialandbluetooth.mediapipe.PoseTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CameraView(
    surface: SurfaceView,
    scaffoldState: ScaffoldState,
) {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenWidthDp

    val context = LocalContext.current
    val dataStore = MyDataStore.getInstance(context)
    var settings = dataStore.settingsFlow.collectAsState(initial = Setting())

    val scope = rememberCoroutineScope()

    var count by remember {
        mutableStateOf(0)
    }

    var modeString by remember {
        mutableStateOf(PoseTracking.mode)
    }

    var reset by remember {
        mutableStateOf(PoseTracking.reset)
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height((height - 240).dp)
        ) {
            Box(
                modifier = Modifier.size((width - 20).dp, (height - 250).dp)
            ) {
                // loadind text
                Text(
                    text = "Loading...",
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(0.dp, (height / 2 - 130).dp)
                )

                // camera view
                AndroidView(
                    factory = { surface },
                    modifier = Modifier.fillMaxSize()
                )

                // reset overlay
                if(reset) {
                    Surface(
                        color = Color(0xDF111111),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Reset pose...",
                            textAlign = TextAlign.Center,
                            fontSize = 40.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(0.dp, (height / 2 - 130).dp)
                        )
                    }
                }

                // count overlay
                if(count != 0) {
                    Surface(
                        color = Color(0xDF111111),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        var id = when(count) {
                            3 -> { com.emureka.serialandbluetooth.R.drawable.three }
                            2 -> { com.emureka.serialandbluetooth.R.drawable.two }
                            else -> { com.emureka.serialandbluetooth.R.drawable.one }
                        }
                        Icon(
                            ImageVector.vectorResource(id = id),
                            contentDescription = "Counter",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }

        }

        // Button
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // reset
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        count = 3
                        // 500 delay for test!!!!!
                        while(count > 0) {
                            delay(500)
                            count -= 1
                        }
                        reset = true
                        PoseTracking.set_ref()
                        while(PoseTracking.reset);
                        Log.d("Reset", "finished")
                        reset = false
                    }
                },
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = "Set Posture")
            }

            // mode
            Button(
                onClick = {
                    val mode = when(settings.value.mode) {
                        (0)-> { 1 }
                        (1)-> { 2 }
                        (2)-> { 0 }
                        else -> 0
                    }
                    dataStore.updateMode(mode)
                    PoseTracking.set_mode(mode)
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Mode changed, Resetting pose is required!", "OK", SnackbarDuration.Long)
                    }
                    modeString = PoseTracking.mode
                },
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = modeString.uppercase())
            }
        }
    }
}