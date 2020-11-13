package com.elementary.tasks.google_tasks.list

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.databinding.FragmentGoogleListBinding
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TaskListActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

class TaskListFragment : BaseNavigationFragment<FragmentGoogleListBinding>() {

  private val adapter = TasksRecyclerAdapter {
    showTasks(viewModel.googleTasks.value ?: listOf())
  }
  private val viewModel: GoogleTaskListViewModel by lazy {
    ViewModelProvider(this, GoogleTaskListViewModel.Factory(mId)).get(GoogleTaskListViewModel::class.java)
  }
  private var mId: String = ""
  private var googleTaskList: GoogleTaskList? = null

  override fun layoutRes(): Int = R.layout.fragment_google_list

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    val bundle = arguments
    if (bundle != null) {
      val args = TaskListFragmentArgs.fromBundle(bundle)
      mId = args.argId
      googleTaskList = args.argList
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    val googleTaskList = googleTaskList
    if (googleTaskList != null) {
      menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
      if (googleTaskList.def != 1) {
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
    val googleTaskList = googleTaskList
    if (googleTaskList != null) {
      startActivity(Intent(context, TaskListActivity::class.java)
        .putExtra(Constants.INTENT_ID, googleTaskList.listId))
    }
  }

  private fun deleteDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setCancelable(true)
      builder.setMessage(R.string.delete_this_list)
      builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
      builder.setPositiveButton(R.string.yes) { dialog, _ ->
        deleteList()
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun deleteList() {
    googleTaskList?.let {
      viewModel.deleteGoogleTaskList(it)
    }
  }

  private fun clearList() {
    googleTaskList?.let {
      viewModel.clearList(it)
    }
  }

  private fun addNewTask() {
    withContext {
      TaskActivity.openLogged(it, Intent(context, TaskActivity::class.java)
        .putExtra(Constants.INTENT_ID, mId)
        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
    }
  }

  private fun initViewModel() {
    viewModel.isInProgress.observe(viewLifecycleOwner, Observer {
      if (it != null) {
        updateProgress(it)
      }
    })
    viewModel.result.observe(viewLifecycleOwner, Observer {
      if (it != null) {
        showResult(it)
      }
    })
    viewModel.googleTasks.observe(viewLifecycleOwner, Observer { googleTasks ->
      if (googleTasks != null) {
        showTasks(googleTasks)
      }
    })
    viewModel.googleTaskList.observe(viewLifecycleOwner, Observer {
      if (it != null) {
        showGoogleTaskList(it)
      }
    })
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
    val newList = GoogleTaskAdsHolder.updateList(googleTasks)
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
    binding.fab.backgroundTintList = ColorStateList.valueOf(ThemeUtil.themedColor(requireContext(), googleTaskList.color))
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
