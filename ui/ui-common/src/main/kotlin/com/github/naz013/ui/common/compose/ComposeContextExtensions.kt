package com.github.naz013.ui.common.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.inputmethod.InputMethodManager

/** Best-effort IME dismiss for a promoted Nav3 screen leaving composition - no Fragment needed. */
fun Context.hideKeyboard() {
  val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
  (this as? Activity)?.window?.currentFocus?.windowToken?.let { imm?.hideSoftInputFromWindow(it, 0) }
}

/** Unwraps a Compose `LocalContext.current`, which is commonly a [ContextWrapper], down to the
 *  underlying [Activity] - needed for APIs like the Play In-App Review flow that require an
 *  Activity rather than any Context. */
fun Context.findActivity(): Activity? {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  return null
}
