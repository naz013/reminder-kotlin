package com.github.naz013.ui.common.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.context.dp2px
import com.google.android.material.color.MaterialColors

class MaterialSearchBar : AppCompatEditText {

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    background = ContextCompat.getDrawable(context, R.drawable.material_search_bar_background)
    foreground = ContextCompat.getDrawable(context, R.drawable.ripple_rounded_corners_extra_large)
    height = context.dp2px(56)
    val padding = context.dp2px(16)
    setPadding(padding, padding, padding, padding)

    setHintTextColor(MaterialColors.getColor(context, R.attr.colorOnSurfaceVariant, Color.BLACK))
    setTextColor(MaterialColors.getColor(context, R.attr.colorOnSurface, Color.BLACK))
    setTypeface(ResourcesCompat.getFont(context, R.font.roboto_regular))
    textSize = 16f

    maxLines = 1
    isSingleLine = true
    gravity = Gravity.CENTER_VERTICAL
    isFocusable = true
    isFocusableInTouchMode = true
    isCursorVisible = true
    isClickable = true
    setOnClickListener {
      requestFocus()
      post {
        showKeyboard()
      }
    }
  }
}
