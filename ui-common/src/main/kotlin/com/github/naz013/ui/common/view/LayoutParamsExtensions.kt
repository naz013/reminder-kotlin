package com.github.naz013.ui.common.view

import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

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
