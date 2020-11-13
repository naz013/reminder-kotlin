package com.elementary.tasks.core.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.Px

object MeasureUtils {

  @Px
  fun dp2px(context: Context, dp: Int): Int {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    var display: Display? = null
    if (wm != null) {
      display = wm.defaultDisplay
    }

    val displaymetrics = DisplayMetrics()
    display?.getMetrics(displaymetrics)
    return (dp * displaymetrics.density + 0.5f).toInt()
  }
}
