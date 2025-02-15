package org.heclgg.eggcut

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.heclgg.eggcut.OpenCVDetect.detectRectangles
import org.heclgg.eggcut.OpenCVDetect.getMaxRectangle
import org.heclgg.eggcut.utils.PictureUtil.uriToBitmap
import org.heclgg.eggcut.utils.ViewUtil.bindView
import org.opencv.android.OpenCVLoader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 主页
 *
 * @author heclgg
 * @since 2025/02/10
 */
class MainActivity : AppCompatActivity() {
    private val mainColumn by bindView<LinearLayout>(R.id.main_column)
    private val imgColumn by bindView<LinearLayout>(R.id.img_column)
    private val importButton by bindView<Button>(R.id.button_main)
    private val backButton by bindView<Button>(R.id.back_img)
    private val saveButton by bindView<Button>(R.id.save_img)
    private val imgView by bindView<ImageView>(R.id.img_view)

    private var savePicture: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        importButton.setOnClickListener {
            if (OpenCVLoader.initLocal()) {
                openGallery()
            } else {
                Toast.makeText(
                    baseContext,
                    baseContext.getString(R.string.init_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        backButton.setOnClickListener {
            imgColumn.visibility = View.GONE
            mainColumn.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {
            saveBitmapToGallery(this, savePicture)
        }
    }

    private fun openGallery() {
        // 启动相册，限制选择类型为图片
        getContent.launch("image/*")
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 处理返回的图片 URI
                handleImage(it)
            }
        }

    private fun handleImage(uri: Uri) {
        mainColumn.visibility = View.GONE
        imgColumn.visibility = View.VISIBLE

        val result = uriToBitmap(uri)?.let { detectRectangles(it) }
        result?.let { rectangles ->
            savePicture = getMaxRectangle(rectangles)
            imgView.setImageBitmap(savePicture)
        }
    }

    private fun saveBitmapToGallery(activity: AppCompatActivity, bitmap: Bitmap?) {
        // 在后台线程执行耗时操作
        Thread {
            try {
                val resolver = activity.contentResolver
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "EGG_CUT_${timeStamp}.jpg"

                // 保存到 MediaStore（兼容 Android 10+ 的作用域存储）
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                }

                // 插入到媒体库
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = resolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                }

                // 在主线程显示提示
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        baseContext.getString(R.string.save_info_succeed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}