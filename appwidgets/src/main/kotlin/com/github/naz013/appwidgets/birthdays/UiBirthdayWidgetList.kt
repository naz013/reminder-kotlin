package com.github.naz013.appwidgets.birthdays

import com.github.naz013.appwidgets.events.data.DateSorted

internal data class UiBirthdayWidgetList(
  val uuId: String,
  val name: String = "",
  val ageFormattedAndBirthdayDate: String = "",
  override val millis: Long
) : DateSorted
