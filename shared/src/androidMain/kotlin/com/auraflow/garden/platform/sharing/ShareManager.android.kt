package com.auraflow.garden.platform.sharing

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.auraflow.garden.platform.PlatformContext
import java.io.File

actual class ShareManager(private val platformContext: PlatformContext) {

    actual suspend fun shareImage(imageBytes: ByteArray, text: String, mimeType: String) {
        val context = platformContext.context
        val cacheDir = File(context.cacheDir, "shared")
        cacheDir.mkdirs()
        val imageFile = File(cacheDir, "crescendo_${System.currentTimeMillis()}.png")
        imageFile.writeBytes(imageBytes)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share your Crescendo")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    actual suspend fun shareText(text: String) {
        val context = platformContext.context
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
