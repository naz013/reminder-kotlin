package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.views.common.ValueSliderView
import com.elementary.tasks.databinding.ViewRepeatLimitBinding

class RepeatLimitView : LinearLayout {

  private lateinit var binding: ViewRepeatLimitBinding
  var onLevelUpdateListener: ((level: Int) -> Unit)? = null

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    binding.valueSlider.isEnabled = enabled
  }

  fun setLimit(level: Int) {
    binding.valueSlider.value = level.toFloat()
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_repeat_limit, this)
    orientation = HORIZONTAL
    binding = ViewRepeatLimitBinding.bind(this)

    binding.valueSlider.valueFormatter = object : ValueSliderView.ValueFormatter {
      override fun apply(value: Float): String {
        val int = value.toInt()
        return if (int < 0) {
          context.getString(R.string.no_limits)
        } else {
          "$int"
        }
      }
    }
    binding.valueSlider.setRange(-1f, 365f, 1f)
    binding.valueSlider.onValueChangeListener = object : ValueSliderView.OnValueChangeListener {
      override fun onChanged(value: Float, displayValue: String) {
        onLevelUpdateListener?.invoke(value.toInt())
      }
    }
  }
}
