package com.elementary.tasks.core.utils.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

fun View.applyMarginsRes(
  @DimenRes startRes: Int = -1,
  @DimenRes topRes: Int = -1,
  @DimenRes endRes: Int = -1,
  @DimenRes bottomRes: Int = -1
) {
  val startMargin = startRes.getPx(context)
  val topMargin = topRes.getPx(context)
  val endMargin = endRes.getPx(context)
  val bottomMargin = bottomRes.getPx(context)
  this.layoutParams = this.layoutParams.applyMargins(
    start = startMargin,
    top = topMargin,
    end = endMargin,
    bottom = bottomMargin
  )
}

fun View.applyMarginsPx(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
) {
  this.layoutParams = this.layoutParams.applyMargins(
    start = start,
    top = top,
    end = end,
    bottom = bottom
  )
}

fun LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): LayoutParams {
  return when (this) {
    is FrameLayout.LayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    is LinearLayout.LayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    is RelativeLayout.LayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    is CoordinatorLayout.LayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    is ConstraintLayout.LayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    is MarginLayoutParams -> {
      applyMargins(start, top, end, bottom)
    }

    else -> {
      this
    }
  }
}

fun MarginLayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): MarginLayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

fun FrameLayout.LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): FrameLayout.LayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

fun LinearLayout.LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): LinearLayout.LayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

fun RelativeLayout.LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): RelativeLayout.LayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

fun CoordinatorLayout.LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): CoordinatorLayout.LayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

fun ConstraintLayout.LayoutParams.applyMargins(
  @Px start: Int = 0,
  @Px top: Int = 0,
  @Px end: Int = 0,
  @Px bottom: Int = 0
): ConstraintLayout.LayoutParams {
  this.setMargins(start, top, end, bottom)
  return this
}

@Px
private fun Int.getPx(context: Context): Int {
  return if (this == -1) {
    0
  } else {
    context.resources.getDimensionPixelSize(this)
  }
}
