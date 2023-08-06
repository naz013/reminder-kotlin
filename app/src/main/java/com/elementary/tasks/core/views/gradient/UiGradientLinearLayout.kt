package com.elementary.tasks.core.views.gradient

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.elementary.tasks.R
import timber.log.Timber

class UiGradientLinearLayout : LinearLayout {

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
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.UiGradientLinearLayout, 0, 0)
      try {
        val animate =
          a.getBoolean(R.styleable.UiGradientLinearLayout_gradientLinearLayout_animate, false)
        val orientation =
          a.getInt(R.styleable.UiGradientLinearLayout_gradientLinearLayout_orientation, 6)

        val animEnter = a.getInt(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_animationEnterDuration,
          10
        )
        val animExit = a.getInt(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_animationExitDuration,
          5000
        )

        val startColor = a.getColor(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_startColor,
          -1
        )
        val centerColor = a.getColor(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_centerColor,
          -1
        )
        val endColor = a.getColor(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_endColor,
          -1
        )

        val colorsId = a.getResourceId(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_colors,
          -1
        )

        val cornerRadius = a.getDimension(
          R.styleable.UiGradientLinearLayout_gradientLinearLayout_cornerRadius,
          0f
        )

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
        Timber.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
    }
  }
}
