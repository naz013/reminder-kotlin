package com.elementary.tasks.core.text

import android.graphics.Paint
import android.util.TypedValue
import android.widget.TextView

fun TextView.applyStyles(textFormat: UiTextFormat) {
  textFormat.font?.also {
    this.typeface = it
  }
  textFormat.textColor?.also {
    this.setTextColor(it)
  }
  this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textFormat.fontSize)
  applyTextStyle(textFormat.textStyle)
  applyTextDecoration(textFormat.textDecoration)
}

fun TextView.applyTextStyle(textStyle: UiTextStyle) {
  setTypeface(typeface, textStyle.flag)
}

fun TextView.applyTextDecoration(textDecoration: UiTextDecoration) {
  when (textDecoration) {
    UiTextDecoration.NONE -> {
    }
    UiTextDecoration.STRIKE_THROUGH -> {
      paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }
    UiTextDecoration.UNDERLINE -> {
      paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
  }
}
