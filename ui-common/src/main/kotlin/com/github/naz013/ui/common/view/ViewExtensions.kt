package com.github.naz013.ui.common.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.github.naz013.ui.common.context.dp2px

fun EditText.showKeyboard() {
  this.requestFocus()
  val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

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

fun View.colorOf(@ColorRes color: Int) = ContextCompat.getColor(context, color)

fun View.inflater(): LayoutInflater = LayoutInflater.from(context)

fun View.dp2px(dp: Int) = context.dp2px(dp)

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
    v.applyMarginsPx(
      bottom = v.marginBottom + bottomMargin + innerPadding.bottom,
      start = v.marginStart,
      end = v.marginEnd,
      top = v.marginTop
    )
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
