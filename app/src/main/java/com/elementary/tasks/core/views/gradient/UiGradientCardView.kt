package com.elementary.tasks.core.views.gradient

import android.content.Context
import android.util.AttributeSet
import com.elementary.tasks.R
import com.google.android.material.card.MaterialCardView
import timber.log.Timber

class UiGradientCardView : MaterialCardView {

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UiGradientCardView, 0, 0)
      try {
        val orientation = a.getInt(R.styleable.UiGradientCardView_gradientCardView_orientation, 6)
        val startColor = a.getColor(R.styleable.UiGradientCardView_gradientCardView_startColor, -1)
        val centerColor =
          a.getColor(R.styleable.UiGradientCardView_gradientCardView_centerColor, -1)
        val endColor = a.getColor(R.styleable.UiGradientCardView_gradientCardView_endColor, -1)

        val colorsId = a.getResourceId(R.styleable.UiGradientCardView_gradientCardView_colors, -1)

        val colors = if (startColor != -1 && endColor != -1) {
          if (centerColor == -1) {
            intArrayOf(startColor, endColor)
          } else {
            intArrayOf(startColor, centerColor, endColor)
          }
        } else if (colorsId != -1) {
          resources.getIntArray(colorsId).takeIf { it.isNotEmpty() }
        } else {
          null
        }
        if (colors != null) {
          val gradientHelper = UiGradientHelper(colors, radius, orientation)
          gradientHelper.applyBackground { background = it }
        }
      } catch (e: Exception) {
        Timber.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
  }
}
