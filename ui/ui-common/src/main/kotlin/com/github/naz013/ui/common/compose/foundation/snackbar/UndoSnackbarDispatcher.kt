package com.github.naz013.ui.common.compose.foundation.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

interface UndoSnackbarDispatcher {
  fun showUndoSnackbar(
    message: String,
    actionLabel: String,
    onUndo: () -> Unit,
    onTimeout: () -> Unit,
  )
}

/**
 * Shows a Snackbar carrying an action button (labelled [UndoSnackbarDispatcher.showUndoSnackbar]'s
 * `actionLabel`) and reports back whether it was tapped ([UndoSnackbarDispatcher.showUndoSnackbar]'s
 * `onUndo`) or the Snackbar's own duration elapsed first (`onTimeout`) - the Snackbar's
 * [SnackbarDuration.Short] window is the single source of truth for how long "undo" stays
 * available, there is no separate timer on the caller's side.
 *
 * A newly shown Snackbar dismisses whatever is currently displayed rather than queuing behind it,
 * so back-to-back deletes only ever show (and only ever grant an undo window for) the most recent
 * one - the same behavior most list apps use for delete-undo Snackbars.
 */
@Composable
fun rememberUndoSnackbarDispatcher(snackbarHostState: SnackbarHostState): UndoSnackbarDispatcher {
  val scope = rememberCoroutineScope()
  return remember(snackbarHostState) {
    object : UndoSnackbarDispatcher {
      override fun showUndoSnackbar(
        message: String,
        actionLabel: String,
        onUndo: () -> Unit,
        onTimeout: () -> Unit,
      ) {
        scope.launch {
          snackbarHostState.currentSnackbarData?.dismiss()
          val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
          )
          if (result == SnackbarResult.ActionPerformed) onUndo() else onTimeout()
        }
      }
    }
  }
}
