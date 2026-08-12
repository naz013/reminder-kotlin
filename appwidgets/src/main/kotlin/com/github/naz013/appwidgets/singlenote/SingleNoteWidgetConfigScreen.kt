package com.github.naz013.appwidgets.singlenote

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.WidgetConfigScaffold
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider
import com.github.naz013.ui.note.CheckableNoteCard
import com.github.naz013.ui.note.UiNoteImage
import com.github.naz013.ui.note.UiNoteListItem
import kotlin.math.roundToInt

@Composable
internal fun SingleNoteWidgetConfigScreen(
  state: SingleNoteWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onNoteSelected: (String) -> Unit,
  onTextSizeChanged: (Float) -> Unit,
  onHorizontalAlignmentChanged: (NoteDrawableParams.HorizontalAlignment) -> Unit,
  onVerticalAlignmentChanged: (NoteDrawableParams.VerticalAlignment) -> Unit,
  onTextColorSelected: (Int) -> Unit,
  onTextColorOpacityChanged: (Float) -> Unit,
  onOverlayColorSelected: (Int) -> Unit,
  onOverlayColorOpacityChanged: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current

  WidgetConfigScaffold(
    title = stringResource(R.string.note),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(172.dp)
        .background(
          brush = Brush.horizontalGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
          ),
        ),
      contentAlignment = Alignment.Center,
    ) {
      state.previewBitmap?.let { bitmap ->
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = null,
          modifier = Modifier.size(156.dp),
        )
      }
    }

    SectionTitle(stringResource(R.string.text_size))
    Column {
      Text(text = state.textSize.roundToInt().toString(), style = MaterialTheme.typography.bodyLarge)
      Slider(
        value = state.textSize,
        onValueChange = {
          val newSize = it.roundToInt().toFloat()
          if (state.hapticFeedbackEnabled && newSize != state.textSize) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
          onTextSizeChanged(newSize)
        },
        valueRange = 6f..250f,
      )
    }

    SectionTitle(stringResource(R.string.text_color))
    ColorSlider(
      colors = state.palette,
      selectedIndex = state.textColorIndex,
      onColorSelected = { index ->
        onTextColorSelected(index)
      },
      modifier = Modifier.fillMaxWidth().height(36.dp).padding(top = 4.dp),
      hapticFeedbackEnabled = state.hapticFeedbackEnabled,
    )

    SectionTitle(stringResource(R.string.text_opacity))
    Slider(
      value = state.textColorOpacity,
      onValueChange = {
        if (state.hapticFeedbackEnabled && it.toInt() != state.textColorOpacity.toInt()) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onTextColorOpacityChanged(it)
      },
      valueRange = 0f..100f,
    )

    SectionTitle(stringResource(R.string.widget_note_horizontal_alignment))
    AlignmentRow(
      options = listOf(
        NoteDrawableParams.HorizontalAlignment.LEFT to stringResource(R.string.widget_note_left),
        NoteDrawableParams.HorizontalAlignment.CENTER to stringResource(R.string.widget_note_center),
        NoteDrawableParams.HorizontalAlignment.RIGHT to stringResource(R.string.widget_note_right),
      ),
      selected = state.horizontalAlignment,
      onSelected = onHorizontalAlignmentChanged,
    )

    SectionTitle(stringResource(R.string.widget_note_vertical_alignment))
    AlignmentRow(
      options = listOf(
        NoteDrawableParams.VerticalAlignment.TOP to stringResource(R.string.widget_note_top),
        NoteDrawableParams.VerticalAlignment.CENTER to stringResource(R.string.widget_note_center),
        NoteDrawableParams.VerticalAlignment.BOTTOM to stringResource(R.string.widget_note_bottom),
      ),
      selected = state.verticalAlignment,
      onSelected = onVerticalAlignmentChanged,
    )

    SectionTitle(stringResource(R.string.foreground_color))
    ColorSlider(
      colors = state.palette,
      selectedIndex = state.overlayColorIndex,
      onColorSelected = { index ->
        onOverlayColorSelected(index)
      },
      modifier = Modifier.fillMaxWidth().height(36.dp).padding(top = 4.dp),
      hapticFeedbackEnabled = state.hapticFeedbackEnabled,
    )

    SectionTitle(stringResource(R.string.foreground_opacity))
    Slider(
      value = state.overlayColorOpacity,
      onValueChange = {
        if (state.hapticFeedbackEnabled && it.toInt() != state.overlayColorOpacity.toInt()) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        onOverlayColorOpacityChanged(it)
      },
      valueRange = 0f..100f,
    )

    SectionTitle(stringResource(R.string.notes))
    Column {
      state.notes.forEach { note ->
        CheckableNoteCard(
          note = note,
          selected = note.id == state.selectedNoteId,
          onClick = { onNoteSelected(note.id) },
          modifier = Modifier.padding(top = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(top = 16.dp),
  )
}

@Composable
private fun <T> AlignmentRow(
  options: List<Pair<T, String>>,
  selected: T,
  onSelected: (T) -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
    options.forEach { (value, label) ->
      Row(
        modifier = Modifier
          .selectable(selected = value == selected, onClick = { onSelected(value) }),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        RadioButton(selected = value == selected, onClick = null)
        Text(text = label)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SingleNoteWidgetConfigScreenPreview() {
  AppTheme {
    SingleNoteWidgetConfigScreen(
      state = SingleNoteWidgetConfigState(
        notes = listOf(
          UiNoteListItem(
            id = "1",
            title = "Shopping",
            text = "Grocery list: milk, eggs, bread",
            backgroundColor = Color(0xFFFFEB3B),
            textColor = Color.Black,
            fontStyle = 0,
            fontSize = 16f,
            titleFontStyle = 0,
            titleFontSize = 18f,
            images = emptyList<UiNoteImage>(),
          ),
        ),
        selectedNoteId = "1",
      ),
      onBackClick = {},
      onSaveClick = {},
      onNoteSelected = {},
      onTextSizeChanged = {},
      onHorizontalAlignmentChanged = {},
      onVerticalAlignmentChanged = {},
      onTextColorSelected = {},
      onTextColorOpacityChanged = {},
      onOverlayColorSelected = {},
      onOverlayColorOpacityChanged = {},
    )
  }
}
