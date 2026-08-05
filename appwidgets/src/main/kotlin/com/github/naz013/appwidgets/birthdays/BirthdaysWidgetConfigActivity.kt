package com.github.naz013.appwidgets.birthdays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class BirthdaysWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<BirthdaysWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()
    BirthdaysWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = {
        viewModel.onSaveClick()
        finishWithResult()
      },
      onHeaderColorSelected = viewModel::onBackgroundColorSelected,
    )
  }
}
