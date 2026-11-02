package com.mechat.app.domain.attachment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Attachments: EXIF/location stripped before sending, images compressed sensibly.
 * Received files stay encrypted in app-private storage; decrypted only to a temp
 * cache for viewing, wiped afterward. No storage permission needed (SAF/Photo
 * Picker); RECORD_AUDIO only when recording voice notes.
 */
object AttachmentManager {

    const val DEFAULT_MAX_BYTES = 25 * 1024 * 1024 // 25 MB; configurable in Settings

    /** Strip EXIF (incl. GPS/location) by re-encoding the bitmap. */
    fun stripExif(context: Context, src: File, maxDimension: Int = 2048, quality: Int = 85): ByteArray {
        // Read EXIF only to confirm orientation handling, then re-encode without metadata.
        val exif = ExifInterface(src)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val bmp = BitmapFactory.decodeFile(src.absolutePath) ?: error("undecodable image")
        val scaled = scale(bmp, maxDimension)
        val rotated = rotate(scaled, orientation)
        val out = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, quality, out) // JPEG re-encode drops EXIF
        return out.toByteArray()
    }

    private fun scale(bmp: Bitmap, maxDim: Int): Bitmap {
        val scale = maxDim.toFloat() / maxOf(bmp.width, bmp.height).coerceAtLeast(1)
        if (scale >= 1f) return bmp
        return Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
    }

    private fun rotate(bmp: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bmp
        }
        val m = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    /** App-private encrypted storage for received files. */
    fun receivedFile(context: Context, messageId: String): File =
        File(context.filesDir, "attachments/$messageId.enc").apply { parentFile?.mkdirs() }

    /** Temp cache for viewing only — caller must wipe after viewing. */
    fun tempViewFile(context: Context, messageId: String): File =
        File(context.cacheDir, "view_$messageId").apply { parentFile?.mkdirs() }

    fun wipeCache(context: Context) {
        context.cacheDir.listFiles()?.filter { it.name.startsWith("view_") }?.forEach {
            runCatching { it.writeBytes(ByteArray(it.length().toInt())); it.delete() }
        }
    }
}
