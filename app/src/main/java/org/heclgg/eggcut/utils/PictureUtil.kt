package org.heclgg.eggcut.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

/**
 * 图片处理工具类
 *
 * @author heclgg
 * @since 2025/02/10
 */
object PictureUtil {
    private const val TAG = "PictureUtil"

    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024

    @JvmStatic
    fun Context.uriToBitmap(uri: Uri): Bitmap? {
        return try {
            // 通过ContentResolver获取输入流
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            // 将输入流解码为Bitmap
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun Context.uriToScaledBitmap(
        uri: Uri,
        maxWidth: Int = MAX_WIDTH,
        maxHeight: Int = MAX_HEIGHT
    ): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true // 只获取图片尺寸，不加载内容
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 计算缩放比例
            val (width, height) = options.outWidth to options.outHeight
            var inSampleSize = 1
            if (width > maxWidth || height > maxHeight) {
                val halfWidth = width / 2
                val halfHeight = height / 2
                while (halfWidth / inSampleSize >= maxWidth && halfHeight / inSampleSize >= maxHeight) {
                    inSampleSize *= 2
                }
            }

            // 加载缩放后的图片
            val scaledOptions = BitmapFactory.Options().apply {
                inSampleSize = this@apply.inSampleSize
            }
            val newInputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, scaledOptions)
            newInputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}