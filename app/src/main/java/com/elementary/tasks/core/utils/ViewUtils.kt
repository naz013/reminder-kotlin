package com.elementary.tasks.core.utils

import android.annotation.SuppressLint
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import timber.log.Timber

object ViewUtils {

  fun registerDragAndDrop(activity: Activity, view: View, markAction: Boolean = true,
                          @ColorInt color: Int,
                          onDrop: (ClipData) -> Unit, vararg mimeTypes: String) {
    view.setOnDragListener { v, event ->
      return@setOnDragListener when (event.action) {
        DragEvent.ACTION_DRAG_STARTED -> {
          Timber.d("registerDragAndDrop: started, ${event.clipDescription}")
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
          if (Module.isNougat) {
            activity.requestDragAndDropPermissions(event)
          }
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
    return tintIcon(context, R.drawable.ic_twotone_arrow_back_24px, isDark)
  }

  fun createIcon(context: Context, @DrawableRes res: Int, @ColorInt color: Int): Bitmap? {
    var icon = ContextCompat.getDrawable(context, res)
    if (icon != null) {
      icon = (DrawableCompat.wrap(icon)).mutate()
      if (icon != null) {
        DrawableCompat.setTint(icon, color)
        DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
        val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)
        return bitmap
      }
    }
    return null
  }

  fun tintIcon(context: Context, @DrawableRes resource: Int, isDark: Boolean): Drawable? {
    var icon = ContextCompat.getDrawable(context, resource)
    if (icon != null) {
      icon = (DrawableCompat.wrap(icon)).mutate()
      if (icon == null) return null
      val color = if (isDark) {
        ContextCompat.getColor(context, R.color.whitePrimary)
      } else {
        ContextCompat.getColor(context, R.color.pureBlack)
      }
      DrawableCompat.setTint(icon, color)
      DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
      return icon
    }
    return null
  }

  fun tintMenuIcon(context: Context, menu: Menu?, index: Int, @DrawableRes resource: Int, isDark: Boolean) {
    try {
      menu?.getItem(index)?.icon = tintIcon(context, resource, isDark)
    } catch (e: Exception) {
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  fun listenScrollableView(scrollView: NestedScrollView, listener: ((y: Int) -> Unit)?) {
    val onScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
      listener?.invoke(scrollView.scrollY)
    }
    scrollView.setOnTouchListener(object : View.OnTouchListener {
      private var observer: ViewTreeObserver? = null
      override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (observer == null) {
          observer = scrollView.viewTreeObserver
          observer?.addOnScrollChangedListener(onScrollChangedListener)
        } else if (observer?.isAlive == false) {
          observer?.removeOnScrollChangedListener(onScrollChangedListener)
          observer = scrollView.viewTreeObserver
          observer?.addOnScrollChangedListener(onScrollChangedListener)
        }
        return false
      }
    })
  }

  fun listenScrollableView(recyclerView: RecyclerView, listener: (y: Int) -> Unit, fabListener: ((show: Boolean) -> Unit)?) {
    if (Module.isMarshmallow) {
      recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
        listener.invoke(if (recyclerView.canScrollVertically(-1)) 1 else 0)
      }
      recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          super.onScrolled(recyclerView, dx, dy)
          if (fabListener != null) {
            if (dy > 0) {
              fabListener.invoke(false)
            } else if (dy <= 0) {
              fabListener.invoke(true)
            }
          }
        }
      })
    } else {
      recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          listener.invoke(if (recyclerView.canScrollVertically(-1)) 1 else 0)
          if (fabListener != null) {
            if (dy > 0) {
              fabListener.invoke(false)
            } else if (dy <= 0) {
              fabListener.invoke(true)
            }
          }
        }
      })
    }
  }

  fun listenScrollableView(recyclerView: RecyclerView, fabListener: (show: Boolean) -> Unit) {
    if (Module.isMarshmallow) {
      recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          super.onScrolled(recyclerView, dx, dy)
          val scrollY = recyclerView.scrollY
//                    Timber.d("onScrolled: $scrollY")
          if (scrollY > 0) {
            fabListener.invoke(false)
          } else {
            fabListener.invoke(true)
          }
        }
      })
    } else {
      recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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
}
