package com.github.naz013.ui.common.compose.foundation.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity

/**
 * [UndoDeleteHost] for the coordinator's controller of [type], additionally registered in the
 * coordinator's host count so a fallback host can tell whether any list is showing undo UI for it.
 */
@Composable
fun PendingDeleteHost(
  coordinator: PendingDeleteCoordinator,
  type: PendingEntity,
  snackbarHostState: SnackbarHostState,
) {
  DisposableEffect(coordinator, type) {
    coordinator.registerHost(type)
    onDispose { coordinator.unregisterHost(type) }
  }
  UndoDeleteHost(coordinator.controller(type), snackbarHostState)
}
