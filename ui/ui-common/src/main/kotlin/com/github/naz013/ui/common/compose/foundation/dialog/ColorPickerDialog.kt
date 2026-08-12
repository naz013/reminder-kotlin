package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider

/**
 * A dialog offering the same [ColorSlider] strip used by the map's marker-style picker. Sliding
 * across it only previews a color (with a haptic tick each time the highlighted swatch changes);
 * the choice isn't handed back to the caller until Save is pressed, and Cancel discards it.
 */
interface ColorPickerDialogDispatcher {
  fun showDialog(
    titleRes: Int? = null,
    title: String? = null,
    colors: List<Color>,
    selectedIndex: Int,
    hapticFeedbackEnabled: Boolean = true,
    onColorSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit = {},
  )
}

@Composable
fun rememberColorPickerDialogDispatcher(): ColorPickerDialogDispatcher {
  val openDialog = remember { mutableStateOf(false) }
  val dialogData = remember { mutableStateOf(ColorPickerDialogData()) }

  if (openDialog.value) {
    ColorPickerDialog(
      data = dialogData.value,
      onDismissRequest = {
        dialogData.value.onDismissRequest()
        openDialog.value = false
      },
      onSave = { index ->
        dialogData.value.onColorSelected(index)
        openDialog.value = false
      },
    )
  }

  return object : ColorPickerDialogDispatcher {
    override fun showDialog(
      titleRes: Int?,
      title: String?,
      colors: List<Color>,
      selectedIndex: Int,
      hapticFeedbackEnabled: Boolean,
      onColorSelected: (Int) -> Unit,
      onDismissRequest: () -> Unit,
    ) {
      dialogData.value = ColorPickerDialogData(
        titleRes = titleRes,
        title = title,
        colors = colors,
        selectedIndex = selectedIndex,
        onColorSelected = onColorSelected,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        onDismissRequest = onDismissRequest,
      )
      openDialog.value = true
    }
  }
}

private data class ColorPickerDialogData(
  val titleRes: Int? = null,
  val title: String? = null,
  val colors: List<Color> = emptyList(),
  val selectedIndex: Int = 0,
  val hapticFeedbackEnabled: Boolean = true,
  val onColorSelected: (Int) -> Unit = {},
  val onDismissRequest: () -> Unit = {},
)

@Composable
private fun ColorPickerDialog(
  data: ColorPickerDialogData,
  onDismissRequest: () -> Unit,
  onSave: (Int) -> Unit,
) {
  var previewIndex by remember { mutableIntStateOf(data.selectedIndex) }

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      data.title?.let { Text(text = it) } ?: data.titleRes?.let { Text(text = stringResource(it)) }
    },
    text = {
      ColorSlider(
        colors = data.colors,
        selectedIndex = previewIndex,
        onColorSelected = { index ->
          previewIndex = index
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp),
        hapticFeedbackEnabled = data.hapticFeedbackEnabled,
      )
    },
    confirmButton = { TextButton(onClick = { onSave(previewIndex) }) { Text(stringResource(R.string.save)) } },
    dismissButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) } },
  )
}
