package com.elementary.tasks.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.GlobalAction
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.SuperUtil
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
  private val mNoteView: QuickNoteCoordinator by lazy {
    binding.closeButton.setOnClickListener { mNoteView.hideNoteView() }
    val noteView = QuickNoteCoordinator(this, binding.quickNoteContainer, binding.quickNoteView,
      noteViewModel, prefs)
    noteView.hideNoteView()
    noteView
  }
  private var mFragment: BaseFragment<*>? = null

  override fun inflateBinding() = ActivityBottomNavBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    Timber.d("onCreate: ${intent.action}, ${intent.data?.toString()}, ${intent.extras?.keySet()}")
    binding.toolbar.setupWithNavController(findNavController(R.id.mainNavigationFragment))
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

    if (!prefs.isBetaWarmingShowed) {
      showBetaDialog()
    }
  }

  override fun onPause() {
    super.onPause()
    buttonObservable.removeObserver(GlobalButtonObservable.Action.QUICK_NOTE, this)
    buttonObservable.removeObserver(GlobalButtonObservable.Action.VOICE, this)
  }

  private fun showBetaDialog() {
    prefs.isBetaWarmingShowed = true
    var appVersion = ""
    try {
      val pInfo = packageManager.getPackageInfo(packageName, 0)
      appVersion = pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }

    if (!appVersion.contains("beta")) {
      return
    }
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle("Beta")
    builder.setMessage("This version of application may work unstable!")
    builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ -> dialogInterface.dismiss() }
    builder.create().show()
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
      val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
      viewModel.parseResults(matches, false, this)
    }
    mFragment?.onActivityResult(requestCode, resultCode, data)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    mFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
      SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false, prefs, language)
    }
  }

  override fun onBackPressed() {
    if (mFragment is HomeFragment) {
      if (mNoteView.isNoteVisible) {
        mNoteView.hideNoteView()
      } else {
        finishAffinity()
      }
    } else {
      super.onBackPressed()
    }
  }

  companion object {
    const val VOICE_RECOGNITION_REQUEST_CODE = 109
    const val ARG_DEST = "arg_dest"

    object Dest {
      const val DAY_VIEW = 0
      const val SETTINGS = 1
    }
  }
}
