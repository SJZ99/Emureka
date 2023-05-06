package com.emureka.serialandbluetooth.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emureka.serialandbluetooth.R
import com.emureka.serialandbluetooth.ui.theme.SerialAndBluetoothTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class Content {
    MAIN, CAMERA, CONNECT
}

@Composable
fun MyScaffold(darkTheme: Boolean,
           onDarkThemeChange: (Boolean) -> Unit,
           soundEnable: Boolean,
           onSoundEnableChange: (Boolean) -> Unit,
           onContentChange: (Content) -> Unit,
           content: @Composable BoxScope.(ScaffoldState) -> Unit = { _ -> }
) {

    SerialAndBluetoothTheme(darkTheme = darkTheme) {
        val scaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()

        Scaffold(
            // State
            scaffoldState = scaffoldState,

            // Header
            topBar = { AppBar(scope = scope, scaffoldState = scaffoldState) },

            // Navigation Drawer
            drawerContent = {
                Text(
                    text = "Tools",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.primary
                )

                Divider()

                DrawerButton(
                    text = "Home",
                    icon = Icons.Filled.Home
                ) {
                    // callback
                    onContentChange(Content.MAIN)

                    // close drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }

                DrawerButton(text = "Add Devices", icon = Icons.Filled.Add) {
                    // callback
                    onContentChange(Content.CONNECT)

                    // close drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }

                DrawerButton(
                    text = "Camera View",
                    icon = ImageVector.vectorResource(id = R.drawable.camera)
                ) {
                    // callback
                    onContentChange(Content.CAMERA)

                    // close drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }

                DrawerSwitch(
                    icon = ImageVector.vectorResource(id = R.drawable.moon),
                    description = "Dark Theme",
                    checked = darkTheme,
                    onCheckedChange = onDarkThemeChange
                )

                DrawerSwitch(
                    icon = ImageVector.vectorResource(id = R.drawable.sound),
                    description = "Sound",
                    checked = soundEnable,
                    onCheckedChange = onSoundEnableChange
                )
            }, // end of drawer
        ) {

            // Main content
            Box(modifier = Modifier.padding(it)) {
                content(scaffoldState)
            }
        }
    }
}

@Composable
fun AppBar(scope: CoroutineScope, scaffoldState: ScaffoldState) {
    TopAppBar(
        title = {
            Text(
                text = "Emu",
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Navigation Icon")
            }
        }
    )
}

@Composable
fun DrawerButton(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Icon(
            icon,
            contentDescription = text,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text,
            fontSize = 17.sp,
        )
    }
}

@Composable
fun DrawerSwitch (icon: ImageVector, description: String = "", checked: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    Card (
        elevation = 0.dp,
        modifier = Modifier
            .padding(vertical = 13.dp)
            .height(30.dp)
            .fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.Center) {

            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxHeight()
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.fillMaxHeight()
            )
        }

    }
}