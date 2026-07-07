package com.elementary.tasks

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

object AssetsUtilExtended {
  fun getFontNames(): List<String> = emptyList()

  fun getTypeface(
    context: Context,
    code: Int,
  ): Typeface? = ResourcesCompat.getFont(context, R.font.roboto_regular)
}
