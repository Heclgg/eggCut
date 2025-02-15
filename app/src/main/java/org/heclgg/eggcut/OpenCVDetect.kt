package org.heclgg.eggcut

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
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

    /**
     * 识别图片中的正方形
     *
     * @param bitmap 原始图片
     * @return 正方形列表
     */
    fun detectRectangles(bitmap: Bitmap): List<Bitmap> {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)

        val grayMat = Mat()
        val blurMat = Mat()
        val edgesMat = Mat()

        // 转为灰度图
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY)
        // 高斯模糊降噪
        Imgproc.GaussianBlur(grayMat, blurMat, Size(5.0, 5.0), 0.0)
        // Canny 边缘检测
        Imgproc.Canny(blurMat, edgesMat, 80.0, 250.0)

        // 外部轮廓
        val contours = mutableListOf<MatOfPoint>()
        // 轮廓层次
        val hierarchy = Mat()
        Imgproc.findContours(
            edgesMat,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // 筛选矩形轮廓
        val rectangles = mutableListOf<Rect>()
        for (contour in contours) {
            val approxCurve = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())

            val epsilon = 0.02 * Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true)

            if (approxCurve.toArray().size == 4) {
                val rect = Imgproc.boundingRect(contour)
                val aspectRatio = rect.width.toFloat() / rect.height
                if (aspectRatio in 0.8..1.2) {
                    Imgproc.drawContours(
                        srcMat,
                        listOf(contour),
                        -1,
                        Scalar(0.0, 0.0, 0.0),
                        3
                    )
                    rectangles.add(rect)
                }
            }
        }

        // 导出裁剪的矩形区域
        val croppedBitmaps = mutableListOf<Bitmap>()
        for (rect in rectangles) {
            val roiMat = srcMat.submat(rect)
            val croppedBitmap =
                Bitmap.createBitmap(rect.width, rect.height, Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(roiMat, croppedBitmap)
            croppedBitmaps.add(croppedBitmap)
            roiMat.release()
        }

        // 释放Mat资源
        grayMat.release()
        blurMat.release()
        edgesMat.release()
        srcMat.release()

        return croppedBitmaps
    }

    fun getMaxRectangle(bitmaps: List<Bitmap>): Bitmap? {
        var maxArea = 0
        var maxBitmap: Bitmap? = null

        for (rectBitmap in bitmaps) {
            val currentArea = rectBitmap.width * rectBitmap.height
            if (currentArea > maxArea) {
                maxArea = currentArea
                maxBitmap = rectBitmap
            }
            if (rectBitmap != maxBitmap) rectBitmap.recycle()
        }

        // 确保至少找到一个矩形
        return maxBitmap
    }
}