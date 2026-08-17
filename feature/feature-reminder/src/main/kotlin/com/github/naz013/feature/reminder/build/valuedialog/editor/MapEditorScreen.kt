package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Place
import kotlinx.coroutines.launch

private const val SHEET_HEIGHT_FRACTION = 0.94f
private const val SCRIM_MAX_ALPHA = 0.32f
private val DISMISS_DRAG_THRESHOLD = 120.dp

/**
 * Bottom-sheet-styled, swipe-to-dismiss host for [MapValueEditor], used instead of
 * [com.github.naz013.feature.reminder.build.valuedialog.ValueEditorSheet]'s `AppModalBottomSheet` for
 * the Arriving/Leaving coordinates editors - that composable renders in a separate Compose
 * `Popup`/`Dialog` window, so a manually-dragged sheet is used here instead to keep the map in the
 * reminder builder's own composition (same window, no Popup), while still supporting
 * swipe-down-to-dismiss and a tap-outside scrim like a real modal bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapEditorScreen(
  builderItem: BuilderItem<Place>,
  dateTimeManager: DateTimeManager,
  onDismissRequest: () -> Unit,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  BackHandler(onBack = onDismissRequest)

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val sheetHeightPx = with(density) { (maxHeight * SHEET_HEIGHT_FRACTION).toPx() }
    val dismissThresholdPx = with(density) { DISMISS_DRAG_THRESHOLD.toPx() }
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    fun dismiss() {
      scope.launch {
        offsetY.animateTo(sheetHeightPx, animationSpec = tween(200))
        onDismissRequest()
      }
    }

    fun settle() {
      scope.launch {
        if (offsetY.value > dismissThresholdPx) {
          dismiss()
        } else {
          offsetY.animateTo(0f, animationSpec = tween(200))
        }
      }
    }

    val scrimAlpha = SCRIM_MAX_ALPHA * (1f - (offsetY.value / sheetHeightPx).coerceIn(0f, 1f))

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = scrimAlpha))
        .pointerInput(Unit) {
          detectTapGestures(onTap = { dismiss() })
        },
    )

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .fillMaxHeight(SHEET_HEIGHT_FRACTION)
        .offset { IntOffset(0, offsetY.value.toInt()) }
        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        .background(MaterialTheme.colorScheme.surface),
    ) {
      // The drag-to-dismiss gesture is scoped to the header only (not the whole sheet) so it
      // doesn't compete with panning/zooming gestures on the embedded Google Map below.
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .pointerInput(Unit) {
            detectVerticalDragGestures(
              onDragEnd = { settle() },
              onDragCancel = { settle() },
            ) { change, dragAmount ->
              change.consume()
              scope.launch {
                offsetY.snapTo((offsetY.value + dragAmount).coerceAtLeast(0f))
              }
            }
          },
      ) {
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = builderItem.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
          )
          IconButton(onClick = ::dismiss) {
            Icon(
              painter = painterResource(R.drawable.ic_builder_chevron_down),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }

      MapValueEditor(
        builderItem = builderItem,
        dateTimeManager = dateTimeManager,
        onValueChange = onValueChange,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )
    }
  }
}
