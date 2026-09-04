package com.elementary.tasks.module.uicommon

import android.content.Context
import android.graphics.Typeface
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.ui.common.font.FontApi

class FontApiImpl(
  private val context: Context,
  private val buildInfo: BuildInfo,
) : FontApi {
  override fun getTypeface(code: Int): Typeface? = AssetsUtil.getTypeface(context, code, buildInfo.isPro)
}
