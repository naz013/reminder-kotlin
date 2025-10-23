package com.elementary.tasks.reminder.lists.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class ReminderFilterDialogViewModel(
  private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

  private val _filterGroups = mutableLiveDataOf<List<UiFilterGroup>>()
  val filterGroups = _filterGroups.toLiveData()

  private val _applyFilters = mutableLiveEventOf<AppliedFilters>()
  val applyFilters = _applyFilters.toLiveData()

  private var filters = Filters(emptyList())
  private var appliedFilters = AppliedFilters()

  fun setFilters(filters: Filters) {
    this.filters = filters
    Logger.i(TAG, "Setting filters: ${filters.filterGroups.size} groups")
    viewModelScope.launch(dispatcherProvider.default()) {
      val uiGroups = prepareFiltersUi(filters)
      val appliedMap = mutableMapOf<String, AppliedFilter>()
      for (group in filters.filterGroups) {
        group.appliedFilter?.let {
          appliedMap[group.id] = it
        }
      }
      appliedFilters = AppliedFilters(appliedMap)
      withContext(dispatcherProvider.main()) {
        _filterGroups.value = uiGroups
      }
    }
  }

  fun onApplyFilters() {
    Logger.i(TAG, "Applying filters: ${appliedFilters.selectedFilters.size}")
    _applyFilters.value = Event(appliedFilters)
  }

  fun dateRangeChanged(
    groupId: String,
    startDate: LocalDate?,
    endDate: LocalDate?
  ) {
    Logger.i(TAG, "On date range changed: $groupId, $startDate - $endDate")
    viewModelScope.launch(dispatcherProvider.default()) {
      val currentGroups = _filterGroups.value ?: return@launch
      val updatedGroups = currentGroups.map { group ->
        if (group.id == groupId && group.filter is UiDateRangeFilter) {
          UiFilterGroup(
            id = group.id,
            title = group.title,
            filter = UiDateRangeFilter(
              minDate = group.filter.minDate,
              maxDate = group.filter.maxDate,
              startDate = startDate,
              endDate = endDate
            )
          )
        } else {
          group
        }
      }

      val updatedAppliedFilters = appliedFilters.selectedFilters.toMutableMap()
      if (startDate != null || endDate != null) {
        updatedAppliedFilters[groupId] = DateRangeAppliedFilter(startDate, endDate)
      } else {
        updatedAppliedFilters.remove(groupId)
      }
      appliedFilters = AppliedFilters(updatedAppliedFilters)

      withContext(dispatcherProvider.main()) {
        _filterGroups.value = updatedGroups
      }
    }
  }

  fun toggleFilter(groupId: String, filterId: String) {
    Logger.i(TAG, "Toggling filter: $groupId, $filterId")
    viewModelScope.launch(dispatcherProvider.default()) {
      val currentGroups = _filterGroups.value ?: return@launch
      val updatedGroups = currentGroups.map { group ->
        if (group.id == groupId && group.filter is UiReminderGroupFilter) {
          val updatedFilters = group.filter.chips.map { filter ->
            if (filter.id == filterId) {
              filter.copy(isSelected = !filter.isSelected)
            } else {
              filter
            }
          }
          UiFilterGroup(
            id = group.id,
            title = group.title,
            filter = UiReminderGroupFilter(updatedFilters)
          )
        } else {
          group
        }
      }

      val updatedAppliedFilters = appliedFilters.selectedFilters.toMutableMap()
      val selectedFilterIds = updatedGroups
        .first { it.id == groupId }
        .filter
        .let { it as UiReminderGroupFilter }
        .chips
        .filter { it.isSelected }
        .map { it.id }
        .toSet()
      if (selectedFilterIds.isNotEmpty()) {
        updatedAppliedFilters[groupId] = ReminderGroupAppliedFilter(selectedFilterIds)
      } else {
        updatedAppliedFilters.remove(groupId)
      }
      appliedFilters = AppliedFilters(updatedAppliedFilters)

      withContext(dispatcherProvider.main()) {
        _filterGroups.value = updatedGroups
      }
    }
  }

  fun clearAllFilters() {
    Logger.i(TAG, "Clearing all filters")
    viewModelScope.launch(dispatcherProvider.default()) {
      val currentGroups = _filterGroups.value ?: return@launch
      val updatedGroups = currentGroups.map { group ->
        when (group.filter) {
          is UiReminderGroupFilter -> {
            val clearedChips = group.filter.chips.map { chip ->
              chip.copy(isSelected = false)
            }
            UiFilterGroup(
              id = group.id,
              title = group.title,
              filter = UiReminderGroupFilter(clearedChips)
            )
          }
          is UiDateRangeFilter -> {
            UiFilterGroup(
              id = group.id,
              title = group.title,
              filter = UiDateRangeFilter(
                minDate = group.filter.minDate,
                maxDate = group.filter.maxDate,
                startDate = null,
                endDate = null
              )
            )
          }
          else -> group
        }
      }
      appliedFilters = AppliedFilters()
      withContext(dispatcherProvider.main()) {
        _filterGroups.value = updatedGroups
      }
    }
  }

  private fun prepareFiltersUi(filters: Filters): List<UiFilterGroup> {
    return filters.filterGroups.map { group ->
      when (group) {
        is ReminderGroupFilterGroup -> {
          val chips = group.filters.map { filter ->
            UiReminderGroupFilterChip(
              id = filter.id,
              label = filter.label,
              isSelected = group.appliedFilter?.selectedFilterIds?.contains(filter.id) == true
            )
          }
          UiFilterGroup(
            id = group.id,
            title = group.title,
            filter = UiReminderGroupFilter(chips)
          )
        }
        is DateRangeFilterGroup -> {
          UiFilterGroup(
            id = group.id,
            title = group.title,
            filter = UiDateRangeFilter(
              minDate = group.minDate,
              maxDate = group.maxDate,
              startDate = group.appliedFilter?.startDate,
              endDate = group.appliedFilter?.endDate
            )
          )
        }
        else -> throw IllegalArgumentException("Unknown filter group type")
      }
    }
  }

  companion object {
    private const val TAG = "ReminderFilterDialogVM"
  }
}
