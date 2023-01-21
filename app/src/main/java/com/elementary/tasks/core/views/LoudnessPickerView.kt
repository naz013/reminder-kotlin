package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewLoudnessBinding

class LoudnessPickerView : LinearLayout {

  private lateinit var binding: ViewLoudnessBinding
  var onLevelUpdateListener: ((level: Int) -> Unit)? = null
  private var level: Int = 0
    get() {
      return field - 1
    }
    private set(value) {
      field = value
      if (value > 0) {
        binding.labelView.text = "${value - 1}"
      } else {
        binding.labelView.text = context.getString(R.string.default_string)
      }
    }

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

  fun setVolume(level: Int) {
    binding.sliderView.value = (level + 1).toFloat()
    this.level = binding.sliderView.value.toInt()
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_loudness, this)
    orientation = HORIZONTAL
    binding = ViewLoudnessBinding.bind(this)

    binding.sliderView.addOnChangeListener { _, value, _ ->
      level = value.toInt()
      onLevelUpdateListener?.invoke(level)
    }
  }
}
