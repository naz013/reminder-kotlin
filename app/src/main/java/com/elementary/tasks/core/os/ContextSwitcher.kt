package com.elementary.tasks.core.os

import android.content.Context
import com.github.naz013.common.ContextProvider

class ContextSwitcher(
  private val contextProvider: ContextProvider
) {

  fun switchContext(context: Context) {
    contextProvider.switchContext(context)
  }
}
