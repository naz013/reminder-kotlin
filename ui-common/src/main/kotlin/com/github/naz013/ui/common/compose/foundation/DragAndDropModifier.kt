package com.github.naz013.ui.common.compose.foundation

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.common.uri.UriUtil

@Composable
fun Modifier.dragAndDropHighlight(
  color: Color,
  markAction: Boolean = true,
  onDrop: (ClipData) -> Unit,
  vararg mimeTypes: String,
): Modifier {
  var highlightAlpha by remember { mutableFloatStateOf(0f) }
  val activity = LocalContext.current.findActivity()

  val target =
    remember(activity, markAction, onDrop) {
      object : DragAndDropTarget {
        override fun onStarted(event: DragAndDropEvent) {
          if (markAction) highlightAlpha = 0.25f
        }

        override fun onEntered(event: DragAndDropEvent) {
          if (markAction) highlightAlpha = 0.5f
        }

        override fun onExited(event: DragAndDropEvent) {
          if (markAction) highlightAlpha = 0.25f
        }

        override fun onEnded(event: DragAndDropEvent) {
          highlightAlpha = 0f
        }

        override fun onDrop(event: DragAndDropEvent): Boolean {
          val dragEvent = event.toAndroidDragEvent()
          activity?.requestDragAndDropPermissions(dragEvent)
          val clipData = dragEvent.clipData ?: return false
          onDrop(clipData)
          return true
        }
      }
    }

  return this
    .background(color.copy(alpha = highlightAlpha))
    .dragAndDropTarget(
      shouldStartDragAndDrop = { event ->
        val eventMimeTypes = event.mimeTypes()
        mimeTypes.any { it == UriUtil.ANY_MIME || eventMimeTypes.contains(it) }
      },
      target = target,
    )
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
