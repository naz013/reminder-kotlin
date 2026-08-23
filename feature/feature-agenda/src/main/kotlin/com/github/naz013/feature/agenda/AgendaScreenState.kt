package com.github.naz013.feature.agenda

import com.github.naz013.domain.Tag
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaItem

internal data class AgendaScreenState(
  val listState: ListState = ListState.Loading,
  val hasAnyItems: Boolean = true,
  val searchQuery: String = "",
  val selectedCategories: Set<AgendaCategory> = AgendaCategory.entries.toSet(),
  val selectedSmartList: SmartListFilter? = null,
  val selectedTagId: String? = null,
  val selectedGroupId: String? = null,
  val availableTags: List<Tag> = emptyList(),
  val availableGroups: List<GroupV2> = emptyList(),
  val todayScrollTargetId: String? = null,
)

internal sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val items: List<UiAgendaItem>,
  ) : ListState

  data object Empty : ListState
}
