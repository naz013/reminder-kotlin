package com.github.naz013.ui.common.compose.foundation.snackbar

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.feature.common.viewmodel.fallbackActive

/**
 * Root-level undo UI for deletes started from screens whose origin has no [PendingDeleteHost], for
 * example a preview opened from Home or from the Calendar side panel. A type is only handled here
 * while no list host is registered for it, so each controller still has exactly one host.
 */
@Composable
fun PendingDeleteFallbackHost(
  coordinator: PendingDeleteCoordinator,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  PendingEntity.entries.forEach { type ->
    key(type) {
      val active by remember(coordinator, type) { coordinator.fallbackActive(type) }.collectAsState(false)
      if (active) UndoDeleteHost(coordinator.controller(type), snackbarHostState)
    }
  }
  SnackbarHost(hostState = snackbarHostState, modifier = modifier.navigationBarsPadding())
}
