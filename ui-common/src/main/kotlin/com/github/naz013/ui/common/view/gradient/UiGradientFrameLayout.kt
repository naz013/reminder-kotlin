package com.github.naz013.ui.common.view.gradient

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R

class UiGradientFrameLayout : FrameLayout {

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
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UiGradientFrameLayout, 0, 0)
      try {
        val animate =
          a.getBoolean(R.styleable.UiGradientFrameLayout_gradientFrameLayout_animate, false)
        val orientation =
          a.getInt(R.styleable.UiGradientFrameLayout_gradientFrameLayout_orientation, 6)

        val animEnter = a.getInt(
          R.styleable.UiGradientFrameLayout_gradientFrameLayout_animationEnterDuration,
          10
        )
        val animExit = a.getInt(
          R.styleable.UiGradientFrameLayout_gradientFrameLayout_animationExitDuration,
          5000
        )

        val startColor =
          a.getColor(R.styleable.UiGradientFrameLayout_gradientFrameLayout_startColor, -1)
        val centerColor =
          a.getColor(R.styleable.UiGradientFrameLayout_gradientFrameLayout_centerColor, -1)
        val endColor =
          a.getColor(R.styleable.UiGradientFrameLayout_gradientFrameLayout_endColor, -1)

        val colorsId =
          a.getResourceId(R.styleable.UiGradientFrameLayout_gradientFrameLayout_colors, -1)

        val cornerRadius =
          a.getDimension(R.styleable.UiGradientFrameLayout_gradientFrameLayout_cornerRadius, 0f)

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
          val gradientHelper = UiGradientHelper(
            colors,
            cornerRadius,
            orientation,
            animate,
            animEnter,
            animExit
          )
          gradientHelper.applyBackground { background = it }
        }
      } catch (e: Exception) {
        Logger.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
  }
}
