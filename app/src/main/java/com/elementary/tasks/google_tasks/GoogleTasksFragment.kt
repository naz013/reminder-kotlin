package com.elementary.tasks.google_tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListsViewModel
import com.elementary.tasks.databinding.FragmentGoogleTasksBinding
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TaskListActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.GoogleTaskAdsViewHolder
import com.elementary.tasks.google_tasks.list.ListsRecyclerAdapter
import com.elementary.tasks.google_tasks.list.TasksRecyclerAdapter
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class GoogleTasksFragment : BaseNavigationFragment<FragmentGoogleTasksBinding>() {

  private val gDrive by inject<GDrive>()
  private val gTasks by inject<GTasks>()
  private val viewModel by viewModel<GoogleTaskListsViewModel>()
  private val googleLogin: GoogleLogin by lazy {
    GoogleLogin(requireActivity(), prefs, gDrive, gTasks)
  }
  private val adapter = TasksRecyclerAdapter(currentStateHolder) {
    showTasks(viewModel.allGoogleTasks.value ?: listOf())
  }
  private val listsRecyclerAdapter = ListsRecyclerAdapter()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentGoogleTasksBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.fab.setOnClickListener { addNewTask() }
    binding.connectButton.setOnClickListener { googleTasksButtonClick() }
    binding.googleButton.setOnClickListener { startActivity(Intent(context, TaskListActivity::class.java)) }

    updateProgress(false)
    initEmpty()
    initList()
    checkGoogleStatus()

    initViewModel()
  }

  private fun googleTasksButtonClick() {
    if (Permissions.checkPermission(requireActivity(), 104,
        Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
        Permissions.WRITE_EXTERNAL)) {
      switchGoogleTasksStatus()
    }
  }

  private fun switchGoogleTasksStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        Toast.makeText(it, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
        return@withActivity
      }
      googleLogin.loginTasks(object : GoogleLogin.TasksCallback {
        override fun onProgress(isLoading: Boolean) {
          updateProgress(isLoading)
        }

        override fun onResult(v: GTasks?, isLogged: Boolean) {
          Timber.d("onResult: $isLogged")
          if (isLogged) {
            viewModel.loadGoogleTasks()
          }
          checkGoogleStatus()
        }

        override fun onFail() {
          showErrorDialog()
        }
      })
    }
  }

  private fun showErrorDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setMessage(getString(R.string.failed_to_login))
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
      builder.create().show()
    }
  }

  private fun checkGoogleStatus() {
    if (!gTasks.isLogged) {
      binding.notLoggedView.show()
      binding.notLoggedView.setOnClickListener { }
      binding.fab.hide()
    } else {
      binding.listsScrollView.show()
      binding.notLoggedView.hide()
      binding.fab.show()
    }
  }

  private fun updateProgress(b: Boolean) {
    if (b) {
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.GONE
    }
  }

  private fun addNewTask() {
    val defId = viewModel.defTaskList.value?.listId ?: return
    withContext {
      TaskActivity.openLogged(it, Intent(context, TaskActivity::class.java)
        .putExtra(Constants.INTENT_ID, defId)
        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
    }
  }

  private fun initViewModel() {
    viewModel.googleTaskLists.observe(viewLifecycleOwner, {
      if (it != null) {
        showLists(it)
      }
    })
    viewModel.allGoogleTasks.observe(viewLifecycleOwner, {
      if (it != null) {
        showTasks(it)
      }
    })
    viewModel.isInProgress.observe(viewLifecycleOwner, {
      if (it != null) {
        updateProgress(it)
      }
    })
  }

  private fun showTasks(list: List<GoogleTask>) {
    val newList = GoogleTaskAdsViewHolder.updateList(list)
    adapter.submitList(newList)
    reloadView(newList.size)
  }

  private fun showLists(list: List<GoogleTaskList>) {
    val map = mutableMapOf<String, GoogleTaskList>()
    list.forEach {
      map[it.listId] = it
    }
    adapter.googleTaskListMap = map
    listsRecyclerAdapter.submitList(list)
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
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) binding.fab.extend()
      else binding.fab.shrink()
    }

    binding.listsView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    listsRecyclerAdapter.actionsListener = object : ActionsListener<GoogleTaskList> {
      override fun onAction(view: View, position: Int, t: GoogleTaskList?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN -> if (t != null) openGoogleTaskList(t)
          else -> {
          }
        }
      }
    }
    binding.listsView.adapter = listsRecyclerAdapter
  }

  private fun openGoogleTaskList(googleTaskList: GoogleTaskList) {
    safeNavigation(
      GoogleTasksFragmentDirections.actionActionGoogleToTaskListFragment(
        googleTaskList.listId,
        googleTaskList
      )
    )
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

  override fun getTitle(): String = getString(R.string.google_tasks)

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googleLogin.onActivityResult(requestCode, resultCode, data)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        104 -> switchGoogleTasksStatus()
      }
    }
  }
}
