package com.elementary.tasks.navigation.nav3

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

/** Best-effort IME dismiss for a promoted Nav3 screen leaving composition - no Fragment needed. */
fun Context.hideKeyboard() {
  val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
  (this as? Activity)?.window?.currentFocus?.windowToken?.let { imm?.hideSoftInputFromWindow(it, 0) }
}
