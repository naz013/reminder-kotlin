package com.elementary.tasks.reminder.create

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityCreateReminderBinding
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import com.elementary.tasks.reminder.create.fragments.*
import com.elementary.tasks.reminder.create.selector.OptionsAdapter
import com.elementary.tasks.reminder.create.selector.OptionsFactory
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.util.*

class CreateReminderActivity : BindingActivity<ActivityCreateReminderBinding>(R.layout.activity_create_reminder), ReminderInterface {

    private val cacheUtil: CacheUtil by inject()

    private val viewModel: ReminderViewModel by lazy {
        ViewModelProviders.of(this, ReminderViewModel.Factory(getId())).get(ReminderViewModel::class.java)
    }
    private val conversationViewModel: ConversationViewModel by lazy {
        ViewModelProviders.of(this).get(ConversationViewModel::class.java)
    }
    private val stateViewModel: StateViewModel by lazy {
        ViewModelProviders.of(this).get(StateViewModel::class.java)
    }
    private val optionsAdapter = OptionsAdapter()

    private var fragment: TypeFragment<*>? = null
    private var isEditing: Boolean = false
    private var mIsTablet = false
    private var hasLocation = false
    override val state: StateViewModel
        get() = stateViewModel
    override val defGroup: ReminderGroup?
        get() = stateViewModel.group
    override var canExportToTasks: Boolean = false
    override var canExportToCalendar: Boolean = false

    private val mReminderObserver: Observer<in Reminder> = Observer { reminder ->
        if (reminder != null) {
            editReminder(reminder)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasLocation = Module.hasLocation(this)
        mIsTablet = resources.getBoolean(R.bool.is_tablet)
        canExportToCalendar = prefs.isCalendarEnabled || prefs.isStockCalendarEnabled
        canExportToTasks = GTasks.getInstance(this)?.isLogged ?: false
        initActionBar()
        initNavigation()

        if (savedInstanceState == null) {
            stateViewModel.reminder.priority = prefs.defaultPriority
            stateViewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        }

        loadReminder()
    }

    override fun onStart() {
        super.onStart()
        if (prefs.hasPinCode && !stateViewModel.isLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun hasGpsPermission(code: Int): Boolean {
        if (!Permissions.checkPermission(this, code, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            return false
        }
        return true
    }

    private fun openScreen(key: String) {
        when (key) {
            OptionsFactory.BY_DATE -> replaceFragment(DateFragment())
            OptionsFactory.BY_TIMER -> replaceFragment(TimerFragment())
            OptionsFactory.BY_WEEK -> replaceFragment(WeekFragment())
            OptionsFactory.BY_DATE_EMAIL -> if (Permissions.checkPermission(this, CONTACTS_REQUEST_E, Permissions.READ_CONTACTS)) {
                replaceFragment(EmailFragment())
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
            OptionsFactory.BY_DATE_SKYPE -> replaceFragment(SkypeFragment())
            OptionsFactory.BY_DATE_APP -> replaceFragment(ApplicationFragment())
            OptionsFactory.BY_MONTH -> replaceFragment(MonthFragment())
            OptionsFactory.BY_DATE_SHOP -> replaceFragment(ShopFragment())
            OptionsFactory.BY_YEAR -> replaceFragment(YearFragment())
            OptionsFactory.BY_LOCATION -> if (hasGpsPermission(GPS)) {
                replaceFragment(LocationFragment())
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
            OptionsFactory.BY_PLACE -> if (hasGpsPermission(GPS_PLACE)) {
                replaceFragment(PlacesTypeFragment())
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
        }
    }

    private fun initViewModel() {
        viewModel.reminder.observe(this, mReminderObserver)
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
                stateViewModel.group = it[0]
                showGroup(it[0])
            }
        })

        stateViewModel.options.observe(this, Observer {
            if (it != null) {
                optionsAdapter.setOptions(it)
            }
        })
    }

    private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

    private fun loadReminder() {
        val id = getId()
        val date = intent.getLongExtra(Constants.INTENT_DATE, 0)
        initViewModel()
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent)
                }
            }
            id != "" -> {
                isEditing = true
            }
            date != 0L -> {
                stateViewModel.reminder.type = Reminder.BY_DATE
                stateViewModel.reminder.eventTime = TimeUtil.getGmtFromDateTime(date)
                editReminder(stateViewModel.reminder, false)
            }
            intent.data != null -> {
                readFromIntent()
            }
            intent.hasExtra(Constants.INTENT_ITEM) -> {
                try {
                    val reminder = intent.getParcelableExtra(Constants.INTENT_ITEM) as Reminder? ?: Reminder()
                    editReminder(reminder, false, fromFile = true)
                } catch (e: Exception) {
                }
            }
            else -> {

            }
        }
    }

