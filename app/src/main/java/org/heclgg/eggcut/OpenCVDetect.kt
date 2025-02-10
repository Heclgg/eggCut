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

    fun detectRectangles(inputBitmap: Bitmap): Bitmap {
        // Convert Bitmap to OpenCV Mat format
        val srcMat = Mat()
        Utils.bitmapToMat(inputBitmap, srcMat)

        // 1. Convert to grayscale
        val gray = Mat()
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY)

        // 2. Gaussian blur to reduce noise
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        // 3. Canny edge detection
        val edges = Mat()
        Imgproc.Canny(gray, edges, 50.0, 150.0)

        // 4. Find contours
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            edges,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        // 5. Filter rectangle contours
        val rectContours = mutableListOf<MatOfPoint>()
        for (contour in contours) {
            val approx = MatOfPoint2f()
            val contour2f = MatOfPoint2f(*contour.toArray())

            // Polygon approximation (epsilon as 2% of the perimeter)
            val epsilon = 0.02 * Imgproc.arcLength(contour2f, true)
            Imgproc.approxPolyDP(contour2f, approx, epsilon, true)

            // Filter quadrilaterals
            if (approx.toArray().size == 4) {
                // Calculate contour area (exclude too small areas)
                val area = Imgproc.contourArea(approx)
                if (area > 1000) {
                    // Check if convex quadrilateral
                    if (Imgproc.isContourConvex(MatOfPoint(*approx.toArray()))) {
                        rectContours.add(MatOfPoint(*approx.toArray()))
                    }
                }
            }
        }

        // 6. Draw detection results on the original image
        val color = Scalar(0.0, 255.0, 0.0) // Green
        for (rect in rectContours) {
            Imgproc.drawContours(srcMat, listOf(rect), -1, color, 3)
        }

        // Convert back to Bitmap
        val outputBitmap =
            Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(srcMat, outputBitmap)

        // Release memory
        srcMat.release()
        gray.release()
        edges.release()
        hierarchy.release()

        return outputBitmap
    }
}