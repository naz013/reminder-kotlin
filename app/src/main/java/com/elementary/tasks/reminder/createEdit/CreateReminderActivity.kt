package com.elementary.tasks.reminder.createEdit

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.fragments.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_reminder.*
import kotlinx.android.synthetic.main.dialog_select_extra.view.*
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.list_item_navigation.view.*
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class CreateReminderActivity : ThemedActivity(), ReminderInterface, View.OnLongClickListener {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var conversationViewModel: ConversationViewModel

    private var fragment: TypeFragment? = null

    override var useGlobal = true
        private set
    override var vibration: Boolean = false
        private set
    override var voice: Boolean = false
        private set
    override var notificationRepeat: Boolean = false
        private set
    override var wake: Boolean = false
        private set
    override var unlock: Boolean = false
        private set
    override var auto: Boolean = false
        private set
    private var hasAutoExtra: Boolean = false
    override var isExportToTasks: Boolean = false
        private set
    override var repeatLimit = -1
    override var volume = -1
        private set
    override var group: Group? = null
        private set
    override var melodyPath: String = ""
        private set
    private var autoLabel: String? = null
    override var ledColor = -1
        private set
    private var isEditing: Boolean = false
    override var attachment: String = ""
        private set

    override var reminder: Reminder? = null
        private set

    @Inject
    lateinit var updatesHelper: UpdatesHelper
    @Inject
    lateinit var backupTool: BackupTool

    private val mOnTypeSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            prefs.lastUsedReminder = position
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
                GPS_OUT -> if (hasGpsPermission(GPS_OUT)) {
                    replaceFragment(LocationOutFragment())
                } else {
                    navSpinner.setSelection(DATE)
                }
                SHOP -> replaceFragment(ShopFragment())
                EMAIL -> if (Permissions.checkPermission(this@CreateReminderActivity, Permissions.READ_CONTACTS)) {
                    replaceFragment(EmailFragment())
                } else {
                    Permissions.requestPermission(this@CreateReminderActivity, CONTACTS_REQUEST_E, Permissions.READ_CONTACTS)
                }
                GPS_PLACE -> if (hasGpsPermission(GPS_PLACE)) {
                    replaceFragment(PlacesFragment())
                } else {
                    navSpinner.setSelection(DATE)
                }
                YEAR -> replaceFragment(YearFragment())
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    private val customizationView: View
        get() {
            val binding = layoutInflater.inflate(R.layout.dialog_select_extra, null)
            binding.extraSwitch.setOnCheckedChangeListener { _, isChecked ->
                binding.autoCheck.isEnabled = !isChecked
                binding.repeatCheck.isEnabled = !isChecked
                binding.unlockCheck.isEnabled = !isChecked
                binding.vibrationCheck.isEnabled = !isChecked
                binding.voiceCheck.isEnabled = !isChecked
                binding.wakeCheck.isEnabled = !isChecked
            }
            binding.voiceCheck.isChecked = voice
            binding.vibrationCheck.isChecked = vibration
            binding.unlockCheck.isChecked = unlock
            binding.repeatCheck.isChecked = notificationRepeat
            binding.autoCheck.isChecked = auto
            binding.wakeCheck.isChecked = wake
            binding.extraSwitch.isChecked = useGlobal
            binding.autoCheck.isEnabled = !useGlobal
            binding.repeatCheck.isEnabled = !useGlobal
            binding.unlockCheck.isEnabled = !useGlobal
            binding.vibrationCheck.isEnabled = !useGlobal
            binding.voiceCheck.isEnabled = !useGlobal
            binding.wakeCheck.isEnabled = !useGlobal
            if (hasAutoExtra && autoLabel != null) {
                binding.autoCheck.visibility = View.VISIBLE
                binding.autoCheck.text = autoLabel
            } else {
                binding.autoCheck.visibility = View.GONE
            }
            return binding
        }

    override val summary: String
        get() = taskSummary.text.toString().trim { it <= ' ' }

    override val windowType: Int
        get() = if (window_type_switch.isChecked) 1 else 0

    override val isExportToCalendar: Boolean
        get() = prefs.isCalendarEnabled || prefs.isStockCalendarEnabled

    init {
        ReminderApp.appComponent.inject(this)
    }

    private fun hasGpsPermission(code: Int): Boolean {
        if (!Permissions.checkPermission(this@CreateReminderActivity, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(this@CreateReminderActivity, code, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_reminder)
        isExportToTasks = Google.getInstance() != null
        initActionBar()
        initNavigation()
        initLongClick()
        loadReminder()
    }

    private fun initViewModel(id: String) {
        conversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)

        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                editReminder(reminder)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> finish()
                }
            }
        })
        viewModel.allGroups.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                showGroup(it[0])
            }
        })
    }

    private fun initLongClick() {
        customButton.setOnLongClickListener(this)
        groupButton.setOnLongClickListener(this)
        voiceButton.setOnLongClickListener(this)
        exclusionButton.setOnLongClickListener(this)
        melodyButton.setOnLongClickListener(this)
        repeatButton.setOnLongClickListener(this)
    }

    private fun loadReminder() {
        val intent = intent
        val id = getIntent().getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (id != "") {
            isEditing = true
        } else if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name!!.scheme
                reminder = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getReminder(cr, name)
                } else {
                    backupTool.getReminder(name.path, null)
                }
            } catch (e: IOException) {
                LogUtil.d(TAG, "loadReminder: " + e.localizedMessage)
            } catch (e: IllegalStateException) {
                LogUtil.d(TAG, "loadReminder: " + e.localizedMessage)
            }
        }
    }

    private fun editReminder(reminder: Reminder) {
        this.reminder = reminder
        viewModel.pauseReminder(reminder)
        taskSummary.setText(reminder.summary)
        showGroup(reminder.group)
        attachment = reminder.attachmentFile
        if (!TextUtils.isEmpty(attachment)) {
            attachmentButton.visibility = View.VISIBLE
        }
        window_type_switch.isChecked = reminder.windowType == 1
        initParams(reminder)
        when (reminder.type) {
            Reminder.BY_DATE, Reminder.BY_DATE_CALL, Reminder.BY_DATE_SMS -> navSpinner.setSelection(DATE)
            Reminder.BY_TIME -> navSpinner.setSelection(TIMER)
            Reminder.BY_WEEK, Reminder.BY_WEEK_CALL, Reminder.BY_WEEK_SMS -> navSpinner.setSelection(WEEK)
            Reminder.BY_LOCATION, Reminder.BY_LOCATION_CALL, Reminder.BY_LOCATION_SMS -> navSpinner.setSelection(GPS)
            Reminder.BY_SKYPE, Reminder.BY_SKYPE_CALL, Reminder.BY_SKYPE_VIDEO -> navSpinner.setSelection(SKYPE)
            Reminder.BY_DATE_APP, Reminder.BY_DATE_LINK -> navSpinner.setSelection(APP)
            Reminder.BY_MONTH, Reminder.BY_MONTH_CALL, Reminder.BY_MONTH_SMS -> navSpinner.setSelection(MONTH)
            Reminder.BY_OUT, Reminder.BY_OUT_SMS, Reminder.BY_OUT_CALL -> navSpinner.setSelection(GPS_OUT)
            Reminder.BY_DATE_SHOP -> navSpinner.setSelection(SHOP)
            Reminder.BY_DATE_EMAIL -> navSpinner.setSelection(EMAIL)
            Reminder.BY_DAY_OF_YEAR, Reminder.BY_DAY_OF_YEAR_CALL, Reminder.BY_DAY_OF_YEAR_SMS -> navSpinner.setSelection(YEAR)
            else -> if (Module.isPro) {
                when (reminder.type) {
                    Reminder.BY_PLACES, Reminder.BY_PLACES_SMS, Reminder.BY_PLACES_CALL -> navSpinner.setSelection(GPS_PLACE)
                }
            }
        }
    }

    private fun initParams(reminder: Reminder) {
        useGlobal = reminder.useGlobal
        auto = reminder.auto
        wake = reminder.awake
        unlock = reminder.unlock
        notificationRepeat = reminder.repeatNotification
        voice = reminder.notifyByVoice
        vibration = reminder.vibrate
        volume = reminder.volume
        repeatLimit = reminder.repeatLimit
        melodyPath = reminder.melodyPath
        ledColor = reminder.color
        updateMelodyIndicator()
    }

    private fun updateMelodyIndicator() {
        if (melodyPath != "") {
            melodyButton.visibility = View.VISIBLE
        } else {
            melodyButton.visibility = View.GONE
        }
    }

    private fun initNavigation() {
        val arrayAdapter = ArrayList<SpinnerItem>()
        arrayAdapter.add(SpinnerItem(getString(R.string.by_date)))
        arrayAdapter.add(SpinnerItem(getString(R.string.timer)))
        arrayAdapter.add(SpinnerItem(getString(R.string.alarm)))
        arrayAdapter.add(SpinnerItem(getString(R.string.location)))
        arrayAdapter.add(SpinnerItem(getString(R.string.skype)))
        arrayAdapter.add(SpinnerItem(getString(R.string.launch_application)))
        arrayAdapter.add(SpinnerItem(getString(R.string.day_of_month)))
        arrayAdapter.add(SpinnerItem(getString(R.string.yearly)))
        arrayAdapter.add(SpinnerItem(getString(R.string.place_out)))
        arrayAdapter.add(SpinnerItem(getString(R.string.shopping_list)))
        arrayAdapter.add(SpinnerItem(getString(R.string.e_mail)))
        if (Module.isPro) {
            arrayAdapter.add(SpinnerItem(getString(R.string.places)))
        }
        val adapter = TitleNavigationAdapter(arrayAdapter)
        navSpinner.adapter = adapter
        navSpinner.onItemSelectedListener = mOnTypeSelectListener
        var lastPos = prefs.lastUsedReminder
        if (lastPos >= arrayAdapter.size) lastPos = 0
        navSpinner.setSelection(lastPos)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        voiceButton.setOnClickListener { openRecognizer() }
        customButton.setOnClickListener { openCustomizationDialog() }
        groupButton.setOnClickListener { changeGroup() }
        melodyButton.setOnClickListener { showCurrentMelody() }
        attachmentButton.setOnClickListener { showAttachmentSnack() }
    }

    private fun changeGroup() {
        val groups = viewModel.allGroups.value
        val names = groups?.map { it.title } ?: listOf()
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.choose_group)
        builder.setSingleChoiceItems(ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, names), names.indexOf(group?.title ?: "")) { dialog, which ->
            dialog.dismiss()
            if (groups != null) {
                showGroup(groups[which])
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun showGroup(item: Group?) {
        if (item == null) return
        groupButton.text = item.title
        group = item
    }

    private fun openCustomizationDialog() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.personalization)
        val b = customizationView
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> saveExtraResults(b) }
        builder.create().show()
    }

    private fun saveExtraResults(b: View) {
        useGlobal = b.extraSwitch.isChecked
        auto = b.autoCheck.isChecked
        wake = b.wakeCheck.isChecked
        unlock = b.unlockCheck.isChecked
        notificationRepeat = b.repeatCheck.isChecked
        voice = b.voiceCheck.isChecked
        vibration = b.vibrationCheck.isChecked
    }

    private fun openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true, prefs, language)
    }

    fun replaceFragment(fragment: TypeFragment) {
        this.fragment = fragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_container, fragment, null)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                save()
                return true
            }
            R.id.action_custom_melody -> {
                if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
                    startActivityForResult(Intent(this, FileExplorerActivity::class.java),
                            Constants.REQUEST_CODE_SELECTED_MELODY)
                } else {
                    Permissions.requestPermission(this, 330, Permissions.READ_EXTERNAL)
                }
                return true
            }
            R.id.action_custom_color -> {
                chooseLedColor()
                return true
            }
            R.id.action_volume -> {
                selectVolume()
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
            R.id.action_attach_file -> {
                attachFile()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun attachFile() {
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
            startActivityForResult(Intent(this, FileExplorerActivity::class.java)
                    .putExtra(Constants.FILE_TYPE, "any"), FILE_REQUEST)
        } else {
            Permissions.requestPermission(this, 331, Permissions.READ_EXTERNAL)
        }
    }

    private fun closeScreen() {
        val rem = reminder
        if (rem != null && prefs.isAutoSaveEnabled) {
            if (!rem.isActive) {
                askAboutEnabling()
            } else {
                save()
            }
        } else if (isEditing && rem != null) {
            if (!rem.isActive) {
                viewModel.resumeReminder(rem)
            }
            finish()
        } else {
            finish()
        }
    }

    private fun deleteReminder() {
        val rem = reminder
        if (rem != null) {
            if (rem.isRemoved) {
                viewModel.deleteReminder(rem, true)
            } else {
                viewModel.moveToTrash(rem)
            }
        }
    }

    private fun selectVolume() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.loudness)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
        b.seekBar.max = 26
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = getVolumeTitle(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = volume + 1
        b.titleView.text = getVolumeTitle(b.seekBar.progress)
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            volume = b.seekBar.progress - 1
            val str = String.format(getString(R.string.selected_loudness_x_for_reminder), getVolumeTitle(b.seekBar.progress))
            showSnackbar(str, getString(R.string.cancel), View.OnClickListener { volume = -1 })
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun getVolumeTitle(progress: Int): String {
        return if (progress == 0) {
            getString(R.string.default_string)
        } else {
            (progress - 1).toString()
        }
    }

    private fun chooseLedColor() {
        val builder = dialogues.getDialog(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.led_color))
        val colors = arrayOfNulls<String>(LED.NUM_OF_LEDS)
        for (i in 0 until LED.NUM_OF_LEDS) {
            colors[i] = LED.getTitle(this, i)
        }
        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, colors)
        builder.setSingleChoiceItems(adapter, ledColor) { dialog, which ->
            if (which != -1) {
                ledColor = which
                val selColor = LED.getTitle(this, which)
                val str = String.format(getString(R.string.led_color_x), selColor)
                showSnackbar(str, getString(R.string.cancel), View.OnClickListener { ledColor = -1 })
                dialog.dismiss()
            }
        }
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton(R.string.disable) { dialog, _ ->
            ledColor = -1
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun askAboutEnabling() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.this_reminder_is_disabled)
        builder.setMessage(R.string.would_you_like_to_enable_it)
        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
            save()
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        builder.create().show()
    }

    private fun save() {
        if (fragment != null) {
            val reminder = fragment?.prepare()
            if (reminder != null) {
                Timber.d("save: %s", reminder)
                viewModel.saveAndStartReminder(reminder)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_reminder, menu)
        if (reminder != null && isEditing) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null) {
                val model = conversationViewModel.findResults(matches)
                if (model != null) {
                    processModel(model)
                } else {
                    val text = matches[0].toString()
                    taskSummary.setText(StringUtils.capitalize(text))
                }
            }
        }
        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY && resultCode == Activity.RESULT_OK) {
            melodyPath = data!!.getStringExtra(Constants.FILE_PICKED)
            updateMelodyIndicator()
            showCurrentMelody()
        }
        if (requestCode == FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            attachment = data!!.getStringExtra(Constants.FILE_PICKED)
            if (attachment != "") {
                attachmentButton.visibility = View.VISIBLE
                showAttachmentSnack()
            }
        }
        if (fragment != null) {
            fragment!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showAttachmentSnack() {
        val file = File(attachment)
        showSnackbar(String.format(getString(R.string.file_x_attached), file.name),
                getString(R.string.cancel), View.OnClickListener {
            attachment = ""
            attachmentButton.visibility = View.GONE
        })
    }

    private fun showCurrentMelody() {
        if (melodyPath != "") {
            val musicFile = File(melodyPath)
            showSnackbar(String.format(getString(R.string.melody_x), musicFile.name),
                    getString(R.string.delete), View.OnClickListener { removeMelody() })
        }
    }

    private fun removeMelody() {
        melodyPath = ""
        updateMelodyIndicator()
    }

    private fun processModel(model: Reminder) {
        this.reminder = model
        editReminder(model)
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
            GPS_OUT -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navSpinner.setSelection(GPS_OUT)
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

    override fun onResume() {
        super.onResume()
        showShowcase()
    }

    private fun showShowcase() {
        if (!prefs.isShowcase(SHOWCASE)) {
            prefs.setShowcase(SHOWCASE, true)
            //            ShowcaseConfig config = new ShowcaseConfig();
            //            config.setDelay(350);
            //            config.setMaskColor(getThemeUtil().getColor(getThemeUtil().colorAccent()));
            //            config.setContentTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            //            config.setDismissTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            //            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
            //            sequence.setConfig(config);
            //            sequence.addSequenceItem(binding.navSpinner,
            //                    getString(R.string.click_to_select_reminder_type),
            //                    getString(R.string.got_it));
            //            sequence.addSequenceItem(binding.voiceButton,
            //                    getString(R.string.to_insert_task_by_voice),
            //                    getString(R.string.got_it));
            //            sequence.addSequenceItem(binding.customButton,
            //                    getString(R.string.click_to_customize),
            //                    getString(R.string.got_it));
            //            sequence.addSequenceItem(binding.groupButton,
            //                    getString(R.string.click_to_change_reminder_group),
            //                    getString(R.string.got_it));
            //            sequence.start();
        }
    }

    override fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener) {
        Snackbar.make(main_container, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener).show()
    }

    override fun showSnackbar(title: String) {
        Snackbar.make(main_container, title, Snackbar.LENGTH_SHORT).show()
    }

    override fun setEventHint(hint: String) {
        taskSummary.hint = hint
    }

    override fun setExclusionAction(listener: View.OnClickListener?) {
        if (listener == null) {
            exclusionButton.visibility = View.GONE
        } else {
            exclusionButton.visibility = View.VISIBLE
            exclusionButton.setOnClickListener(listener)
        }
    }

    override fun setRepeatAction(listener: View.OnClickListener?) {
        if (listener == null) {
            repeatButton.visibility = View.GONE
        } else {
            repeatButton.visibility = View.VISIBLE
            repeatButton.setOnClickListener(listener)
        }
    }

    override fun setFullScreenMode(b: Boolean) {
        if (b) {
            ViewUtils.collapse(toolbar)
        } else {
            ViewUtils.expand(toolbar)
        }
    }

    override fun setHasAutoExtra(hasAutoExtra: Boolean, label: String) {
        this.hasAutoExtra = hasAutoExtra
        this.autoLabel = label
    }

    override fun onDestroy() {
        super.onDestroy()
        updatesHelper.updateWidget()
        updatesHelper.updateCalendarWidget()
    }

    override fun onBackPressed() {
        if (fragment != null && fragment!!.onBackPressed()) {
            closeScreen()
        }
    }

    override fun onLongClick(view: View): Boolean {
        when (view.id) {
            R.id.customButton -> Toast.makeText(this, getString(R.string.acc_customize_reminder), Toast.LENGTH_SHORT).show()
            R.id.groupButton -> Toast.makeText(this, getString(R.string.change_group), Toast.LENGTH_SHORT).show()
            R.id.voiceButton -> Toast.makeText(this, getString(R.string.acc_type_by_voice), Toast.LENGTH_SHORT).show()
            R.id.exclusionButton -> Toast.makeText(this, getString(R.string.acc_customize_exclusions), Toast.LENGTH_SHORT).show()
            R.id.melodyButton -> Toast.makeText(this, getString(R.string.acc_select_melody), Toast.LENGTH_SHORT).show()
            R.id.repeatButton -> Toast.makeText(this, getString(R.string.repeat_limit), Toast.LENGTH_SHORT).show()
        }
        return true
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
        private const val GPS_OUT = 8
        private const val SHOP = 9
        private const val EMAIL = 10
        private const val GPS_PLACE = 11

        private const val VOICE_RECOGNITION_REQUEST_CODE = 109
        private const val MENU_ITEM_DELETE = 12
        private const val CONTACTS_REQUEST_E = 501
        private const val FILE_REQUEST = 323
        private const val TAG = "CreateReminderActivity"
        private const val SHOWCASE = "reminder_showcase"
    }
}
