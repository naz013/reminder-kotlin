package com.elementary.tasks.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import timber.log.Timber

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                                v.setBackgroundColor(ThemeUtil.adjustAlpha(color, 25))
                            }
                            return@setOnDragListener true
                        }
                    }
                    false
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    if (markAction) {
                        v.setBackgroundColor(ThemeUtil.adjustAlpha(color, 50))
                    }
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    if (markAction) {
                        v.setBackgroundColor(ThemeUtil.adjustAlpha(color, 25))
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
                    v.setBackgroundColor(Color.argb(0,255,255,255))
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                else -> false
            }
        }
    }

    fun isHorizontal(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun tintOverflowButton(toolbar: Toolbar, isDark: Boolean): Boolean {
        val overflowIcon = toolbar.overflowIcon ?: return false
        val color = if (isDark) {
            ContextCompat.getColor(toolbar.context, R.color.whitePrimary)
        } else {
            ContextCompat.getColor(toolbar.context, R.color.pureBlack)
        }
        val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        overflowIcon.colorFilter = colorFilter
        return true
    }

    fun navIcon(context: Context, isDark: Boolean): Drawable? {
        return if (isDark) {
            ContextCompat.getDrawable(context, R.drawable.ic_new_menu_big_white)
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_new_menu_big)
        }
    }

    fun backIcon(context: Context, isDark: Boolean): Drawable? {
        return tintIcon(context, R.drawable.ic_twotone_arrow_back_24px, isDark)
    }

    fun createIcon(context: Context, @DrawableRes res: Int, @ColorInt color: Int): Bitmap? {
        var icon = ContextCompat.getDrawable(context, res)
        if (icon != null) {
            icon = (DrawableCompat.wrap(icon)).mutate()
            DrawableCompat.setTint(icon, color)
            DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
            if (icon != null) {
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
        menu?.getItem(index)?.icon = tintIcon(context, resource, isDark)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun listenScrollableView(scrollView: ScrollView, listener: ((x: Int) -> Unit)?) {
        val onScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
            listener?.invoke(scrollView.scrollY)
        }
        scrollView.setOnTouchListener(object : View.OnTouchListener {
            private var observer: ViewTreeObserver? = null
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (observer == null) {
                    observer = scrollView.viewTreeObserver
                    observer?.addOnScrollChangedListener(onScrollChangedListener)
                } else if (!observer!!.isAlive) {
                    observer?.removeOnScrollChangedListener(onScrollChangedListener)
                    observer = scrollView.viewTreeObserver
                    observer?.addOnScrollChangedListener(onScrollChangedListener)
                }
                return false
            }
        })
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
                } else if (!observer!!.isAlive) {
                    observer?.removeOnScrollChangedListener(onScrollChangedListener)
                    observer = scrollView.viewTreeObserver
                    observer?.addOnScrollChangedListener(onScrollChangedListener)
                }
                return false
            }
        })
    }

    fun listenScrollableView(recyclerView: RecyclerView, listener: ((y: Int) -> Unit)?) {
        if (Module.isMarshmallow) {
            recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                listener?.invoke(if (recyclerView.canScrollVertically(-1)) 1 else 0)
            }
        } else {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    listener?.invoke(if (recyclerView.canScrollVertically(-1)) 1 else 0)
                }
            })
        }
    }

    fun fadeInAnimation(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.startOffset = 400
        fadeIn.duration = 400
        view.animation = fadeIn
        view.visibility = View.VISIBLE
    }

    fun fadeOutAnimation(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator() //and this
        fadeOut.duration = 400
        view.animation = fadeOut
        view.visibility = View.GONE
    }
}
