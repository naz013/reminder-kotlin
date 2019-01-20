package com.elementary.tasks.reminder.createEdit

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import com.elementary.tasks.reminder.createEdit.fragments.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_reminder.*
import kotlinx.android.synthetic.main.list_item_navigation.view.*
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class CreateReminderActivity : ThemedActivity(), ReminderInterface {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var conversationViewModel: ConversationViewModel

    private var fragment: TypeFragment? = null

    private var isEditing: Boolean = false
    private var mIsLogged = false
    private var mIsSaving = false
    override var reminder: Reminder = Reminder()
        private set
    override var defGroup: ReminderGroup? = null
    override var canExportToTasks: Boolean = false
    override var canExportToCalendar: Boolean = false

    @Inject
    lateinit var backupTool: BackupTool

    private val mOnTypeSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            prefs.lastUsedReminder = position
            openScreen(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        setContentView(R.layout.activity_create_reminder)
        canExportToTasks = GTasks.getInstance(this)?.isLogged ?: false
        initActionBar()
        initNavigation()
        loadReminder(savedInstanceState)
        if (prefs.hasPinCode && !mIsLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun hasGpsPermission(code: Int): Boolean {
        if (!Permissions.checkPermission(this, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(this, code, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private fun openScreen(position: Int) {
        when (position) {
            DATE -> replaceFragment(DateFragment())
            TIMER -> replaceFragment(TimerFragment())
            WEEK -> replaceFragment(WeekFragment())
            GPS -> if (hasGpsPermission(GPS)) {
                replaceFragment(LocationFragment())
            } else {
                navSpinner.setSelection(DATE)
            }
            SKYPE -> replaceFragment(SkypeFragment())
            APP -> replaceFragment(ApplicationFragment())
            MONTH -> replaceFragment(MonthFragment())
            SHOP -> replaceFragment(ShopFragment())
            EMAIL -> if (Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
                replaceFragment(EmailFragment())
            } else {
                navSpinner.setSelection(DATE)
                Permissions.requestPermission(this, CONTACTS_REQUEST_E, Permissions.READ_CONTACTS)
            }
            GPS_PLACE -> if (hasGpsPermission(GPS_PLACE)) {
                replaceFragment(PlacesFragment())
            } else {
                navSpinner.setSelection(DATE)
            }
            YEAR -> replaceFragment(YearFragment())
        }
    }

    private fun initViewModel(id: String) {
        conversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)

        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null && !mIsSaving) {
                editReminder(reminder)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    else -> {
                    }
                }
            }
        })
        viewModel.allGroups.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                defGroup = it[0]
                showGroup(it[0])
            }
        })
    }

    private fun loadReminder(savedInstanceState: Bundle?) {
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        val date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        initViewModel(id)
        when {
            savedInstanceState != null -> {
                editReminder(savedInstanceState.getSerializable(ARG_ITEM) as Reminder? ?: reminder, false)
            }
            id != "" -> {
                isEditing = true
                updateDefaultButton()
            }
            date != 0L -> {
                reminder.type = Reminder.BY_DATE
                reminder.eventTime = TimeUtil.getGmtFromDateTime(date)
                editReminder(reminder, false)
            }
            intent.data != null -> try {
                val uri = intent.data ?: return
                val scheme = uri.scheme
                reminder = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getReminder(cr, uri) ?: Reminder()
                } else {
                    backupTool.getReminder(uri.path, null) ?: Reminder()
                }
                editReminder(reminder, false)
            } catch (e: IOException) {
                Timber.d("loadReminder: ${e.message}")
            } catch (e: IllegalStateException) {
                Timber.d("loadReminder: ${e.message}")
            }
            else -> {
                var lastPos = prefs.lastUsedReminder
                if (lastPos >= navSpinner.adapter.count) lastPos = 0
                navSpinner.setSelection(lastPos)
            }
        }
    }

    private fun editReminder(reminder: Reminder, stop: Boolean = true) {
        Timber.d("editReminder: ")
        this.reminder = reminder
        if (stop) viewModel.pauseReminder(reminder)
        else {
            val group = defGroup
            if (reminder.groupUuId.isBlank() && group != null) {
                this.reminder.groupUuId = group.groupUuId
                this.reminder.groupColor = group.groupColor
                this.reminder.groupTitle = group.groupTitle
            }
        }
        val current = navSpinner.selectedItemPosition
        var toSelect = 0
        when (reminder.type) {
            Reminder.BY_DATE, Reminder.BY_DATE_CALL, Reminder.BY_DATE_SMS -> toSelect = DATE
            Reminder.BY_TIME -> toSelect = TIMER
            Reminder.BY_WEEK, Reminder.BY_WEEK_CALL, Reminder.BY_WEEK_SMS -> toSelect = WEEK
            Reminder.BY_LOCATION, Reminder.BY_LOCATION_CALL, Reminder.BY_LOCATION_SMS,
            Reminder.BY_OUT_SMS, Reminder.BY_OUT_CALL, Reminder.BY_OUT -> toSelect = GPS
            Reminder.BY_SKYPE, Reminder.BY_SKYPE_CALL, Reminder.BY_SKYPE_VIDEO -> toSelect = SKYPE
            Reminder.BY_DATE_APP, Reminder.BY_DATE_LINK -> toSelect = APP
            Reminder.BY_MONTH, Reminder.BY_MONTH_CALL, Reminder.BY_MONTH_SMS -> toSelect = MONTH
            Reminder.BY_DATE_SHOP -> toSelect = SHOP
            Reminder.BY_DATE_EMAIL -> toSelect = EMAIL
            Reminder.BY_DAY_OF_YEAR, Reminder.BY_DAY_OF_YEAR_CALL, Reminder.BY_DAY_OF_YEAR_SMS -> toSelect = YEAR
            else -> if (Module.isPro) {
                when (reminder.type) {
                    Reminder.BY_PLACES, Reminder.BY_PLACES_SMS, Reminder.BY_PLACES_CALL -> toSelect = GPS_PLACE
                }
            }
        }
        if (current == toSelect) {
            openScreen(toSelect)
        } else {
            navSpinner.setSelection(toSelect)
        }
    }

    private fun initNavigation() {
        updateDefaultButton()

        val arrayAdapter = ArrayList<SpinnerItem>()
        arrayAdapter.add(SpinnerItem(getString(R.string.by_date)))
        arrayAdapter.add(SpinnerItem(getString(R.string.timer)))
        arrayAdapter.add(SpinnerItem(getString(R.string.alarm)))
        arrayAdapter.add(SpinnerItem(getString(R.string.location)))
        arrayAdapter.add(SpinnerItem(getString(R.string.skype)))
        arrayAdapter.add(SpinnerItem(getString(R.string.launch_application)))
        arrayAdapter.add(SpinnerItem(getString(R.string.day_of_month)))
        arrayAdapter.add(SpinnerItem(getString(R.string.yearly)))
        arrayAdapter.add(SpinnerItem(getString(R.string.shopping_list)))
        arrayAdapter.add(SpinnerItem(getString(R.string.e_mail)))
        if (Module.isPro) {
            arrayAdapter.add(SpinnerItem(getString(R.string.places)))
        }
        val adapter = TitleNavigationAdapter(arrayAdapter)
        navSpinner.adapter = adapter
        navSpinner.onItemSelectedListener = mOnTypeSelectListener
        Timber.d("initNavigation: ")
    }

    private fun updateDefaultButton() {
        buttonDefault.setOnLongClickListener {
            Toast.makeText(this, getString(R.string.clear_all_changes), Toast.LENGTH_SHORT).show()
            true
        }
        buttonDefault.setOnClickListener {
            reminder = Reminder()
            editReminder(reminder, false)
        }
        if (isEditing) {
            buttonDefault.visibility = View.INVISIBLE
        } else {
            buttonDefault.visibility = View.VISIBLE
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun changeGroup() {
        val groups = viewModel.groups
        val names = groups.map { it.groupTitle }
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.choose_group)
        builder.setSingleChoiceItems(ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, names), names.indexOf(reminder.groupTitle)) { dialog, which ->
            dialog.dismiss()
            showGroup(groups[which])
        }
        val alert = builder.create()
        alert.show()
    }

    private fun showGroup(item: ReminderGroup?) {
        if (item == null) return
        val frag = fragment ?: return
        frag.onGroupUpdate(item)
    }

    private fun openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true, prefs, language)
    }

    private fun replaceFragment(fragment: TypeFragment) {
        Timber.d("replaceFragment: ")
        this.fragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment, null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commitAllowingStateLoss()
        Timber.d("replaceFragment: done")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                save()
                return true
            }
            R.id.action_voice -> {
                openRecognizer()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteReminder()
                return true
            }
            android.R.id.home -> {
                closeScreen()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun selectMelody() {
        if (Permissions.ensurePermissions(this,330, Permissions.READ_EXTERNAL)) {
            startActivityForResult(Intent(this, FileExplorerActivity::class.java),
                    Constants.REQUEST_CODE_SELECTED_MELODY)
        }
    }

    override fun attachFile() {
        if (Permissions.ensurePermissions(this, 331, Permissions.READ_EXTERNAL)) {
            startActivityForResult(Intent(this, FileExplorerActivity::class.java)
                    .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST)
        }
    }

    private fun closeScreen() {
        if (isEditing) {
            if (!reminder.isActive) {
                viewModel.resumeReminder(reminder)
            }
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun deleteReminder() {
        if (reminder.isRemoved) {
            viewModel.deleteReminder(reminder, true)
        } else {
            viewModel.moveToTrash(reminder)
        }
    }

    private fun save() {
        if (fragment != null) {
            val reminder = fragment?.prepare()
            if (reminder != null) {
                mIsSaving = true
                Timber.d("save: %s", reminder)
                viewModel.saveAndStartReminder(reminder, isEditing)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_reminder, menu)
        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_mic_24px, isDark)
        if (isEditing) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            }
        } else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null) {
                val model = conversationViewModel.findResults(matches)
                if (model != null) {
                    editReminder(model, false)
                } else {
                    val text = matches[0].toString()
                    fragment?.onVoiceAction(StringUtils.capitalize(text))
                }
            }
        }
        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY && resultCode == Activity.RESULT_OK) {
            val melodyPath = data!!.getStringExtra(Constants.FILE_PICKED)
            fragment?.onMelodySelect(melodyPath)
            showCurrentMelody()
        }
        if (requestCode == FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            val attachment = data!!.getStringExtra(Constants.FILE_PICKED)
            if (attachment != "") {
                fragment?.onAttachmentSelect(attachment)
                showAttachmentSnack()
            }
        }
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun showAttachmentSnack() {
        val file = File(reminder.attachmentFile)
        showSnackbar(String.format(getString(R.string.file_x_attached), file.name),
                getString(R.string.cancel), View.OnClickListener {
            fragment?.onAttachmentSelect("")
        })
    }

    private fun showCurrentMelody() {
        val musicFile = File(reminder.melodyPath)
        showSnackbar(String.format(getString(R.string.melody_x), musicFile.name),
                getString(R.string.delete), View.OnClickListener { removeMelody() })
    }

    private fun removeMelody() {
        fragment?.onMelodySelect("")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CONTACTS_REQUEST_E -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navSpinner.setSelection(EMAIL)
            } else {
                navSpinner.setSelection(DATE)
            }
            GPS_PLACE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navSpinner.setSelection(GPS_PLACE)
            } else {
                navSpinner.setSelection(DATE)
            }
            GPS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navSpinner.setSelection(GPS)
            } else {
                navSpinner.setSelection(DATE)
            }
            331 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent(this, FileExplorerActivity::class.java)
                        .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST)
            }
        }
    }

    override fun selectGroup() {
        changeGroup()
    }

    override fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener) {
        Snackbar.make(main_container, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener).show()
    }

    override fun showSnackbar(title: String) {
        Snackbar.make(main_container, title, Snackbar.LENGTH_SHORT).show()
    }

    override fun setFullScreenMode(b: Boolean) {
        if (b) {
            appBar.visibility = View.GONE
        } else {
            appBar.visibility = View.VISIBLE
        }
    }

    override fun updateScroll(y: Int) {
        appBar.isSelected = y > 0
    }

    override fun onDestroy() {
        super.onDestroy()
        UpdatesHelper.updateWidget(this)
        UpdatesHelper.updateCalendarWidget(this)
    }

    override fun onBackPressed() {
        if (fragment != null && fragment!!.onBackPressed()) {
            closeScreen()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, reminder)
        super.onSaveInstanceState(outState)
    }

    private class SpinnerItem internal constructor(val title: String)

    private inner class TitleNavigationAdapter(private val spinnerNavItem: ArrayList<SpinnerItem>) : BaseAdapter() {

        override fun getCount(): Int {
            return spinnerNavItem.size
        }

        override fun getItem(index: Int): Any {
            return spinnerNavItem[index]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var cView = convertView
            if (cView == null) {
                cView = layoutInflater.inflate(R.layout.list_item_navigation, null)!!
            }
            cView.txtTitle.text = spinnerNavItem[position].title
            return cView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var cView = convertView
            if (cView == null) {
                cView = layoutInflater.inflate(R.layout.list_item_navigation, null)!!
            }
            cView.txtTitle.text = spinnerNavItem[position].title
            return cView
        }
    }

    companion object {

        private const val DATE = 0
        private const val TIMER = 1
        private const val WEEK = 2
        private const val GPS = 3
        private const val SKYPE = 4
        private const val APP = 5
        private const val MONTH = 6
        private const val YEAR = 7
        private const val SHOP = 8
        private const val EMAIL = 9
        private const val GPS_PLACE = 10

        private const val VOICE_RECOGNITION_REQUEST_CODE = 109
        private const val MENU_ITEM_DELETE = 12
        private const val CONTACTS_REQUEST_E = 501
        private const val FILE_REQUEST = 323

        private const val ARG_ITEM = "arg_item"
        private const val ARG_LOGGED = "arg_logged"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, CreateReminderActivity::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }
    }
}
