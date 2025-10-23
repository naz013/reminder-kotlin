package com.elementary.tasks.reminder.lists.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.threeten.bp.LocalDate

typealias FilterGroupId = String
typealias FilterId = String

@Parcelize
data class Filters(
  val filterGroups: List<FilterGroup>
) : Parcelable

interface FilterGroup : Parcelable {
  val id: FilterGroupId
  val title: String
  val appliedFilter: AppliedFilter?
}

@Parcelize
data class ReminderGroupFilterGroup(
  override val id: FilterGroupId,
  override val title: String,
  override val appliedFilter: ReminderGroupAppliedFilter?,
  val filters: List<ReminderGroupFilter>
) : FilterGroup

@Parcelize
data class ReminderGroupFilter(
  val id: FilterId,
  val label: String,
) : Parcelable

@Parcelize
data class DateRangeFilterGroup(
  override val id: FilterGroupId,
  override val title: String,
  override val appliedFilter: DateRangeAppliedFilter?,
  val minDate: LocalDate,
  val maxDate: LocalDate
) : FilterGroup

@Parcelize
data class AppliedFilters(
  val selectedFilters: Map<FilterGroupId, AppliedFilter> = emptyMap()
) : Parcelable

interface AppliedFilter : Parcelable

@Parcelize
data class ReminderGroupAppliedFilter(
  val selectedFilterIds: Set<FilterId>
) : AppliedFilter

@Parcelize
data class DateRangeAppliedFilter(
  val startDate: LocalDate?,
  val endDate: LocalDate?
) : AppliedFilter
