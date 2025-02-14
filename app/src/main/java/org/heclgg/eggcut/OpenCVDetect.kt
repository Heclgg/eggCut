package org.heclgg.eggcut

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * OpenCV 识别
 *
 * @author heclgg
 * @since 2025/02/10
 */
object OpenCVDetect {
    private const val TAG = "OpenCVDetect"

    fun detectRectangles(bitmap: Bitmap): Bitmap {
        // 1. 将 Bitmap 转换为 OpenCV 的 Mat
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)

        // 2. 图像预处理
        val grayMat = Mat()
        val blurMat = Mat()
        val edgesMat = Mat()

        // 转为灰度图
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY)
        // 高斯模糊降噪
        Imgproc.GaussianBlur(grayMat, blurMat, Size(5.0, 5.0), 0.0)
        // Canny 边缘检测
        Imgproc.Canny(blurMat, edgesMat, 80.0, 250.0)

        // 3. 查找轮廓
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edgesMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        // 4. 筛选矩形轮廓
        for (contour in contours) {
            val approxCurve = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())

            // 多边形逼近
            val epsilon = 0.02 * Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true)

            // 判断是否为矩形（4个顶点）
            if (approxCurve.toArray().size == 4) {
                // 进一步筛选：计算面积和宽高比
                val area = Imgproc.contourArea(contour)
                if (area > 1000) { // 过滤小面积噪声
                    val rect = Imgproc.boundingRect(contour)
                    val aspectRatio = rect.width.toFloat() / rect.height
                    if (aspectRatio in 0.8..1.2) { // 近似正方形的矩形
                        // 标记矩形
                        Imgproc.drawContours(srcMat, listOf(contour), -1, Scalar(0.0, 255.0, 0.0), 3)
                    }
                }
            }
        }

        // 5. 将结果 Mat 转换回 Bitmap
        val resultBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(srcMat, resultBitmap)

        return resultBitmap
    }
}