package com.github.naz013.feature.settings

import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class NoteSettingsViewModel(
  private val prefs: NoteSettingsPreferences,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {
  val state: StateFlow<NoteSettingsState> field = MutableStateFlow(buildState())

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTE_SETTINGS))
  }

  fun onColorRememberToggle() {
    prefs.isNoteColorRememberingEnabled = !prefs.isNoteColorRememberingEnabled
    refreshState()
  }

  fun onFontSizeRememberToggle() {
    prefs.isNoteFontSizeRememberingEnabled = !prefs.isNoteFontSizeRememberingEnabled
    refreshState()
  }

  fun onFontStyleRememberToggle() {
    prefs.isNoteFontStyleRememberingEnabled = !prefs.isNoteFontStyleRememberingEnabled
    refreshState()
  }

  fun onOpacityClick() {
    state.update { it.copy(opacityDialog = OpacityDialogState(previewValue = prefs.noteColorOpacity)) }
  }

  fun onOpacityPreviewChange(value: Int) {
    state.update { it.copy(opacityDialog = it.opacityDialog?.copy(previewValue = value)) }
  }

  fun onOpacityConfirm() {
    val value = state.value.opacityDialog?.previewValue ?: return
    prefs.noteColorOpacity = value
    state.update { it.copy(colorOpacity = value, opacityDialog = null) }
  }

  fun onOpacityDialogDismiss() {
    state.update { it.copy(opacityDialog = null) }
  }

  private fun refreshState() {
    state.update { buildState() }
  }

  private fun buildState(): NoteSettingsState =
    NoteSettingsState(
      isColorRememberChecked = prefs.isNoteColorRememberingEnabled,
      isFontSizeRememberChecked = prefs.isNoteFontSizeRememberingEnabled,
      isFontStyleRememberChecked = prefs.isNoteFontStyleRememberingEnabled,
      colorOpacity = prefs.noteColorOpacity,
      hapticFeedbackEnabled = prefs.hapticsEnabled,
    )
}
