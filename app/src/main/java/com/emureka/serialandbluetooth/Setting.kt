package com.emureka.serialandbluetooth

import kotlinx.serialization.Serializable

@Serializable
data class Setting(
    val isDark: Boolean = true,
    val isSoundOn: Boolean = false,
    val isEmuActive: Boolean = false,
    val isCameraAtFront: Boolean = false,
    val isInPowerSavingMode: Boolean = false,
)
