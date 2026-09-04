package com.github.naz013.feature.agenda

import com.github.naz013.domain.Tag
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.ui.agenda.AgendaCategory
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.agenda.UiAgendaItem
import com.github.naz013.ui.agenda.UiAgendaReminder

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
  val selectedCount: Int = 0,
)

internal fun AgendaScreenState.withSelectedItem(selectedItemId: String?): AgendaScreenState {
  val ready = listState as? ListState.Ready ?: return this
  return copy(
    listState = ListState.Ready(
      ready.items.map { item ->
        when (item) {
          is UiAgendaReminder -> item.copy(isHighlighted = item.id == selectedItemId)
          is UiAgendaBirthday -> item.copy(isHighlighted = item.id == selectedItemId)
          else -> item
        }
      },
    ),
  )
}

/**
 * [UiAgendaItem] is heterogeneous (headers/calendar events aren't selectable), so it can't satisfy
 * the shared `Selectable<T>` list extensions (`com.github.naz013.ui.common.selection`) the way a
 * homogeneous list (Notes/Groups/Tags/Birthdays) can - these mirror that recipe for this list.
 */
internal val UiAgendaItem.isSelectedItem: Boolean
  get() = when (this) {
    is UiAgendaReminder -> isSelected
    is UiAgendaBirthday -> isSelected
    else -> false
  }

private fun UiAgendaItem.withItemSelected(selected: Boolean): UiAgendaItem =
  when (this) {
    is UiAgendaReminder -> withSelected(selected)
    is UiAgendaBirthday -> withSelected(selected)
    else -> this
  }

internal fun List<UiAgendaItem>.selectItem(id: String): List<UiAgendaItem> =
  map { if (it.id == id) it.withItemSelected(true) else it }

internal fun List<UiAgendaItem>.toggleItemSelection(id: String): List<UiAgendaItem> =
  map { if (it.id == id) it.withItemSelected(!it.isSelectedItem) else it }

internal fun List<UiAgendaItem>.clearItemSelection(): List<UiAgendaItem> =
  map { if (it.isSelectedItem) it.withItemSelected(false) else it }

internal fun List<UiAgendaItem>.selectedItemCount(): Int = count { it.isSelectedItem }

internal sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val items: List<UiAgendaItem>,
  ) : ListState

  data object Empty : ListState
}
