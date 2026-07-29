package com.github.naz013.appwidgets

import android.content.Context

internal class WidgetUpdater(
  private val context: Context,
) {

  fun update(block: Context.() -> Unit) {
    block.invoke(context)
  }
}
