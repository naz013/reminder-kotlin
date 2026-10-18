package com.github.naz013.feature.habit.editor

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.habit.HabitFrequency

internal data class HabitEditorState(
  val id: String? = null,
  val name: String = "",
  val nameError: String? = null,
  val description: String = "",
  val frequency: HabitFrequency = HabitFrequency.DAILY,
  val targetLabel: String = "",
  val canDecreaseTarget: Boolean = false,
  val canIncreaseTarget: Boolean = true,
  val colorPosition: Int = 0,
  val sliderColors: List<Color> = emptyList(),
  val iconIndex: Int? = null,
  val hapticFeedbackEnabled: Boolean = true,
  val canSave: Boolean = false,
  val canDelete: Boolean = false,
  val isDeleteConfirmationVisible: Boolean = false,
)
