package com.elementary.tasks.googletasks.tasklist

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
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

class EditGoogleTaskListFragment : BaseComposeToolbarFragment() {
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val viewModel by viewModel<EditGoogleTaskListViewModel> { parametersOf(arguments) }

  override fun getTitle(): String =
    if (viewModel.hasId()) {
      getString(R.string.edit_task_list)
    } else {
      getString(R.string.new_tasks_list)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the Google Task List edit screen for id: ${viewModel.listId}")
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    addMenu(
      menuRes = R.menu.fragment_google_task_list_edit,
      onMenuItemListener = { menuItem ->
        when (menuItem.itemId) {
          R.id.action_delete -> {
            doIfPossible { viewModel.onDeleteClick() }
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
        menu.showOrHideItem(R.id.action_delete, currentState.canDelete)
        menu.enableOrDisableItem(R.id.action_delete, !currentState.isLoading)
        menu.enableOrDisableItem(R.id.action_add, !currentState.isLoading)
      },
    )

    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event ->
      when (event) {
        EditGoogleTaskListEvent.Saved, EditGoogleTaskListEvent.Deleted -> moveBack()
      }
    }

    lifecycle.addObserver(viewModel)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    hideKeyboard()
    appWidgetUpdater.updateScheduleWidget()
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.canDelete, state.isLoading) {
      invalidateOptionsMenu()
    }
    EditGoogleTaskListScreen(
      state = state,
      onNameChange = viewModel::onNameChange,
      onColorSelected = viewModel::onColorSelected,
      onDefaultToggle = viewModel::onDefaultToggle,
      onDeleteConfirmed = viewModel::deleteGoogleTaskList,
      onDeleteDismiss = viewModel::onDeleteDismiss,
    )
  }

  private fun doIfPossible(f: () -> Unit) {
    if (viewModel.state.value.isLoading) {
      toast(R.string.please_wait)
    } else {
      f.invoke()
    }
  }

  companion object {
    private const val TAG = "EditGoogleTaskFragment"
  }
}
