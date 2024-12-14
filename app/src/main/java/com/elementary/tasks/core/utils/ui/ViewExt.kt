package com.elementary.tasks.core.utils.ui

import android.view.View
import android.view.View.OnLayoutChangeListener

fun View.getSize(block: (width: Int, height: Int) -> Unit) {
  addOnLayoutChangeListener(
    object : OnLayoutChangeListener {
      override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
      ) {
        removeOnLayoutChangeListener(this)
        block(v?.height ?: -1, v?.width ?: -1)
      }
    }
  )
}

fun View.singleClick(function: (View) -> Unit) {
  this.setOnClickListener {
    if (shouldDispatchClick(it)) {
      function(it)
    }
  }
}

private fun shouldDispatchClick(key: Any): Boolean {
  return if (ClickMap.viewClickMap.containsKey(key)) {
    (System.currentTimeMillis() - (ClickMap.viewClickMap[key] ?: 0L) > ClickMap.DELAY).also {
      if (it) {
        ClickMap.viewClickMap[key] = System.currentTimeMillis()
      }
    }
  } else {
    ClickMap.viewClickMap[key] = System.currentTimeMillis()
    true
  }
}

object ClickMap {
  const val DELAY = 500L
  val viewClickMap = mutableMapOf<Any, Long>()
}
