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
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcherFactory
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.settings.export.work.BackupSettingsWorker
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.ui.common.activity.BindingActivity
import org.koin.android.ext.android.inject

class BottomNavActivity :
  BindingActivity<ActivityBottomNavBinding>(),
  FragmentCallback {

  private val navigationObservable by inject<NavigationObservable>()
  private val navigationDispatcherFactory by inject<NavigationDispatcherFactory>()

  private lateinit var navController: NavController
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: Fragment? = null

  private val navigationConsumer = object : NavigationConsumer {
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
      val deepLinkDestination = intent.readParcelable(
        DeepLinkDestination.KEY,
        DeepLinkDestination::class.java
      )
      Logger.i(TAG, "Deep link destination: $deepLinkDestination")
      deepLinkDestination
        ?.let { ScreenDestinationIdResolver().resolve(deepLinkDestination) }
        ?.also {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setArguments(deepLinkDestination.extras)
            .setDestination(it)
            .createTaskStackBuilder()
            .startActivities()
        }
    } else if (ShortcutDestination.hasShortcut(intent.extras)) {
      val shortcut = ShortcutDestination.getShortcut(intent.extras)
      val destinationId = when (shortcut) {
        ShortcutDestination.Shortcut.GoogleTask -> {
          R.id.editGoogleTaskFragment
        }

        ShortcutDestination.Shortcut.Reminder -> {
          R.id.buildReminderFragment
        }

        ShortcutDestination.Shortcut.Note -> TODO()
        null -> null
      }
      destinationId?.also {
        NavDeepLinkBuilder(this)
          .setGraph(R.navigation.home_nav)
          .setArguments(intent.extras)
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
    BackupSettingsWorker.schedule(this)
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
