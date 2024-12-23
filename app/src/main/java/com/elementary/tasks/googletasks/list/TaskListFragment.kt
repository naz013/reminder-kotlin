package com.elementary.tasks.googletasks.list

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.view.ViewUtils
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentGoogleListBinding
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.googletasks.tasklist.GoogleTaskListActivity
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.fragment.startActivity
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import com.github.naz013.usecase.googletasks.TasksIntentKeys
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TaskListFragment : BaseToolbarFragment<FragmentGoogleListBinding>() {

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
    binding.fab.applyBottomInsetsMargin()
    binding.recyclerView.applyBottomInsets()

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
      menu.clear()
      viewModel.currentTaskList?.also {
        menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
        if (it.def != 1) {
          menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
        }
        menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks)
      }
    }

    binding.fab.setOnClickListener { addNewTask() }
    updateProgress(false)
    initEmpty()
    initList()
    initViewModel()
  }

  private fun editListClick() {
    viewModel.currentTaskList?.also {
      startActivity(GoogleTaskListActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, it.listId)
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
      LoginApi.openLogged(requireContext(), GoogleTaskActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, it.listId)
        putExtra(TasksIntentKeys.INTENT_ACTION, TasksIntentKeys.CREATE)
      }
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
    binding.swipeRefresh.isRefreshing = b
  }

  private fun showTasks(googleTasks: List<UiGoogleTaskList>) {
    adapter.submitList(googleTasks)
    reloadView(googleTasks.size)
  }

  private fun initList() {
    binding.swipeRefresh.setOnRefreshListener {
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
    binding.recyclerView.addItemDecoration(SpaceBetweenItemDecoration(dp2px(8)))

    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.extend()
      } else {
        binding.fab.shrink()
      }
    }
  }

  private fun openTask(taskId: String) {
    LoginApi.openLogged(requireContext(), GoogleTaskPreviewActivity::class.java) {
      putExtra(IntentKeys.INTENT_ID, taskId)
      putExtra(TasksIntentKeys.INTENT_ACTION, TasksIntentKeys.EDIT)
    }
  }

  private fun initEmpty() {
    binding.emptyItem.visible()
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
    setTitle(googleTaskList.title)
    val color = ThemeProvider.themedColor(requireContext(), googleTaskList.color)
    binding.fab.backgroundTintList = ColorStateList.valueOf(color)
    val textColor = if (color.isColorDark()) {
      ContextCompat.getColor(requireContext(), R.color.pureWhite)
    } else {
      ContextCompat.getColor(requireContext(), R.color.pureBlack)
    }
    binding.fab.setTextColor(textColor)
    binding.fab.iconTint = ColorStateList.valueOf(textColor)
    invalidateOptionsMenu()
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
