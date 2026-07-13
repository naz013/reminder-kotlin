package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R

/**
 * A dialog offering a grid of color swatches to pick from, tap-to-select-and-dismiss (no
 * preview/confirm step) - the Compose replacement for the legacy `Dialogues.showColorDialog`'s
 * `ColorSlider` widget.
 */
interface ColorPickerDialogDispatcher {
  fun showDialog(
    titleRes: Int? = null,
    title: String? = null,
    colors: List<Color>,
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
  )
}

@Composable
fun rememberColorPickerDialogDispatcher(): ColorPickerDialogDispatcher {
  val openDialog = remember { mutableStateOf(false) }
  val dialogData = remember { mutableStateOf(ColorPickerDialogData()) }

  if (openDialog.value) {
    ColorPickerDialog(dialogData.value, onDismissRequest = { openDialog.value = false })
  }

  return object : ColorPickerDialogDispatcher {
    override fun showDialog(
      titleRes: Int?,
      title: String?,
      colors: List<Color>,
      selectedIndex: Int,
      onColorSelected: (Int) -> Unit,
    ) {
      dialogData.value = ColorPickerDialogData(
        titleRes = titleRes,
        title = title,
        colors = colors,
        selectedIndex = selectedIndex,
        onColorSelected = {
          onColorSelected(it)
          openDialog.value = false
        },
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
  val onColorSelected: (Int) -> Unit = {},
)

@Composable
private fun ColorPickerDialog(
  data: ColorPickerDialogData,
  onDismissRequest: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      data.title?.let { Text(text = it) } ?: data.titleRes?.let { Text(text = stringResource(it)) }
    },
    text = {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        data.colors.forEachIndexed { index, color ->
          val selected = index == data.selectedIndex
          Box(
            modifier = Modifier
              .size(40.dp)
              .background(color, CircleShape)
              .then(
                if (selected) {
                  Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                  Modifier
                },
              ).selectable(selected = selected, onClick = { data.onColorSelected(index) }, role = Role.RadioButton),
          )
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) } },
  )
}
