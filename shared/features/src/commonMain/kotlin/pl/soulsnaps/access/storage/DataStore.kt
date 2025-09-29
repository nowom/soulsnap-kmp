package pl.soulsnaps.access.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import okio.FileSystem
import okio.Path.Companion.toPath as toOkioPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { 
            // Create absolute path in temp directory
            val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
            val fileName = producePath()
            tempDir / fileName
        }
    )

internal const val dataStoreFileName = "dice.preferences_pb"