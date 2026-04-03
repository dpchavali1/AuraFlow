package com.auraflow.garden.platform.sharing

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

actual class ShareManager {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun shareImage(imageBytes: ByteArray, text: String, mimeType: String) {
        val nsData = imageBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
        }
        val image = UIImage(data = nsData)
        val items: List<Any> = listOf(image, text)
        presentActivityViewController(items)
    }

    actual suspend fun shareText(text: String) {
        presentActivityViewController(listOf(text))
    }

    private fun presentActivityViewController(items: List<Any>) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return
        val activityVC = UIActivityViewController(
            activityItems = items,
            applicationActivities = null,
        )
        rootViewController.presentViewController(activityVC, animated = true, completion = null)
    }
}
