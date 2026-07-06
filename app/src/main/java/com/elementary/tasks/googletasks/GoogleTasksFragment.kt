package com.elementary.tasks.googletasks

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GoogleTasksFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<GoogleTasksViewModel>()
  private val googleLogin: GoogleLogin by inject {
    parametersOf(this@GoogleTasksFragment, loginCallback)
  }
  private val loginCallback =
    object : GoogleLogin.LoginCallback {
      override fun onProgress(
        isLoading: Boolean,
        mode: GoogleLogin.Mode,
      ) {
        if (mode == GoogleLogin.Mode.TASKS) {
          viewModel.setLoginInProgress(isLoading)
        }
      }

      override fun onResult(
        isLogged: Boolean,
        mode: GoogleLogin.Mode,
      ) {
        Logger.d(TAG, "On Google Tasks login result: $isLogged")
        viewModel.updateLoginStatus(isLogged)
        if (!isLogged) {
          showErrorDialog()
        }
      }

      override fun onFail(mode: GoogleLogin.Mode) {
        Logger.e(TAG, "Google Tasks login failed")
        if (mode == GoogleLogin.Mode.TASKS) {
          showErrorDialog()
        }
      }
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.updateLoginStatus(googleLogin.isGoogleTasksLogged)

    addMenu(
      menuRes = R.menu.fragment_google_tasks_menu,
      onMenuItemListener = { menuItem ->
        when (menuItem.itemId) {
          R.id.action_add -> {
            openAddTaskList()
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.findItem(R.id.action_add)?.isVisible = viewModel.state.value.isLoggedIn
      },
    )

    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))
    lifecycle.addObserver(viewModel)
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.isLoggedIn) {
      updateMenuItem(R.id.action_add) { isVisible = state.isLoggedIn }
    }
    GoogleTasksScreen(
      state = state,
      onConnectClick = ::googleTasksButtonClick,
      onAddTaskClick = ::addNewTask,
      onTaskListClick = ::openGoogleTaskList,
      onTaskClick = ::openTask,
      onTaskToggle = viewModel::toggleTask,
      onRefresh = viewModel::sync,
    )
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

  private fun openAddTaskList() {
    Logger.i(TAG, "Add new Google Task List clicked")
    navigate {
      navigate(
        R.id.editGoogleTaskListFragment,
        null,
        NavigationAnimations.inDepthNavOptions(),
      )
    }
  }

  private fun addNewTask() {
    Logger.i(TAG, "Add new Google Task clicked")
    navigate {
      navigate(
        R.id.editGoogleTaskFragment,
        null,
        NavigationAnimations.inDepthNavOptions(),
      )
    }
  }

  private fun openGoogleTaskList(listId: String) {
    Logger.i(TAG, "Open Google Task List: $listId")
    safeNavigation(
      GoogleTasksFragmentDirections.actionActionGoogleToTaskListFragment(listId),
    )
  }

  private fun openTask(taskId: String) {
    Logger.i(TAG, "Open Google Task: $taskId")
    navigate {
      navigate(
        R.id.previewGoogleTaskFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, taskId)
        },
        NavigationAnimations.inDepthNavOptions(),
      )
    }
  }

  override fun getTitle(): String = getString(R.string.google_tasks)

  companion object {
    private const val TAG = "GoogleTasksFragment"
  }
}
