package com.elementary.tasks.experimental

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.notes.QuickNoteCoordinator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class BottomNavActivity : ThemedActivity<ActivityBottomNavBinding>(), FragmentCallback,
        RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver, (View, GlobalButtonObservable.Action) -> Unit {

    private lateinit var remotePrefs: RemotePrefs
    private val buttonObservable: GlobalButtonObservable by inject()

    private val viewModel: ConversationViewModel by viewModel()

    private var mNoteView: QuickNoteCoordinator? = null

    private val prefsObserver: (String) -> Unit = {
        Handler(Looper.getMainLooper()).post {
            if (it == PrefsConstants.DATA_BACKUP) {
                checkBackupPrefs()
            } else {
                checkDoNotDisturb()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)

        binding.bottomNav.setupWithNavController(findNavController(R.id.mainNavigationFragment))
        binding.toolbar.setupWithNavController(findNavController(R.id.mainNavigationFragment))

        remotePrefs = RemotePrefs(this)
        initQuickNote()
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
        prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
        prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
        prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
        prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
        prefs.addObserver(PrefsConstants.DATA_BACKUP, prefsObserver)
        if (prefs.isUiChanged) {
            prefs.isUiChanged = false
            recreate()
        }
        if (!prefs.isBetaWarmingShowed) {
            showBetaDialog()
        }
        remotePrefs.addUpdateObserver(this)
        if (!Module.isPro) {
            remotePrefs.addSaleObserver(this)
        }
        checkDoNotDisturb()
        checkBackupPrefs()
    }

    private fun checkBackupPrefs() {
        if (prefs.isBackupEnabled) {
//            mHeaderView?.backupBadge?.hide()
        } else {
//            mHeaderView?.backupBadge?.show()
        }
    }

    private fun checkDoNotDisturb() {
        if (prefs.applyDoNotDisturb(0)) {
            Timber.d("checkDoNotDisturb: active")
//            mHeaderView?.doNoDisturbIcon?.show()
        } else {
            Timber.d("checkDoNotDisturb: not active")
//            mHeaderView?.doNoDisturbIcon?.hide()
        }
    }

    override fun onPause() {
        super.onPause()
        prefs.removeObserver(PrefsConstants.DATA_BACKUP, prefsObserver)
        prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
        prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
        prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
        prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
        buttonObservable.removeObserver(GlobalButtonObservable.Action.QUICK_NOTE, this)
        buttonObservable.removeObserver(GlobalButtonObservable.Action.VOICE, this)
        if (!Module.isPro) {
            remotePrefs.removeSaleObserver(this)
        }
        remotePrefs.removeUpdateObserver(this)
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

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.mainNavigationFragment).navigateUp()
    }

    override fun layoutRes(): Int {
        return R.layout.activity_bottom_nav
    }

    override fun onScrollUpdate(y: Int) {
        binding.toolbar.isSelected = y > 0
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

    override fun onSale(discount: String, expiryDate: String) {
        val expiry = TimeUtil.getFireFormatted(prefs, expiryDate)
        if (TextUtils.isEmpty(expiry)) {
//            mHeaderView?.saleBadge?.visibility = View.GONE
        } else {
//            mHeaderView?.saleBadge?.visibility = View.VISIBLE
//            mHeaderView?.saleBadge?.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
        }
    }

    override fun noSale() {
//        mHeaderView?.saleBadge?.visibility = View.GONE
    }

    override fun onUpdate(version: String) {
//        mHeaderView?.updateBadge?.visibility = View.VISIBLE
//        mHeaderView?.updateBadge?.text = getString(R.string.update_available) + ": " + version
//        mHeaderView?.updateBadge?.setOnClickListener { SuperUtil.launchMarket(this) }
    }

    override fun noUpdate() {
//        mHeaderView?.updateBadge?.visibility = View.GONE
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
        private const val PRESS_AGAIN_TIME = 2000
        private const val CURRENT_SCREEN = "current_screen"
    }
}
