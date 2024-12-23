package com.elementary.tasks.core.utils

import android.content.Context
import android.graphics.Typeface
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.ui.common.font.FontApi

class FontApiImpl(
  private val context: Context
) : FontApi {
  override fun getTypeface(code: Int): Typeface? {
    return AssetsUtil.getTypeface(context, code)
  }
}
