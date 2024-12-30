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
import com.elementary.tasks.navigation.ActivityNavigator
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.navigation.SearchableFragmentCallback
import com.elementary.tasks.navigation.SearchableFragmentQueryObserver
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.navigation.topfragment.BaseTopFragment
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.FragmentDayView
import com.github.naz013.navigation.FragmentEditBirthday
import com.github.naz013.navigation.FragmentSettings
import com.github.naz013.navigation.FragmentViewBirthday
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
  private lateinit var navController: NavController
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: BaseNavigationFragment<*>? = null
  private var fragmentSearchView: SearchView? = null
  private var searchableFragmentQueryObserver: SearchableFragmentQueryObserver? = null

  private val navigationConsumer = object : NavigationConsumer {
    override fun consume(destination: Destination) {
      if (destination is ActivityDestination) {
        ActivityNavigator(this@BottomNavActivity).navigate(destination)
      }
    }
  }

  override fun inflateBinding() = ActivityBottomNavBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    Logger.d(
      "BottomNavActivity",
      "onCreate: ${intent.action}, ${intent.data?.toString()}, ${intent.extras?.keySet()}"
    )

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
      when (deepLinkDestination) {
        is FragmentDayView -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setArguments(deepLinkDestination.extras)
            .setDestination(R.id.dayViewFragment)
            .createTaskStackBuilder()
            .startActivities()
        }

        is FragmentSettings -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setDestination(R.id.settingsFragment)
            .createTaskStackBuilder()
            .startActivities()
        }

        is FragmentEditBirthday -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setDestination(R.id.editBirthdayFragment)
            .setArguments(deepLinkDestination.extras)
            .createTaskStackBuilder()
            .startActivities()
        }

        is FragmentViewBirthday -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setDestination(R.id.previewBirthdayFragment)
            .setArguments(deepLinkDestination.extras)
            .createTaskStackBuilder()
            .startActivities()
        }

        else -> {
          Logger.e("BottomNavActivity", "Unknown deep link destination: $deepLinkDestination")
        }
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
    Logger.d("handleBackPress: $currentResumedFragment")
    if (currentResumedFragment is HomeFragment) {
      finishAffinity()
    } else {
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
}
