package com.elementary.tasks.core.os

import android.util.TypedValue
import androidx.annotation.Px

class UnitsConverter(
  private val contextProvider: ContextProvider
) {

  @Px
  fun dp2px(dp: Int): Float {
    return dp2px(dp.toFloat())
  }

  @Px
  fun dp2px(dp: Float): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp,
      contextProvider.themedContext.resources.displayMetrics
    )
  }

  @Px
  fun spToPx(sp: Float): Float {
    return contextProvider.themedContext.spToPx(sp)
  }
}
