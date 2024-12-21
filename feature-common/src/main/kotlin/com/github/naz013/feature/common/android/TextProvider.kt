package com.github.naz013.feature.common.android

import android.content.Context
import androidx.annotation.StringRes

class TextProvider(initContext: Context) {

  private var context: Context = initContext

  fun updateContext(newContext: Context) {
    if (
      newContext.resources.configuration.locale.language !=
      context.resources.configuration.locale.language
    ) {
      this.context = newContext
    }
  }

  fun getText(@StringRes id: Int): String {
    return context.getString(id)
  }

  fun getText(@StringRes id: Int, vararg args: Any): String {
    return context.getString(id, *args)
  }
}
