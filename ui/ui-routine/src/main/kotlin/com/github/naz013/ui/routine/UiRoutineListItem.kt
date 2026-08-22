package com.github.naz013.ui.routine

import androidx.compose.ui.graphics.Color

/**
 * Display model for [RoutineCard]. Kept deliberately free of domain/logic-module types (`Routine`,
 * `RoutineStep`) - `ui-routine` only depends on `core:domain`/`ui-common`, so anything that needs
 * `RoutineDurationCalculator` (duration formatting) or a color-contrast decision must be resolved
 * by the caller (`feature-routine`'s ViewModel) and handed over as plain display values here.
 *
 * @param contentColor Text/icon color for [backgroundColor] - the caller picks it (e.g. via
 * [androidx.compose.ui.graphics.Color.luminance]) since `ui-routine` doesn't own a palette engine.
 * @param iconRes The routine's selected icon, already resolved from `RoutineIconSet.ALL` by index
 * (`Routine.icon`) - null when no icon is selected.
 * @param stepCountLabel Pre-formatted, already-pluralized label, e.g. "5 steps".
 * @param durationLabel Pre-formatted total duration, e.g. "25m" (see `RoutineDurationCalculator`).
 * @param scheduleRangeLabel Pre-formatted first-to-last scheduled-step time range, e.g.
 * "07:30 - 08:45", or null when no step in the routine has a
 * [com.github.naz013.domain.routine.RoutineStep.scheduledTime].
 */
data class UiRoutineListItem(
  val id: String,
  val title: String,
  val description: String?,
  val backgroundColor: Color,
  val contentColor: Color,
  val isPinned: Boolean,
  val iconRes: Int?,
  val stepCountLabel: String,
  val durationLabel: String,
  val scheduleRangeLabel: String?,
)
