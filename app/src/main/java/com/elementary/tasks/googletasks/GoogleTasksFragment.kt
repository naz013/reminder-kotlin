package com.elementary.tasks.googletasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject

/**
 * Hosts the Google Tasks feature as a self-contained Navigation 3 "island": this Fragment owns
 * the internal backstack and the Android-framework glue (Google sign-in, date/time pickers,
 * dialogs) that only a Fragment can provide, and is otherwise responsible only for
 * screen-to-screen navigation. The actual screens and their wiring live in [GoogleTasksNavGraph].
 */
class GoogleTasksFragment :
  Fragment(),
  RootFragment,
  BackPressHandler {
  internal val dialogues by inject<Dialogues>()
  internal val dateTimePickerProvider by inject<DateTimePickerProvider>()
  internal val appWidgetUpdater by inject<AppWidgetUpdater>()
  internal val analyticsEventSender by inject<AnalyticsEventSender>()
  internal val adsProvider = AdsProvider()
  internal lateinit var permissionFlow: PermissionFlow

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  /** The [GoogleTasksViewModel] currently on screen, if any — [googleLogin]'s callback is
   *  Fragment-scoped and long-lived (registered once here), so its results have to be routed to
   *  the list entry only while it's actually active. */
  internal var activeGoogleTasksViewModel: GoogleTasksViewModel? = null

  /** Constructed eagerly in [onCreate], never lazily — see [GoogleLogin]'s kdoc for why. */
  internal lateinit var googleLogin: GoogleLogin
  private val loginCallback =
    object : GoogleLogin.LoginCallback {
      override fun onProgress(
        isLoading: Boolean,
        mode: GoogleLogin.Mode,
      ) {
        if (mode == GoogleLogin.Mode.TASKS) {
          activeGoogleTasksViewModel?.setLoginInProgress(isLoading)
        }
      }

      override fun onResult(
        isLogged: Boolean,
        mode: GoogleLogin.Mode,
      ) {
        Logger.d(TAG, "On Google Tasks login result: $isLogged")
        activeGoogleTasksViewModel?.updateLoginStatus(isLogged)
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
    googleLogin = GoogleLogin(this, loginCallback)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      GoogleTasksNavGraph(backStack)
    }

  override fun onDestroyView() {
    super.onDestroyView()
    currentBackStack = null
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  /** Seeds the island's backstack from a Google Task deep link / app shortcut / cross-feature
   *  link, read once from [getArguments] — see [com.elementary.tasks.home.ScreenDestinationIdResolver],
   *  [com.elementary.tasks.home.BottomNavActivity] and
   *  [com.elementary.tasks.reminder.preview.PreviewReminderFragment], which all route Google Task
   *  deep links to this fragment (`R.id.actionGoogle`) instead of a dedicated destination. */
  private fun initialBackStackKeys(): List<GoogleTasksNavKey> {
    val args = arguments ?: return listOf(GoogleTasksNavKey.List)
    val id = args.getString(IntentKeys.INTENT_ID)
    val listId = args.getString(IntentKeys.INTENT_LIST_ID)
    return when {
      args.getBoolean(ARG_OPEN_EDIT, false) -> {
        listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit(id ?: "", listId ?: ""))
      }

      id != null -> listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskPreview(id))
      else -> listOf(GoogleTasksNavKey.List)
    }
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1

  internal fun showErrorDialog() {
    context?.also {
      val builder = dialogues.getMaterialDialog(it)
      builder.setMessage(getString(R.string.failed_to_login))
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
      builder.create().show()
    }
  }

  companion object {
    private const val TAG = "GoogleTasksFragment"
    const val ARG_OPEN_EDIT = "google_tasks_open_edit"
  }
}
