package com.elementary.tasks.core.views.gradient

import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation

class UiGradientHelper(
  private val colors: IntArray,
  private val cornerRadius: Float,
  private val orientation: Int,
  private val animate: Boolean = false,
  private val animationEnterDuration: Int = DEF_ENTER_DURATION,
  private val animationExitDuration: Int = DEF_EXIT_DURATION
) {

  fun applyBackground(callback: (Drawable?) -> Unit) {
    val gradientDrawable = getBackground()
    callback(gradientDrawable)
    if (gradientDrawable is AnimationDrawable) {
      gradientDrawable.start()
    }
  }

  fun getBackground(): Drawable? {
    if (colors.size < 2) return null
    val orientation = GradientDrawable.Orientation.values()[orientation]
    return if (animate) {
      createAnimation(
        colors,
        orientation,
        cornerRadius,
        animationEnterDuration,
        animationExitDuration
      ) ?: createGradient(colors, orientation, cornerRadius)
    } else {
      createGradient(colors, orientation, cornerRadius)
    }
  }

  private fun createAnimation(
    colors: IntArray,
    orientation: Orientation,
    radius: Float,
    enterDuration: Int = DEF_ENTER_DURATION,
    exitDuration: Int = DEF_EXIT_DURATION
  ): AnimationDrawable? {
    if (colors.size <= 1) return null
    val animationDrawable = AnimationDrawable()
    for (i in colors.indices) {
      animationDrawable.addFrame(
        createGradient(
          sortColors(colors, i),
          orientation,
          radius
        ),
        exitDuration
      )
    }
    animationDrawable.setEnterFadeDuration(enterDuration)
    animationDrawable.setExitFadeDuration(exitDuration)
    return animationDrawable
  }

  private fun sortColors(colors: IntArray, index: Int): IntArray {
    val list = colors.toMutableList()
    for (i in index until colors.size) {
      list[i] = colors[i - index]
    }
    for (i in 0 until index) {
      list[i] = colors[colors.size - index + i]
    }
    return list.toIntArray()
  }

  private fun createGradient(
    colors: IntArray,
    orientation: Orientation,
    radius: Float
  ): GradientDrawable {
    return GradientDrawable(orientation, colors).apply { cornerRadius = radius }
  }

  private companion object {
    private const val DEF_ENTER_DURATION = 10
    private const val DEF_EXIT_DURATION = 5000
  }
}
