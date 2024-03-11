package com.elementary.tasks.core.views.square

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class UiSquareTextView : AppCompatTextView {

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val measuredWidth = measuredWidth
    setMeasuredDimension(measuredWidth, measuredWidth)
  }
}
