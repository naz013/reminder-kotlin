package com.elementary.tasks.notes.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.colorslider.ColorSlider

@Composable
fun NoteEditBottomBar(
  state: NoteEditState,
  speechState: SpeechUiState,
  supportsSpeech: Boolean,
  contentColor: Color,
  sliderColors: IntArray,
  actions: NoteEditActions,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .height(56.dp),
    ) {
      if (supportsSpeech) {
        MicButton(
          speechState = speechState,
          contentColor = contentColor,
          onClick = actions.onMicClick
        )
      }
      TabIconButton(
        iconRes = R.drawable.ic_fluent_color_background,
        contentDescription = stringResource(R.string.acc_select_color),
        contentColor = contentColor,
        selected = state.expandedTab == EditTab.COLOR,
        onClick = actions.onColorTabClick
      )
      IconButton(onClick = actions.onImagePickClick, modifier = Modifier.size(56.dp)) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_image),
          contentDescription = stringResource(R.string.acc_add_image_to_reminder),
          tint = contentColor
        )
      }
      TabIconButton(
        iconRes = R.drawable.ic_fluent_alert,
        contentDescription = stringResource(R.string.acc_add_reminder),
        contentColor = contentColor,
        selected = state.expandedTab == EditTab.REMINDER,
        onClick = actions.onReminderTabClick,
        showDot = state.isReminderAttached
      )
      TabIconButton(
        iconRes = R.drawable.ic_fluent_text,
        contentDescription = stringResource(R.string.acc_change_text_font_style),
        contentColor = contentColor,
        selected = state.expandedTab == EditTab.FONT,
        onClick = actions.onFontTabClick
      )
    }

    AnimatedVisibility(visible = state.expandedTab != null) {
      when (state.expandedTab) {
        EditTab.REMINDER -> ReminderPanel(state, contentColor, actions)
        EditTab.FONT -> FontPanel(state, contentColor, actions)
        EditTab.COLOR -> ColorPanel(state, contentColor, sliderColors, actions)
        null -> Unit
      }
    }
  }
}

@Composable
private fun MicButton(
  speechState: SpeechUiState,
  contentColor: Color,
  onClick: () -> Unit
) {
  IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
    when (speechState) {
      SpeechUiState.IDLE -> {
        Icon(
          painter = painterResource(R.drawable.ic_builder_mic_on),
          contentDescription = stringResource(R.string.acc_type_by_voice),
          tint = contentColor
        )
      }

      SpeechUiState.SPEAKING -> {
        val composition by rememberLottieComposition(
          LottieCompositionSpec.RawRes(R.raw.mic_speaking_waves)
        )
        LottieAnimation(
          composition = composition,
          iterations = LottieConstants.IterateForever,
          modifier = Modifier.size(40.dp)
        )
      }

      SpeechUiState.STARTED, SpeechUiState.STOPPED -> {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_recording_stop),
          contentDescription = stringResource(R.string.acc_type_by_voice),
          tint = contentColor
        )
      }
    }
  }
}

@Composable
private fun RowScope.TabIconButton(
  iconRes: Int,
  contentDescription: String,
  contentColor: Color,
  selected: Boolean,
  onClick: () -> Unit,
  showDot: Boolean = false
) {
  Box(modifier = Modifier.size(56.dp)) {
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = contentColor
      )
    }
    if (showDot) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 8.dp)
          .size(6.dp)
          .clip(CircleShape)
          .background(contentColor)
      )
    }
    if (selected) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(horizontal = 8.dp, vertical = 8.dp)
          .fillMaxWidth()
          .height(2.dp)
          .background(contentColor)
      )
    }
  }
}

