package com.elementary.tasks.core.os

import android.content.Context

class ContextSwitcher(
  private val contextProvider: ContextProvider
) {

  fun switchContext(context: Context) {
    contextProvider.switchContext(context.applicationContext)
  }
}
