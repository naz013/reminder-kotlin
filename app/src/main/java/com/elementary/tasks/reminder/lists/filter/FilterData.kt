package com.elementary.tasks.reminder.lists.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

typealias FilterGroupId = String
typealias FilterId = String

@Parcelize
data class FilterGroup(
  val id: FilterGroupId,
  val title: String,
  val filters: List<Filter>
) : Parcelable

@Parcelize
data class Filter(
  val id: FilterId,
  val label: String,
  val isSelected: Boolean
) : Parcelable

@Parcelize
data class AppliedFilters(
  val selectedFilters: Map<FilterGroupId, List<FilterId>>
) : Parcelable