@Composable
private fun ReminderPanel(
  state: NoteEditState,
  contentColor: Color,
  actions: NoteEditActions
) {
  Column(modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)) {
    Row(
      modifier = Modifier.padding(start = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Switch(
        checked = state.isReminderAttached,
        onCheckedChange = actions.onReminderAttachedChanged,
        colors = SwitchDefaults.colors(
          checkedThumbColor = contentColor,
          checkedTrackColor = contentColor.copy(alpha = 0.5f)
        )
      )
      Text(
        text = stringResource(R.string.add_reminder),
        color = contentColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
    Row(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
      val dateTimeAlpha = if (state.isReminderAttached) 1f else 0.5f
      Text(
        text = state.reminderDateFormatted,
        color = contentColor.copy(alpha = dateTimeAlpha),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
          .clickableIfEnabled(state.isReminderAttached, actions.onDateClick)
      )
      Text(
        text = state.reminderTimeFormatted,
        color = contentColor.copy(alpha = dateTimeAlpha),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
          .padding(start = 24.dp)
          .clickableIfEnabled(state.isReminderAttached, actions.onTimeClick)
      )
    }
  }
}

@Composable
private fun FontPanel(
  state: NoteEditState,
  contentColor: Color,
  actions: NoteEditActions
) {
  Column(modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)) {
    Text(
      text = stringResource(R.string.font_style),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(start = 16.dp, top = 8.dp)
    )
    Text(
      text = fontStyleName(state.fontStyle),
      color = contentColor,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier
        .padding(start = 16.dp, top = 8.dp)
        .clickableIfEnabled(true, actions.onFontStyleDialogClick)
    )
    Text(
      text = stringResource(R.string.text_size),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(start = 16.dp, top = 16.dp)
    )
    Slider(
      value = state.fontSize.toFloat(),
      onValueChange = { actions.onFontSizeChanged(it.toInt()) },
      valueRange = 6f..150f,
      colors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = 0.24f)
      ),
      modifier = Modifier.padding(horizontal = 16.dp)
    )
  }
}

@Composable
private fun ColorPanel(
  state: NoteEditState,
  contentColor: Color,
  sliderColors: IntArray,
  actions: NoteEditActions
) {
  Column(modifier = Modifier.padding(bottom = 48.dp)) {
    Row(
      modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = actions.onPaletteDialogClick, modifier = Modifier.size(24.dp)) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_settings),
          contentDescription = null,
          tint = contentColor
        )
      }
      ColorSliderView(
        colors = sliderColors,
        selectedIndex = state.colorIndex,
        onSelected = actions.onColorSelected,
        modifier = Modifier
          .padding(start = 8.dp)
          .weight(1f)
          .height(36.dp)
      )
    }
    Text(
      text = stringResource(R.string.opacity),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(start = 16.dp, top = 8.dp)
    )
    Slider(
      value = state.opacity.toFloat(),
      onValueChange = { actions.onOpacityChanged(it.toInt()) },
      valueRange = 0f..100f,
      colors = SliderDefaults.colors(
        thumbColor = contentColor,
        activeTrackColor = contentColor,
        inactiveTrackColor = contentColor.copy(alpha = 0.24f)
      ),
      modifier = Modifier.padding(horizontal = 16.dp)
    )
  }
}

@Composable
private fun ColorSliderView(
  colors: IntArray,
  selectedIndex: Int,
  onSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  AndroidView(
    modifier = modifier,
    factory = { context ->
      ColorSlider(context).apply {
        setSelectorColorResource(if (isDark) R.color.pureWhite else R.color.pureBlack)
        setListener { position, _ -> onSelected(position) }
      }
    },
    update = { view ->
      view.setColors(colors)
      if (view.selectedItem != selectedIndex) {
        view.setSelection(selectedIndex)
      }
    }
  )
}

private fun Modifier.clickableIfEnabled(enabled: Boolean, onClick: () -> Unit): Modifier =
  this.clickable(enabled = enabled, onClick = onClick)

@Composable
private fun fontStyleName(fontStyle: Int): String {
  val names = remember { AssetsUtil.getFontNames() }
  return names.getOrNull(fontStyle) ?: stringResource(R.string.font_style)
}
