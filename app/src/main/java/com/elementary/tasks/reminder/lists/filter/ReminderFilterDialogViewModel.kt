package com.elementary.tasks.reminder.lists.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReminderFilterDialogViewModel : ViewModel() {

  private val _filterGroups = MutableLiveData<List<FilterGroup>>()
  val filterGroups: LiveData<List<FilterGroup>> = _filterGroups

  private val _selectedFilters = MutableLiveData<Map<String, List<String>>>()
  val selectedFilters: LiveData<Map<String, List<String>>> = _selectedFilters

  fun setFilterGroups(groups: List<FilterGroup>) {
    _filterGroups.value = groups
    updateSelectedFilters()
  }

  fun toggleFilter(groupId: String, filterId: String) {
    val currentGroups = _filterGroups.value ?: return
    val updatedGroups = currentGroups.map { group ->
      if (group.id == groupId) {
        val updatedFilters = group.filters.map { filter ->
          if (filter.id == filterId) {
            Filter(filter.id, filter.label, !filter.isSelected)
          } else {
            filter
          }
        }
        FilterGroup(group.id, group.title, updatedFilters)
      } else {
        group
      }
    }
    _filterGroups.value = updatedGroups
    updateSelectedFilters()
  }

  fun clearAllFilters() {
    val currentGroups = _filterGroups.value ?: return
    val clearedGroups = currentGroups.map { group ->
      val clearedFilters = group.filters.map { filter ->
        Filter(filter.id, filter.label, false)
      }
      FilterGroup(group.id, group.title, clearedFilters)
    }
    _filterGroups.value = clearedGroups
    updateSelectedFilters()
  }

  private fun updateSelectedFilters() {
    val groups = _filterGroups.value ?: return
    val selected = groups.associate { group ->
      group.id to group.filters.filter { it.isSelected }.map { it.id }
    }.filterValues { it.isNotEmpty() }
    _selectedFilters.value = selected
  }

  fun getSelectedFiltersMap(): Map<String, List<String>> {
    return _selectedFilters.value ?: emptyMap()
  }
}
