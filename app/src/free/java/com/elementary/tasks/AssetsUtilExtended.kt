package com.elementary.tasks

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

object AssetsUtilExtended {

  fun getFontNames(): List<String> {
    return emptyList()
  }

  fun getTypeface(context: Context, code: Int): Typeface? {
    return ResourcesCompat.getFont(context, R.font.roboto_regular)
  }
}
