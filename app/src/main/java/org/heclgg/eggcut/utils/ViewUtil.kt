package org.heclgg.eggcut.utils

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes

/**
 * 简化 findViewById
 *
 * @author heclgg
 * @since 2025/02/10
 */
object ViewUtil {
    private const val TAG = "ViewUtil"

    fun <T : View> Activity.bindView(@IdRes id: Int): Lazy<T> {
        return lazy { findViewById(id) }
    }
}