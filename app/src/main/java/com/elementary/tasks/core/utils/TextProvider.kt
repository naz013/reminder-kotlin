package com.elementary.tasks.core.utils

import android.content.Context
import androidx.annotation.StringRes

class TextProvider(
  private val context: Context
) {

  fun getText(@StringRes id: Int): String {
    return context.getString(id)
  }

  fun getText(@StringRes id: Int, vararg args: Any): String {
    return context.getString(id, *args)
  }
}
