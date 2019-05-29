package com.elementary.tasks.experimental

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.BindingActivity
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.ScreenInsets
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.notes.QuickNoteCoordinator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BottomNavActivity : BindingActivity<ActivityBottomNavBinding>(R.layout.activity_bottom_nav),
        FragmentCallback, (View, GlobalButtonObservable.Action) -> Unit {

    private val buttonObservable: GlobalButtonObservable by inject()
    private val viewModel: ConversationViewModel by viewModel()

    private var mFragment: BaseFragment<*>? = null

    private var mNoteView: QuickNoteCoordinator? = null
    private var mInsets: ScreenInsets = ScreenInsets(0, 0, 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setupWithNavController(findNavController(R.id.mainNavigationFragment))
        binding.container.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        binding.container.setOnApplyWindowInsetsListener { _, insets ->
            handleInsets(insets)
            return@setOnApplyWindowInsetsListener insets.consumeSystemWindowInsets()
        }

        initQuickNote()
    }

    private fun handleInsets(insets: WindowInsets) {
        mInsets = ScreenInsets(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
        Timber.d("handleInsets: $mInsets")

        val lpToolbar = binding.appBar.layoutParams as ViewGroup.MarginLayoutParams
        lpToolbar.topMargin = insets.systemWindowInsetTop
        lpToolbar.leftMargin = insets.systemWindowInsetLeft
        lpToolbar.rightMargin = insets.systemWindowInsetRight
        binding.appBar.layoutParams = lpToolbar

        mFragment?.let {
            Timber.d("handleInsets: $it")
            it.handleInsets(mInsets)
        }
    }

    private fun initQuickNote() {
        val noteViewModel = ViewModelProviders.of(this, NoteViewModel.Factory("")).get(NoteViewModel::class.java)
//        mNoteView = QuickNoteCoordinator(this, binding.quickNoteContainer, binding.quickNoteView,
//                noteViewModel, prefs, notifier)
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

    override fun provideInsets(): ScreenInsets {
        return mInsets
    }

    override fun onAlphaUpdate(alpha: Float) {
//        Timber.d("onAlphaUpdate: $alpha")
        if (alpha > 0f) {
            var alphaNew = 1f - alpha
            if (alphaNew <= 0f) {
                alphaNew = 0f
                if (binding.appBar.isVisible()) {
                    binding.appBar.transparent()
                }
            } else if (alphaNew > 0f) {
                if (!binding.appBar.isVisible()) {
                    binding.appBar.show()
                }
            }
            binding.appBar.alpha = alphaNew
        } else {
            binding.appBar.alpha = 1f
            if (!binding.appBar.isVisible()) {
                binding.appBar.show()
            }
        }
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

    private fun openMarket() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=" + "com.cray.software.justreminderpro")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.could_not_launch_market, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProDialog() {
        dialogues.getMaterialDialog(this)
                .setTitle(getString(R.string.buy_pro))
                .setMessage(getString(R.string.pro_advantages) + "\n" +
                        getString(R.string.different_settings_for_birthdays) + "\n" +
                        "- " + getString(R.string.additional_reminder) + "\n" +
                        getString(R.string._led_notification_) + "\n" +
                        getString(R.string.led_color_for_each_reminder) + "\n" +
                        "- " + getString(R.string.exclusive_themes) + "\n" +
                        getString(R.string.styles_for_marker) + "\n" +
                        "- " + getString(R.string.no_ads))
                .setPositiveButton(R.string.buy) { dialog, _ ->
                    dialog.dismiss()
                    openMarket()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .create().show()
    }

    override fun invoke(view: View, action: GlobalButtonObservable.Action) {
        if (action == GlobalButtonObservable.Action.QUICK_NOTE) {
            mNoteView?.switchQuickNote()
        } else if (action == GlobalButtonObservable.Action.VOICE) {
            SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false, prefs, language)
        }
    }

    companion object {
        const val VOICE_RECOGNITION_REQUEST_CODE = 109
    }
}
