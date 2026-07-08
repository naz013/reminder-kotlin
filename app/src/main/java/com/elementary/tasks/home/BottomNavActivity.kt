package com.elementary.tasks.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.NavHostFragment
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysFragment
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.googletasks.GoogleTasksFragment
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcherFactory
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.notes.list.NotesFragment
import com.elementary.tasks.places.list.PlacesFragment
import com.elementary.tasks.settings.export.work.BackupSettingsTask
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.koin.android.ext.android.inject

class BottomNavActivity :
  BindingActivity<ActivityBottomNavBinding>(),
  FragmentCallback {
  private val navigationObservable by inject<NavigationObservable>()
  private val navigationDispatcherFactory by inject<NavigationDispatcherFactory>()
  private val workScheduler by inject<WorkScheduler>()

  private lateinit var navController: NavController
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: Fragment? = null

  private val navigationConsumer =
    object : NavigationConsumer {
      override fun consume(destination: Destination) {
        navigationDispatcherFactory.create(destination).dispatch(destination)
      }
    }

  override fun inflateBinding() = ActivityBottomNavBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Starting with action: ${intent.action}")
    Logger.i(TAG, "Starting with data: ${intent.data}")
    Logger.i(TAG, "Starting with extras: ${intent.extras?.keySet()?.toList()}")

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.mainNavigationFragment) as NavHostFragment
    val navController = navHostFragment.navController
    this.navController = navController

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
          val arguments =
            Bundle(deepLinkDestination.extras ?: Bundle()).apply {
              if (deepLinkDestination is EditNoteScreen) putBoolean(NotesFragment.ARG_OPEN_EDIT, true)
              if (deepLinkDestination is EditPlaceScreen) putBoolean(PlacesFragment.ARG_OPEN_EDIT, true)
              if (deepLinkDestination is EditGoogleTaskScreen) putBoolean(GoogleTasksFragment.ARG_OPEN_EDIT, true)
              if (deepLinkDestination is EditBirthdayScreen) putBoolean(BirthdaysFragment.ARG_OPEN_EDIT, true)
            }
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setArguments(arguments)
            .setDestination(it)
            .createTaskStackBuilder()
            .startActivities()
        }
    } else if (ShortcutDestination.hasShortcut(intent.extras)) {
      val shortcut = ShortcutDestination.getShortcut(intent.extras)
      val destinationId =
        when (shortcut) {
          ShortcutDestination.Shortcut.GoogleTask -> {
            R.id.actionGoogle
          }

          ShortcutDestination.Shortcut.Reminder -> {
            R.id.buildReminderFragment
          }

          ShortcutDestination.Shortcut.Note -> {
            R.id.actionNotes
          }
          null -> null
        }
      destinationId?.also {
        val arguments =
          Bundle(intent.extras ?: Bundle()).apply {
            if (shortcut == ShortcutDestination.Shortcut.Note) putBoolean(NotesFragment.ARG_OPEN_EDIT, true)
            if (shortcut == ShortcutDestination.Shortcut.GoogleTask) putBoolean(GoogleTasksFragment.ARG_OPEN_EDIT, true)
          }
        NavDeepLinkBuilder(this)
          .setGraph(R.navigation.home_nav)
          .setArguments(arguments)
          .setDestination(it)
          .createTaskStackBuilder()
          .startActivities()
      }
    }

    adsProvider.showConsentMessage(this)
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
    Logger.i(TAG, "Handle back press, current fragment: $currentResumedFragment")
    if (currentResumedFragment is HomeFragment) {
      finishAffinity()
    } else if (currentResumedFragment is BackPressHandler && (currentResumedFragment as BackPressHandler).canGoBack()) {
      navController.popBackStack()
    }
    return true
  }

  companion object {
    private const val TAG = "BottomNavActivity"
  }
}
