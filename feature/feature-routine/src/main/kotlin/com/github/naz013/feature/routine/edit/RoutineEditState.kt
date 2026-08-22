package com.github.naz013.feature.routine.edit

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.tag.TagChipState

internal data class RoutineStepUiState(
  val id: String,
  val title: String,
  val durationSeconds: Int,
  val scheduledTime: String?,
)

/** Mirrors the subset of [com.github.naz013.domain.reminder.v2.RecurrenceRule] variants the routine
 * editor actually offers. [Weekly.weekdays] and [Monthly.dayOfMonth] follow the app's
 * 0=Sunday..6=Saturday / 1..28 conventions respectively. */
internal sealed class RoutineRecurrenceOption {
  data object None : RoutineRecurrenceOption()
  data object Daily : RoutineRecurrenceOption()
  data class Weekly(val weekdays: Set<Int> = emptySet()) : RoutineRecurrenceOption()
  data class Monthly(val dayOfMonth: Int = 1) : RoutineRecurrenceOption()
}

internal data class RoutineEditState(
  val id: String? = null,
  val title: String = "",
  val description: String = "",
  val colorPosition: Int = 0,
  val sliderColors: List<Color> = emptyList(),
  val isPinned: Boolean = false,
  val iconIndex: Int? = null,
  val steps: List<RoutineStepUiState> = emptyList(),
  val recurrenceOption: RoutineRecurrenceOption = RoutineRecurrenceOption.None,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
  val canDelete: Boolean = false,
  val canSave: Boolean = false,
  val hapticFeedbackEnabled: Boolean = true,
)
