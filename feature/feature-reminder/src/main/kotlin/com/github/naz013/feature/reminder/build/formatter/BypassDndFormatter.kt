package com.github.naz013.feature.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.github.naz013.ui.common.R

internal class BypassDndFormatter(
  private val context: Context,
) : Formatter<Boolean>() {
  override fun format(bypassDoNotDisturb: Boolean): String =
    context.getString(
      if (bypassDoNotDisturb) {
        R.string.bypass_do_not_disturb_enabled
      } else {
        R.string.bypass_do_not_disturb_disabled
      },
    )
}
