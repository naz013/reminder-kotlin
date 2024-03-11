package com.elementary.tasks.home.scheduleview.data

import com.elementary.tasks.core.data.ui.UiTextElement
import org.threeten.bp.LocalDateTime

data class UiBirthdayScheduleList(
  val id: String,
  val dueDateTime: LocalDateTime,
  val mainText: UiTextElement,
  val secondaryText: UiTextElement?,
  val timeText: UiTextElement,
  val tags: List<UiTextElement>
)
