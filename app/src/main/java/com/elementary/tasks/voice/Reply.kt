package com.elementary.tasks.voice

import java.util.*

data class Reply(
  var viewType: Int,
  var content: Any?,
  val uuId: String = UUID.randomUUID().toString()
) {

  companion object {

    const val REPLY = 0
    const val REMINDER = 1
    const val NOTE = 2
    const val PREFS = 3
    const val GROUP = 4
    const val RESPONSE = 5
    const val SHOW_MORE = 6
    const val BIRTHDAY = 7
    const val SHOPPING = 8
    const val ASK = 9
  }
}
