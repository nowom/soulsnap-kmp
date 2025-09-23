package pl.soulsnaps.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.Objects
import pl.soulsnaps.utils.getCurrentTimeMillis

class ComposeFileProvider : FileProvider() {
    companion object {

        fun getImageUri(context: Context): Uri {
            val tempFile = File.createTempFile(
                "picture_${getCurrentTimeMillis()}", ".png", context.cacheDir
            ).apply {
                createNewFile()
            }
            // 2
            val authority = context.applicationContext.packageName + ".provider"
            // 3
            println("getImageUri: ${tempFile.absolutePath}")
            return getUriForFile(
                Objects.requireNonNull(context),
                authority,
                tempFile,
            )
        }
    }
}