package com.github.naz013.appwidgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.context.intentForClass

internal object WidgetUtils {

  private const val TAG = "WidgetUtils"

  fun initButton(
    context: Context,
    rv: RemoteViews,
    @DrawableRes iconId: Int,
    @ColorRes color: Int,
    @IdRes viewId: Int,
    intent: Intent,
    requestCode: Int = RandomRequestCode.generate()
  ) {
    val configPendingIntent = PendingIntentWrapper.getActivity(context, requestCode, intent, 0)
    rv.setOnClickPendingIntent(viewId, configPendingIntent)
    setIcon(context, rv, iconId, viewId, color)
  }

  fun initButton(
    context: Context,
    rv: RemoteViews,
    @DrawableRes iconId: Int,
    @ColorRes color: Int,
    @IdRes viewId: Int,
    cls: Class<*>,
    extras: ((Intent) -> Intent)? = null
  ) {
    var configIntent = context.intentForClass(cls)
    if (extras != null) {
      configIntent = extras.invoke(configIntent)
    }
    extras?.invoke(configIntent)
    val configPendingIntent = PendingIntentWrapper.getActivity(context, 0, configIntent, 0)
    rv.setOnClickPendingIntent(viewId, configPendingIntent)
    setIcon(context, rv, iconId, viewId, color)
  }

  fun setIcon(
    context: Context,
    rv: RemoteViews,
    @DrawableRes iconId: Int,
    @IdRes viewId: Int,
    @ColorRes color: Int
  ) {
    rv.setImageViewBitmap(
      viewId,
      createIcon(context, iconId, ContextCompat.getColor(context, color))
    )
  }

  fun createIcon(context: Context, @DrawableRes res: Int, @ColorInt color: Int): Bitmap? {
    var icon = ContextCompat.getDrawable(context, res)
    if (icon != null) {
      icon = (DrawableCompat.wrap(icon)).mutate()
      if (icon != null) {
        DrawableCompat.setTint(icon, color)
        DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
        val bitmap =
          Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        return bitmap
      }
    }
    return null
  }

  fun initButton(
    context: Context,
    rv: RemoteViews,
    @IdRes viewId: Int,
    icon: Bitmap?,
    cls: Class<*>,
    extras: ((Intent) -> Intent)? = null
  ) {
    var configIntent = context.intentForClass(cls)
    if (extras != null) {
      configIntent = extras.invoke(configIntent)
    }
    extras?.invoke(configIntent)
    val configPendingIntent = PendingIntentWrapper.getActivity(context, 0, configIntent, 0)
    rv.setOnClickPendingIntent(viewId, configPendingIntent)
    icon?.also {
      rv.setImageViewBitmap(viewId, it)
    }
  }

  @DrawableRes
  fun newWidgetBg(code: Int): Int {
    return when (code) {
      0 -> R.drawable.widget_bg_transparent
      1 -> R.drawable.widget_bg_white_20
      2 -> R.drawable.widget_bg_white_40
      3 -> R.drawable.widget_bg_white_60
      4 -> R.drawable.widget_bg_white
      5 -> R.drawable.widget_bg_light1
      6 -> R.drawable.widget_bg_light2
      7 -> R.drawable.widget_bg_light3
      8 -> R.drawable.widget_bg_light4
      9 -> R.drawable.widget_bg_dark1
      10 -> R.drawable.widget_bg_dark2
      11 -> R.drawable.widget_bg_dark3
      12 -> R.drawable.widget_bg_dark4
      13 -> R.drawable.widget_bg_black
      else -> R.drawable.widget_bg_black
    }
  }

  fun isDarkBg(code: Int): Boolean = code > 8

  fun getComposeColor(code: Int): Color {
    return when (code) {
      0 -> Color.Transparent
      1 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.25f)
      2 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.50f)
      3 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.75f)
      4 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 1f)
      5 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.97f)
      6 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.94f)
      7 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.90f)
      8 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.75f)
      9 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.60f)
      10 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.45f)
      11 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.30f)
      12 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.15f)
      13 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0f)
      else -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0f)
    }
  }

  fun getContrastColor(code: Int): Color {
    return getContrastColor(getComposeColor(code))
  }

  fun getContrastColor(color: Color): Color {
    val luminance = color.luminance()
    Logger.d(TAG, "Get contrast color for luminance: $luminance")
    return Color.hsl(
      hue = 0f,
      saturation = 0f,
      lightness = if (luminance > 0.5f) 0.1f else 0.9f
    )
  }
}
