package com.github.naz013.feature.routine.edit

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.tag.TagChipState

internal data class RoutineStepUiState(
  val id: String,
  val title: String,
  val durationSeconds: Int,
  val scheduledTime: String?,
)

internal data class RoutineEditState(
  val id: String? = null,
  val title: String = "",
  val description: String = "",
  val colorPosition: Int = 0,
  val sliderColors: List<Color> = emptyList(),
  val isPinned: Boolean = false,
  val steps: List<RoutineStepUiState> = emptyList(),
  val repeatsDaily: Boolean = false,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
  val canDelete: Boolean = false,
  val canSave: Boolean = false,
  val hapticFeedbackEnabled: Boolean = true,
)
