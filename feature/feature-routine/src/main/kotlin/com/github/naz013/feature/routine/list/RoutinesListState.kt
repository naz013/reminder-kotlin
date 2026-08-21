package com.github.naz013.feature.routine.list

import com.github.naz013.ui.routine.UiRoutineListItem
import com.github.naz013.ui.tag.TagChipState

internal enum class RoutineSortOrder {
  CREATION_DATE,
  NAME
}

internal data class RoutinesListState(
  val listState: RoutinesListDisplayState = RoutinesListDisplayState.Loading,
  val query: String = "",
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagId: String? = null,
  val sortOrder: RoutineSortOrder = RoutineSortOrder.CREATION_DATE,
)

internal sealed interface RoutinesListDisplayState {
  data object Loading : RoutinesListDisplayState

  data object Empty : RoutinesListDisplayState

  data class Ready(
    val routines: List<UiRoutineListItem>
  ) : RoutinesListDisplayState
}
