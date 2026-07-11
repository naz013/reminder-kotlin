package com.elementary.tasks.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderTextDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.googletasks.GoogleTasksNavKey
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcherFactory
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.calendar.monthview.CalendarNavKey
import com.elementary.tasks.navigation.nav3.AppNavGraph
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.places.PlacesNavKey
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.export.work.BackupSettingsTask
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewNoteScreen
import com.github.naz013.navigation.ViewReminderScreen
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.koin.android.ext.android.inject

class BottomNavActivity :
  LightThemedActivity(),
  FragmentCallback {
  private val navigationObservable by inject<NavigationObservable>()
  private val navigationDispatcherFactory by inject<NavigationDispatcherFactory>()
  private val workScheduler by inject<WorkScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()

  private var navController: NavController? = null
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: Fragment? = null

  private val navigationConsumer =
    object : NavigationConsumer {
      override fun consume(destination: Destination) {
        navigationDispatcherFactory.create(destination).dispatch(destination)
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Starting with action: ${intent.action}")
    Logger.i(TAG, "Starting with data: ${intent.data}")
    Logger.i(TAG, "Starting with extras: ${intent.extras?.keySet()?.toList()}")

    val initialNavKeys = resolveInitialNavKeys()
    composeView {
      AppNavGraph(initialKeys = initialNavKeys, onLegacyNavHostReady = { navController = it })
    }

    if (initialNavKeys.isEmpty()) {
      dispatchLegacyIntent()
    }

    adsProvider.showConsentMessage(this)
  }

  /**
   * Resolves an incoming deep link / app shortcut to typed Nav3 keys for screens already promoted
   * out of `home_nav.xml` (Notes, Groups, Places) - seeded straight into [AppNavGraph]'s initial
   * backstack instead of going through [dispatchLegacyIntent]'s `NavDeepLinkBuilder`, since those
   * screens no longer have a `home_nav.xml` destination id to target. Returns an empty list for
   * anything else, which [dispatchLegacyIntent] still handles exactly as before.
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
          return if (dateMillis >= 0L) listOf(CalendarNavKey.Month, CalendarNavKey.Day(dateMillis)) else listOf(CalendarNavKey.Month)
        }

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
          deepLinkDateTimeType = deepLinkData.type,
          deepLinkDateTimeMillis = dateTimeManager.toMillis(deepLinkData.dateTime),
        )

      is ReminderTodoTypeDeepLinkData -> BuildReminderNavKey.Main(deepLinkTodo = true)
      is ReminderTextDeepLinkData -> BuildReminderNavKey.Main(deepLinkText = deepLinkData.text)
      else -> BuildReminderNavKey.Main()
    }
  }

  /**
   * Unchanged legacy dispatch for every destination still living in `home_nav.xml`. App shortcuts
   * (`ShortcutDestination`) are no longer dispatched here - every shortcut now resolves to a typed
   * `NavKey` in [resolveInitialNavKeys] (Note, Google Task and Reminder are all promoted), so
   * [dispatchLegacyIntent] is only ever invoked for `ACTION_VIEW` deep links to destinations still
   * living in `home_nav.xml`.
   */
  private fun dispatchLegacyIntent() {
    if (intent.action == Intent.ACTION_VIEW) {
      val deepLinkDestination =
        intent.readParcelable(
          DeepLinkDestination.KEY,
          DeepLinkDestination::class.java,
        )
      Logger.i(TAG, "Deep link destination: $deepLinkDestination")
      deepLinkDestination
        ?.let { ScreenDestinationIdResolver().resolve(deepLinkDestination) }
        ?.also {
          val arguments = Bundle(deepLinkDestination.extras ?: Bundle())
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setArguments(arguments)
            .setDestination(it)
            .createTaskStackBuilder()
            .startActivities()
        }
    }
  }

  override fun onResume() {
    super.onResume()
    navigationObservable.subscribe(navigationConsumer)
  }

  override fun onPause() {
    super.onPause()
    navigationObservable.unsubscribe(navigationConsumer)
  }

  override fun setCurrentFragment(fragment: Fragment) {
    currentResumedFragment = fragment
    Logger.logEvent("Fragment opened = ${fragment.javaClass.name}")
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

  override fun handleBackPress(): Boolean {
    val fragment = currentResumedFragment
    Logger.i(TAG, "Handle back press, current fragment: $fragment")
    // fragment can be a *stale* reference once the outer Nav3 backstack has fallen through to
    // here with no legacy Fragment currently attached (e.g. back-ing out of a promoted screen -
    // legacy Fragment via AppNavBridge - back to a promoted screen with nothing legacy left in the
    // outer backstack): navController would then point at a detached NavHostFragment and silently
    // no-op. Treat that the same as being at a promoted screen (Home included, now that it's a
    // Nav3 entry with no Fragment of its own) - there's nothing legacy left to pop to.
    if (fragment == null || !fragment.isAdded) {
      finishAffinity()
    } else if (fragment is BackPressHandler && fragment.canGoBack()) {
      navController?.popBackStack()
    }
    return true
  }

  companion object {
    private const val TAG = "BottomNavActivity"
  }
}
