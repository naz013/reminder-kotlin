package com.elementary.tasks.core.utils.ui

import android.annotation.SuppressLint
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("ClickableViewAccessibility")
fun RecyclerView.listenScrollableView(listener: ((x: Int) -> Unit)?) {
  setOnScrollChangeListener { _, _, scrollY, _, _ ->
    listener?.invoke(scrollY)
  }
}

fun AppCompatTextView.text() = text.toString()
