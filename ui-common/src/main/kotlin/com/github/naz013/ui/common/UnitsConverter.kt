package com.github.naz013.ui.common

import android.util.TypedValue
import androidx.annotation.Px
import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.common.context.spToPx

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
