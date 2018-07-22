package com.elementary.tasks.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.async.BackupSettingTask
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel
import com.elementary.tasks.core.viewModels.notes.NotesViewModel
import com.elementary.tasks.core.viewModels.reminders.ActiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.google_tasks.GoogleTasksFragment
import com.elementary.tasks.groups.list.GroupsFragment
import com.elementary.tasks.navigation.fragments.*
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.SettingsFragment
import com.elementary.tasks.navigation.settings.images.MainImageActivity
import com.elementary.tasks.navigation.settings.images.SaveAsync
import com.elementary.tasks.notes.QuickNoteCoordinator
import com.elementary.tasks.notes.list.NotesFragment
import com.elementary.tasks.places.list.PlacesFragment
import com.elementary.tasks.reminder.lists.ArchiveFragment
import com.elementary.tasks.reminder.lists.RemindersFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import timber.log.Timber
import java.io.File

class MainActivity : ThemedActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentCallback,
        RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver {

    private var fragment: Fragment? = null
    private var mNoteView: QuickNoteCoordinator? = null

    private lateinit var viewModel: ConversationViewModel

    private var prevItem: Int = 0
    private var beforeSettings: Int = 0
    private var isBackPressed: Boolean = false
    private var pressedTime: Long = 0

    private val mQuickCallback = object : QuickNoteCoordinator.Callback {
        override fun onOpen() {
            fab.setImageResource(R.drawable.ic_clear_white_24dp)
        }

        override fun onClose() {
            fab.setImageResource(R.drawable.ic_add_white_24dp)
        }
    }

    private val isRateDialogShowed: Boolean
        get() {
            var count = prefs.rateCount
            count++
            prefs.rateCount = count
            return count == 10
        }

    override val isFiltersVisible: Boolean
        get() = filterView.visibility == View.VISIBLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnLongClickListener {
            mNoteView?.switchQuickNote()
            true
        }
        initActionBar()
        initNavigation()
        initViewModel()
        initQuickNote(savedInstanceState)
    }

    private fun initQuickNote(savedInstanceState: Bundle?) {
        val noteViewModel = ViewModelProviders.of(this).get(NotesViewModel::class.java)
        val reminderViewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel::class.java)
        mNoteView = QuickNoteCoordinator(this, quickNoteContainer, quickNoteView,
                mQuickCallback, reminderViewModel, noteViewModel)
        when {
            savedInstanceState != null -> openScreen(savedInstanceState.getInt(CURRENT_SCREEN, R.id.nav_current))
            intent.getIntExtra(Constants.INTENT_POSITION, 0) != 0 -> {
                prevItem = intent.getIntExtra(Constants.INTENT_POSITION, 0)
                nav_view.setCheckedItem(prevItem)
                openScreen(prevItem)
            }
            else -> initStartFragment()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_SCREEN, prevItem)
        super.onSaveInstanceState(outState)
    }

    private fun initStartFragment() {
        prevItem = R.id.nav_current
        nav_view.setCheckedItem(prevItem)
        replaceFragment(RemindersFragment(), getString(R.string.events))
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
        toolbar.setNavigationOnClickListener { onDrawerClick() }
    }

    private fun onDrawerClick() {
        if (this.fragment is BaseSettingsFragment) {
            onBackPressed()
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun replaceFragment(fragment: Fragment, title: String) {
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, fragment, title)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(title)
        ft.commit()
        toolbar.title = title
    }

    override fun onResume() {
        super.onResume()
        if (prefs.isUiChanged) {
            prefs.isUiChanged = false
            recreate()
        }
        if (!prefs.isBetaWarmingShowed) {
            showBetaDialog()
        }
        if (isRateDialogShowed) {
            showRateDialog()
        }
        showMainImage()
        RemotePrefs.getInstance(this).addUpdateObserver(this)
        if (!Module.isPro) {
            RemotePrefs.getInstance(this).addSaleObserver(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!Module.isPro) {
            RemotePrefs.getInstance(this).removeSaleObserver(this)
        }
        RemotePrefs.getInstance(this).removeUpdateObserver(this)
    }

    private fun showRateDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.rate)
        builder.setMessage(R.string.can_you_rate_this_application)
        builder.setPositiveButton(R.string.rate) { dialogInterface, _ ->
            dialogInterface.dismiss()
            SuperUtil.launchMarket(this@MainActivity)
        }
        builder.setNegativeButton(R.string.never) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setNeutralButton(R.string.later) { dialogInterface, _ ->
            dialogInterface.dismiss()
            prefs.rateCount = 0
        }
        builder.create().show()
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
        val builder = Dialogues.getDialog(this)
        builder.setTitle("Beta")
        builder.setMessage("This version of application may work unstable!")
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun showMainImage() {
        val path = prefs.imagePath
        val view = nav_view.getHeaderView(0)
        if (!path.isEmpty() && !path.contains("{")) {
            var fileName: String = path
            if (path.contains("=")) {
                val index = path.indexOf("=")
                fileName = path.substring(index)
            }
            val file = File(MemoryUtil.imageCacheDir, "$fileName.jpg")
            val readPerm = Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            if (readPerm && file.exists()) {
                Glide.with(this).load(file).into(view.headerImage)
                view.headerImage.visibility = View.VISIBLE
            } else {
                Glide.with(this).load(path).into(view.headerImage)
                view.headerImage.visibility = View.VISIBLE
                if (readPerm) {
                    SaveAsync(this).execute(path)
                }
            }
        } else {
            view.headerImage.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (prefs.isAutoBackupEnabled && prefs.isSettingsBackupEnabled
                && Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            BackupSettingTask(this).execute()
        }
    }

    override fun onTitleChange(title: String) {
        toolbar.title = title
    }

    override fun onFragmentSelect(fragment: Fragment) {
        this.fragment = fragment
        if (this.fragment is BaseSettingsFragment) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
        }
    }

    override fun setClick(listener: View.OnClickListener?) {
        Timber.d("setClick: $listener")
        if (listener == null) {
            hideFab()
        } else {
            showFab()
            fab.setOnClickListener { view ->
                if (mNoteView != null) {
                    if (mNoteView!!.isNoteVisible) {
                        mNoteView?.hideNoteView()
                        return@setOnClickListener
                    }
                }
                listener.onClick(view)
            }
        }
    }

    override fun onThemeChange(@ColorInt primary: Int, @ColorInt primaryDark: Int, @ColorInt accent: Int) {
        var primary = primary
        var primaryDark = primaryDark
        var accent = accent
        if (primary == 0) {
            primary = themeUtil.getColor(themeUtil.colorPrimary())
        }
        if (primaryDark == 0) {
            primaryDark = themeUtil.getColor(themeUtil.colorPrimaryDark())
        }
        if (accent == 0) {
            accent = themeUtil.getColor(themeUtil.colorAccent())
        }
        toolbar.setBackgroundColor(primary)
        if (Module.isLollipop) {
            window.statusBarColor = primaryDark
        }
        fab.backgroundTintList = ViewUtils.getFabState(accent, accent)
    }

    override fun refreshMenu() {
        setMenuVisible()
    }

    override fun onScrollChanged(recyclerView: RecyclerView?) {

    }

    override fun addFilters(filters: List<FilterView.Filter>, clear: Boolean) {
        if (filters.isEmpty()) {
            hideFilters()
            if (clear) {
                filterView.clear()
            }
        } else {
            if (clear) {
                filterView.clear()
            }
            for (filter in filters) {
                filterView.addFilter(filter)
            }
            ViewUtils.expand(filterView)
        }
    }

    override fun hideFilters() {
        if (isFiltersVisible) {
            ViewUtils.collapse(filterView)
        }
    }

    override fun onVoiceAction() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false)
    }

    override fun onMenuSelect(menu: Int) {
        prevItem = menu
        nav_view.setCheckedItem(prevItem)
    }

    private fun initNavigation() {
        nav_view.setNavigationItemSelectedListener(this)
        val view = nav_view.getHeaderView(0)
        view.sale_badge.visibility = View.INVISIBLE
        view.update_badge.visibility = View.INVISIBLE
        view.headerImage.setOnClickListener { openImageScreen() }
        view.findViewById<View>(R.id.headerItem).setOnClickListener { openImageScreen() }
        val nameView = view.findViewById<RoboTextView>(R.id.appNameBanner)
        var appName = getString(R.string.app_name)
        if (Module.isPro) {
            appName = getString(R.string.app_name_pro)
        }
        nameView.text = appName.toUpperCase()
        setMenuVisible()
    }

    private fun openImageScreen() {
        startActivity(Intent(this, MainImageActivity::class.java))
    }

    private fun setMenuVisible() {
        val menu = nav_view.menu
        menu.getItem(4).isVisible = Google.getInstance() != null
        menu.getItem(13).isVisible = !Module.isPro && !SuperUtil.isAppInstalled(this, "com.cray.software.justreminderpro")
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (isFiltersVisible) {
            addFilters(listOf(), true)
        } else if (mNoteView != null && mNoteView!!.isNoteVisible) {
            mNoteView?.hideNoteView()
        } else {
            if (isBackPressed) {
                if (System.currentTimeMillis() - pressedTime < PRESS_AGAIN_TIME) {
                    finish()
                } else {
                    isBackPressed = false
                    onBackPressed()
                }
            }
            if (fragment is SettingsFragment) {
                if (beforeSettings != 0) {
                    prevItem = beforeSettings
                    nav_view.setCheckedItem(beforeSettings)
                    openScreen(beforeSettings)
                } else {
                    initStartFragment()
                }
            } else if (fragment is BaseSettingsFragment) {
                super.onBackPressed()
            } else if (!isBackPressed) {
                firstBackPress()
            }
        }
    }

    private fun firstBackPress() {
        isBackPressed = true
        pressedTime = System.currentTimeMillis()
        Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
    }

    private fun showFab() {
        if (fab.visibility != View.VISIBLE) {
            fab.show()
        }
    }

    private fun hideFab() {
        if (fab.visibility == View.VISIBLE) {
            fab.hide()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
            viewModel.parseResults(matches, false)
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
        drawer_layout.closeDrawer(GravityCompat.START)
        Handler().postDelayed({
            if (prevItem == item.itemId && (item.itemId != R.id.nav_feedback || item.itemId != R.id.nav_help && item.itemId != R.id.nav_pro)) {
                return@postDelayed
            }
            openScreen(item.itemId)
            if (item.itemId != R.id.nav_feedback && item.itemId != R.id.nav_help && item.itemId != R.id.nav_pro) {
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
            R.id.nav_calendar -> replaceFragment(CalendarFragment(), getString(R.string.calendar))
            R.id.nav_day_view -> replaceFragment(DayViewFragment(), getString(R.string.events))
            R.id.nav_tasks -> replaceFragment(GoogleTasksFragment(), getString(R.string.google_tasks))
            R.id.nav_groups -> replaceFragment(GroupsFragment(), getString(R.string.groups))
            R.id.nav_map -> replaceFragment(MapFragment(), getString(R.string.map))
            R.id.nav_places -> replaceFragment(PlacesFragment(), getString(R.string.places))
            R.id.nav_backups -> replaceFragment(BackupsFragment(), getString(R.string.backup_files))
            R.id.nav_archive -> replaceFragment(ArchiveFragment(), getString(R.string.trash))
            R.id.nav_settings -> {
                beforeSettings = prevItem
                replaceFragment(SettingsFragment(), getString(R.string.action_settings))
            }
            R.id.nav_feedback -> replaceFragment(FeedbackFragment(), getString(R.string.feedback))
            R.id.nav_help -> replaceFragment(HelpFragment(), getString(R.string.help))
            R.id.nav_pro -> showProDialog()
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
        Dialogues.getDialog(this)
                .setTitle(getString(R.string.buy_pro))
                .setMessage(getString(R.string.pro_advantages) + "\n" +
                        getString(R.string.different_settings_for_birthdays) + "\n" +
                        getString(R.string.additional_reminder) + "\n" +
                        getString(R.string._led_notification_) + "\n" +
                        getString(R.string.led_color_for_each_reminder) + "\n" +
                        getString(R.string.styles_for_marker) + "\n" +
                        getString(R.string.option_for_image_blurring) + "\n" +
                        getString(R.string.additional_app_themes))
                .setPositiveButton(R.string.buy) { dialog, _ ->
                    dialog.dismiss()
                    openMarket()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .create().show()
    }

    override fun onSale(discount: String, expiryDate: String) {
        val expiry = TimeUtil.getFireFormatted(this, expiryDate)
        val view = nav_view.getHeaderView(0)
        if (TextUtils.isEmpty(expiry)) {
            view.sale_badge.visibility = View.INVISIBLE
        } else {
            view.sale_badge.visibility = View.VISIBLE
            view.sale_badge.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
        }
    }

    override fun noSale() {
        val view = nav_view.getHeaderView(0)
        view.sale_badge.visibility = View.INVISIBLE
    }

    override fun onUpdate(version: String) {
        val view = nav_view.getHeaderView(0)
        view.update_badge.visibility = View.VISIBLE
        view.update_badge.text = getString(R.string.update_available) + ": " + version
        view.update_badge.setOnClickListener { SuperUtil.launchMarket(this@MainActivity) }
    }

    override fun noUpdate() {
        val view = nav_view.getHeaderView(0)
        view.update_badge.visibility = View.INVISIBLE
    }

    companion object {

        const val VOICE_RECOGNITION_REQUEST_CODE = 109
        private const val PRESS_AGAIN_TIME = 2000
        private const val CURRENT_SCREEN = "current_screen"
    }
}
