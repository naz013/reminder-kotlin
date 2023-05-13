package com.elementary.tasks.google_tasks.list

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentGoogleListBinding
import com.elementary.tasks.google_tasks.TasksConstants
import com.elementary.tasks.google_tasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.google_tasks.task.GoogleTaskActivity
import com.elementary.tasks.google_tasks.tasklist.GoogleTaskListActivity
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TaskListFragment : BaseNavigationFragment<FragmentGoogleListBinding>() {

  private val adapter = TasksRecyclerAdapter()
  private val viewModel by viewModel<TaskListViewModel> { parametersOf(getListId()) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentGoogleListBinding.inflate(inflater, container, false)

  private fun getListId() = arguments?.let { TaskListFragmentArgs.fromBundle(it) }?.argId

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(null, { menuItem ->
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
    }) { menu ->
      viewModel.currentTaskList?.also {
        menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
        if (it.def != 1) {
          menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
        }
        menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks)
      }
    }

    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.fab.setOnClickListener { addNewTask() }
    updateProgress(false)
    initEmpty()
    initList()
    initViewModel()
  }

  private fun editListClick() {
    viewModel.currentTaskList?.also {
      startActivity(
        Intent(context, GoogleTaskListActivity::class.java)
          .putExtra(Constants.INTENT_ID, it.listId)
      )
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
      PinLoginActivity.openLogged(
        requireContext(), Intent(context, GoogleTaskActivity::class.java)
          .putExtra(Constants.INTENT_ID, it.listId)
          .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
      )
    }
  }

  private fun initViewModel() {
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) { updateProgress(it) }
    viewModel.result.nonNullObserve(viewLifecycleOwner) { showResult(it) }
    viewModel.tasks.nonNullObserve(viewLifecycleOwner) { showTasks(it) }
    viewModel.taskList.nonNullObserve(viewLifecycleOwner) { showGoogleTaskList(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun showResult(commands: Commands) {
    when (commands) {
      Commands.FAILED -> {
        Toast.makeText(
          requireContext(),
          getString(R.string.failed_to_update_task),
          Toast.LENGTH_SHORT
        ).show()
      }

      else -> {
      }
    }
  }

  private fun updateProgress(b: Boolean) {
    binding.progressView.visibleGone(b)
  }

  private fun showTasks(googleTasks: List<UiGoogleTaskList>) {
    adapter.submitList(googleTasks)
    reloadView(googleTasks.size)
  }

  private fun initList() {
    binding.swipeRefresh.setOnRefreshListener {
      binding.swipeRefresh.isRefreshing = false
      viewModel.sync()
    }

    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    adapter.actionsListener = object : ActionsListener<UiGoogleTaskList> {
      override fun onAction(view: View, position: Int, t: UiGoogleTaskList?, actions: ListActions) {
        when (actions) {
          ListActions.EDIT -> if (t != null) openTask(t.id)
          ListActions.SWITCH -> if (t != null) viewModel.toggleTask(t.id)
          else -> {
          }
        }
      }
    }
    binding.recyclerView.adapter = adapter
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) binding.fab.extend()
      else binding.fab.shrink()
    }
  }

  private fun openTask(taskId: String) {
    PinLoginActivity.openLogged(
      requireContext(), Intent(activity, GoogleTaskPreviewActivity::class.java)
        .putExtra(Constants.INTENT_ID, taskId)
        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
    )
  }

  private fun initEmpty() {
    binding.emptyItem.visible()
    binding.emptyText.setText(R.string.no_google_tasks)
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }

  override fun onResume() {
    super.onResume()
    viewModel.currentTaskList?.let { showGoogleTaskList(it) }
  }

  private fun showGoogleTaskList(googleTaskList: GoogleTaskList) {
    callback?.onTitleChange(googleTaskList.title)
    binding.fab.backgroundTintList = ColorStateList.valueOf(
      ThemeProvider.themedColor(requireContext(), googleTaskList.color)
    )
    activity?.invalidateOptionsMenu()
  }

  override fun getTitle(): String {
    return viewModel.currentTaskList?.title ?: ""
  }

  companion object {
    const val MENU_ITEM_EDIT = 12
    const val MENU_ITEM_DELETE = 13
    const val MENU_ITEM_CLEAR = 14
  }
}
