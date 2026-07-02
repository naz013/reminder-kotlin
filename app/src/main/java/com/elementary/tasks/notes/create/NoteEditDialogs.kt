package com.elementary.tasks.notes.create

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.colorslider.ColorSlider

@Composable
fun FontStyleDialog(
  selected: Int,
  onDismiss: () -> Unit,
  onSelected: (Int) -> Unit
) {
  val names = remember { AssetsUtil.getFontNames() }
  var choice by remember { mutableIntStateOf(selected) }
  val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.font_style)) },
    text = {
      LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
        itemsIndexed(names) { index, name ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { choice = index }
              .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = choice == index, onClick = { choice = index })
            AndroidView(
              modifier = Modifier.padding(start = 8.dp),
              factory = { context -> TextView(context) },
              update = { textView ->
                textView.text = name
                textView.typeface = AssetsUtil.getTypeface(textView.context, index)
                textView.setTextColor(textColor)
              }
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        onSelected(choice)
        onDismiss()
      }) {
        Text(stringResource(R.string.ok))
      }
    }
  )
}

@Composable
fun PaletteDialog(
  currentPalette: Int,
  colorsForPalette: (Int) -> IntArray,
  onDismiss: () -> Unit,
  onConfirm: (Int) -> Unit
) {
  var choice by remember { mutableIntStateOf(currentPalette) }
  AlertDialog(
    onDismissRequest = onDismiss,
    text = {
      Column {
        (0..2).forEach { palette ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { choice = palette }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = choice == palette, onClick = { choice = palette })
            AndroidView(
              modifier = Modifier
                .padding(start = 8.dp)
                .height(36.dp)
                .fillMaxWidth(),
              factory = { context ->
                ColorSlider(context).apply { isEnabled = false }
              },
              update = { it.setColors(colorsForPalette(palette)) }
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        onConfirm(choice)
        onDismiss()
      }) {
        Text(stringResource(R.string.save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.cancel))
      }
    }
  )
}

@Composable
fun DeleteNoteDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = { Text(stringResource(R.string.delete_this_note)) },
    confirmButton = {
      TextButton(onClick = {
        onConfirm()
        onDismiss()
      }) {
        Text(stringResource(R.string.yes))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.no))
      }
    }
  )
}

@Composable
fun SameNoteDialog(
  onDismiss: () -> Unit,
  onKeep: () -> Unit,
  onReplace: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = { Text(stringResource(R.string.same_note_message)) },
    confirmButton = {
      TextButton(onClick = {
        onKeep()
        onDismiss()
      }) {
        Text(stringResource(R.string.keep))
      }
    },
    dismissButton = {
      Row {
        TextButton(onClick = {
          onReplace()
          onDismiss()
        }) {
          Text(stringResource(R.string.replace))
        }
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.cancel))
        }
      }
    }
  )
}
