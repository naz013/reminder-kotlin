package com.elementary.tasks.googletasks.task

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.menu.enableOrDisableItem
import com.github.naz013.ui.common.menu.showOrHideItem
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditGoogleTaskFragment : BaseComposeToolbarFragment() {
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val viewModel by viewModel<EditGoogleTaskViewModel> { parametersOf(arguments) }

  override fun getTitle(): String =
    if (viewModel.hasId()) {
      getString(R.string.edit_task)
    } else {
      getString(R.string.new_task)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task edit screen for id: ${viewModel.id}")
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    addMenu(
      menuRes = R.menu.fragment_google_task_edit,
      onMenuItemListener = { menuItem ->
        when (menuItem.itemId) {
          R.id.action_delete -> {
            doIfPossible { viewModel.onDeleteMenuClick() }
            true
          }

          R.id.action_move -> {
            doIfPossible { viewModel.onMoveMenuClick() }
            true
          }

          R.id.action_add -> {
            doIfPossible { viewModel.save() }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        val currentState = viewModel.state.value
        menu.showOrHideItem(R.id.action_delete, currentState.hasId)
        menu.showOrHideItem(R.id.action_move, currentState.hasId)
        menu.enableOrDisableItem(R.id.action_delete, !currentState.isLoading)
        menu.enableOrDisableItem(R.id.action_move, !currentState.isLoading)
        menu.enableOrDisableItem(R.id.action_add, !currentState.isLoading)
      },
    )

    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event ->
      when (event) {
        is EditGoogleTaskEvent.ShowDatePicker -> {
          dateTimePickerProvider.showDatePicker(
            fragmentManager = childFragmentManager,
            date = event.date,
            title = getString(R.string.select_date),
          ) { viewModel.onDateSet(it) }
        }

        is EditGoogleTaskEvent.ShowTimePicker -> {
          dateTimePickerProvider.showTimePicker(
            fragmentManager = childFragmentManager,
            time = event.time,
            title = getString(R.string.select_time),
          ) { viewModel.onTimeSet(it) }
        }

        EditGoogleTaskEvent.Saved, EditGoogleTaskEvent.Deleted -> moveBack()
      }
    }

    lifecycle.addObserver(viewModel)
    viewModel.onCreated(arguments, savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    hideKeyboard()
    appWidgetUpdater.updateScheduleWidget()
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.hasId, state.isLoading) {
      invalidateOptionsMenu()
    }
    EditGoogleTaskScreen(
      state = state,
      onTitleChange = viewModel::onTitleChange,
      onNotesChange = viewModel::onNotesChange,
      onDateFieldClick = { doIfPossible { viewModel.onDateFieldClick() } },
      onTimeFieldClick = { doIfPossible { viewModel.onTimeFieldClick() } },
      onListFieldClick = { doIfPossible { viewModel.onListFieldClick() } },
      onDateTypeSelected = viewModel::onDateTypeSelected,
      onTimeTypeSelected = viewModel::onTimeTypeSelected,
      onListPicked = viewModel::onListPicked,
      onDeleteConfirmed = viewModel::onDeleteConfirmed,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun doIfPossible(f: () -> Unit) {
    if (viewModel.state.value.isLoading) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  override fun canGoBack(): Boolean = !viewModel.state.value.isLoading

  companion object {
    private const val TAG = "EditGoogleTaskFragment"
  }
}
