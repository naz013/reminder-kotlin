package com.github.naz013.feature.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.bi.OtherParams

internal class OtherParamsFormatter(
  private val context: Context,
) : Formatter<OtherParams>() {
  override fun format(otherParams: OtherParams): String =
    if (otherParams.useGlobal) {
      context.getString(R.string.default_string)
    } else {
      buildString(otherParams)
    }

  private fun buildString(otherParams: OtherParams): String =
    listOfNotNull(
      context.getString(R.string.vibrate).takeIf { otherParams.vibrate },
      context.getString(R.string.repeat_notification).takeIf { otherParams.repeatNotification },
      context.getString(R.string.voice_notification).takeIf { otherParams.notifyByVoice },
    ).joinToString(separator = "\n") { "• $it" }
}
