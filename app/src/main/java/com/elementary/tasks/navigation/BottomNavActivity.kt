package com.elementary.tasks.navigation

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.github.naz013.feature.birthday.BirthdaysNavKey
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.elementary.tasks.core.os.ContextSwitcher
import com.github.naz013.feature.googletask.GoogleTasksNavKey
import com.github.naz013.group.GroupsNavKey
import com.elementary.tasks.navigation.nav3.AppNavGraph
import com.github.naz013.feature.note.NotesNavKey
import com.elementary.tasks.places.PlacesNavKey
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.export.work.BackupSettingsTask
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.navigation.SettingsScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewNoteScreen
import com.github.naz013.navigation.ViewReminderScreen
import com.github.naz013.ui.common.compose.BaseAuthActivity
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BottomNavActivity : BaseAuthActivity() {
  private val workScheduler by inject<WorkScheduler>()
  private val contextSwitcher by inject<ContextSwitcher>()
  private val initViewModel by viewModel<BottomNavInitViewModel>()

  private val adsProvider = AdsProvider()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    contextSwitcher.switchContext(this)

    enableEdgeToEdge()
    Logger.i(TAG, "Starting with action: ${intent.action}")
    Logger.i(TAG, "Starting with data: ${intent.data}")
    Logger.i(TAG, "Starting with extras: ${intent.extras?.keySet()?.toList()}")

    composeView {
      val initState by initViewModel.state.collectAsStateWithLifecycle()
      val readyState = initState as? BottomNavInitState.Ready

      if (readyState?.requiresLogin == true) {
        LaunchedEffect(Unit) {
          startActivity(LoginApi.authIntent(this@BottomNavActivity, isBack = false))
          finish()
        }
      }

      // Keeps the animated splash on screen through Loading and the brief requiresLogin ->
      // finish() window, then crossfades into AppNavGraph once home is actually ready to show.
      Crossfade(targetState = readyState != null && !readyState.requiresLogin, label = "bottomNavSplash") { showHome ->
        if (showHome) {
          LaunchedEffect(Unit) {
            enableShortcuts()
          }
          AppNavGraph(initialKeys = resolveInitialNavKeys())
        } else {
          BottomNavSplashScreen()
        }
      }
    }

    adsProvider.showConsentMessage(this)
  }

  private fun enableShortcuts() {
    val shortcutManager = getSystemService(ShortcutManager::class.java)
    if (shortcutManager != null) {
      val shortcut =
        run {
          val bundle =
            ShortcutDestination.createBundle(
              shortcut = ShortcutDestination.Shortcut.Reminder,
            )
          ShortcutInfo
            .Builder(this, "id.reminder")
            .setShortLabel(getString(R.string.add_reminder_menu))
            .setLongLabel(getString(R.string.add_reminder_menu))
            .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
            .setIntents(
              arrayOf(
                Intent(Intent.ACTION_MAIN)
                  .setClass(this, BottomNavActivity::class.java)
                  .putExtras(bundle),
              ),
            ).build()
        }

      val shortcut2 =
        run {
          val bundle =
            ShortcutDestination.createBundle(
              shortcut = ShortcutDestination.Shortcut.Note,
            )
          ShortcutInfo
            .Builder(this, "id.note")
            .setShortLabel(getString(R.string.add_note))
            .setLongLabel(getString(R.string.add_note))
            .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
            .setIntents(
              arrayOf(
                Intent(Intent.ACTION_MAIN)
                  .setClass(this, BottomNavActivity::class.java)
                  .putExtras(bundle),
              ),
            ).build()
        }

      if (initViewModel.isGoogleTasksEnabled) {
        val bundle =
          ShortcutDestination.createBundle(
            shortcut = ShortcutDestination.Shortcut.GoogleTask,
          )
        val shortcut3 =
          ShortcutInfo
            .Builder(this, "id.google.tasks")
            .setShortLabel(getString(R.string.add_google_task))
            .setLongLabel(getString(R.string.add_google_task))
            .setIcon(Icon.createWithResource(this, R.drawable.add_google_shortcut))
            .setIntents(
              arrayOf(
                Intent(Intent.ACTION_MAIN)
                  .setClass(this, BottomNavActivity::class.java)
                  .putExtras(bundle),
              ),
            ).build()
        shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2, shortcut3)
      } else {
        shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2)
      }
    }
  }

  /**
   * Resolves an incoming deep link / app shortcut to typed Nav3 keys, seeded straight into
   * [AppNavGraph]'s initial backstack. Every [com.github.naz013.navigation.DeepLinkDestination] and [com.elementary.tasks.splash.ShortcutDestination] now
   * resolves to a typed `NavKey` here - there is no longer a legacy Fragment graph to fall back to.
   */
  private fun resolveInitialNavKeys(): List<NavKey> {
    if (intent.action == Intent.ACTION_VIEW) {
      intent.setExtrasClassLoader(DeepLinkDestination::class.java.classLoader)
      val deepLinkDestination =
        intent.readParcelable(DeepLinkDestination.KEY, DeepLinkDestination::class.java)
      when (deepLinkDestination) {
        is ViewNoteScreen -> {
          val id = deepLinkDestination.id
          return if (id != null) {
            listOf(NotesNavKey.List, NotesNavKey.Preview(id))
          } else {
            listOf(NotesNavKey.List)
          }
        }

        is EditNoteScreen ->
          return listOf(
            NotesNavKey.List,
            NotesNavKey.Edit(
              id = deepLinkDestination.id,
              fromIntentData = deepLinkDestination.fromIntentData,
              sharedText = deepLinkDestination.sharedText,
              sharedImageUris = deepLinkDestination.sharedImageUris,
            ),
          )

        is EditGroupScreen ->
          return listOf(
            GroupsNavKey.List,
            GroupsNavKey.Edit(id = deepLinkDestination.id ?: "", fromIntentData = deepLinkDestination.fromIntentData),
          )

        is EditPlaceScreen ->
          return listOf(
            PlacesNavKey.List,
            PlacesNavKey.Edit(id = deepLinkDestination.id ?: "", fromIntentData = deepLinkDestination.fromIntentData),
          )

        is EditBirthdayScreen ->
          return listOf(
            BirthdaysNavKey.Edit(
              id = deepLinkDestination.id ?: "",
              fromIntentData = deepLinkDestination.fromIntentData,
            ),
          )

        is ViewBirthdayScreen -> {
          val id = deepLinkDestination.id
          return if (id != null) {
            listOf(BirthdaysNavKey.Preview(id))
          } else {
            listOf(BirthdaysNavKey.Edit())
          }
        }

        is EditGoogleTaskScreen -> {
          return listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit())
        }

        is ViewGoogleTaskScreen -> {
          val id = deepLinkDestination.id
          return if (id != null) {
            listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskPreview(id))
          } else {
            listOf(GoogleTasksNavKey.List)
          }
        }

        is EditReminderScreen ->
          return listOf(
            BuildReminderNavKey.Main(
              fromIntentItem = deepLinkDestination.fromIntentItem,
              deepLinkText = deepLinkDestination.deepLinkText,
            ),
          )

        is ViewReminderScreen -> {
          val id = deepLinkDestination.id
          return if (id != null) listOf(ReminderPreviewNavKey.Preview(id)) else emptyList()
        }

        is DayViewScreen ->
          return listOf(CalendarNavKey.Month, CalendarNavKey.Day(deepLinkDestination.dateMillis))

        is SettingsScreen -> return listOf(SettingsNavKey.Hub)

        else -> return emptyList()
      }
    } else if (ShortcutDestination.hasShortcut(intent.extras)) {
      when (ShortcutDestination.getShortcut(intent.extras)) {
        ShortcutDestination.Shortcut.Note -> return listOf(NotesNavKey.List, NotesNavKey.Edit())
        ShortcutDestination.Shortcut.GoogleTask -> return listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskEdit())
        ShortcutDestination.Shortcut.Reminder -> return listOf(BuildReminderNavKey.Main())
        else -> Unit
      }
    }
    return emptyList()
  }

  override fun onDestroy() {
    super.onDestroy()
    workScheduler.enqueue(
      WorkRequest(
        taskKey = BackupSettingsTask.TASK_KEY,
        tag = BackupSettingsTask.TASK_KEY,
        networkRequirement = NetworkRequirement.UNMETERED,
        requiresBatteryNotLow = true,
      ),
    )
  }

  /**
   * [AppNavGraph]'s [androidx.navigation3.ui.NavDisplay] handles back press internally (its own
   * `onBack` pops the outer Nav3 backstack, with predictive-back support built in) whenever there's
   * more than one entry on the stack - this only actually fires once that backstack has bottomed
   * out, so there's nothing left to do but exit.
   */
  override fun handleBackPress(): Boolean {
    finishAffinity()
    return true
  }

  companion object {
    private const val TAG = "BottomNavActivity"
  }
}
