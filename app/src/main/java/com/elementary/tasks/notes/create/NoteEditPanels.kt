package com.elementary.tasks.notes.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.tags.compose.TagChipPicker
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider

/**
 * The icon content for the mic/speech bar item — switches between a static mic icon, an
 * animated waveform while speech is being recognized, and a stop icon. Rendered inside the
 * floating bar's generic icon slot, so it deliberately doesn't include its own [IconButton].
 */
@Composable
fun MicIcon(
  speechState: SpeechUiState,
  contentColor: Color,
) {
  when (speechState) {
    SpeechUiState.IDLE -> {
      Icon(
        painter = painterResource(R.drawable.ic_builder_mic_on),
        contentDescription = stringResource(R.string.acc_type_by_voice),
        tint = contentColor,
      )
    }

    SpeechUiState.SPEAKING -> {
      val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.mic_speaking_waves),
      )
      LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(40.dp),
      )
    }

    SpeechUiState.STARTED, SpeechUiState.STOPPED -> {
      Icon(
        painter = painterResource(R.drawable.ic_fluent_recording_stop),
        contentDescription = stringResource(R.string.acc_type_by_voice),
        tint = contentColor,
      )
    }
  }
}

@Composable
fun ReminderPanel(
  state: NoteEditState,
  contentColor: Color,
  actions: NoteEditActions,
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Switch(
        checked = state.isReminderAttached,
        onCheckedChange = actions.onReminderAttachedChanged,
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = contentColor,
            checkedTrackColor = contentColor.copy(alpha = 0.5f),
          ),
      )
      Text(
        text = stringResource(R.string.add_reminder),
        color = contentColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 8.dp),
      )
    }
    Row(modifier = Modifier.padding(top = 16.dp)) {
      val dateTimeAlpha = if (state.isReminderAttached) 1f else 0.5f
      Text(
        text = state.reminderDateFormatted,
        color = contentColor.copy(alpha = dateTimeAlpha),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.clickableIfEnabled(state.isReminderAttached, actions.onDateClick),
      )
      Text(
        text = state.reminderTimeFormatted,
        color = contentColor.copy(alpha = dateTimeAlpha),
        style = MaterialTheme.typography.titleLarge,
        modifier =
          Modifier
            .padding(start = 24.dp)
            .clickableIfEnabled(state.isReminderAttached, actions.onTimeClick),
      )
    }
  }
}

/**
 * Shows and edits the font size/style of whichever text field ([NoteEditState.focusedField])
 * last had focus — title or body. Both fields share this one panel/bar item rather than each
 * getting their own, matching the body's existing font controls exactly.
 */
@Composable
fun FontPanel(
  state: NoteEditState,
  contentColor: Color,
  containerColor: Color,
  actions: NoteEditActions,
) {
  val isTitle = state.focusedField == NoteTextField.TITLE
  val fontStyle = if (isTitle) state.titleFontStyle else state.fontStyle
  val fontSize = if (isTitle) state.titleFontSize else state.fontSize

  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(
      text = stringResource(R.string.font_style),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
    )
    var showFontPicker by remember { mutableStateOf(false) }
    Box {
      Text(
        text = fontStyleName(fontStyle),
        color = contentColor,
        style = MaterialTheme.typography.titleMedium,
        modifier =
          Modifier
            .padding(top = 8.dp)
            .clickableIfEnabled(true) { showFontPicker = true },
      )
      if (showFontPicker) {
        CloudBubble(
          onDismissRequest = { showFontPicker = false },
          containerColor = containerColor,
          contentColor = contentColor,
          modifier = Modifier.width(272.dp),
        ) {
          FontPickerList(
            selected = fontStyle,
            contentColor = contentColor,
            onSelected = {
              actions.onFontStyleSelected(it)
              showFontPicker = false
            },
          )
        }
      }
    }
    Text(
      text = stringResource(R.string.text_size),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(top = 16.dp),
    )
    Slider(
      value = fontSize.toFloat(),
      onValueChange = { actions.onFontSizeChanged(it.toInt()) },
      valueRange = 6f..150f,
      colors =
        SliderDefaults.colors(
          thumbColor = contentColor,
          activeTrackColor = contentColor,
          inactiveTrackColor = contentColor.copy(alpha = 0.24f),
        ),
    )
  }
}

