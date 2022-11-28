package com.elementary.tasks.core.utils

import android.content.Context
import androidx.annotation.StringRes

class TextProvider(
  private val context: Context
) {

  fun getText(@StringRes id: Int): String {
    return context.getString(id)
  }
}