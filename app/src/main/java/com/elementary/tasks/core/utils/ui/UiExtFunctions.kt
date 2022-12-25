package com.elementary.tasks.core.utils.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.AutoCompleteTextView
import android.widget.ScrollView
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.colorOf
import com.google.android.material.textfield.TextInputLayout

fun Context.dp2px(dp: Int): Int {
  val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
  var display: Display? = null
  if (wm != null) {
    display = wm.defaultDisplay
  }
  val displaymetrics = DisplayMetrics()
  display?.getMetrics(displaymetrics)
  return (dp * displaymetrics.density + 0.5f).toInt()
}

fun View.dp2px(dp: Int) = context.dp2px(dp)

fun Fragment.dp2px(dp: Int) = requireContext().dp2px(dp)

@Px
fun Context.getActionBarSize(): Int {
  val value = TypedValue()
  theme.resolveAttribute(android.R.attr.actionBarSize, value, true)
  return TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
}

fun Context.isHorizontal() =
  resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun View.fadeInAnimation() {
  val fadeIn = AlphaAnimation(0f, 1f)
  fadeIn.interpolator = DecelerateInterpolator()
  fadeIn.startOffset = 400
  fadeIn.duration = 400
  animation = fadeIn
  visibility = View.VISIBLE
}

fun View.fadeOutAnimation() {
  val fadeOut = AlphaAnimation(1f, 0f)
  fadeOut.interpolator = AccelerateInterpolator() //and this
  fadeOut.duration = 400
  animation = fadeOut
  visibility = View.GONE
}

fun Toolbar.tintOverflowButton(isDark: Boolean): Boolean {
  val overflowIcon = overflowIcon ?: return false
  val color = if (isDark) context.colorOf(R.color.whitePrimary)
  else context.colorOf(R.color.pureBlack)
  val colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
  overflowIcon.colorFilter = colorFilter
  return true
}

@SuppressLint("ClickableViewAccessibility")
fun ScrollView.listenScrollableView(listener: ((x: Int) -> Unit)?) {
  val onScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
    listener?.invoke(scrollY)
  }
  setOnTouchListener(object : View.OnTouchListener {
    private var observer: ViewTreeObserver? = null
    override fun onTouch(v: View, event: MotionEvent): Boolean {
      if (observer == null) {
        observer = viewTreeObserver
        observer?.addOnScrollChangedListener(onScrollChangedListener)
      } else if (observer?.isAlive == false) {
        observer?.removeOnScrollChangedListener(onScrollChangedListener)
        observer = viewTreeObserver
        observer?.addOnScrollChangedListener(onScrollChangedListener)
      }
      return false
    }
  })
}

fun TextInputLayout.showError(@StringRes message: Int) {
  error = context.getString(message)
  isErrorEnabled = true
}

fun TextInputLayout.showError(message: String) {
  error = message
  isErrorEnabled = true
}

fun AppCompatEditText.trimmedText() = text.toString().trim()

fun AutoCompleteTextView.trimmedText() = text.toString().trim()

fun AppCompatTextView.text() = text.toString()