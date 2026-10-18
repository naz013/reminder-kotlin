package com.github.naz013.feature.habit.list

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

internal data class UiHabitListItem(
  val id: String,
  val name: String,
  val backgroundColor: Color,
  val contentColor: Color,
  @DrawableRes val iconRes: Int?,
  val isPinned: Boolean,
  val todayProgressLabel: String,
  val weekProgressLabel: String?,
  val streakLabel: String,
  val streakDescription: String,
  val isTargetReached: Boolean,
  val checkButtonContainerColor: Color,
  val checkButtonContentColor: Color,
)

internal sealed interface HabitListDisplayState {
  data object Loading : HabitListDisplayState

  data object Empty : HabitListDisplayState

  data class Ready(
    val habits: List<UiHabitListItem>
  ) : HabitListDisplayState
}

internal data class HabitListState(
  val listState: HabitListDisplayState = HabitListDisplayState.Loading,
)
