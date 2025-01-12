package com.github.naz013.ui.common.fragment

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.naz013.ui.common.context.dp2px
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.ui.common.context.startActivity

fun Fragment.dp2px(dp: Int) = requireContext().dp2px(dp)

fun Fragment.intentForClass(clazz: Class<*>): Intent {
  return requireContext().intentForClass(clazz)
}

fun Fragment.startActivity(clazz: Class<*>, builder: Intent.() -> Unit = { }) {
  requireActivity().startActivity(clazz, builder)
}

fun Fragment.colorOf(@ColorRes color: Int) = ContextCompat.getColor(requireContext(), color)

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(requireContext(), message, duration).show()
}

fun Fragment.argumentsString(key: String): String? {
  return arguments?.getString(key)
}

fun Fragment.argumentsString(key: String, default: String): String {
  return arguments?.getString(key, default) ?: default
}

fun Fragment.argumentsBoolean(key: String): Boolean? {
  return arguments?.getBoolean(key)
}

fun Fragment.argumentsBoolean(key: String, default: Boolean): Boolean {
  return arguments?.getBoolean(key, default) ?: default
}

fun Fragment.hideKeyboard(token: IBinder? = null) {
  val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
  if (token == null) {
    val currentToken = activity?.window?.currentFocus?.windowToken
    currentToken?.let { imm?.hideSoftInputFromWindow(token, 0) }
  } else {
    imm?.hideSoftInputFromWindow(token, 0)
  }
}
