package com.elementary.tasks

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

object AssetsUtilExtended {

  fun getFontNames(): List<String> {
    return listOf(
      "Pacifico",
      "Caveat",
      "Ubuntu Mono",
      "Ubuntu Condensed",
      "Marck Script",
      "Bad Script"
    )
  }

  fun getTypeface(context: Context, code: Int): Typeface? {
    return when (code) {
      21 -> ResourcesCompat.getFont(context, R.font.pacifico_regular)
      22 -> ResourcesCompat.getFont(context, R.font.caveat_regular)
      23 -> ResourcesCompat.getFont(context, R.font.ubuntu_mono_regular)
      24 -> ResourcesCompat.getFont(context, R.font.ubuntu_condensed_regular)
      25 -> ResourcesCompat.getFont(context, R.font.marck_script_regular)
      26 -> ResourcesCompat.getFont(context, R.font.bad_script_regular)
      else -> ResourcesCompat.getFont(context, R.font.roboto_regular)
    }
  }
}
