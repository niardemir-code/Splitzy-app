package com.apleq.app.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    private const val MAX_ICON_DIMENSION = 256
    private const val COMPRESSION_QUALITY = 85

    /**
     * Sube una imagen personalizada a Firebase Storage y devuelve su URL de descarga.
     */
    suspend fun uploadImageToStorage(
        context: Context,
        imagePathOrUri: String,
        storagePath: String,
        maxDim: Int = MAX_ICON_DIMENSION,
        quality: Int = COMPRESSION_QUALITY
    ): String? {
        return try {
            if (imagePathOrUri.isBlank()) return null
            val bitmap = if (imagePathOrUri.startsWith("content://") || imagePathOrUri.startsWith("file://")) {
                decodeSampledBitmapFromUri(context, Uri.parse(imagePathOrUri), maxDim, maxDim)
            } else {
                val file = File(imagePathOrUri)
                if (!file.exists()) return null
                decodeSampledBitmapFromFile(file.absolutePath, maxDim, maxDim)
            } ?: return null

            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val bytes = byteStream.toByteArray()
            bitmap.recycle()

            val ref = FirebaseStorage.getInstance().reference.child(storagePath)
            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
            ref.putBytes(bytes, metadata).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Guarda una imagen seleccionada desde la galería optimizándola a resolución mínima
     * (~256x256 px JPEG/PNG, ~15-25 KB) para no saturar memoria ni almacenamiento.
     */
    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val bitmap = decodeSampledBitmapFromUri(context, sourceUri, MAX_ICON_DIMENSION, MAX_ICON_DIMENSION)
                ?: return null

            val iconsDir = File(context.filesDir, "subscription_icons").apply {
                if (!exists()) mkdirs()
            }

            val fileName = "icon_${System.currentTimeMillis()}.jpg"
            val destFile = File(iconsDir, fileName)

            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out)
                out.flush()
            }
            bitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte una imagen (por ruta local o URI) en una cadena Base64 compacta y ligera
     * ideal para incluir dentro del JSON de copia de seguridad o sincronización en la nube.
     */
    fun imageToBase64(
        context: Context,
        imagePathOrUri: String,
        maxDim: Int = MAX_ICON_DIMENSION,
        quality: Int = COMPRESSION_QUALITY
    ): String? {
        return try {
            if (imagePathOrUri.isBlank()) return null
            val bitmap = if (imagePathOrUri.startsWith("content://") || imagePathOrUri.startsWith("file://")) {
                decodeSampledBitmapFromUri(context, Uri.parse(imagePathOrUri), maxDim, maxDim)
            } else {
                val file = File(imagePathOrUri)
                if (!file.exists()) return null
                decodeSampledBitmapFromFile(file.absolutePath, maxDim, maxDim)
            } ?: return null

            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val bytes = byteStream.toByteArray()
            bitmap.recycle()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restaura una imagen guardada en Base64 en el almacenamiento interno privado de la app
     * y devuelve la ruta absoluta del archivo para asociarla a la suscripción.
     */
    fun saveBase64Image(context: Context, base64String: String, identifier: String = "icon"): String? {
        return try {
            if (base64String.isBlank()) return null
            val cleanBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }.trim().replace("\n", "").replace("\r", "")
            
            val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            if (bytes == null || bytes.isEmpty()) return null

            val iconsDir = File(context.filesDir, "subscription_icons").apply {
                if (!exists()) mkdirs()
            }

            val fileName = "icon_${identifier}.jpg"
            val destFile = File(iconsDir, fileName)

            FileOutputStream(destFile).use { out ->
                out.write(bytes)
                out.flush()
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Guarda un array de bytes directamente en el almacenamiento interno privado.
     */
    fun saveByteArrayImage(context: Context, bytes: ByteArray, identifier: String = "icon"): String? {
        return try {
            if (bytes.isEmpty()) return null
            val iconsDir = File(context.filesDir, "subscription_icons").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "icon_${identifier}.jpg"
            val destFile = File(iconsDir, fileName)
            FileOutputStream(destFile).use { out ->
                out.write(bytes)
                out.flush()
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            if (bitmap.width > reqWidth || bitmap.height > reqHeight) {
                val scale = minOf(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            var bitmap = BitmapFactory.decodeFile(path, options) ?: return null

            if (bitmap.width > reqWidth || bitmap.height > reqHeight) {
                val scale = minOf(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

