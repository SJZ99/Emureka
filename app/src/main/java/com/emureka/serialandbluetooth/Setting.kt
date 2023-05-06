package com.emureka.serialandbluetooth

import kotlinx.serialization.Serializable

@Serializable
data class Setting(
    val isDark: Boolean = true,
    val isSoundOn: Boolean = false,
    val emuState: Int = 0,
    val mode: Int = 0,
    val isInPowerSavingMode: Boolean = false,
)
