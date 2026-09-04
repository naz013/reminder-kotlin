package com.elementary.tasks.core.notes

import android.content.Context
import android.graphics.Typeface
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.ui.note.NoteFontProvider

class AppNoteFontProvider(
  private val buildInfo: BuildInfo,
) : NoteFontProvider {
  override fun getTypeface(context: Context, code: Int): Typeface? =
    AssetsUtil.getTypeface(context, code, buildInfo.isPro)

  override fun getFontNames(): List<String> = AssetsUtil.getFontNames(buildInfo.isPro)
}
