package com.github.naz013.feature.calendar.preview

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.ui.common.compose.foundation.navigation.sidePanelSupporting
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.googleCalendarEventPreviewEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  isRenderedAsSidePanel: (NavKey) -> Boolean,
) {
  entry<GoogleCalendarEventPreviewNavKey.Preview>(
    metadata = ListDetailSceneStrategy.detailPane() + sidePanelSupporting(),
  ) { key ->
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) || isRenderedAsSidePanel(key) }
    PreviewEntry(key, backStack, renderAsDetailPane)
  }
}

@Composable
private fun PreviewEntry(
  key: GoogleCalendarEventPreviewNavKey.Preview,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
) {
  val viewModel = koinViewModel<GoogleCalendarEventPreviewViewModel> { parametersOf(key.id) }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      GoogleCalendarEventPreviewViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }
    }
  }

  val state by viewModel.state.collectAsState()
  GoogleCalendarEventPreviewScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteLocalOnly = viewModel::onDeleteLocalOnly,
    onDeleteFromDeviceCalendarToo = viewModel::onDeleteFromDeviceCalendarToo,
    onDeleteOptionsDismiss = viewModel::onDeleteOptionsDismiss,
  )
}
