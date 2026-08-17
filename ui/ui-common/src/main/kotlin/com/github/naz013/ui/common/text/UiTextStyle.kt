package com.github.naz013.ui.common.text

import android.graphics.Typeface

enum class UiTextStyle(
  val flag: Int,
) {
  BOLD(Typeface.BOLD),
  ITALIC(Typeface.ITALIC),
  NORMAL(Typeface.NORMAL),
  BOLD_ITALIC(Typeface.BOLD_ITALIC),
}
