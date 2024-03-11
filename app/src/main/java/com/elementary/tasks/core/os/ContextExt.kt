package com.elementary.tasks.core.os

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

fun Context.colorOf(@ColorRes color: Int) = ContextCompat.getColor(this, color)

fun Context.intentForClass(clazz: Class<*>): Intent {
  return Intent(this, clazz)
    .setPackage(this.packageName)
    .setClassName(packageName, clazz.name)
}

fun Context.buildIntent(clazz: Class<*>, builder: Intent.() -> Unit = { }): Intent {
  return intentForClass(clazz)
    .apply { builder(this) }
}

fun Context.startActivity(clazz: Class<*>, builder: Intent.() -> Unit = { }) {
  buildIntent(clazz, builder)
    .run { startActivity(this) }
}

@Px
fun Context.dp2px(dp: Int): Int {
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp.toFloat(),
    resources.displayMetrics
  ).toInt()
}

@Px
fun Context.spToPx(sp: Float): Float {
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    sp,
    resources.displayMetrics
  )
}

@Px
fun Context.getActionBarSize(): Int {
  val value = TypedValue()
  theme.resolveAttribute(android.R.attr.actionBarSize, value, true)
  return TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
}

fun Context.isHorizontal() =
  resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
