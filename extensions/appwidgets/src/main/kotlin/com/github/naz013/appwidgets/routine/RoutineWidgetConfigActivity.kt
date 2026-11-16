package com.github.naz013.appwidgets.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class RoutineWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<RoutineWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
      viewModel.events.collect { event ->
        when (event) {
          RoutineWidgetConfigEvent.Saved -> finishWithResult()
        }
      }
    }

    RoutineWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = viewModel::onSaveClick,
      onRoutineSelected = viewModel::onRoutineSelected
    )
  }
}
