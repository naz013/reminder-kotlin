package com.elementary.tasks.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcherFactory
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.navigation.SearchableFragmentCallback
import com.elementary.tasks.navigation.SearchableFragmentQueryObserver
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.navigation.topfragment.BaseTopFragment
import com.elementary.tasks.splash.ShortcutDestination
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.view.visibleGone
import com.google.android.material.search.SearchView
import org.koin.android.ext.android.inject

class BottomNavActivity :
  BindingActivity<ActivityBottomNavBinding>(),
  FragmentCallback,
  SearchableFragmentCallback {

  private val navigationObservable by inject<NavigationObservable>()
  private val prefs by inject<Prefs>()
  private val navigationDispatcherFactory by inject<NavigationDispatcherFactory>()

  private lateinit var navController: NavController
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: BaseNavigationFragment<*>? = null
  private var fragmentSearchView: SearchView? = null
  private var searchableFragmentQueryObserver: SearchableFragmentQueryObserver? = null

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
    binding.bottomNavigation.setupWithNavController(navController)

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
    fragmentSearchView?.takeIf { it.isShowing }?.also {
      it.clearText()
      it.hide()
    }
  }

  override fun setCurrentFragment(fragment: BaseNavigationFragment<*>) {
    currentResumedFragment = fragment
    binding.bottomNavigation.visibleGone(fragment is BaseTopFragment<*>)
    Logger.logEvent("Fragment opened = ${fragment.javaClass.name}")
  }

  override fun onCreateFragment(fragment: BaseNavigationFragment<*>) {
    fragmentSearchView?.also {
      binding.container.removeView(it)
    }
    fragmentSearchView = null
    searchableFragmentQueryObserver = null
  }

  override fun hideKeyboard() {
    val focus = window.currentFocus ?: return
    val token = focus.windowToken ?: return
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(token, 0)
  }

  override fun onDestroy() {
    super.onDestroy()
    if (prefs.isBackupEnabled && prefs.isSettingsBackupEnabled) {
      BackupSettingsWorker.schedule(this)
    }
  }

  override fun handleBackPress(): Boolean {
    Logger.i("NavActivity", "Handle back press, current fragment: $currentResumedFragment")
    if (currentResumedFragment is HomeFragment) {
      finishAffinity()
    } else if (currentResumedFragment?.canGoBack() == true) {
      navController.popBackStack()
    }
    return true
  }

  override fun setQuery(text: String) {
    fragmentSearchView?.run {
      editText.setText(text)
      editText.setSelection(text.length)
    }
  }

  override fun setSearchViewParams(
    anchorId: Int,
    hint: String,
    adapter: RecyclerView.Adapter<*>,
    observer: SearchableFragmentQueryObserver
  ) {
    this.searchableFragmentQueryObserver = observer
    initSearchView(anchorId, hint, adapter)
  }

  private fun initSearchView(
    anchorId: Int,
    hint: String,
    adapter: RecyclerView.Adapter<*>
  ) {
    val searchView = SearchView(this)
    searchView.hint = hint
    searchView.layoutParams = CoordinatorLayout.LayoutParams(binding.container.layoutParams).apply {
      this.anchorId = anchorId
      this.width = CoordinatorLayout.LayoutParams.MATCH_PARENT
      this.height = CoordinatorLayout.LayoutParams.MATCH_PARENT
    }

    val searchRecyclerView = RecyclerView(this)
    searchRecyclerView.layoutParams = FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.MATCH_PARENT,
      FrameLayout.LayoutParams.MATCH_PARENT
    )
    searchRecyclerView.layoutManager = LinearLayoutManager(this)
    searchRecyclerView.adapter = adapter

    searchView.addView(searchRecyclerView)

    binding.container.addView(searchView)

    searchView.editText.doOnTextChanged { text, _, _, _ ->
      searchableFragmentQueryObserver?.onQueryChanged(text?.toString() ?: "")
    }

    fragmentSearchView = searchView
  }

  companion object {
    private const val TAG = "BottomNavActivity"
  }
}