    private fun readFromIntent() {
        if (Permissions.checkPermission(this, SD_PERM, Permissions.READ_EXTERNAL)) {
            intent.data?.let {
                try {
                    var fromFile = false
                    val reminder = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
                        val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_REMINDER)
                        if (any != null && any is Reminder) {
                            fromFile = true
                            any
                        } else {
                            Reminder()
                        }
                    } else Reminder()
                    editReminder(reminder, false, fromFile)
                } catch (e: java.lang.Exception) {
                    Timber.d("loadReminder: ${e.message}")
                }
            }
        }
    }

    private fun editReminder(reminder: Reminder, stop: Boolean = true, fromFile: Boolean = false) {
        Timber.d("editReminder: $stop, $reminder")
        stateViewModel.reminder = reminder
        stateViewModel.isFromFile = fromFile
        if (fromFile) {
            viewModel.findSame(reminder.uuId)
        }
        if (stop) {
            viewModel.pauseReminder(reminder)
            stateViewModel.original = reminder
            stateViewModel.isPaused = true
        } else {
            val group = defGroup
            if (reminder.groupUuId.isBlank() && group != null) {
                stateViewModel.reminder.groupUuId = group.groupUuId
                stateViewModel.reminder.groupColor = group.groupColor
                stateViewModel.reminder.groupTitle = group.groupTitle
            }
        }
    }

    private fun initNavigation() {
        binding.optionsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.optionsList.adapter = optionsAdapter
        optionsAdapter.selectListener = { _, option ->
            prefs.lastUsedReminder = option.key
            binding.toolbar.title = option.name
            openScreen(option.key)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun changeGroup() {
        val groups = viewModel.groups
        val names = groups.map { it.groupTitle }
        val builder = dialogues.getMaterialDialog(this)
        builder.setTitle(R.string.choose_group)
        builder.setSingleChoiceItems(ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, names), names.indexOf(stateViewModel.reminder.groupTitle)) { dialog, which ->
            dialog.dismiss()
            showGroup(groups[which])
        }
        builder.create().show()
    }

    private fun showGroup(item: ReminderGroup?) {
        if (item == null) return
        val frag = fragment ?: return
        frag.onGroupUpdate(item)
    }

    private fun openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true, prefs, language)
    }

    private fun replaceFragment(fragment: TypeFragment<*>) {
        try {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment, null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                askCopySaving()
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
        if (Permissions.checkPermission(this,330, Permissions.READ_EXTERNAL)) {
            cacheUtil.pickMelody(this, Constants.REQUEST_CODE_SELECTED_MELODY)
        }
    }

    override fun attachFile() {
        if (Permissions.checkPermission(this, 331, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            selectAnyFile()
        }
    }

    private fun closeScreen() {
        if (isEditing) {
            if (!stateViewModel.reminder.isActive) {
                viewModel.resumeReminder(stateViewModel.reminder)
            }
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun deleteReminder() {
        if (stateViewModel.reminder.isRemoved) {
            dialogues.askConfirmation(this, getString(R.string.delete)) {
                if (it) viewModel.deleteReminder(stateViewModel.reminder, true)
            }
        } else {
            dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
                if (it) viewModel.moveToTrash(stateViewModel.reminder)
            }
        }
    }

    private fun askCopySaving() {
        if (stateViewModel.isFromFile && viewModel.hasSameInDb) {
            dialogues.getMaterialDialog(this)
                    .setMessage(R.string.same_reminder_message)
                    .setPositiveButton(R.string.keep) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        save(true)
                    }
                    .setNegativeButton(R.string.replace) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        save()
                    }
                    .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
        } else {
            save()
        }
    }

    private fun save(newId: Boolean = false) {
        fragment?.let {
            it.prepare()?.let { item ->
                Timber.d("save: %s", item)
                viewModel.reminder.removeObserver(mReminderObserver)
                stateViewModel.isSaving = true
                if (newId) {
                    item.uuId = UUID.randomUUID().toString()
                }
                viewModel.saveAndStartReminder(item, isEditing)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_reminder, menu)
        if (Module.hasMicrophone(this)) {
            menu[0].isVisible = true
            ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_mic_24px, isDarkMode)
        } else {
            menu[0].isVisible = false
        }
        if (isEditing && !stateViewModel.isFromFile) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            } else {
                stateViewModel.isLogged = true
            }
        } else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null) {
                val model = conversationViewModel.findResults(matches)
                if (model != null) {
                    editReminder(model, false)
                } else {
                    val text = matches[0].toString()
                    fragment?.onVoiceAction(StringUtils.capitalize(text))
                }
            }
        } else if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY && resultCode == Activity.RESULT_OK) {
            if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
                val melodyPath = cacheUtil.cacheFile(data)
                if (melodyPath != null) {
                    fragment?.onMelodySelect(melodyPath)
                    showCurrentMelody()
                }
            }
        } else if (requestCode == FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                fragment?.onAttachmentSelect(it)
            }
        }
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            stateViewModel.reminder.summary = it
            editReminder(stateViewModel.reminder, false)
        }
    }

    private fun showCurrentMelody() {
        val musicFile = File(stateViewModel.reminder.melodyPath)
        showSnackbar(String.format(getString(R.string.melody_x), musicFile.name),
                getString(R.string.delete), View.OnClickListener { removeMelody() })
    }

    private fun removeMelody() {
        fragment?.onMelodySelect("")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CONTACTS_REQUEST_E -> if (Permissions.checkPermission(grantResults)) {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE_EMAIL)
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
            GPS_PLACE -> if (Permissions.checkPermission(grantResults)) {
                optionsAdapter.selectKey(OptionsFactory.BY_PLACE)
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
            GPS -> if (Permissions.checkPermission(grantResults)) {
                optionsAdapter.selectKey(OptionsFactory.BY_LOCATION)
            } else {
                optionsAdapter.selectKey(OptionsFactory.BY_DATE)
            }
            331 -> if (Permissions.checkPermission(grantResults)) {
                selectAnyFile()
            }
            SD_PERM -> if (Permissions.checkPermission(grantResults)) {
                readFromIntent()
            }
        }
    }

    private fun selectAnyFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        try {
            startActivityForResult(intent, FILE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun selectGroup() {
        changeGroup()
    }

    override fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener) {
        Snackbar.make(binding.coordinator, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener).show()
    }

    override fun showSnackbar(title: String) {
        Snackbar.make(binding.coordinator, title, Snackbar.LENGTH_SHORT).show()
    }

    override fun setFullScreenMode(b: Boolean) {
        if (!mIsTablet) {
            if (b) {
                binding.appBar.visibility = View.GONE
            } else {
                binding.appBar.visibility = View.VISIBLE
            }
        }
    }

    override fun updateScroll(y: Int) {
        if (!mIsTablet) binding.appBar.isSelected = y > 0
    }

    override fun isTablet(): Boolean {
        return mIsTablet
    }

    override fun setFragment(typeFragment: TypeFragment<*>?) {
        this.fragment = typeFragment
    }

    override fun onDestroy() {
        super.onDestroy()
        if (stateViewModel.isPaused && !stateViewModel.isSaving) {
            stateViewModel.original?.let { viewModel.resumeReminder(it) }
        }
        UpdatesHelper.updateWidget(this)
        UpdatesHelper.updateCalendarWidget(this)
    }

    override fun onBackPressed() {
        if (fragment != null && fragment?.onBackPressed() == true) {
            closeScreen()
        }
    }

    companion object {

        private const val VOICE_RECOGNITION_REQUEST_CODE = 109
        private const val MENU_ITEM_DELETE = 12
        private const val CONTACTS_REQUEST_E = 501
        private const val FILE_REQUEST = 323
        private const val SD_PERM = 555
        private const val GPS_PLACE = 600
        private const val GPS = 601

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
