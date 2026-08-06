package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class BypassDndFormatter(
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
