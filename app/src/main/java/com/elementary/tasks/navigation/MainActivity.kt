package com.elementary.tasks.navigation

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.async.BackupSettingTask
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.RemotePrefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.core.views.ReturnScrollListener
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ActivityMainBinding
import com.elementary.tasks.groups.list.GroupsFragment
import com.elementary.tasks.navigation.fragments.BackupsFragment
import com.elementary.tasks.navigation.fragments.CalendarFragment
import com.elementary.tasks.navigation.fragments.DayViewFragment
import com.elementary.tasks.navigation.fragments.FeedbackFragment
import com.elementary.tasks.google_tasks.GoogleTasksFragment
import com.elementary.tasks.navigation.fragments.HelpFragment
import com.elementary.tasks.navigation.fragments.MapFragment
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

import java.io.File
import java.util.ArrayList

import androidx.annotation.ColorInt
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ThemedActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentCallback, RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver {

    private var binding: ActivityMainBinding? = null
    private var mMainImageView: ImageView? = null
    private var mSaleBadge: RoboTextView? = null
    private var mUpdateBadge: RoboTextView? = null
    private var mNavigationView: NavigationView? = null
    private var fragment: Fragment? = null
    private var mNoteView: QuickNoteCoordinator? = null
    private var returnScrollListener: ReturnScrollListener? = null
    private var listener: RecyclerView.OnScrollListener? = null
    private var mPrevList: RecyclerView? = null

    private var viewModel: ConversationViewModel? = null

    private var prevItem: Int = 0
    private var beforeSettings: Int = 0
    private var isBackPressed: Boolean = false
    private var pressedTime: Long = 0

    private val mQuickCallback = object : QuickNoteCoordinator.Callback {
        override fun onOpen() {
            binding!!.fab.setImageResource(R.drawable.ic_clear_white_24dp)
        }

        override fun onClose() {
            binding!!.fab.setImageResource(R.drawable.ic_add_white_24dp)
        }
    }

    private val isRateDialogShowed: Boolean
        get() {
            var count = prefs!!.rateCount
            count++
            prefs!!.rateCount = count
            return count == 10
        }

    private val onScrollListener: RecyclerView.OnScrollListener
        get() = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                returnScrollListener!!.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                returnScrollListener!!.onScrolled(recyclerView, dx, dy)
            }
        }

    override val isFiltersVisible: Boolean
        get() = binding!!.filterView.visibility == View.VISIBLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding!!.fab.setOnLongClickListener { view ->
            mNoteView!!.switchQuickNote()
            true
        }
        initActionBar()
        initNavigation()
        mNoteView = QuickNoteCoordinator(this, binding, mQuickCallback)
        if (savedInstanceState != null) {
            openScreen(savedInstanceState.getInt(CURRENT_SCREEN, R.id.nav_current))
        } else if (intent.getIntExtra(Constants.INTENT_POSITION, 0) != 0) {
            prevItem = intent.getIntExtra(Constants.INTENT_POSITION, 0)
            mNavigationView!!.setCheckedItem(prevItem)
            openScreen(prevItem)
        } else {
            initStartFragment()
        }

        initViewModel()
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
        mNavigationView!!.setCheckedItem(prevItem)
        replaceFragment(RemindersFragment(), getString(R.string.events))
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        binding!!.toolbar.setNavigationOnClickListener { v -> onDrawerClick() }
    }

    private fun onDrawerClick() {
        if (this.fragment is BaseSettingsFragment) {
            onBackPressed()
        } else {
            binding!!.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun replaceFragment(fragment: Fragment, title: String) {
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, fragment, title)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(title)
        ft.commit()
        binding!!.toolbar.title = title
    }

    override fun onResume() {
        super.onResume()
        if (prefs!!.isUiChanged) {
            prefs!!.isUiChanged = false
            recreate()
        }
        if (!prefs!!.isBetaWarmingShowed) {
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
        builder.setPositiveButton(R.string.rate) { dialogInterface, i ->
            dialogInterface.dismiss()
            SuperUtil.launchMarket(this@MainActivity)
        }
        builder.setNegativeButton(R.string.never) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setNeutralButton(R.string.later) { dialogInterface, i ->
            dialogInterface.dismiss()
            prefs!!.rateCount = 0
        }
        builder.create().show()
    }

    private fun showBetaDialog() {
        prefs!!.isBetaWarmingShowed = true
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
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun showMainImage() {
        val path = prefs!!.imagePath
        if (path != null && !path.isEmpty() && !path.contains("{")) {
            var fileName: String = path
            if (path.contains("=")) {
                val index = path.indexOf("=")
                fileName = path.substring(index)
            }
            val file = File(MemoryUtil.imageCacheDir, "$fileName.jpg")
            val readPerm = Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            if (readPerm && file.exists()) {
                Glide.with(this).load(file).into(mMainImageView!!)
                mMainImageView!!.visibility = View.VISIBLE
            } else {
                Glide.with(this).load(path).into(mMainImageView!!)
                mMainImageView!!.visibility = View.VISIBLE
                if (readPerm) {
                    SaveAsync(this).execute(path)
                }
            }
        } else {
            mMainImageView!!.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (prefs!!.isAutoBackupEnabled && prefs!!.isSettingsBackupEnabled
                && Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            BackupSettingTask(this).execute()
        }
    }

    override fun onTitleChange(title: String) {
        binding!!.toolbar.title = title
    }

    override fun onFragmentSelect(fragment: Fragment) {
        this.fragment = fragment
        if (this.fragment is BaseSettingsFragment) {
            binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        } else {
            binding!!.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        }
    }

    override fun setClick(listener: View.OnClickListener?) {
        if (listener == null) {
            hideFab()
        } else {
            showFab()
            binding!!.fab.setOnClickListener { view ->
                if (mNoteView!!.isNoteVisible) {
                    mNoteView!!.hideNoteView()
                    return@binding.fab.setOnClickListener
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
            primary = themeUtil!!.getColor(themeUtil!!.colorPrimary())
        }
        if (primaryDark == 0) {
            primaryDark = themeUtil!!.getColor(themeUtil!!.colorPrimaryDark())
        }
        if (accent == 0) {
            accent = themeUtil!!.getColor(themeUtil!!.colorAccent())
        }
        binding!!.toolbar.setBackgroundColor(primary)
        if (Module.isLollipop) {
            window.statusBarColor = primaryDark
        }
        binding!!.fab.backgroundTintList = ViewUtils.getFabState(accent, accent)
    }

    override fun refreshMenu() {
        setMenuVisible()
    }

    override fun onScrollChanged(recyclerView: RecyclerView?) {
        if (listener != null && mPrevList != null) {
            mPrevList!!.removeOnScrollListener(listener!!)
        }
        if (recyclerView != null) {
            returnScrollListener = ReturnScrollListener.Builder(ReturnScrollListener.QuickReturnViewType.FOOTER)
                    .footer(binding!!.fab)
                    .minFooterTranslation(MeasureUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build()
            listener = onScrollListener
            if (Module.isLollipop) {
                recyclerView.addOnScrollListener(listener!!)
            } else {
                recyclerView.setOnScrollListener(listener)
            }
            mPrevList = recyclerView
        }
    }

    override fun addFilters(filters: List<FilterView.Filter>?, clear: Boolean) {
        if (filters == null || filters.size == 0) {
            hideFilters()
            if (clear) {
                binding!!.filterView.clear()
            }
        } else {
            if (clear) {
                binding!!.filterView.clear()
            }
            for (filter in filters) {
                binding!!.filterView.addFilter(filter)
            }
            ViewUtils.expand(binding!!.filterView)
        }
    }

    override fun hideFilters() {
        if (isFiltersVisible) {
            ViewUtils.collapse(binding!!.filterView)
        }
    }

    override fun onVoiceAction() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false)
    }

    override fun onMenuSelect(menu: Int) {
        prevItem = menu
        mNavigationView!!.setCheckedItem(prevItem)
    }

    private fun initNavigation() {
        mNavigationView = binding!!.navView
        mNavigationView!!.setNavigationItemSelectedListener(this)
        val view = mNavigationView!!.getHeaderView(0)
        mSaleBadge = view.findViewById(R.id.sale_badge)
        mUpdateBadge = view.findViewById(R.id.update_badge)
        mSaleBadge!!.visibility = View.INVISIBLE
        mUpdateBadge!!.visibility = View.INVISIBLE
        mMainImageView = view.findViewById(R.id.headerImage)
        mMainImageView!!.setOnClickListener { view1 -> openImageScreen() }
        view.findViewById<View>(R.id.headerItem).setOnClickListener { view12 -> openImageScreen() }
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
        val menu = mNavigationView!!.menu
        menu.getItem(4).isVisible = Google.getInstance(this) != null
        if (!Module.isPro && !SuperUtil.isAppInstalled(this, "com.cray.software.justreminderpro")) {
            menu.getItem(13).isVisible = true
        } else {
            menu.getItem(13).isVisible = false
        }
    }

    override fun onBackPressed() {
        if (binding!!.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (isFiltersVisible) {
            addFilters(null, true)
        } else if (mNoteView!!.isNoteVisible) {
            mNoteView!!.hideNoteView()
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
                    mNavigationView!!.setCheckedItem(beforeSettings)
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
        if (binding!!.fab.visibility != View.VISIBLE) {
            binding!!.fab.show()
        }
    }

    private fun hideFab() {
        if (binding!!.fab.visibility != View.GONE) {
            binding!!.fab.hide()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            viewModel!!.parseResults(matches, false)
        }
        if (fragment != null) {
            fragment!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Module.isMarshmallow && fragment != null) {
            fragment!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        Handler().postDelayed({
            if (prevItem == item.itemId && (item.itemId != R.id.nav_feedback || item.itemId != R.id.nav_help && item.itemId != R.id.nav_pro)) {
                return@new Handler().postDelayed
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
                .setPositiveButton(R.string.buy) { dialog, which ->
                    dialog.dismiss()
                    openMarket()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .setCancelable(true)
                .create().show()
    }

    override fun onSale(discount: String, expiryDate: String) {
        val expiry = TimeUtil.getFireFormatted(this, expiryDate)
        if (TextUtils.isEmpty(expiry)) {
            mSaleBadge!!.visibility = View.INVISIBLE
        } else {
            mSaleBadge!!.visibility = View.VISIBLE
            mSaleBadge!!.text = "SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry
        }
    }

    override fun noSale() {
        mSaleBadge!!.visibility = View.INVISIBLE
    }

    override fun onUpdate(version: String) {
        mUpdateBadge!!.visibility = View.VISIBLE
        mUpdateBadge!!.text = getString(R.string.update_available) + ": " + version
        mUpdateBadge!!.setOnClickListener { v -> SuperUtil.launchMarket(this@MainActivity) }
    }

    override fun noUpdate() {
        mUpdateBadge!!.visibility = View.INVISIBLE
    }

    companion object {

        val VOICE_RECOGNITION_REQUEST_CODE = 109
        private val PRESS_AGAIN_TIME = 2000
        private val CURRENT_SCREEN = "current_screen"
    }
}
