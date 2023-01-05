package com.elementary.tasks.google_tasks

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentGoogleTasksBinding
import com.elementary.tasks.google_tasks.list.ListsRecyclerAdapter
import com.elementary.tasks.google_tasks.list.TasksRecyclerAdapter
import com.elementary.tasks.google_tasks.task.GoogleTaskActivity
import com.elementary.tasks.google_tasks.tasklist.GoogleTaskListActivity
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class GoogleTasksFragment : BaseNavigationFragment<FragmentGoogleTasksBinding>() {

  private val viewModel by viewModel<GoogleTasksViewModel>()
  private val googleLogin: GoogleLogin by inject {
    parametersOf(this@GoogleTasksFragment, loginCallback)
  }
  private val adapter = TasksRecyclerAdapter()
  private val listsRecyclerAdapter = ListsRecyclerAdapter()
  private val loginCallback = object : GoogleLogin.LoginCallback {
    override fun onProgress(isLoading: Boolean, mode: GoogleLogin.Mode) {
      if (mode == GoogleLogin.Mode.TASKS) {
        updateProgress(isLoading)
      }
    }

    override fun onResult(isLogged: Boolean, mode: GoogleLogin.Mode) {
      Timber.d("onResult: $isLogged")
      if (isLogged) {
        viewModel.loadGoogleTasks()
      }
      updateGoogleStatus(isLogged)
    }

    override fun onFail(mode: GoogleLogin.Mode) {
      if (mode == GoogleLogin.Mode.TASKS) {
        showErrorDialog()
      }
    }
  }

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
    binding.googleButton.setOnClickListener {
      startActivity(
        Intent(
          context,
          GoogleTaskListActivity::class.java
        )
      )
    }

    updateProgress(false)
    initEmpty()
    initList()
    updateGoogleStatus(googleLogin.isGoogleTasksLogged)

    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))

    initViewModel()
  }

  private fun googleTasksButtonClick() {
    permissionFlow.askPermission(Permissions.GET_ACCOUNTS) { switchGoogleTasksStatus() }
  }

  private fun switchGoogleTasksStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        Toast.makeText(it, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
        return@withActivity
      }
      googleLogin.loginTasks()
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

  private fun updateGoogleStatus(isLogged: Boolean) {
    if (!isLogged) {
      binding.notLoggedView.visible()
      binding.notLoggedView.setOnClickListener { }
      binding.fab.hide()
    } else {
      binding.listsScrollView.visible()
      binding.notLoggedView.gone()
      binding.fab.show()
    }
  }

  private fun updateProgress(b: Boolean) {
    binding.progressView.visibleGone(b)
  }

  private fun addNewTask() {
    val defId = viewModel.defTaskList.value?.listId ?: return
    withContext {
      PinLoginActivity.openLogged(
        it, Intent(context, GoogleTaskActivity::class.java)
          .putExtra(Constants.INTENT_ID, defId)
          .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE)
      )
    }
  }

  private fun initViewModel() {
    viewModel.googleTaskLists.nonNullObserve(viewLifecycleOwner) { showLists(it) }
    viewModel.allGoogleTasks.nonNullObserve(viewLifecycleOwner) { showTasks(it) }
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) { updateProgress(it) }
    viewModel.defTaskList.nonNullObserve(viewLifecycleOwner) { updateMainButton(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun updateMainButton(taskList: GoogleTaskList) {
    binding.fab.backgroundTintList = ColorStateList.valueOf(
      ThemeProvider.themedColor(requireContext(), taskList.color)
    )
    activity?.invalidateOptionsMenu()
  }

  private fun showTasks(list: List<UiGoogleTaskList>) {
    adapter.submitList(list)
    reloadView(list.size)
  }

  private fun showLists(list: List<GoogleTaskList>) {
    listsRecyclerAdapter.submitList(list)
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
          ListActions.EDIT -> if (t != null) editTask(t.id)
          ListActions.SWITCH -> if (t != null) viewModel.toggleTask(t.id)
          else -> {
          }
        }
      }
    }
    binding.recyclerView.adapter = adapter
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.extend()
      } else {
        binding.fab.shrink()
      }
    }

    binding.listsView.layoutManager = LinearLayoutManager(
      context,
      LinearLayoutManager.HORIZONTAL,
      false
    )
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
      GoogleTasksFragmentDirections.actionActionGoogleToTaskListFragment(googleTaskList.listId)
    )
  }

  private fun editTask(taskId: String) {
    PinLoginActivity.openLogged(
      requireContext(), Intent(activity, GoogleTaskActivity::class.java)
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
    if (count > 0) {
      binding.emptyItem.gone()
    } else {
      binding.emptyItem.visible()
    }
  }

  override fun getTitle(): String = getString(R.string.google_tasks)
}
