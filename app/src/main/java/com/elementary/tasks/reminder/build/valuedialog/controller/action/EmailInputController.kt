package com.elementary.tasks.reminder.build.valuedialog.controller.action

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import com.elementary.tasks.core.os.PermissionFlow
import com.github.naz013.common.Permissions
import com.elementary.tasks.databinding.BuilderItemEmailBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class EmailInputController(
  builderItem: BuilderItem<String>,
  private val permissionFlow: PermissionFlow,
  private val inputMethodManager: InputMethodManager
) : AbstractBindingValueController<String, BuilderItemEmailBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemEmailBinding {
    return BuilderItemEmailBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.emailInputField.isFocusableInTouchMode = true
    binding.emailInputField.setOnFocusChangeListener { _, hasFocus ->
      if (isResumed()) {
        if (!hasFocus) {
          inputMethodManager.hideSoftInputFromWindow(binding.emailInputField.windowToken, 0)
        } else {
          inputMethodManager.showSoftInput(binding.emailInputField, 0)
        }
      }
    }
    binding.emailInputField.setOnClickListener {
      permissionFlow.askPermission(
        permission = Permissions.READ_CONTACTS,
        callback = { startInput() },
        deniedCallback = { startInput() }
      )
    }
    binding.emailInputField.doOnTextChanged { text, _, _, _ ->
      if (isResumed()) {
        updateValue(text?.toString())
      }
    }
    binding.emailInputField.setImeOptions(EditorInfo.IME_ACTION_DONE)
    binding.emailInputField.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        inputMethodManager.hideSoftInputFromWindow(binding.emailInputField.windowToken, 0)
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    binding.emailInputField.setText(data ?: "")
  }

  private fun startInput() {
    if (!inputMethodManager.isActive(binding.emailInputField)) {
      inputMethodManager.showSoftInput(binding.emailInputField, 0)
    }
  }
}
