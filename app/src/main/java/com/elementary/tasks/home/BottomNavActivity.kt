package com.elementary.tasks.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.os.datapicker.VoiceRecognitionLauncher
import com.elementary.tasks.core.utils.GlobalAction
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.notes.QuickNoteCoordinator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class BottomNavActivity : BindingActivity<ActivityBottomNavBinding>(),
  FragmentCallback, GlobalAction {

  private val buttonObservable by inject<GlobalButtonObservable>()
  private val viewModel by viewModel<ConversationViewModel>()
  private val noteViewModel by viewModel<NoteViewModel> { parametersOf("") }
  private val voiceRecognitionLauncher = VoiceRecognitionLauncher(this) { processResult(it) }
  private val mNoteView: QuickNoteCoordinator by lazy {
    binding.closeButton.setOnClickListener { mNoteView.hideNoteView() }
    QuickNoteCoordinator(
      this,
      binding.quickNoteContainer,
      binding.quickNoteView,
      noteViewModel,
      prefs,
      notifier
    ).also { it.hideNoteView() }
  }
  private var mFragment: BaseFragment<*>? = null
  private lateinit var navController: NavController

  override fun inflateBinding() = ActivityBottomNavBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    Timber.d("onCreate: ${intent.action}, ${intent.data?.toString()}, ${intent.extras?.keySet()}")

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.mainNavigationFragment) as NavHostFragment
    val navController = navHostFragment.navController
    this.navController = navController
    binding.toolbar.setupWithNavController(navController)

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
  }

  override fun onResume() {
    super.onResume()
    buttonObservable.addObserver(GlobalButtonObservable.Action.QUICK_NOTE, this)
    buttonObservable.addObserver(GlobalButtonObservable.Action.VOICE, this)
  }

  override fun onPause() {
    super.onPause()
    buttonObservable.removeObserver(GlobalButtonObservable.Action.QUICK_NOTE, this)
    buttonObservable.removeObserver(GlobalButtonObservable.Action.VOICE, this)
  }

  override fun setCurrentFragment(fragment: BaseFragment<*>) {
    Timber.d("setCurrentFragment: $fragment")
    mFragment = fragment
  }

  override fun onAlphaUpdate(alpha: Float) {
//        Timber.d("onAlphaUpdate: $alpha")
//        if (alpha > 0f) {
//            var alphaNew = 1f - alpha
//            if (alphaNew <= 0f) {
//                alphaNew = 0f
//                if (binding.appBar.isVisible()) {
//                    binding.appBar.transparent()
//                }
//            } else if (alphaNew > 0f) {
//                if (!binding.appBar.isVisible()) {
//                    binding.appBar.show()
//                }
//            }
//            binding.appBar.alpha = alphaNew
//        } else {
//            binding.appBar.alpha = 1f
//            if (!binding.appBar.isVisible()) {
//                binding.appBar.show()
//            }
//        }
  }

  override fun onTitleChange(title: String) {
    binding.toolbar.title = title
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
    if (action == GlobalButtonObservable.Action.QUICK_NOTE) {
      mNoteView.switchQuickNote()
    } else if (action == GlobalButtonObservable.Action.VOICE) {
      voiceRecognitionLauncher.recognize(false)
    }
  }

  override fun handleBackPress(): Boolean {
    Timber.d("handleBackPress: $mFragment, ${navController.backQueue.size}")
    if (mFragment is HomeFragment) {
      if (mNoteView.isNoteVisible) {
        mNoteView.hideNoteView()
      } else {
        finishAffinity()
      }
    } else {
      navController.popBackStack()
    }
    return true
  }

  companion object {
    const val ARG_DEST = "arg_dest"

    object Dest {
      const val DAY_VIEW = 0
      const val SETTINGS = 1
    }
  }
}
