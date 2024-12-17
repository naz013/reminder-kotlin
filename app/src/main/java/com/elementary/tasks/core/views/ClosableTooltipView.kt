package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewClosableTooltipBinding
import com.github.naz013.logging.Logger

class ClosableTooltipView : FrameLayout {

  private lateinit var binding: ViewClosableTooltipBinding

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_closable_tooltip, this)
    binding = ViewClosableTooltipBinding.bind(this)

    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ClosableTooltipView, 0, 0)
      var text = ""
      try {
        text = a.getString(R.styleable.ClosableTooltipView_text) ?: ""
      } catch (e: Exception) {
        Logger.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
      binding.textView.text = text
    }
  }

  fun setText(text: String) {
    binding.textView.text = text
  }

  override fun setOnClickListener(l: OnClickListener?) {
    binding.closeIconView.setOnClickListener(l)
  }
}
