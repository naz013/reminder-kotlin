package com.elementary.tasks.reminder.build.valuedialog.controller.action

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.databinding.BuilderItemPhoneNumberBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class PhoneInputController(
  builderItem: BuilderItem<String>,
  private val permissionFlow: PermissionFlow,
  private val contactPicker: ContactPicker,
  private val inputMethodManager: InputMethodManager
) : AbstractBindingValueController<String, BuilderItemPhoneNumberBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemPhoneNumberBinding {
    return BuilderItemPhoneNumberBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.numberInputView.isFocusableInTouchMode = true
    binding.numberInputView.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) {
        inputMethodManager.hideSoftInputFromWindow(binding.numberInputView.windowToken, 0)
      } else {
        inputMethodManager.showSoftInput(binding.numberInputView, 0)
      }
    }
    binding.numberInputView.setOnClickListener {
      if (!inputMethodManager.isActive(binding.numberInputView)) {
        inputMethodManager.showSoftInput(binding.numberInputView, 0)
      }
    }
    binding.numberInputView.doOnTextChanged { text, _, _, _ ->
      updateValue(text?.toString())
    }
    binding.numberInputView.setImeOptions(EditorInfo.IME_ACTION_DONE)
    binding.numberInputView.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        inputMethodManager.hideSoftInputFromWindow(binding.numberInputView.windowToken, 0)
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }

    binding.selectNumberButton.setOnClickListener {
      permissionFlow.askPermission(Permissions.READ_CONTACTS) {
        contactPicker.pickContact {
          binding.numberInputView.setText(it.phone)
          updateValue(it.phone)
        }
      }
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    binding.numberInputView.setText(data ?: "")
  }

  override fun onDestroy() {
    super.onDestroy()
    inputMethodManager.hideSoftInputFromWindow(binding.numberInputView.windowToken, 0)
  }
}
