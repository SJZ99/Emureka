package com.emureka.serialandbluetooth.ui.main

import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.emureka.serialandbluetooth.R

@Composable
fun MainUi(
    emuState: Int
) {
    val img = if(emuState == 0) {
        R.drawable.s0
    } else if(emuState == 1) {
        R.drawable.s1
    } else if(emuState == 2) {
        R.drawable.s2
    }
    else{
        R.drawable.s3
    }

    val color = if(MaterialTheme.colors.isLight) {
        Color.Black
    } else {
        Color.White
    }

    Icon(
        painter = painterResource(id = img),
        contentDescription = "emu",
        tint = color,
        modifier = Modifier.absoluteOffset(0.dp, 125.dp)
    )
}