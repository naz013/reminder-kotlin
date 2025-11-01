package com.elementary.tasks.googletasks

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentGoogleTasksBinding
import com.elementary.tasks.googletasks.list.ListsRecyclerAdapter
import com.elementary.tasks.googletasks.list.TasksRecyclerAdapter
import com.elementary.tasks.navigation.topfragment.BaseTopToolbarFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTasksFragment : BaseTopToolbarFragment<FragmentGoogleTasksBinding>() {

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
      Logger.d(TAG, "On Google Tasks login result: $isLogged")
      if (isLogged) {
        viewModel.loadGoogleTasks()
      }
      updateGoogleStatus(isLogged)
    }

    override fun onFail(mode: GoogleLogin.Mode) {
      Logger.e(TAG, "Google Tasks login failed")
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
    binding.recyclerView.applyBottomInsets()

    if (googleLogin.isGoogleTasksLogged) {
      addMenu(
        R.menu.fragment_google_tasks_menu,
        {
          when (it.itemId) {
            R.id.action_add -> {
              Logger.i(TAG, "Add new Google Task List clicked")
              navigate { navigate(R.id.editGoogleTaskListFragment) }
            }
          }
          true
        }
      )
    }

    binding.fab.setOnClickListener { addNewTask() }
    binding.connectButton.setOnClickListener { googleTasksButtonClick() }

    updateProgress(false)
    initEmpty()
    initList()
    updateGoogleStatus(googleLogin.isGoogleTasksLogged)

    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))

    initViewModel()
  }

  private fun googleTasksButtonClick() {
    Logger.i(TAG, "Google Tasks connect button clicked")
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
      binding.emptyItem.gone()
      binding.listsView.gone()
      binding.fab.hide()
    } else {
      binding.listsView.visible()
      binding.notLoggedView.gone()
      binding.fab.show()
    }
  }

  private fun updateProgress(b: Boolean) {
    binding.swipeRefresh.isRefreshing = b
  }

  private fun addNewTask() {
    Logger.i(TAG, "Add new Google Task clicked")
    val defId = viewModel.defTaskList.value?.listId ?: return
    navigate {
      navigate(
        R.id.editGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, defId)
        }
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
    val color = ThemeProvider.themedColor(requireContext(), taskList.color)
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

  private fun showTasks(list: List<UiGoogleTaskList>) {
    adapter.submitList(list)
    reloadView(list.size)
  }

  private fun showLists(list: List<GoogleTaskList>) {
    listsRecyclerAdapter.submitList(list)
  }

  private fun initList() {
    binding.swipeRefresh.setOnRefreshListener {
      Logger.i(TAG, "Swipe to refresh triggered")
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
          ListActions.OPEN -> if (t != null) openTask(t.id)
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
    Logger.i(TAG, "Open Google Task List: ${googleTaskList.listId}")
    safeNavigation(
      GoogleTasksFragmentDirections.actionActionGoogleToTaskListFragment(googleTaskList.listId)
    )
  }

  private fun openTask(taskId: String) {
    Logger.i(TAG, "Open Google Task: $taskId")
    navigate {
      navigate(
        R.id.previewGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, taskId)
        }
      )
    }
  }

  private fun initEmpty() {
    binding.emptyItem.visible()
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }

  override fun getTitle(): String = getString(R.string.google_tasks)

  companion object {
    private const val TAG = "GoogleTasksFragment"
  }
}
