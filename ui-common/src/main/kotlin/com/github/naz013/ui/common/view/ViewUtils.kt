package com.github.naz013.ui.common.view

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.Menu
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.adjustAlpha

object ViewUtils {

  fun registerDragAndDrop(
    activity: Activity,
    view: View,
    markAction: Boolean = true,
    @ColorInt color: Int,
    onDrop: (ClipData) -> Unit,
    vararg mimeTypes: String
  ) {
    view.setOnDragListener { v, event ->
      return@setOnDragListener when (event.action) {
        DragEvent.ACTION_DRAG_STARTED -> {
          Logger.d("registerDragAndDrop: started, ${event.clipDescription}")
          for (type in mimeTypes) {
            if (type == UriUtil.ANY_MIME || event.clipDescription.hasMimeType(type)) {
              if (markAction) {
                v.setBackgroundColor(color.adjustAlpha(25))
              }
              return@setOnDragListener true
            }
          }
          false
        }

        DragEvent.ACTION_DRAG_ENTERED -> {
          if (markAction) {
            v.setBackgroundColor(color.adjustAlpha(50))
          }
          true
        }

        DragEvent.ACTION_DRAG_EXITED -> {
          if (markAction) {
            v.setBackgroundColor(color.adjustAlpha(25))
          }
          true
        }

        DragEvent.ACTION_DROP -> {
          activity.requestDragAndDropPermissions(event)
          onDrop.invoke(event.clipData)
          true
        }

        DragEvent.ACTION_DRAG_ENDED -> {
          v.setBackgroundColor(Color.argb(0, 255, 255, 255))
          true
        }

        DragEvent.ACTION_DRAG_LOCATION -> true
        else -> false
      }
    }
  }

  fun backIcon(context: Context, isDark: Boolean): Drawable? {
    return tintIcon(context, R.drawable.ic_builder_arrow_left, isDark)
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

  fun tintIcon(context: Context, @DrawableRes resource: Int, @ColorInt color: Int): Drawable? {
    var icon = ContextCompat.getDrawable(context, resource)
    if (icon != null) {
      icon = (DrawableCompat.wrap(icon)).mutate()
      if (icon == null) return null
      DrawableCompat.setTint(icon, color)
      DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
      return icon
    }
    return null
  }

  @ColorInt
  fun tintIconColor(context: Context, isDark: Boolean): Int {
    return if (isDark) {
      ContextCompat.getColor(context, R.color.whitePrimary)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }
  }

  fun tintIcon(context: Context, @DrawableRes resource: Int, isDark: Boolean): Drawable? {
    var icon = ContextCompat.getDrawable(context, resource)
    if (icon != null) {
      icon = (DrawableCompat.wrap(icon)).mutate()
      if (icon == null) return null
      DrawableCompat.setTint(icon, tintIconColor(context, isDark))
      DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
      return icon
    }
    return null
  }

  fun tintMenuIcon(
    context: Context,
    menu: Menu?,
    index: Int,
    @DrawableRes resource: Int,
    isDark: Boolean
  ) {
    runCatching { menu?.getItem(index)?.icon = tintIcon(context, resource, isDark) }
  }

  fun listenScrollableView(recyclerView: RecyclerView, fabListener: (show: Boolean) -> Unit) {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val scrollY = recyclerView.scrollY
        if (scrollY > 0) {
          fabListener.invoke(false)
        } else {
          fabListener.invoke(true)
        }
      }
    })
  }
}
