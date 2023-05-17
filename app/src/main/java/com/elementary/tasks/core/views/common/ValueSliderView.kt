package com.elementary.tasks.core.views.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewValueSliderBinding

class ValueSliderView : LinearLayout {

  private lateinit var binding: ViewValueSliderBinding

  var step: Float = 1f
    private set
  var minValue: Float = 0f
    private set
  var maxValue: Float = 100f
    private set

  var valueFormatter: ValueFormatter = DefaultFormatter()
  var onValueChangeListener: OnValueChangeListener? = null

  var value: Float = 0f
    get() = binding.sliderView.value
    set(value) {
      field = value
      updateLabel(value)
      updateSlider(value)
      updateButtons(value)
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    binding.sliderView.isEnabled = enabled
    binding.leftButton.isEnabled = enabled
    binding.rightButton.isEnabled = enabled
  }

  fun setRange(minValue: Float, maxValue: Float, step: Float) {
    if ((maxValue - minValue) % step != 0f) {
      throw IllegalArgumentException("Wrong step = $step")
    }
    this.minValue = minValue
    this.maxValue = maxValue
    this.step = step

    binding.sliderView.valueTo = maxValue
    binding.sliderView.valueFrom = minValue
    binding.sliderView.stepSize = step
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_value_slider, this)
    orientation = HORIZONTAL
    binding = ViewValueSliderBinding.bind(this)

    binding.leftButton.setOnClickListener { onLeftClicked() }
    binding.rightButton.setOnClickListener { onRightClicked() }

    binding.sliderView.addOnChangeListener { _, value, _ ->
      updateLabel(value)
      updateButtons(value)
      onValueChangeListener?.onChanged(value, getFormattedValue(value))
    }
  }

  private fun onLeftClicked() {
    val newValue = value - step

    updateSlider(newValue)
    updateButtons(newValue)
    updateLabel(newValue)
  }

  private fun onRightClicked() {
    val newValue = value + step

    updateSlider(newValue)
    updateButtons(newValue)
    updateLabel(newValue)
  }

  private fun getFormattedValue(value: Float): String {
    return valueFormatter.apply(value)
  }

  private fun updateSlider(value: Float) {
    binding.sliderView.value = value
  }

  private fun updateLabel(value: Float) {
    binding.labelView.text = valueFormatter.apply(value)
  }

  private fun updateButtons(value: Float) {
    binding.leftButton.isEnabled = isEnabled
    binding.rightButton.isEnabled = isEnabled
    if (value >= maxValue) {
      binding.rightButton.isEnabled = false
    } else if (value <= minValue) {
      binding.leftButton.isEnabled = false
    }
  }

  interface OnValueChangeListener {
    fun onChanged(value: Float, displayValue: String)
  }

  interface ValueFormatter {
    fun apply(value: Float): String
  }

  internal class DefaultFormatter : ValueFormatter {
    override fun apply(value: Float): String {
      return "${value.toInt()}"
    }
  }
}
