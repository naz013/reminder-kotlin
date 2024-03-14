package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.bi.OtherParams

class OtherParamsFormatter(
  private val context: Context
) : Formatter<OtherParams>() {

  override fun format(otherParams: OtherParams): String {
    return if (otherParams.useGlobal) {
      context.getString(R.string.default_string)
    } else {
      buildString(otherParams)
    }
  }

  private fun buildString(otherParams: OtherParams): String {
    return listOfNotNull(
      context.getString(R.string.vibrate).takeIf { otherParams.vibrate },
      context.getString(R.string.repeat_notification).takeIf { otherParams.repeatNotification },
      context.getString(R.string.voice_notification).takeIf { otherParams.notifyByVoice }
    ).joinToString(separator = "\n") { "• $it" }
  }
}
