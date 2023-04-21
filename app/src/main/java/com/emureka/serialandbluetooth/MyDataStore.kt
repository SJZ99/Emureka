package com.emureka.serialandbluetooth

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
private class SettingSerializer @Inject constructor() : Serializer<Setting> {

    override val defaultValue = Setting(
        isDark = true,
        isSoundOn = false,
        isEmuActive = false,
        isCameraAtFront = false,
        isInPowerSavingMode = false,
    )

    override suspend fun readFrom(input: InputStream): Setting =
        try {
            Json.decodeFromString(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: Setting, output: OutputStream) {
        output.write(
            Json.encodeToString(t)
                .encodeToByteArray()
        )
    }
}

class MyDataStore private constructor(context: Context) {

    private val dataStore: DataStore<Setting> = MultiProcessDataStoreFactory.create(
            serializer = SettingSerializer(),
            produceFile = {
                File("${context.cacheDir.path}/emu.preferences_pb")
            },
            scope = CoroutineScope(Dispatchers.IO)
        )

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        @Volatile private var instance: MyDataStore? = null

        fun getInstance(context: Context): MyDataStore {
            return instance ?: synchronized(this) {
                instance ?: MyDataStore(context).also { instance = it }
            }
        }
    }

    fun updateIsDark(isDark: Boolean) {
        scope.launch {
            dataStore.updateData { curr ->
                Setting(
                    isDark = isDark,
                    isCameraAtFront = curr.isCameraAtFront,
                    isEmuActive = curr.isEmuActive,
                    isInPowerSavingMode = curr.isInPowerSavingMode,
                    isSoundOn = curr.isSoundOn
                )
            }
        }
    }

    fun updateIsActive(isActive: Boolean) {
        scope.launch {
            dataStore.updateData { curr ->
                Setting(
                    isDark = curr.isDark,
                    isCameraAtFront = curr.isCameraAtFront,
                    isEmuActive = isActive,
                    isInPowerSavingMode = curr.isInPowerSavingMode,
                    isSoundOn = curr.isSoundOn
                )
            }
        }

    }

    fun updateIsCameraAtFront(isCameraAtFront: Boolean) {
        scope.launch {
            dataStore.updateData { curr ->
                Setting(
                    isDark = curr.isDark,
                    isCameraAtFront = isCameraAtFront,
                    isEmuActive = curr.isEmuActive,
                    isInPowerSavingMode = curr.isInPowerSavingMode,
                    isSoundOn = curr.isSoundOn
                )
            }
        }
    }

    fun updateIsInPowerSavingMode(isInPowerSavingMode: Boolean) {
        scope.launch {
            dataStore.updateData { curr ->
                Setting(
                    isDark = curr.isDark,
                    isCameraAtFront = curr.isCameraAtFront,
                    isEmuActive = curr.isEmuActive,
                    isInPowerSavingMode = isInPowerSavingMode,
                    isSoundOn = curr.isSoundOn
                )
            }
        }
    }

    fun updateIsSoundOn(isSoundOn: Boolean) {
        scope.launch {
            dataStore.updateData { curr ->
                Setting(
                    isDark = curr.isDark,
                    isCameraAtFront = curr.isCameraAtFront,
                    isEmuActive = curr.isEmuActive,
                    isInPowerSavingMode = curr.isInPowerSavingMode,
                    isSoundOn = isSoundOn
                )
            }
        }
    }

    val settingsFlow: Flow<Setting> = dataStore.data
}
