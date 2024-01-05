package com.elementary.tasks.core.views.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.databinding.ViewNumberValuePickerBinding

class NumberValuePickerView : LinearLayout {

  var onValueChangedListener: OnValueChangedListener? = null
  var maxValue: Long = 999
  var minValue: Long = 0
  var topButtonStep: Long = 1
  var bottomButtonStep: Long = -1

  val value: String
    get() = getInputValue()

  private var binding: ViewNumberValuePickerBinding
  private var inputMethodManager: InputMethodManager? = null

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    View.inflate(context, R.layout.view_number_value_picker, this)
    orientation = HORIZONTAL
    binding = ViewNumberValuePickerBinding.bind(this)

    binding.plusButton.setOnClickListener { calculateChange(topButtonStep) }
    binding.minusButton.setOnClickListener { calculateChange(bottomButtonStep) }

    inputMethodManager =
      context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    binding.viewInputEditText.onTextChanged {
      try {
        val int = Integer.parseInt(it.toString())
        if (int > 0 && it.toString().startsWith("0")) {
          binding.viewInputEditText.setText(int.toString())
          updateEditField()
        }
      } catch (e: NumberFormatException) {
        binding.viewInputEditText.setText("0")
      }
      notifyChange()
    }
    binding.viewInputEditText.setOnFocusChangeListener { _, hasFocus ->
      if (inputMethodManager == null) return@setOnFocusChangeListener
      if (!hasFocus) {
        inputMethodManager?.hideSoftInputFromWindow(binding.viewInputEditText.windowToken, 0)
      } else {
        inputMethodManager?.showSoftInput(binding.viewInputEditText, 0)
      }
    }
    binding.viewInputEditText.setOnClickListener {
      if (inputMethodManager == null) return@setOnClickListener
      if (inputMethodManager?.isActive(binding.viewInputEditText) == false) {
        inputMethodManager?.showSoftInput(binding.viewInputEditText, 0)
      }
    }
    binding.viewInputEditText.setText("0")

    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.NumberValuePickerView,
        defStyle,
        0
      )
      try {
        if (a.hasValue(R.styleable.NumberValuePickerView_valuePicker_minValue)) {
          minValue = a.getInt(
            /* index = */ R.styleable.NumberValuePickerView_valuePicker_minValue,
            /* defValue = */ 0
          ).toLong()
        }
        if (a.hasValue(R.styleable.NumberValuePickerView_valuePicker_maxValue)) {
          maxValue = a.getInt(
            /* index = */ R.styleable.NumberValuePickerView_valuePicker_maxValue,
            /* defValue = */ 999
          ).toLong()
        }
        if (a.hasValue(R.styleable.NumberValuePickerView_valuePicker_topButtonText)) {
          a.getString(R.styleable.NumberValuePickerView_valuePicker_topButtonText)?.also {
            binding.plusButton.text = it
          }
        }
        if (a.hasValue(R.styleable.NumberValuePickerView_valuePicker_bottomButtonText)) {
          a.getString(R.styleable.NumberValuePickerView_valuePicker_bottomButtonText)?.also {
            binding.minusButton.text = it
          }
        }
        if (a.hasValue(R.styleable.NumberValuePickerView_valuePicker_defaultValue)) {
          a.getString(R.styleable.NumberValuePickerView_valuePicker_defaultValue).also {
            binding.viewInputEditText.setText(it)
          }
        }
      } catch (_: Exception) {
      } finally {
        a.recycle()
      }
    }
  }

  fun setValue(value: String) {
    val currentValue = runCatching { value.toLong() }.getOrNull() ?: return
    validateAndSet(currentValue)
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    binding.plusButton.isEnabled = enabled
    binding.minusButton.isEnabled = enabled
    binding.viewInputEditText.isEnabled = enabled
  }

  private fun calculateChange(change: Long) {
    val currentValue = runCatching { getInputValue().toLong() }.getOrNull() ?: return
    val newValue = currentValue + change
    validateAndSet(newValue)
    notifyChange()
  }

  private fun validateAndSet(newValue: Long) {
    val fixedValue = if (newValue > maxValue) {
      maxValue
    } else if (newValue < minValue) {
      minValue
    } else {
      newValue
    }
    binding.viewInputEditText.setText(fixedValue.toString())
    updateEditField()
  }

  private fun updateEditField() {
    binding.viewInputEditText.setSelection(getInputValue().length)
  }

  private fun getInputValue(): String {
    return binding.viewInputEditText.text.toString()
  }

  private fun notifyChange() {
    onValueChangedListener?.onChanged(getInputValue())
  }

  interface OnValueChangedListener {
    fun onChanged(value: String)
  }
}
