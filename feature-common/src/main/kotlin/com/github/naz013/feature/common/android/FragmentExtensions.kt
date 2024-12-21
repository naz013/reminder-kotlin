package com.github.naz013.feature.common.android

import android.content.Intent
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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
