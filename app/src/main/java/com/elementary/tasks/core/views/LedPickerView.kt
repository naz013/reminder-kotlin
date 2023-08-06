package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.databinding.ViewLedColorBinding

class LedPickerView : LinearLayout {

  private lateinit var binding: ViewLedColorBinding
  var onLedChangeListener: ((Int) -> Unit)? = null
  var led: Int = LED.BLUE
    set(value) {
      field = value
      binding.ledGroup.check(chipIdFromLed(value))
    }
    get() {
      return ledFromChip(binding.ledGroup.checkedRadioButtonId)
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

  private fun chipIdFromLed(id: Int): Int {
    return when (id) {
      0 -> R.id.ledRed
      1 -> R.id.ledGreen
      2 -> R.id.ledBlue
      3 -> R.id.ledYellow
      4 -> R.id.ledPink
      5 -> R.id.ledOrange
      6 -> R.id.ledTeal
      else -> R.id.ledBlue
    }
  }

  private fun ledFromChip(id: Int): Int {
    return when (id) {
      R.id.ledRed -> 0
      R.id.ledGreen -> 1
      R.id.ledBlue -> 2
      R.id.ledYellow -> 3
      R.id.ledPink -> 4
      R.id.ledOrange -> 5
      R.id.ledTeal -> 6
      else -> 2
    }
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_led_color, this)
    orientation = VERTICAL
    binding = ViewLedColorBinding.bind(this)

    binding.ledGroup.setOnCheckedChangeListener { _, checkedId ->
      updateState(ledFromChip(checkedId))
    }
  }

  private fun updateState(led: Int) {
    onLedChangeListener?.invoke(led)
  }
}
