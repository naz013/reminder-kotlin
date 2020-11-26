package com.elementary.tasks.google_tasks.list

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.databinding.FragmentGoogleListBinding
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TaskListActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TaskListFragment : BaseNavigationFragment<FragmentGoogleListBinding>() {

  private val adapter = TasksRecyclerAdapter(currentStateHolder) {
    showTasks(viewModel.googleTasks.value ?: listOf())
  }
  private val viewModel by viewModel<GoogleTaskListViewModel> { parametersOf(getListId()) }
  private var mId: String = ""
  private var googleTaskList: GoogleTaskList? = null

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentGoogleListBinding.inflate(inflater, container, false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    arguments?.let {
      TaskListFragmentArgs.fromBundle(it)
    }?.also {
      mId = it.argId
      googleTaskList = it.argList
    }
  }

  private fun getListId() = arguments?.let { TaskListFragmentArgs.fromBundle(it) }?.argId

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    googleTaskList?.also {
      menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
      if (it.def != 1) {
        menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
      }
      menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks)
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      MENU_ITEM_EDIT -> {
        editListClick()
        return true
      }
      MENU_ITEM_DELETE -> {
        deleteDialog()
        return true
      }
      MENU_ITEM_CLEAR -> {
        clearList()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.fab.setOnClickListener { addNewTask() }
    updateProgress(false)
    initEmpty()
    initList()
    initViewModel()
  }

  private fun editListClick() {
    googleTaskList?.also {
      startActivity(Intent(context, TaskListActivity::class.java)
        .putExtra(Constants.INTENT_ID, it.listId))
    }
  }

  private fun deleteDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setCancelable(true)
    builder.setMessage(R.string.delete_this_list)
    builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
    builder.setPositiveButton(R.string.yes) { dialog, _ ->
      deleteList()
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun deleteList() {
    googleTaskList?.also {
      viewModel.deleteGoogleTaskList(it)
    }
  }

  private fun clearList() {
    googleTaskList?.also {
      viewModel.clearList(it)
    }
  }

  private fun addNewTask() {
    TaskActivity.openLogged(requireContext(), Intent(context, TaskActivity::class.java)
      .putExtra(Constants.INTENT_ID, mId)
      .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
  }

  private fun initViewModel() {
    viewModel.isInProgress.observe(viewLifecycleOwner, { updateProgress(it) })
    viewModel.result.observe(viewLifecycleOwner, { showResult(it) })
    viewModel.googleTasks.observe(viewLifecycleOwner, { showTasks(it) })
    viewModel.googleTaskList.observe(viewLifecycleOwner, { showGoogleTaskList(it) })
  }

  private fun showResult(commands: Commands) {
    when (commands) {
      Commands.FAILED -> {
        Toast.makeText(requireContext(), getString(R.string.failed_to_update_task), Toast.LENGTH_SHORT).show()
      }
      else -> {
      }
    }
  }

  private fun updateProgress(b: Boolean) {
    if (b) {
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.GONE
    }
  }

  private fun showTasks(googleTasks: List<GoogleTask>) {
    val newList = GoogleTaskAdsViewHolder.updateList(googleTasks)
    adapter.submitList(newList)
    reloadView(newList.size)
  }

  private fun initList() {
    binding.swipeRefresh.setOnRefreshListener {
      binding.swipeRefresh.isRefreshing = false
      viewModel.sync()
    }

    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    val map = mutableMapOf<String, GoogleTaskList>()
    googleTaskList?.let {
      map[it.listId] = it
    }
    adapter.googleTaskListMap = map
    adapter.actionsListener = object : ActionsListener<GoogleTask> {
      override fun onAction(view: View, position: Int, t: GoogleTask?, actions: ListActions) {
        when (actions) {
          ListActions.EDIT -> if (t != null) editTask(t)
          ListActions.SWITCH -> if (t != null) viewModel.toggleTask(t)
          else -> {
          }
        }
      }
    }
    binding.recyclerView.adapter = adapter
    ViewUtils.listenScrollableView(binding.recyclerView, {}) {
      if (it) binding.fab.extend()
      else binding.fab.shrink()
    }
  }

  private fun editTask(googleTask: GoogleTask) {
    TaskActivity.openLogged(requireContext(), Intent(activity, TaskActivity::class.java)
      .putExtra(Constants.INTENT_ID, googleTask.taskId)
      .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
  }

  private fun initEmpty() {
    binding.emptyItem.visibility = View.VISIBLE
    binding.emptyText.setText(R.string.no_google_tasks)
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    if (count > 0) {
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  override fun onResume() {
    super.onResume()
    googleTaskList?.let { showGoogleTaskList(it) }
  }

  private fun showGoogleTaskList(googleTaskList: GoogleTaskList) {
    callback?.onTitleChange(googleTaskList.title)
    binding.fab.backgroundTintList = ColorStateList.valueOf(ThemeProvider.themedColor(requireContext(), googleTaskList.color))
    val map = mutableMapOf<String, GoogleTaskList>()
    map[googleTaskList.listId] = googleTaskList
    adapter.googleTaskListMap = map
  }

  override fun getTitle(): String {
    return googleTaskList?.title ?: ""
  }

  companion object {
    const val MENU_ITEM_EDIT = 12
    const val MENU_ITEM_DELETE = 13
    const val MENU_ITEM_CLEAR = 14
  }
}
