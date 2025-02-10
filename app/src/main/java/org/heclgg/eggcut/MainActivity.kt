package org.heclgg.eggcut

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import org.heclgg.eggcut.utils.PictureUtil.uriToBitmap
import org.heclgg.eggcut.utils.ViewUtil.bindView
import org.opencv.android.OpenCVLoader

/**
 * 主页
 *
 * @author heclgg
 * @since 2025/02/10
 */
class MainActivity : AppCompatActivity() {
    private val button by bindView<Button>(R.id.button_main)
    private val mainColumn by bindView<LinearLayout>(R.id.main_column)
    private val imgView by bindView<ImageView>(R.id.img_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button.setOnClickListener {
            if (OpenCVLoader.initLocal()) {
                Log.d("hcllog", "init succeed")
                openGallery()
            } else {
                Log.d("hcllog", "init fail")
                Toast.makeText(baseContext, "初始化 OpenCV 失败", Toast.LENGTH_SHORT).show()
            }
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
                Log.d("hcllog", "pic uri is: $it")
            }
        }

    private fun handleImage(uri: Uri) {
        mainColumn.visibility = View.GONE
//        imgView.setImageURI(uri)
        imgView.visibility = View.VISIBLE

        val bitmap = uriToBitmap(uri)?.let { OpenCVDetect.detectRectangles(it) }
        imgView.setImageBitmap(bitmap)
    }
}