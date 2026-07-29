package com.elementary.tasks.navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderTextDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.navigation.nav3.AppNavGraph
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.places.PlacesNavKey
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.settings.export.work.BackupSettingsTask
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
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
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.koin.android.ext.android.inject

class BottomNavActivity : BaseAuthActivity() {
  private val workScheduler by inject<WorkScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()

  private val adsProvider = AdsProvider()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    Logger.i(TAG, "Starting with action: ${intent.action}")
    Logger.i(TAG, "Starting with data: ${intent.data}")
    Logger.i(TAG, "Starting with extras: ${intent.extras?.keySet()?.toList()}")

    composeView {
      AppNavGraph(initialKeys = resolveInitialNavKeys())
    }

    adsProvider.showConsentMessage(this)
  }

  /**
   * Resolves an incoming deep link / app shortcut to typed Nav3 keys, seeded straight into
   * [AppNavGraph]'s initial backstack. Every [com.github.naz013.navigation.DeepLinkDestination] and [com.elementary.tasks.splash.ShortcutDestination] now
   * resolves to a typed `NavKey` here - there is no longer a legacy Fragment graph to fall back to.
   */
  private fun resolveInitialNavKeys(): List<NavKey> {
    if (intent.action == Intent.ACTION_VIEW) {
      val deepLinkDestination =
        intent.readParcelable(DeepLinkDestination.KEY, DeepLinkDestination::class.java)
      when (deepLinkDestination) {
        is ViewNoteScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return if (id != null) {
            listOf(NotesNavKey.List, NotesNavKey.Preview(id))
          } else {
            listOf(NotesNavKey.List)
          }
        }

        is EditNoteScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return listOf(NotesNavKey.List, NotesNavKey.Edit(id))
        }

        is EditGroupScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return listOf(GroupsNavKey.List, GroupsNavKey.Edit(id ?: ""))
        }

        is EditPlaceScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return listOf(PlacesNavKey.List, PlacesNavKey.Edit(id ?: ""))
        }

        is EditBirthdayScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return listOf(BirthdaysNavKey.Edit(id ?: ""))
        }

        is ViewBirthdayScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
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
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return if (id != null) {
            listOf(GoogleTasksNavKey.List, GoogleTasksNavKey.TaskPreview(id))
          } else {
            listOf(GoogleTasksNavKey.List)
          }
        }

        is EditReminderScreen -> return listOf(resolveBuildReminderNavKey(deepLinkDestination.extras))

        is ViewReminderScreen -> {
          val id = deepLinkDestination.extras.getString(IntentKeys.INTENT_ID)
          return if (id != null) listOf(ReminderPreviewNavKey.Preview(id)) else emptyList()
        }

        is DayViewScreen -> {
          val dateMillis = deepLinkDestination.extras.getLong("date", -1L)
          return if (dateMillis >= 0L) listOf(CalendarNavKey.Month, CalendarNavKey.Day(dateMillis)) else listOf(
            CalendarNavKey.Month)
        }

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

  /**
   * Decomposes an [EditReminderScreen] deep link's `Bundle` extras - a widget click
   * (`INTENT_ITEM`, read from [com.github.naz013.navigation.intent.IntentDataReader] by
   * `BuildReminderViewModel`) or a share-text/new-reminder-with-date-or-todo deep link (still
   * `Bundle`-encoded since it crosses an OS `Intent` boundary) - into [BuildReminderNavKey.Main]'s
   * typed fields.
   */
  private fun resolveBuildReminderNavKey(extras: Bundle?): BuildReminderNavKey.Main {
    if (extras?.getBoolean(IntentKeys.INTENT_ITEM, false) == true) {
      return BuildReminderNavKey.Main(fromIntentItem = true)
    }
    return when (val deepLinkData = extras?.let { DeepLinkDataParser().readDeepLinkData(it) }) {
      is ReminderDatetimeTypeDeepLinkData ->
        BuildReminderNavKey.Main(
          deepLinkDateTimeType = BuildReminderNavKey.Main.DateTimeType.Date,
          deepLinkDateTimeMillis = dateTimeManager.toMillis(deepLinkData.dateTime),
        )

      is ReminderTodoTypeDeepLinkData -> BuildReminderNavKey.Main(deepLinkTodo = true)
      is ReminderTextDeepLinkData -> BuildReminderNavKey.Main(deepLinkText = deepLinkData.text)
      else -> BuildReminderNavKey.Main()
    }
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
