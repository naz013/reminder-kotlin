package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.elementary.tasks.core.utils.ui.radius.RadiusSliderBehaviour
import com.elementary.tasks.databinding.ViewRadiusPickerBinding

class RadiusPickerView : LinearLayout {

  private lateinit var radiusFormatter: DefaultRadiusFormatter
  private lateinit var binding: ViewRadiusPickerBinding
  private lateinit var behaviour: RadiusSliderBehaviour

  private var shouldNotify = true

  var onRadiusChangeListener: ((radius: Int) -> Unit)? = null
  var radiusInM: Int
    get() = behaviour.getRadius()
    set(value) {
      binding.labelView.text = radiusFormatter.format(value)
      shouldNotify = false
      binding.sliderView.value = value.toFloat()
      shouldNotify = true
    }
  var useMetric: Boolean = true
    set(value) {
      field = value
      radiusFormatter.useMetric = value
      binding.labelView.text = radiusFormatter.format(radiusInM)
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

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_radius_picker, this)
    orientation = HORIZONTAL

    binding = ViewRadiusPickerBinding.bind(this)
    radiusFormatter = DefaultRadiusFormatter(context, useMetric)
    behaviour = RadiusSliderBehaviour(binding.sliderView, 0) {
      binding.labelView.text = radiusFormatter.format(it)
      if (shouldNotify) {
        onRadiusChangeListener?.invoke(it)
      }
    }
  }
}
