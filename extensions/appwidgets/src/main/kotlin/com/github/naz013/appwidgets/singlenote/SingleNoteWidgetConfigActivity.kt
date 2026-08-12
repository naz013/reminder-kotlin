package com.github.naz013.appwidgets.singlenote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.naz013.appwidgets.ComposeWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.ui.common.activity.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

internal class SingleNoteWidgetConfigActivity : ComposeWidgetConfigActivity() {

  private val viewModel by viewModel<SingleNoteWidgetConfigViewModel> { parametersOf(widgetId) }

  @Composable
  override fun ActivityContent() {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
      viewModel.events.collect { event ->
        when (event) {
          SingleNoteWidgetConfigEvent.NoteNotSelected ->
            toast(getString(R.string.widget_note_note_not_selected))

          SingleNoteWidgetConfigEvent.Saved -> finishWithResult()
        }
      }
    }

    SingleNoteWidgetConfigScreen(
      state = state,
      onBackClick = { finish() },
      onSaveClick = viewModel::onSaveClick,
      onNoteSelected = viewModel::onNoteSelected,
      onTextSizeChanged = viewModel::onTextSizeChanged,
      onHorizontalAlignmentChanged = viewModel::onHorizontalAlignmentChanged,
      onVerticalAlignmentChanged = viewModel::onVerticalAlignmentChanged,
      onTextColorSelected = viewModel::onTextColorSelected,
      onTextColorOpacityChanged = viewModel::onTextColorOpacityChanged,
      onOverlayColorSelected = viewModel::onOverlayColorSelected,
      onOverlayColorOpacityChanged = viewModel::onOverlayColorOpacityChanged,
    )
  }
}
