package com.elementary.tasks.navigation

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
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.QrShareProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdaysFragment
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.binding.views.NavHeaderBinding
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.databinding.ActivityMainBinding
import com.elementary.tasks.day_view.DayViewFragment
import com.elementary.tasks.google_tasks.GoogleTasksFragment
import com.elementary.tasks.groups.list.GroupsFragment
import com.elementary.tasks.month_view.CalendarFragment
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.navigation.fragments.FeedbackFragment
import com.elementary.tasks.navigation.fragments.MapFragment
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.SettingsFragment
import com.elementary.tasks.navigation.settings.general.home.PageIdentifier
import com.elementary.tasks.notes.QuickNoteCoordinator
import com.elementary.tasks.notes.list.NotesFragment
import com.elementary.tasks.reminder.lists.ArchiveFragment
import com.elementary.tasks.reminder.lists.RemindersFragment
import com.google.android.material.navigation.NavigationView
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : ThemedActivity<ActivityMainBinding>(), NavigationView.OnNavigationItemSelectedListener, FragmentCallback,
        RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver, (View, GlobalButtonObservable.Action) -> Unit {

    private lateinit var remotePrefs: RemotePrefs
    private val buttonObservable: GlobalButtonObservable by inject()

    private var fragment: BaseFragment<*>? = null
    private var mNoteView: QuickNoteCoordinator? = null
    private var mHeaderView: NavHeaderBinding? = null

    private lateinit var viewModel: ConversationViewModel

    private var prevItem: Int = 0
    private var beforeSettings: Int = 0
    private var isBackPressed: Boolean = false
    private var pressedTime: Long = 0
    private val prefsObserver: (String) -> Unit = {
        Handler(Looper.getMainLooper()).post {
            if (it == PrefsConstants.DATA_BACKUP) {
                checkBackupPrefs()
            } else {
                checkDoNotDisturb()
            }
        }
    }

    override fun layoutRes(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remotePrefs = RemotePrefs(this)
        initActionBar()
        initNavigation()
        initViewModel()
        initQuickNote()
        initScreen(savedInstanceState)
    }

    private fun initScreen(savedInstanceState: Bundle?) {
        when {
            savedInstanceState != null -> {
                openScreen(savedInstanceState.getInt(CURRENT_SCREEN, PageIdentifier.menuId(this, prefs.homePage)))
            }
            intent.getIntExtra(Constants.INTENT_POSITION, 0) != 0 -> {
                var pos = intent.getIntExtra(Constants.INTENT_POSITION, 0)
                if (pos == 0) {
                    pos = PageIdentifier.menuId(this, 0)
                }
                prevItem = PageIdentifier.menuId(this, 0)
                binding.navView.setCheckedItem(pos)
                openScreen(pos)
            }
            else -> initStartFragment()
        }
    }

    private fun initQuickNote() {
        val noteViewModel = ViewModelProviders.of(this, NoteViewModel.Factory("")).get(NoteViewModel::class.java)
        mNoteView = QuickNoteCoordinator(this, binding.quickNoteContainer, binding.quickNoteView,
                noteViewModel, prefs, notifier)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_SCREEN, prevItem)
        super.onSaveInstanceState(outState)
    }

    private fun initStartFragment() {
        Timber.d("initStartFragment: ")
        prevItem = PageIdentifier.menuId(this, prefs.homePage)
        binding.navView.setCheckedItem(prevItem)
        openScreen(prevItem)
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon = ViewUtils.navIcon(this, isDark)
        binding.toolbar.setNavigationOnClickListener { onDrawerClick() }
    }

    private fun onDrawerClick() {
        if (this.fragment is BaseSettingsFragment) {
            onBackPressed()
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun replaceFragment(fragment: BaseFragment<*>, title: String) {
        clearBackStack()
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, fragment, title)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(title)
        ft.commit()
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
            mHeaderView?.backupBadge?.hide()
        } else {
            mHeaderView?.backupBadge?.show()
        }
    }

    private fun checkDoNotDisturb() {
        if (prefs.applyDoNotDisturb(0)) {
            Timber.d("checkDoNotDisturb: active")
            mHeaderView?.doNoDisturbIcon?.show()
        } else {
            Timber.d("checkDoNotDisturb: not active")
            mHeaderView?.doNoDisturbIcon?.hide()
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

    override fun onTitleChange(title: String) {
        binding.toolbar.title = title
    }

    override fun onFragmentSelect(fragment: BaseFragment<*>) {
        this.fragment = fragment
        if (this.fragment is BaseSettingsFragment) {
            binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        } else {
            binding.toolbar.navigationIcon = ViewUtils.navIcon(this, isDark)
        }
    }

    private fun clearBackStack() {
        try {
            val fm = supportFragmentManager
            for (i in 0 until fm.backStackEntryCount) {
                fm.popBackStack()
            }
        } catch (e: Exception) {
        }
    }

    override fun openFragment(fragment: BaseFragment<*>, tag: String, replace: Boolean) {
        if (replace) {
            replaceFragment(fragment, tag)
        } else {
            openFragment(fragment, tag)
        }
    }

    override fun openFragment(fragment: BaseFragment<*>, tag: String) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, fragment, tag)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(tag)
        ft.commit()
    }

    override fun refreshMenu() {
        setMenuVisible()
    }

    override fun onScrollUpdate(y: Int) {
        binding.appBar.isSelected = y > 0
    }

    override fun onMenuSelect(menu: Int) {
        prevItem = menu
        binding.navView.setCheckedItem(prevItem)
    }

    override fun hideKeyboard() {
        val focus = window.currentFocus ?: return
        val token = focus.windowToken ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(token, 0)
    }

    private fun initNavigation() {
        binding.navView.isVerticalScrollBarEnabled = false
        binding.navView.setNavigationItemSelectedListener(this)
        mHeaderView = NavHeaderBinding(binding.navView.getHeaderView(0))
        mHeaderView?.saleBadge?.hide()
        mHeaderView?.updateBadge?.hide()
        mHeaderView?.doNoDisturbIcon?.hide()
        if (Module.isPro) {
            mHeaderView?.appNameBannerPro?.show()
        } else {
            mHeaderView?.appNameBannerPro?.hide()
        }
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            mHeaderView?.playServicesWarning?.hide()
        } else {
            mHeaderView?.playServicesWarning?.show()
        }
        setMenuVisible()
    }

    private fun setMenuVisible() {
        val menu = binding.navView.menu
        menu.getItem(5)?.isVisible = GTasks.getInstance(this)?.isLogged ?: false
        menu.getItem(7)?.isVisible = Module.hasLocation(this)
        menu.getItem(11)?.isVisible = !Module.isPro && !SuperUtil.isAppInstalled(this, "com.cray.software.justreminderpro")
        menu.getItem(12)?.isVisible = Module.isPro && QrShareProvider.hasQrSupport()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (mNoteView != null && mNoteView?.isNoteVisible == true) {
            mNoteView?.hideNoteView()
        } else {
            moveBack()
        }
    }

    private fun moveBack() {
        if (fragment != null) {
            if (fragment is SettingsFragment || fragment is FeedbackFragment) {
                if (beforeSettings != 0) {
                    prevItem = beforeSettings
                    binding.navView.setCheckedItem(beforeSettings)
                    openScreen(beforeSettings)
                    beforeSettings = 0
                } else {
                    initStartFragment()
                }
                return
            } else if (fragment is BaseSettingsFragment && fragment?.canGoBack() == true) {
                super.onBackPressed()
                return
            } else if (fragment?.canGoBack() == false) {
                return
            }
        }
        if (isBackPressed) {
            if (System.currentTimeMillis() - pressedTime < PRESS_AGAIN_TIME) {
                beforeSettings = 0
                finish()
            } else {
                isBackPressed = false
                onBackPressed()
            }
        } else {
            isBackPressed = true
            pressedTime = System.currentTimeMillis()
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
            viewModel.parseResults(matches, false, this)
        }
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Module.isMarshmallow) {
            fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        Handler().postDelayed({
            if (prevItem == item.itemId && (item.itemId != R.id.nav_feedback && item.itemId != R.id.nav_pro && item.itemId != R.id.nav_qr)) {
                return@postDelayed
            }
            openScreen(item.itemId)
            if (item.itemId != R.id.nav_feedback && item.itemId != R.id.nav_pro && item.itemId != R.id.nav_qr) {
                prevItem = item.itemId
            }
        }, 250)
        return true
    }

    private fun openScreen(itemId: Int) {
        beforeSettings = 0
        when (itemId) {
            R.id.nav_current -> replaceFragment(RemindersFragment(), getString(R.string.tasks))
            R.id.nav_notes -> replaceFragment(NotesFragment(), getString(R.string.notes))
            R.id.nav_birthdays -> replaceFragment(BirthdaysFragment(), getString(R.string.birthdays))
            R.id.nav_calendar -> replaceFragment(CalendarFragment(), getString(R.string.calendar))
            R.id.nav_day_view -> replaceFragment(DayViewFragment(), getString(R.string.events))
            R.id.nav_tasks -> replaceFragment(GoogleTasksFragment(), getString(R.string.google_tasks))
            R.id.nav_groups -> replaceFragment(GroupsFragment(), getString(R.string.groups))
            R.id.nav_map -> replaceFragment(MapFragment(), getString(R.string.map))
            R.id.nav_archive -> replaceFragment(ArchiveFragment(), getString(R.string.trash))
            R.id.nav_settings -> {
                beforeSettings = prevItem
                replaceFragment(SettingsFragment(), getString(R.string.action_settings))
            }
            R.id.nav_feedback -> replaceFragment(FeedbackFragment(), getString(R.string.feedback))
            R.id.nav_pro -> showProDialog()
            R.id.nav_qr -> QrShareProvider.openImportScreen(this)
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
            mHeaderView?.saleBadge?.visibility = View.GONE
        } else {
            mHeaderView?.saleBadge?.visibility = View.VISIBLE
            mHeaderView?.saleBadge?.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
        }
    }

    override fun noSale() {
        mHeaderView?.saleBadge?.visibility = View.GONE
    }

    override fun onUpdate(version: String) {
        mHeaderView?.updateBadge?.visibility = View.VISIBLE
        mHeaderView?.updateBadge?.text = getString(R.string.update_available) + ": " + version
        mHeaderView?.updateBadge?.setOnClickListener { SuperUtil.launchMarket(this) }
    }

    override fun noUpdate() {
        mHeaderView?.updateBadge?.visibility = View.GONE
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
