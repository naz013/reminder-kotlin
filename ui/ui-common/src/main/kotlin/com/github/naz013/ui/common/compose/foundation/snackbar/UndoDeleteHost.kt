package com.github.naz013.ui.common.compose.foundation.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.github.naz013.feature.common.viewmodel.UndoDeleteController
import com.github.naz013.ui.common.R

/**
 * Shows an undo Snackbar for every [UndoDeleteController.undoEvents] emission and routes the
 * outcome back: action tap -> [UndoDeleteController.undo], timeout -> [UndoDeleteController.commit].
 */
@Composable
fun UndoDeleteHost(
  controller: UndoDeleteController,
  snackbarHostState: SnackbarHostState,
) {
  val dispatcher = rememberUndoSnackbarDispatcher(snackbarHostState)
  val actionLabel = stringResource(R.string.undo)
  val currentController by rememberUpdatedState(controller)
  LaunchedEffect(controller) {
    controller.undoEvents.collect { event ->
      dispatcher.showUndoSnackbar(
        message = event.message,
        actionLabel = actionLabel,
        onUndo = { currentController.undo(event.batchKey) },
        onTimeout = { currentController.commit(event.batchKey) },
      )
    }
  }
}
