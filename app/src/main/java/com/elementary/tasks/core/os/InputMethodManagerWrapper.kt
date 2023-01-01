package com.elementary.tasks.core.os

import android.view.View
import android.view.Window

class InputMethodManagerWrapper(systemServiceProvider: SystemServiceProvider) {

  private val inputMethodManager = systemServiceProvider.provideInputMethodManager()

  fun hideKeyboard(view: View) {
    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
  }

  fun hideKeyboard(window: Window?) {
    val currentToken = window?.currentFocus?.windowToken ?: return
    inputMethodManager?.hideSoftInputFromWindow(currentToken, 0)
  }
}
