package com.github.naz013.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSearchItemKeys
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.dialog.SeekValueDialog

@Composable
internal fun NoteSettingsScreen(
  modifier: Modifier = Modifier,
  state: NoteSettingsState,
  onColorRememberToggle: () -> Unit,
  onFontSizeRememberToggle: () -> Unit,
  onFontStyleRememberToggle: () -> Unit,
  onTextColorRememberToggle: () -> Unit,
  onOpacityClick: () -> Unit,
  onOpacityPreviewChange: (Int) -> Unit,
  onOpacityConfirm: () -> Unit,
  onOpacityDialogDismiss: () -> Unit,
) {
  Column(
    modifier = modifier
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
      itemKey = SettingsSearchItemKeys.NOTES_LAST_COLOR,
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.color_saturation),
      subtitle = "${state.colorOpacity}%",
      icon = AppIcons.Fluent.CircleHalfFill,
      itemKey = SettingsSearchItemKeys.NOTES_OPACITY,
      dividerBottom = true,
      onClick = onOpacityClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.text_size),
      checked = state.isFontSizeRememberChecked,
      onCheckedChange = { onFontSizeRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_set_text_size),
      subtitleOff = stringResource(R.string.use_default_text_size),
      icon = AppIcons.Fluent.Text,
      itemKey = SettingsSearchItemKeys.NOTES_TEXT_SIZE,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.font_style),
      checked = state.isFontStyleRememberChecked,
      onCheckedChange = { onFontStyleRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_set_font_style),
      subtitleOff = stringResource(R.string.use_default_font_style),
      itemKey = SettingsSearchItemKeys.NOTES_FONT_STYLE,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.text_color),
      checked = state.isTextColorRememberChecked,
      onCheckedChange = { onTextColorRememberToggle() },
      subtitleOn = stringResource(R.string.remember_last_used_text_color),
      subtitleOff = stringResource(R.string.do_not_remember_last_text_color),
      icon = AppIcons.Fluent.TextColor,
      itemKey = SettingsSearchItemKeys.NOTES_TEXT_COLOR,
    )
  }

  val opacityDialog = state.opacityDialog
  if (opacityDialog != null) {
    SeekValueDialog(
      title = stringResource(R.string.color_saturation),
      value = opacityDialog.previewValue,
      valueText = "${opacityDialog.previewValue}%",
      valueRange = 0f..100f,
      onValueChange = onOpacityPreviewChange,
      onConfirm = onOpacityConfirm,
      onDismiss = onOpacityDialogDismiss,
      hapticFeedbackEnabled = state.hapticFeedbackEnabled,
    )
  }
}
