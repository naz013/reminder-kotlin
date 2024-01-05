package com.elementary.tasks.core.views.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ViewValueAndTypePickerBinding

class ValueAndTypePickerView : LinearLayout {

  var onChangedListener: OnChangedListener? = null
  var maxValue: Long
    set(value) {
      binding.numberPickerView.maxValue = value
    }
    get() = binding.numberPickerView.maxValue
  val value: String
    get() = binding.numberPickerView.value
  val typeIndex: Int
    get() = binding.selectorView.selectedItemPosition

  private var binding: ViewValueAndTypePickerBinding
  private var inputMethodManager: InputMethodManager? = null

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    View.inflate(context, R.layout.view_value_and_type_picker, this)
    orientation = VERTICAL
    binding = ViewValueAndTypePickerBinding.bind(this)

    inputMethodManager =
      context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    binding.selectorView.onSelectionChangedListener =
      object : VerticalWheelSelector.OnSelectionChangedListener {
        override fun onSelectionChanged(position: Int, selectedItem: String) {
          notifyChange()
        }
      }
    binding.numberPickerView.onValueChangedListener =
      object : NumberValuePickerView.OnValueChangedListener {
        override fun onChanged(value: String) {
          notifyChange()
        }
      }
  }

  fun setItems(items: List<String>) {
    binding.selectorView.setItems(items)
  }

  fun setTypeSelection(position: Int) {
    runCatching {
      binding.selectorView.selectItem(position)
    }
  }

  fun setValue(value: String) {
    binding.numberPickerView.setValue(value)
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    binding.numberPickerView.isEnabled = enabled
    binding.selectorView.isEnabled = enabled
  }

  private fun notifyChange() {
    onChangedListener?.onChanged(
      binding.numberPickerView.value,
      binding.selectorView.selectedItemPosition
    )
  }

  interface OnChangedListener {
    fun onChanged(value: String, typeIndex: Int)
  }
}
