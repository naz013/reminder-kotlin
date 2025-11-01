package com.github.naz013.ui.common.context

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

fun Context.colorOf(@ColorRes color: Int) = ContextCompat.getColor(this, color)

/**
 * Retrieves a color attribute value from the current theme.
 *
 * @param attrRes The attribute resource ID (e.g., R.attr.colorSurfaceContainer)
 * @param defaultColor The default color to return if the attribute is not found
 * @return The resolved color value as an integer
 */
@ColorInt
fun Context.getThemeColor(@AttrRes attrRes: Int, @ColorInt defaultColor: Int = 0): Int {
  val typedValue = TypedValue()
  return if (theme.resolveAttribute(attrRes, typedValue, true)) {
    typedValue.data
  } else {
    defaultColor
  }
}

/**
 * Retrieves a dimension attribute value from the current theme.
 *
 * @param attrRes The attribute resource ID (e.g., R.attr.cornerRadiusLarge)
 * @param defaultDp The default dimension in dp to return if the attribute is not found
 * @return The resolved dimension value in pixels
 */
@Px
fun Context.getThemeDimension(@AttrRes attrRes: Int, defaultDp: Float = 0f): Float {
  val typedValue = TypedValue()
  return if (theme.resolveAttribute(attrRes, typedValue, true)) {
    TypedValue.complexToDimension(typedValue.data, resources.displayMetrics)
  } else {
    dp2px(defaultDp.toInt()).toFloat()
  }
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
