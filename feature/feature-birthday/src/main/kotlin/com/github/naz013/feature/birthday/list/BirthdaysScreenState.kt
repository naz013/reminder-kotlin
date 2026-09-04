package com.github.naz013.feature.birthday.list

import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.tag.TagChipState

internal data class BirthdaysScreenState(
  val listState: ListState = ListState.Loading,
  val hasAnyItems: Boolean = true,
  val searchQuery: String = "",
  val selectedSmartList: SmartListFilter? = null,
  val selectedTagId: String? = null,
  val availableTags: List<TagChipState> = emptyList(),
  val confirmDeleteId: String? = null,
  val selectedCount: Int = 0,
)

internal fun BirthdaysScreenState.withSelectedItem(selectedItemId: String?): BirthdaysScreenState {
  val ready = listState as? ListState.Ready ?: return this
  return copy(
    listState = ListState.Ready(ready.items.map { it.copy(isHighlighted = it.id == selectedItemId) }),
  )
}

internal sealed interface ListState {
  data object Loading : ListState
  data class Ready(val items: List<UiAgendaBirthday>) : ListState
  data object Empty : ListState
}
