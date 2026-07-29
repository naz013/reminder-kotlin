package com.github.naz013.appwidgets.googletasks

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import com.github.naz013.appwidgets.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class TasksWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<TasksWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    if (!state.isAuthorized) {
      LaunchedEffect(Unit) {
        Toast.makeText(this@TasksWidgetConfigActivity, getString(R.string.you_not_logged_to_google_tasks), Toast.LENGTH_SHORT).show()
        finish()
      }
      return
    }

    TasksWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = {
        viewModel.onSaveClick()
        finishWithResult()
      },
      onHeaderColorSelected = viewModel::onHeaderColorSelected,
      onItemColorSelected = viewModel::onItemColorSelected,
    )
  }
}
