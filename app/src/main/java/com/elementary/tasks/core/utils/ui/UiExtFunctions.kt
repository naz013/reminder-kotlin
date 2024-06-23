package com.elementary.tasks.core.utils.ui

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.os.dp2px
import com.elementary.tasks.core.utils.lazyUnSynchronized
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.textfield.TextInputLayout

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isTransparent(): Boolean = visibility == View.INVISIBLE

fun View.transparent() {
  visibility = View.INVISIBLE
}

fun View.gone() {
  visibility = View.GONE
}

fun View.visible() {
  visibility = View.VISIBLE
}

fun View.visibleGone(value: Boolean) {
  if (value && !isVisible()) {
    visible()
  } else if (!value && !isGone()) {
    gone()
  }
}

fun View.visibleInvisible(value: Boolean) {
  if (value && !isVisible()) {
    visible()
  } else if (!value && !isTransparent()) {
    transparent()
  }
}

fun <ViewT : View> View.bindView(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronized {
    findViewById(idRes)
  }
}

fun View.colorOf(@ColorRes color: Int) = ContextCompat.getColor(context, color)

fun AppCompatEditText.onTextChanged(f: (String?) -> Unit): TextWatcher {
  return doOnTextChanged { text, _, _, _ -> f.invoke(text?.toString()) }
}

fun View.inflater(): LayoutInflater = LayoutInflater.from(context)

fun View.dp2px(dp: Int) = context.dp2px(dp)

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
  fadeOut.interpolator = AccelerateInterpolator() // and this
  fadeOut.duration = 400
  animation = fadeOut
  visibility = View.GONE
}

fun Toolbar.tintOverflowButton(isDark: Boolean): Boolean {
  val overflowIcon = overflowIcon ?: return false
  val color = if (isDark) {
    context.colorOf(R.color.whitePrimary)
  } else {
    context.colorOf(R.color.pureBlack)
  }
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

@SuppressLint("ClickableViewAccessibility")
fun NestedScrollView.listenScrollableView(listener: ((x: Int) -> Unit)?) {
  setOnScrollChangeListener { _, _, scrollY, _, _ ->
    listener?.invoke(scrollY)
  }
}

@SuppressLint("ClickableViewAccessibility")
fun RecyclerView.listenScrollableView(listener: ((x: Int) -> Unit)?) {
  setOnScrollChangeListener { _, _, scrollY, _, _ ->
    listener?.invoke(scrollY)
  }
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

fun EditText.readText() = text.toString()

fun TextView.setTextOrHide(text: String?) {
  visibleGone(!text.isNullOrEmpty())
  this.text = text
}

fun TabLayout.onTabSelected(function: (TabLayout.Tab) -> Unit) {
  addOnTabSelectedListener(object : OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
      if (tab != null) {
        function(tab)
      }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) { }

    override fun onTabUnselected(tab: TabLayout.Tab?) { }
  })
}
