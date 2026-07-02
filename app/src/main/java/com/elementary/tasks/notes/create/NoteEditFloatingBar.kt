package com.elementary.tasks.notes.create

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One entry in the floating editing bar. Deliberately generic — new tools can be added to the
 * bar (per REM-1027 Phase 2) without touching [NoteEditFloatingBar] itself: just append another
 * item to the list built by the caller.
 *
 * @param bubbleContent when non-null, tapping this item opens a [NoteEditCloudBubble] anchored
 *   to it; when null, [onClick] is a direct one-shot action (e.g. opening the image picker).
 */
data class NoteEditBarItem(
  val id: String,
  val contentDescription: String,
  val selected: Boolean = false,
  val showBadge: Boolean = false,
  val onClick: () -> Unit,
  val icon: @Composable () -> Unit,
  val bubbleContent: (@Composable () -> Unit)? = null
)

private val BAR_ITEM_SIZE = 52.dp

@Composable
fun NoteEditFloatingBar(
  items: List<NoteEditBarItem>,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(percent = 50),
    color = containerColor,
    shadowElevation = 6.dp,
    tonalElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 6.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEach { item ->
        NoteEditBarIconSlot(item = item, containerColor = containerColor, contentColor = contentColor)
      }
    }
  }
}

@Composable
private fun NoteEditBarIconSlot(
  item: NoteEditBarItem,
  containerColor: Color,
  contentColor: Color
) {
  Box(
    modifier = Modifier.size(BAR_ITEM_SIZE),
    contentAlignment = Alignment.Center
  ) {
    IconButton(onClick = item.onClick, modifier = Modifier.fillMaxWidth().height(BAR_ITEM_SIZE)) {
      item.icon()
    }
    if (item.showBadge) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 8.dp)
          .size(6.dp)
          .clip(CircleShape)
          .background(contentColor)
      )
    }
    if (item.selected) {
      item.bubbleContent?.let { bubble ->
        NoteEditCloudBubble(
          onDismissRequest = item.onClick,
          containerColor = containerColor,
          contentColor = contentColor,
          content = bubble
        )
      }
    }
  }
}
