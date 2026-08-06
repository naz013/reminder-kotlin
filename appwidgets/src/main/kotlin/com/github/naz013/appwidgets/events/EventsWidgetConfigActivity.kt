package com.github.naz013.appwidgets.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class EventsWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<EventsWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
      viewModel.saved.collect { finishWithResult() }
    }

    EventsWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = viewModel::onSaveClick,
      onBackgroundColorSelected = viewModel::onBackgroundColorSelected,
      onTextSizeChanged = viewModel::onTextSizeChanged,
      onTextSizeDialogConfirm = viewModel::onTextSizeDialogConfirm,
      onTextSizeDialogDismiss = viewModel::onTextSizeDialogDismiss,
    )
  }
}