@Composable
fun ImageSourcePanel(
  hasCamera: Boolean,
  contentColor: Color,
  onGalleryClick: () -> Unit,
  onCameraClick: () -> Unit,
  onUrlClick: () -> Unit,
) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    ImageSourceRow(stringResource(R.string.gallery), contentColor, onGalleryClick)
    if (hasCamera) {
      ImageSourceRow(stringResource(R.string.take_a_shot), contentColor, onCameraClick)
    }
    ImageSourceRow(stringResource(R.string.from_url), contentColor, onUrlClick)
  }
}

@Composable
private fun ImageSourceRow(
  text: String,
  contentColor: Color,
  onClick: () -> Unit,
) {
  Text(
    text = text,
    color = contentColor,
    style = MaterialTheme.typography.titleMedium,
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 12.dp),
  )
}

@Composable
fun TagsPanel(
  state: NoteEditState,
  actions: NoteEditActions,
) {
  Column(modifier = Modifier.padding(vertical = 8.dp).widthIn(max = 272.dp)) {
    TagChipPicker(
      allTags = state.allTags,
      selectedTagIds = state.selectedTagIds,
      onToggle = actions.onTagToggle,
      onManageTagsClick = actions.onManageTagsClick,
    )
  }
}

@Composable
fun ColorPanel(
  colors: List<Color>,
  selectedIndex: Int,
  opacity: Int,
  contentColor: Color,
  onColorSelected: (Int) -> Unit,
  onOpacityChanged: (Int) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    ColorSlider(
      colors = colors,
      selectedIndex = selectedIndex,
      onColorSelected = { onColorSelected(it) },
      selectorColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
      modifier = Modifier
        .fillMaxWidth()
        .height(36.dp),
      hapticFeedbackEnabled = hapticFeedbackEnabled,
    )
    Text(
      text = stringResource(R.string.opacity),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(top = 8.dp),
    )
    Slider(
      value = opacity.toFloat(),
      onValueChange = { onOpacityChanged(it.toInt()) },
      valueRange = 0f..100f,
      colors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = 0.24f),
      ),
    )
  }
}

private fun Modifier.clickableIfEnabled(
  enabled: Boolean,
  onClick: () -> Unit,
): Modifier = this.clickable(enabled = enabled, onClick = onClick)

@Composable
private fun fontStyleName(fontStyle: Int): String {
  val names = remember { AssetsUtil.getFontNames() }
  return names.getOrNull(fontStyle) ?: stringResource(R.string.font_style)
}

/**
 * The font-style picker, hosted inside a third-level [CloudBubble] anchored to the
 * "current font" row in [FontPanel] — replaces the old full-screen `AlertDialog` version.
 *
 * Renders previews with plain Compose `Text` + [FontFamily] (loaded once, up front, and cached)
 * instead of a per-row `AndroidView`-wrapped `TextView` — an `AndroidView` inside a scrolling
 * `LazyColumn` is expensive to measure/recycle and made this list noticeably janky to scroll.
 */
@Composable
private fun FontPickerList(
  selected: Int,
  contentColor: Color,
  onSelected: (Int) -> Unit,
) {
  val context = LocalContext.current
  val fonts =
    remember {
      AssetsUtil.getFontNames().mapIndexed { index, name ->
        val fontFamily =
          AssetsUtil.getTypeface(context, index)?.let { FontFamily(it) }
            ?: FontFamily.Default
        name to fontFamily
      }
    }
  LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
    itemsIndexed(fonts, key = { index, _ -> index }) { index, (name, fontFamily) ->
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable { onSelected(index) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        RadioButton(
          selected = selected == index,
          onClick = { onSelected(index) },
          colors =
            RadioButtonDefaults.colors(
              selectedColor = contentColor,
              unselectedColor = contentColor.copy(alpha = 0.6f),
            ),
        )
        Text(
          text = name,
          color = contentColor,
          fontFamily = fontFamily,
          modifier = Modifier.padding(start = 8.dp),
        )
      }
    }
  }
}
