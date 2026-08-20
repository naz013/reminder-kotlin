package com.github.naz013.feature.settings

internal data class NoteSettingsState(
  val isColorRememberChecked: Boolean = false,
  val isFontSizeRememberChecked: Boolean = false,
  val isFontStyleRememberChecked: Boolean = false,
  val colorOpacity: Int = 100,
  val opacityDialog: OpacityDialogState? = null,
  val hapticFeedbackEnabled: Boolean = true,
)

internal data class OpacityDialogState(
  val previewValue: Int,
)
