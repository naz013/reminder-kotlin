package com.elementary.tasks.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
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
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.os.datapicker.VoiceRecognitionLauncher
import com.elementary.tasks.core.utils.ui.GlobalAction
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.SearchableFragmentCallback
import com.elementary.tasks.navigation.SearchableFragmentQueryObserver
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.navigation.topfragment.BaseTopFragment
import com.elementary.tasks.voice.ConversationViewModel
import com.google.android.material.search.SearchView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BottomNavActivity :
  BindingActivity<ActivityBottomNavBinding>(),
  FragmentCallback,
  GlobalAction,
  SearchableFragmentCallback {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val viewModel by viewModel<ConversationViewModel>()
  private val voiceRecognitionLauncher = VoiceRecognitionLauncher(this) { processResult(it) }
  private lateinit var navController: NavController
  private val adsProvider = AdsProvider()

  private var currentResumedFragment: BaseNavigationFragment<*>? = null
  private var fragmentSearchView: SearchView? = null
  private var searchableFragmentQueryObserver: SearchableFragmentQueryObserver? = null

  override fun inflateBinding() = ActivityBottomNavBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Timber.d("onCreate: ${intent.action}, ${intent.data?.toString()}, ${intent.extras?.keySet()}")

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.mainNavigationFragment) as NavHostFragment
    val navController = navHostFragment.navController
    this.navController = navController
    binding.bottomNavigation.setupWithNavController(navController)

    if (intent.action == Intent.ACTION_VIEW) {
      when (intent.getIntExtra(ARG_DEST, Dest.DAY_VIEW)) {
        Dest.DAY_VIEW -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setArguments(intent.extras)
            .setDestination(R.id.dayViewFragment)
            .createTaskStackBuilder()
            .startActivities()
        }

        Dest.SETTINGS -> {
          NavDeepLinkBuilder(this)
            .setGraph(R.navigation.home_nav)
            .setDestination(R.id.settingsFragment)
            .createTaskStackBuilder()
            .startActivities()
        }
      }
    }

    adsProvider.showConsentMessage(this)
  }

  override fun onResume() {
    super.onResume()
    buttonObservable.addObserver(GlobalButtonObservable.Action.VOICE, this)
  }

  override fun onPause() {
    super.onPause()
    buttonObservable.removeObserver(GlobalButtonObservable.Action.VOICE, this)
    fragmentSearchView?.takeIf { it.isShowing }?.also {
      it.clearText()
      it.hide()
    }
  }

  override fun setCurrentFragment(fragment: BaseNavigationFragment<*>) {
    currentResumedFragment = fragment
    binding.bottomNavigation.visibleGone(fragment is BaseTopFragment<*>)
    Traces.logEvent("Fragment opened = ${fragment.javaClass.name}")
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

  private fun processResult(matches: List<String>) {
    if (matches.isNotEmpty()) {
      viewModel.parseResults(matches, false, this)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (prefs.isBackupEnabled && prefs.isSettingsBackupEnabled) {
      BackupSettingsWorker.schedule(this)
    }
  }

  override fun invoke(view: View, action: GlobalButtonObservable.Action) {
    if (action == GlobalButtonObservable.Action.VOICE) {
      voiceRecognitionLauncher.recognize(false)
    }
  }

  override fun handleBackPress(): Boolean {
    Timber.d("handleBackPress: $currentResumedFragment")
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

  companion object {
    const val ARG_DEST = "arg_dest"

    object Dest {
      const val DAY_VIEW = 0
      const val SETTINGS = 1
    }
  }
}
