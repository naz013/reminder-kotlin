package com.elementary.tasks.core.appwidgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.core.os.intentForClass
import com.elementary.tasks.core.utils.ui.ViewUtils

object WidgetUtils {

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

  fun initButton(
    context: Context,
    rv: RemoteViews,
    @DrawableRes iconId: Int,
    @ColorRes color: Int,
    @IdRes viewId: Int,
    cls: Class<*>
  ) {
    val configIntent = context.intentForClass(cls)
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
      ViewUtils.createIcon(context, iconId, ContextCompat.getColor(context, color))
    )
  }

  fun setIcon(rv: RemoteViews, @DrawableRes iconId: Int, @IdRes viewId: Int) {
    rv.setImageViewResource(viewId, iconId)
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
}
