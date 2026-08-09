package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color

internal data class TagEditState(
  val id: String? = null,
  val name: String = "",
  val nameError: Boolean = false,
  val colorPosition: Int = 0,
  val canDelete: Boolean = false,
  val sliderColors: List<Color> = emptyList(),
  val hapticFeedbackEnabled: Boolean = true,
)
