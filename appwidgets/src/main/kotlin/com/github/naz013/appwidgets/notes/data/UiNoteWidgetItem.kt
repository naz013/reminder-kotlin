package com.github.naz013.appwidgets.notes.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

internal data class UiNoteWidgetItem(
  val uuId: String,
  val text: String,
  val textSize: TextUnit,
  val backgroundColor: Color,
  val contentColor: Color,
  val image: Bitmap?
)
