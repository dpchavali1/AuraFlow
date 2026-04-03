package com.auraflow.garden.platform.sharing

/**
 * Cross-platform share sheet.
 * Android: Intent.ACTION_SEND with chooser + FileProvider
 * iOS: UIActivityViewController from root UIViewController
 */
expect class ShareManager {
    suspend fun shareImage(imageBytes: ByteArray, text: String, mimeType: String = "image/png")
    suspend fun shareText(text: String)
}
