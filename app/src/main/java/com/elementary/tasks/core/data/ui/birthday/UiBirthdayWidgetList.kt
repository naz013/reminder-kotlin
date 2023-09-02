package com.elementary.tasks.core.data.ui.birthday

import com.elementary.tasks.core.data.ui.widget.DateSorted

data class UiBirthdayWidgetList(
  val uuId: String,
  val name: String = "",
  val ageFormattedAndBirthdayDate: String = "",
  val remainingTimeFormatted: String? = null,
  override val millis: Long
) : DateSorted
