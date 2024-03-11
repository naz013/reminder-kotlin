package com.elementary.tasks.reminder.build.valuedialog.controller.action

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import com.elementary.tasks.databinding.BuilderItemUrlBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class WebAddressInputController(
  builderItem: BuilderItem<String>,
  private val inputMethodManager: InputMethodManager
) : AbstractBindingValueController<String, BuilderItemUrlBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemUrlBinding {
    return BuilderItemUrlBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.urlInput.isFocusableInTouchMode = true
    binding.urlInput.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        inputMethodManager.hideSoftInputFromWindow(binding.urlInput.windowToken, 0)
      } else {
        inputMethodManager.showSoftInput(binding.urlInput, 0)
      }
    }
    binding.urlInput.setOnClickListener { startInput() }
    binding.urlInput.doOnTextChanged { text, _, _, _ ->
      updateValue(text?.toString())
    }
    binding.urlInput.setImeOptions(EditorInfo.IME_ACTION_DONE)
    binding.urlInput.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        inputMethodManager.hideSoftInputFromWindow(binding.urlInput.windowToken, 0)
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    binding.urlInput.setText(data ?: "")
  }

  private fun startInput() {
    if (!inputMethodManager.isActive(binding.urlInput)) {
      inputMethodManager.showSoftInput(binding.urlInput, 0)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    inputMethodManager.hideSoftInputFromWindow(binding.urlInput.windowToken, 0)
  }
}
