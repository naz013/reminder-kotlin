package com.elementary.tasks.core.utils.ui

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.view.visibleGone
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.textfield.TextInputLayout

fun AppCompatEditText.onTextChanged(f: (String?) -> Unit): TextWatcher {
  return doOnTextChanged { text, _, _, _ -> f.invoke(text?.toString()) }
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
  showError(context.getString(message))
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
