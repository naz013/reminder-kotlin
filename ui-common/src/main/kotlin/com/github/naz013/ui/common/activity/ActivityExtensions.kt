package com.github.naz013.ui.common.activity

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.naz013.ui.common.context.startActivity

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
