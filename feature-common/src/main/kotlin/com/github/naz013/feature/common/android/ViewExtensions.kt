package com.github.naz013.feature.common.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom

fun View.getSize(block: (width: Int, height: Int) -> Unit) {
  addOnLayoutChangeListener(
    object : View.OnLayoutChangeListener {
      override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
      ) {
        removeOnLayoutChangeListener(this)
        block(v?.height ?: -1, v?.width ?: -1)
      }
    }
  )
}

fun View.singleClick(function: (View) -> Unit) {
  this.setOnClickListener {
    if (shouldDispatchClick(it)) {
      function(it)
    }
  }
}

private fun shouldDispatchClick(key: Any): Boolean {
  return if (ClickMap.viewClickMap.containsKey(key)) {
    (System.currentTimeMillis() - (ClickMap.viewClickMap[key] ?: 0L) > ClickMap.DELAY).also {
      if (it) {
        ClickMap.viewClickMap[key] = System.currentTimeMillis()
      }
    }
  } else {
    ClickMap.viewClickMap[key] = System.currentTimeMillis()
    true
  }
}

object ClickMap {
  const val DELAY = 500L
  val viewClickMap = mutableMapOf<Any, Long>()
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isTransparent(): Boolean = visibility == View.INVISIBLE

fun View.transparent() {
  visibility = View.INVISIBLE
}

fun View.gone() {
  visibility = View.GONE
}

fun View.visible() {
  visibility = View.VISIBLE
}

fun View.visibleGone(value: Boolean) {
  if (value && !isVisible()) {
    visible()
  } else if (!value && !isGone()) {
    gone()
  }
}

fun View.visibleInvisible(value: Boolean) {
  if (value && !isVisible()) {
    visible()
  } else if (!value && !isTransparent()) {
    transparent()
  }
}

fun View.colorOf(@ColorRes color: Int) = ContextCompat.getColor(context, color)

fun View.inflater(): LayoutInflater = LayoutInflater.from(context)

fun View.dp2px(dp: Int) = context.dp2px(dp)

fun View.fadeInAnimation() {
  val fadeIn = AlphaAnimation(0f, 1f)
  fadeIn.interpolator = DecelerateInterpolator()
  fadeIn.startOffset = 400
  fadeIn.duration = 400
  animation = fadeIn
  visibility = View.VISIBLE
}

fun View.fadeOutAnimation() {
  val fadeOut = AlphaAnimation(1f, 0f)
  fadeOut.interpolator = AccelerateInterpolator()
  fadeOut.duration = 400
  animation = fadeOut
  visibility = View.GONE
}

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

@Px
private fun Int.getPx(context: Context): Int {
  return if (this == -1) {
    0
  } else {
    context.resources.getDimensionPixelSize(this)
  }
}

fun View.applyTopInsets(
  topExtra: Int = 0
) {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ v.paddingLeft,
      /* top = */ innerPadding.top + topExtra,
      /* right = */ v.paddingRight,
      /* bottom = */ v.paddingBottom
    )
    insets
  }
}

fun View.applyBottomInsetsMargin(
  @DimenRes bottomExtraRes: Int = -1
) {
  val bottomMargin = if (bottomExtraRes != -1) {
    context.resources.getDimensionPixelSize(bottomExtraRes)
  } else {
    0
  }
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.applyMarginsPx(bottom = v.marginBottom + bottomMargin + innerPadding.bottom)
    insets
  }
}

fun View.applyBottomInsets(
  @DimenRes bottomExtraRes: Int = -1
) {
  val bottomMargin = if (bottomExtraRes != -1) {
    context.resources.getDimensionPixelSize(bottomExtraRes)
  } else {
    0
  }
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ v.paddingLeft,
      /* top = */ v.paddingTop,
      /* right = */ v.paddingRight,
      /* bottom = */ innerPadding.bottom + bottomMargin
    )
    insets
  }
}

fun View.applyInsets(
  leftExtra: Int = 0,
  topExtra: Int = 0,
  rightExtra: Int = 0,
  bottomExtra: Int = 0
) {
  ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
    val innerPadding = insets.getInsets(
      WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.displayCutout()
    )
    v.setPadding(
      /* left = */ innerPadding.left + leftExtra,
      /* top = */ innerPadding.top + topExtra,
      /* right = */ innerPadding.right + rightExtra,
      /* bottom = */ innerPadding.bottom + bottomExtra
    )
    insets
  }
}
