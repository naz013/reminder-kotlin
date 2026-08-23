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
)

internal sealed interface ListState {
  data object Loading : ListState
  data class Ready(val items: List<UiAgendaBirthday>) : ListState
  data object Empty : ListState
}
