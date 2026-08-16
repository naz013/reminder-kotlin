package com.github.naz013.feature.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import java.io.File

class MelodyFormatter : Formatter<String>() {
  override fun format(path: String): String {
    if (path.isEmpty()) return ""
    val file = File(path)
    return if (file.exists()) {
      file.name
    } else {
      path
    }
  }
}
