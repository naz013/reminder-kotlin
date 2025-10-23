package com.elementary.tasks.reminder.lists.filter

import org.threeten.bp.LocalDate

data class UiFilterGroup(
  val id: String,
  val title: String,
  val filter: UiFilter
)

interface UiFilter

data class UiReminderGroupFilter(
  val chips: List<UiReminderGroupFilterChip>
) : UiFilter

data class UiReminderGroupFilterChip(
  val id: String,
  val label: String,
  val isSelected: Boolean
)

data class UiDateRangeFilter(
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val startDate: LocalDate?,
  val endDate: LocalDate?
) : UiFilter
