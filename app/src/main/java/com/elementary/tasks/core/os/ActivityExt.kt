package com.elementary.tasks.core.os

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.elementary.tasks.core.utils.lazyUnSynchronized

fun <ViewT : View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronized {
    findViewById(idRes)
  }
}

fun Activity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}

fun Activity.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}

fun AppCompatActivity.colorOf(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Activity.finishWith(clazz: Class<*>, builder: Intent.() -> Unit = { }) {
  startActivity(clazz, builder)
  finish()
}

fun Activity.coloredStatusBarMode(
  @ColorInt color: Int = Color.WHITE,
  lightSystemUI: Boolean? = null
) {
  var flags: Int = window.decorView.systemUiVisibility // get current flags
  var systemLightUIFlag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
  var setSystemUILight = lightSystemUI

  if (setSystemUILight == null) {
    // Automatically check if the desired status bar is dark or light
    setSystemUILight = ColorUtils.calculateLuminance(color) < 0.5
  }

  flags = if (setSystemUILight) {
    // Set System UI Light (Battery Status Icon, Clock, etc)
    removeFlag(flags, systemLightUIFlag)
  } else {
    // Set System UI Dark (Battery Status Icon, Clock, etc)
    addFlag(flags, systemLightUIFlag)
  }

  window.decorView.systemUiVisibility = flags
  window.statusBarColor = color
}

private fun containsFlag(flags: Int, flagToCheck: Int) = (flags and flagToCheck) != 0

private fun addFlag(flags: Int, flagToAdd: Int): Int {
  return if (!containsFlag(flags, flagToAdd)) {
    flags or flagToAdd
  } else {
    flags
  }
}

private fun removeFlag(flags: Int, flagToRemove: Int): Int {
  return if (containsFlag(flags, flagToRemove)) {
    flags and flagToRemove.inv()
  } else {
    flags
  }
}
