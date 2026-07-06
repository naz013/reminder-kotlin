package com.elementary.tasks.googletasks.list

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.fragment.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTaskListFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<TaskListViewModel> { parametersOf(getListId()) }

  private fun getListId() = arguments?.let { GoogleTaskListFragmentArgs.fromBundle(it) }?.argId

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    addMenu(
      menuRes = null,
      onMenuItemListener = { menuItem ->
        when (menuItem.itemId) {
          MENU_ITEM_EDIT -> {
            editListClick()
            true
          }

          MENU_ITEM_DELETE -> {
            deleteDialog()
            true
          }

          MENU_ITEM_CLEAR -> {
            viewModel.clearList()
            true
          }

          else -> false
        }
      },
      menuModifier = { menu -> buildMenu(menu) },
    )

    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event ->
      when (event) {
        TaskListEvent.Deleted -> moveBack()
      }
    }

    lifecycle.addObserver(viewModel)
  }

  private fun buildMenu(menu: Menu) {
    menu.clear()
    viewModel.currentTaskList?.also {
      menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
      if (it.def != 1) {
        menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
      }
      menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks)
    }
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.title, state.canDelete) {
      setTitle(state.title)
      invalidateOptionsMenu()
    }
    TaskListScreen(
      state = state,
      onTaskClick = ::openTask,
      onTaskToggle = viewModel::toggleTask,
      onAddTaskClick = ::addNewTask,
      onRefresh = viewModel::sync,
    )
  }

  private fun editListClick() {
    viewModel.currentTaskList?.also {
      navigate {
        navigate(
          R.id.editGoogleTaskListFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, it.listId)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }
    }
  }

  private fun deleteDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setCancelable(true)
    builder.setMessage(R.string.delete_this_list)
    builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
    builder.setPositiveButton(R.string.yes) { dialog, _ ->
      viewModel.deleteGoogleTaskList()
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun addNewTask() {
    viewModel.currentTaskList?.also {
      navigate {
        navigate(
          R.id.editGoogleTaskFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_LIST_ID, it.listId)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }
    }
  }

  private fun openTask(taskId: String) {
    navigate {
      navigate(
        GoogleTaskListFragmentDirections.actionTaskListFragmentToPreviewGoogleTaskFragment(taskId),
      )
    }
  }

  override fun getTitle(): String = viewModel.currentTaskList?.title ?: ""

  companion object {
    const val MENU_ITEM_EDIT = 12
    const val MENU_ITEM_DELETE = 13
    const val MENU_ITEM_CLEAR = 14
  }
}
