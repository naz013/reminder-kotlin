package com.github.naz013.appwidgets.habit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class HabitWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<HabitWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
      viewModel.events.collect { event ->
        when (event) {
          HabitWidgetConfigEvent.Saved -> finishWithResult()
        }
      }
    }

    HabitWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = viewModel::onSaveClick,
      onBackgroundColorSelected = viewModel::onBackgroundColorSelected
    )
  }
}
