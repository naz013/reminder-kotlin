package com.github.naz013.ui.common.compose.foundation.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.naz013.feature.common.viewmodel.UndoDeleteController
import com.github.naz013.feature.common.viewmodel.UndoEvent
import com.github.naz013.ui.common.R

/**
 * Shows an undo Snackbar for every [UndoDeleteController.undoEvents] emission and routes the
 * outcome back: action tap -> [UndoDeleteController.undo], timeout -> [UndoDeleteController.commit].
 * Use exactly one host per controller. Leaving composition resolves nothing; the controller replays
 * the unresolved event to the next host, so items are never left hidden without undo UI.
 */
@Composable
fun UndoDeleteHost(
  controller: UndoDeleteController,
  snackbarHostState: SnackbarHostState,
) {
  val dispatcher = rememberUndoSnackbarDispatcher(snackbarHostState)
  val actionLabel = stringResource(R.string.undo)
  val currentController by rememberUpdatedState(controller)
  var shownEvent by remember { mutableStateOf<UndoEvent?>(null) }
  LaunchedEffect(controller) {
    controller.undoEvents.collect { event ->
      shownEvent = event
      dispatcher.showUndoSnackbar(
        message = event.message,
        actionLabel = actionLabel,
        onUndo = {
          if (shownEvent == event) shownEvent = null
          currentController.undo(event)
        },
        onTimeout = {
          if (shownEvent == event) shownEvent = null
          currentController.commit(event)
        },
      )
    }
  }
  LaunchedEffect(controller) {
    controller.latestEvent.collect { latest ->
      if (latest == null && shownEvent != null) {
        shownEvent = null
        snackbarHostState.currentSnackbarData?.dismiss()
      }
    }
  }
}
