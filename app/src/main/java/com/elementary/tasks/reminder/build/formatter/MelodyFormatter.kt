package com.elementary.tasks.reminder.build.formatter

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
