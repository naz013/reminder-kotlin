package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class DividerView : View {

  private var color = Color.GRAY

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun init(context: Context) {
    val typedValue = TypedValue()
    val themeArray = context.theme.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.textColorPrimary))
    val color = try {
      themeArray.getColor(0, 0)
    } catch (e: Exception) {
      Color.BLACK
    } finally {
      themeArray.recycle()
    }
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    this.color = Color.argb(38, r, g, b)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawColor(color)
  }
}
