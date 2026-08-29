package com.github.naz013.feature.note.create

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.naz013.domain.note.NoteSpanAttribute
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider
import com.github.naz013.ui.note.NoteFontProvider
import com.github.naz013.ui.tag.TagChipPicker
import org.koin.compose.koinInject

/**
 * The icon content for the mic/speech bar item — switches between a static mic icon, an
 * animated waveform while speech is being recognized, and a stop icon. Rendered inside the
 * floating bar's generic icon slot, so it deliberately doesn't include its own [IconButton].
 */
@Composable
internal fun MicIcon(
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
internal fun ReminderPanel(
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
 * Font family, font size, bold/italic/underline/strikethrough and block format (H1/H2/H3/
 * paragraph/bullet), grouped into one bubble with a titled section per option so they read as
 * one "text formatting" tool rather than four separate ones. Font family/size fall back to the
 * note's default when nothing is selected, and apply to just the selection otherwise (see
 * [NoteEditViewModel.onFontStyleChanged]/[NoteEditViewModel.onFontSizeChanged]); style and block
 * format always act on the current selection/line (see [NoteEditViewModel.onToggleBold] and
 * [NoteEditViewModel.onApplyLineFormat]).
 */
@Composable
internal fun TextFormatPanel(
  state: NoteEditState,
  activeFormat: ActiveTextFormat,
  contentColor: Color,
  containerColor: Color,
  actions: NoteEditActions,
) {
  val fontStyle = state.fontStyle
  val fontSize = state.fontSize

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

    Text(
      text = stringResource(R.string.text_style_section),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(top = 16.dp),
    )
    Row(modifier = Modifier.padding(top = 4.dp)) {
      GlyphToggleButton("B", FontWeight.Bold, active = activeFormat.bold, contentColor = contentColor, onClick = actions.onToggleBold)
      GlyphToggleButton(
        "I",
        FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        active = activeFormat.italic,
        contentColor = contentColor,
        onClick = actions.onToggleItalic,
      )
      GlyphToggleButton(
        "U",
        FontWeight.Normal,
        decoration = TextDecoration.Underline,
        active = activeFormat.underline,
        contentColor = contentColor,
        onClick = actions.onToggleUnderline,
      )
      GlyphToggleButton(
        "S",
        FontWeight.Normal,
        decoration = TextDecoration.LineThrough,
        active = activeFormat.strikethrough,
        contentColor = contentColor,
        onClick = actions.onToggleStrikethrough,
      )
    }

    Text(
      text = stringResource(R.string.acc_select_text_format),
      color = contentColor,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(top = 16.dp),
    )
    Row(modifier = Modifier.padding(top = 4.dp)) {
      LineFormatButton(
        text = "H1",
        fontSize = 20.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading1,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading1) },
      )
      LineFormatButton(
        text = "H2",
        fontSize = 17.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading2,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading2) },
      )
      LineFormatButton(
        text = "H3",
        fontSize = 15.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading3,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading3) },
      )
      LineFormatButton(
        text = stringResource(R.string.format_paragraph),
        fontSize = 14.sp,
        active = activeFormat.lineFormat == null,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(null) },
      )
      LineFormatButton(
        text = "•",
        fontSize = 18.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.BulletItem,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.BulletItem) },
      )
    }
  }
}

@Composable
internal fun ImageSourcePanel(
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
internal fun TagsPanel(
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
internal fun ColorPanel(
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
  val noteFontProvider = koinInject<NoteFontProvider>()
  val names = remember { noteFontProvider.getFontNames() }
  return names.getOrNull(fontStyle) ?: stringResource(R.string.font_style)
}

/**
 * The font-style picker, hosted inside a third-level [CloudBubble] anchored to the
 * "current font" row in [TextFormatPanel] — replaces the old full-screen `AlertDialog` version.
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
  val noteFontProvider = koinInject<NoteFontProvider>()
  val fonts =
    remember {
      noteFontProvider.getFontNames().mapIndexed { index, name ->
        val fontFamily =
          noteFontProvider.getTypeface(context, index)?.let { FontFamily(it) }
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

/**
 * Bold/italic/underline/strikethrough toggles + H1/H2/H3/paragraph/bullet line format, all
 * applying to the current selection (or, for line format, the current line). Buttons render as
 * styled glyphs rather than icons - self-descriptive without needing new drawable assets.
 */
@Composable
internal fun FormatPanel(
  activeFormat: ActiveTextFormat,
  contentColor: Color,
  actions: NoteEditActions,
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Row {
      GlyphToggleButton("B", FontWeight.Bold, active = activeFormat.bold, contentColor = contentColor, onClick = actions.onToggleBold)
      GlyphToggleButton(
        "I",
        FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        active = activeFormat.italic,
        contentColor = contentColor,
        onClick = actions.onToggleItalic,
      )
      GlyphToggleButton(
        "U",
        FontWeight.Normal,
        decoration = TextDecoration.Underline,
        active = activeFormat.underline,
        contentColor = contentColor,
        onClick = actions.onToggleUnderline,
      )
      GlyphToggleButton(
        "S",
        FontWeight.Normal,
        decoration = TextDecoration.LineThrough,
        active = activeFormat.strikethrough,
        contentColor = contentColor,
        onClick = actions.onToggleStrikethrough,
      )
    }
    Row(modifier = Modifier.padding(top = 8.dp)) {
      LineFormatButton(
        text = "H1",
        fontSize = 20.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading1,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading1) },
      )
      LineFormatButton(
        text = "H2",
        fontSize = 17.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading2,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading2) },
      )
      LineFormatButton(
        text = "H3",
        fontSize = 15.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.Heading3,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.Heading3) },
      )
      LineFormatButton(
        text = stringResource(R.string.format_paragraph),
        fontSize = 14.sp,
        active = activeFormat.lineFormat == null,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(null) },
      )
      LineFormatButton(
        text = "•",
        fontSize = 18.sp,
        active = activeFormat.lineFormat == NoteSpanAttribute.BulletItem,
        contentColor = contentColor,
        onClick = { actions.onApplyLineFormat(NoteSpanAttribute.BulletItem) },
      )
    }
  }
}

