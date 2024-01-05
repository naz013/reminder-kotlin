package com.elementary.tasks.reminder.build.valuedialog.controller

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.databinding.BuilderItemSummaryBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class TextInputController(
  builderItem: BuilderItem<String>,
  private val inputMethodManager: InputMethodManager
) : AbstractBindingValueController<String, BuilderItemSummaryBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemSummaryBinding {
    return BuilderItemSummaryBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.inputEditText.isFocusableInTouchMode = true
    binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
      } else {
        inputMethodManager.showSoftInput(binding.inputEditText, 0)
      }
    }
    binding.inputEditText.setOnClickListener {
      if (!inputMethodManager.isActive(binding.inputEditText)) {
        inputMethodManager.showSoftInput(binding.inputEditText, 0)
      }
    }
    binding.inputEditText.setImeOptions(EditorInfo.IME_ACTION_DONE)
    binding.inputEditText.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }

    binding.inputEditText.hint = builderItem.title
    binding.inputEditText.onTextChanged { updateValue(it) }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    binding.inputEditText.setText(data)
  }
}
