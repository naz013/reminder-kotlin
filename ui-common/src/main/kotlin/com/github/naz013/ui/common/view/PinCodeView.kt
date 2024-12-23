package com.github.naz013.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.github.naz013.ui.common.R

class PinCodeView : LinearLayout {

  private lateinit var binding: PinCodeViewBinding
  private var pinString = ""
  private val digits = mutableListOf<Int>()
  var supportFinger = false
    set(value) {
      field = value
      updateFButton()
    }

  private fun updateFButton() {
    if (supportFinger) {
      binding.fingerButton.visible()
    } else {
      binding.fingerButton.transparent()
    }
  }

  var shuffleMode = false
  var callback: ((String) -> Unit)? = null
  var fingerprintButtonCallback: (() -> Unit)? = null

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
    orientation = VERTICAL
    View.inflate(context, R.layout.view_pin_code, this)
    binding = PinCodeViewBinding(this)

    binding.deleteButton.setOnClickListener {
      clearPin()
    }
    binding.fingerButton.setOnClickListener { fingerprintButtonCallback?.invoke() }
    updateFButton()
    initButtons()
    updatePinView()
  }

  fun clearPin() {
    pinString = ""
    updatePinView()
  }

  private fun initButtons() {
    binding.b0.setOnClickListener { clickButton(0) }
    binding.b1.setOnClickListener { clickButton(1) }
    binding.b2.setOnClickListener { clickButton(2) }
    binding.b3.setOnClickListener { clickButton(3) }
    binding.b4.setOnClickListener { clickButton(4) }
    binding.b5.setOnClickListener { clickButton(5) }
    binding.b6.setOnClickListener { clickButton(6) }
    binding.b7.setOnClickListener { clickButton(7) }
    binding.b8.setOnClickListener { clickButton(8) }
    binding.b9.setOnClickListener { clickButton(9) }
  }

  private fun clickButton(i: Int) {
    if (pinString.length < 6) {
      pinString += (digits[i]).toString()
      updatePinView()
      callback?.invoke(pinString)
    }
  }

  private fun updatePinView() {
    binding.deleteButton.isEnabled = pinString.isNotEmpty()
    clearIcons()
    showIcons()
    updateButtons()
  }

  private fun updateButtons() {
    this.digits.clear()
    val list = mutableListOf<Int>()
    for (i in 0 until 10) list.add(i)
    if (shuffleMode) list.shuffle()
    this.digits.addAll(list)
    setButtonLabels()
  }

  private fun setButtonLabels() {
    binding.t0.text = digits[0].toString()
    binding.t1.text = digits[1].toString()
    binding.t2.text = digits[2].toString()
    binding.t3.text = digits[3].toString()
    binding.t4.text = digits[4].toString()
    binding.t5.text = digits[5].toString()
    binding.t6.text = digits[6].toString()
    binding.t7.text = digits[7].toString()
    binding.t8.text = digits[8].toString()
    binding.t9.text = digits[9].toString()
  }

  private fun showIcons() {
    for (i in pinString.indices) {
      binding.birdsView.getChildAt(i)?.visible()
    }
  }

  private fun clearIcons() {
    for (child in binding.birdsView.children) {
      child.transparent()
    }
  }
}
