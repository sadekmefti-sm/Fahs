package com.example.core.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object InspectionPhotoHelper {

    private const val PHOTOS_DIR = "inspection_photos"
    private const val CAMERA_TEMP_DIR = "camera_temp"
    private const val MAX_PHOTO_DIMENSION = 1280
    private const val JPEG_QUALITY = 82

    fun createTempCameraUri(context: Context, itemId: String): Pair<File, Uri> {
        val tempDir = File(context.cacheDir, CAMERA_TEMP_DIR).apply { mkdirs() }
        val safeId = itemId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val tempFile = File(tempDir, "capture_${safeId}_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        return tempFile to uri
    }

    /**
     * Copies and downsamples an image from a gallery Uri into the app's persistent
     * `filesDir/inspection_photos/` directory so it survives app restarts and can be
     * embedded in PDF reports. Returns the absolute path of the saved image, or null if
     * the image could not be decoded.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, itemId: String): String? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }
            val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, MAX_PHOTO_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            saveDownsampledBitmap(context, bitmap, itemId)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Copies and downsamples a photo captured by the camera into the app's persistent
     * `filesDir/inspection_photos/` directory. Returns the absolute path of the saved image,
     * or null if the capture failed or was cancelled.
     */
    fun copyFileToInternalStorage(context: Context, sourceFile: File, itemId: String): String? {
        if (!sourceFile.exists() || sourceFile.length() <= 0L) return null
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceFile.absolutePath, boundsOptions)
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return null
            }
            val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, MAX_PHOTO_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions) ?: return null
            val savedPath = saveDownsampledBitmap(context, bitmap, itemId)
            try {
                sourceFile.delete()
            } catch (_: Exception) {}
            savedPath
        } catch (_: Exception) {
            null
        }
    }

    fun loadThumbnailBitmap(path: String?, maxDimensionPx: Int = 360): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() <= 0L) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimensionPx)
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads, resizes, and JPEG-compresses a photo thumbnail specifically for embedding in A4 PDF reports
     * so the resulting PDF file stays compact and fast to send on WhatsApp.
     */
    fun loadCompressedPdfThumbnailBitmap(
        path: String?,
        maxDimensionPx: Int = 180,
        jpegQuality: Int = 72
    ): Bitmap? {
        val raw = loadThumbnailBitmap(path, maxDimensionPx) ?: return null
        return try {
            val scaled = scaleBitmapIfNeeded(raw, maxDimensionPx)
            val baos = java.io.ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)
            if (scaled !== raw) {
                raw.recycle()
            }
            val bytes = baos.toByteArray()
            val decodeOpts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts) ?: scaled
        } catch (_: Exception) {
            raw
        }
    }

    fun isRealStoredPhoto(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return file.exists() && file.isFile && file.length() > 0L
    }

    private fun saveDownsampledBitmap(context: Context, bitmap: Bitmap, itemId: String): String? {
        val photosDir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val safeId = itemId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val destFile = File(photosDir, "photo_${safeId}_${System.currentTimeMillis()}.jpg")
        val scaled = scaleBitmapIfNeeded(bitmap, MAX_PHOTO_DIMENSION)
        FileOutputStream(destFile).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.flush()
        }
        if (scaled !== bitmap) {
            bitmap.recycle()
        }
        return if (destFile.exists() && destFile.length() > 0L) destFile.absolutePath else null
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val largest = maxOf(width, height)
        while (largest / inSampleSize > maxDim * 2) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val largest = maxOf(width, height)
        if (largest <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / largest.toFloat()
        val targetW = (width * ratio).toInt().coerceAtLeast(1)
        val targetH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
