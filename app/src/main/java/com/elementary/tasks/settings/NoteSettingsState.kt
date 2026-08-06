package com.elementary.tasks.settings

data class NoteSettingsState(
  val isColorRememberChecked: Boolean = false,
  val isFontSizeRememberChecked: Boolean = false,
  val isFontStyleRememberChecked: Boolean = false,
  val colorOpacity: Int = 100,
  val opacityDialog: OpacityDialogState? = null,
  val hapticFeedbackEnabled: Boolean = true,
)

data class OpacityDialogState(
  val previewValue: Int,
)
