package com.elementary.tasks.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@Composable
fun NoteSettingsScreen(
  state: NoteSettingsState,
  onColorRememberToggle: () -> Unit,
  onFontSizeRememberToggle: () -> Unit,
  onFontStyleRememberToggle: () -> Unit,
  onOpacityClick: () -> Unit,
  onOpacityPreviewChange: (Int) -> Unit,
  onOpacityConfirm: () -> Unit,
  onOpacityDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    SettingsSwitchItem(
      title = stringResource(R.string.last_color),
      checked = state.isColorRememberChecked,
      onCheckedChange = { onColorRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_used_note_color),
      subtitleOff = stringResource(R.string.do_not_remember_last_color),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.color_saturation),
      subtitle = "${state.colorOpacity}%",
      icon = painterResource(R.drawable.ic_fluent_circle_half_fill),
      dividerBottom = true,
      onClick = onOpacityClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.text_size),
      checked = state.isFontSizeRememberChecked,
      onCheckedChange = { onFontSizeRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_set_text_size),
      subtitleOff = stringResource(R.string.use_default_text_size),
      icon = painterResource(R.drawable.ic_fluent_text),
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.font_style),
      checked = state.isFontStyleRememberChecked,
      onCheckedChange = { onFontStyleRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_set_font_style),
      subtitleOff = stringResource(R.string.use_default_font_style),
      dividerBottom = true,
    )
  }

  val opacityDialog = state.opacityDialog
  if (opacityDialog != null) {
    AlertDialog(
      onDismissRequest = onOpacityDialogDismiss,
      title = { Text(stringResource(R.string.color_saturation)) },
      text = {
        Column {
          Text(text = "${opacityDialog.previewValue}%", style = MaterialTheme.typography.bodyLarge)
          Slider(
            value = opacityDialog.previewValue.toFloat(),
            onValueChange = {
              if (it.toInt() != state.colorOpacity && state.hapticFeedbackEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
              onOpacityPreviewChange(it.toInt()) },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        TextButton(onClick = onOpacityConfirm) { Text(stringResource(R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = onOpacityDialogDismiss) { Text(stringResource(R.string.cancel)) }
      },
    )
  }
}