@Composable
private fun GlyphToggleButton(
  glyph: String,
  fontWeight: FontWeight,
  contentColor: Color,
  active: Boolean,
  onClick: () -> Unit,
  fontStyle: FontStyle = FontStyle.Normal,
  decoration: TextDecoration = TextDecoration.None,
) {
  Text(
    text = glyph,
    color = contentColor,
    fontWeight = fontWeight,
    fontStyle = fontStyle,
    textDecoration = decoration,
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (active) contentColor.copy(alpha = 0.2f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 8.dp),
  )
}

@Composable
private fun LineFormatButton(
  text: String,
  fontSize: androidx.compose.ui.unit.TextUnit,
  contentColor: Color,
  active: Boolean,
  onClick: () -> Unit,
) {
  Text(
    text = text,
    color = contentColor,
    fontSize = fontSize,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.titleMedium,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (active) contentColor.copy(alpha = 0.2f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 8.dp),
  )
}

/** Text color picker for the current selection - a text-content counterpart to [ColorPanel],
 * which sets the note's background instead. Toggles between a solid color (single swatch) and a
 * gradient (start/end swatch + sweep angle), reusing the same [Brush.linearGradient] approach
 * already used by `GradientHighlightTextField` for live speech-transcript highlighting. */
@Composable
internal fun TextColorPanel(
  colors: List<Color>,
  contentColor: Color,
  onColorSelected: (Int) -> Unit,
  onGradientSelected: (List<Int>, Float) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  var isGradientMode by remember { mutableStateOf(false) }

  Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
    Row {
      TextColorModeChip(
        text = stringResource(R.string.text_color_mode_solid),
        selected = !isGradientMode,
        contentColor = contentColor,
        onClick = { isGradientMode = false },
      )
      TextColorModeChip(
        text = stringResource(R.string.text_color_mode_gradient),
        selected = isGradientMode,
        contentColor = contentColor,
        onClick = { isGradientMode = true },
        modifier = Modifier.padding(start = 8.dp),
      )
    }
    if (isGradientMode) {
      GradientColorControls(colors, contentColor, onGradientSelected, hapticFeedbackEnabled)
    } else {
      SolidColorControls(colors, onColorSelected, hapticFeedbackEnabled)
    }
  }
}

@Composable
private fun TextColorModeChip(
  text: String,
  selected: Boolean,
  contentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    color = contentColor,
    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
    style = MaterialTheme.typography.labelLarge,
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(if (selected) contentColor.copy(alpha = 0.2f) else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
  )
}

@Composable
private fun SolidColorControls(
  colors: List<Color>,
  onColorSelected: (Int) -> Unit,
  hapticFeedbackEnabled: Boolean,
) {
  ColorSlider(
    colors = colors,
    selectedIndex = -1,
    onColorSelected = { onColorSelected(colors[it].toArgb()) },
    selectorColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .padding(top = 12.dp),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

@Composable
private fun GradientColorControls(
  colors: List<Color>,
  contentColor: Color,
  onGradientSelected: (List<Int>, Float) -> Unit,
  hapticFeedbackEnabled: Boolean,
) {
  var startIndex by remember { mutableStateOf(0) }
  var endIndex by remember { mutableStateOf((colors.size - 1).coerceAtLeast(0)) }
  var angle by remember { mutableStateOf(0f) }

  fun apply() {
    if (colors.isEmpty()) return
    onGradientSelected(listOf(colors[startIndex].toArgb(), colors[endIndex].toArgb()), angle)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(28.dp)
      .padding(top = 12.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(Brush.linearGradient(listOf(colors.getOrElse(startIndex) { contentColor }, colors.getOrElse(endIndex) { contentColor }))),
  )
  Text(
    text = stringResource(R.string.gradient_start_color),
    color = contentColor,
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(top = 12.dp),
  )
  ColorSlider(
    colors = colors,
    selectedIndex = startIndex,
    onColorSelected = { startIndex = it; apply() },
    selectorColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    modifier = Modifier.fillMaxWidth().height(36.dp),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
  Text(
    text = stringResource(R.string.gradient_end_color),
    color = contentColor,
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(top = 8.dp),
  )
  ColorSlider(
    colors = colors,
    selectedIndex = endIndex,
    onColorSelected = { endIndex = it; apply() },
    selectorColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
    modifier = Modifier.fillMaxWidth().height(36.dp),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
  Text(
    text = stringResource(R.string.gradient_angle),
    color = contentColor,
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(top = 8.dp),
  )
  Slider(
    value = angle,
    onValueChange = { angle = it; apply() },
    valueRange = 0f..360f,
    colors = SliderDefaults.colors(
      thumbColor = contentColor,
      activeTrackColor = contentColor,
      inactiveTrackColor = contentColor.copy(alpha = 0.24f),
    ),
  )
}
