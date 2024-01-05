package com.elementary.tasks.core.os

import android.content.Context
import android.util.TypedValue
import androidx.annotation.Px

class UnitsConverter(
  private val context: Context
) {

  @Px
  fun dp2px(dp: Float): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp,
      context.resources.displayMetrics
    )
  }

  @Px
  fun spToPx(sp: Float): Float {
    return context.spToPx(sp)
  }
}
