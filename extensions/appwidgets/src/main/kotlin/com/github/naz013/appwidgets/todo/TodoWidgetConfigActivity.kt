package com.github.naz013.appwidgets.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.ui.common.activity.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class TodoWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<TodoWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
      viewModel.events.collect { event ->
        when (event) {
          TodoWidgetConfigEvent.ReminderNotSelected ->
            toast(getString(R.string.widget_todo_not_selected))

          TodoWidgetConfigEvent.Saved -> finishWithResult()
        }
      }
    }

    TodoWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = viewModel::onSaveClick,
      onReminderSelected = viewModel::onReminderSelected,
      onBackgroundColorSelected = viewModel::onBackgroundColorSelected
    )
  }
}
