package com.elementary.tasks.reminder.create_edit

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.file_explorer.FileExplorerActivity
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.core.views.TextViewWithIcon
import com.elementary.tasks.databinding.ActivityCreateReminderBinding
import com.elementary.tasks.databinding.DialogSelectExtraBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.groups.Position
import com.elementary.tasks.reminder.create_edit.fragments.ApplicationFragment
import com.elementary.tasks.reminder.create_edit.fragments.DateFragment
import com.elementary.tasks.reminder.create_edit.fragments.EmailFragment
import com.elementary.tasks.reminder.create_edit.fragments.LocationFragment
import com.elementary.tasks.reminder.create_edit.fragments.LocationOutFragment
import com.elementary.tasks.reminder.create_edit.fragments.MonthFragment
import com.elementary.tasks.reminder.create_edit.fragments.PlacesFragment
import com.elementary.tasks.reminder.create_edit.fragments.ReminderInterface
import com.elementary.tasks.reminder.create_edit.fragments.ShopFragment
import com.elementary.tasks.reminder.create_edit.fragments.SkypeFragment
import com.elementary.tasks.reminder.create_edit.fragments.TimerFragment
import com.elementary.tasks.reminder.create_edit.fragments.TypeFragment
import com.elementary.tasks.reminder.create_edit.fragments.WeekFragment
import com.elementary.tasks.reminder.create_edit.fragments.YearFragment
import com.google.android.material.snackbar.Snackbar

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.io.IOException
import java.util.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import timber.log.Timber

class CreateReminderActivity : ThemedActivity(), ReminderInterface, View.OnLongClickListener {

    private var binding: ActivityCreateReminderBinding? = null
    private var viewModel: ReminderViewModel? = null
    private var conversationViewModel: ConversationViewModel? = null

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
    override var group: String? = null
        private set
    override var melodyPath: String? = null
        private set
    private var autoLabel: String? = null
    override var ledColor = -1
        private set
    private var isEditing: Boolean = false
    override var attachment: String? = null
        private set

    override var reminder: Reminder? = null
        private set

    private val mOnTypeSelectListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            prefs!!.lastUsedReminder = position
            when (position) {
                DATE -> replaceFragment(DateFragment())
                TIMER -> replaceFragment(TimerFragment())
                WEEK -> replaceFragment(WeekFragment())
                GPS -> if (hasGpsPermission(GPS)) {
                    replaceFragment(LocationFragment())
                } else {
                    binding!!.navSpinner.setSelection(DATE)
                }
                SKYPE -> replaceFragment(SkypeFragment())
                APP -> replaceFragment(ApplicationFragment())
                MONTH -> replaceFragment(MonthFragment())
                GPS_OUT -> if (hasGpsPermission(GPS_OUT)) {
                    replaceFragment(LocationOutFragment())
                } else {
                    binding!!.navSpinner.setSelection(DATE)
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
                    binding!!.navSpinner.setSelection(DATE)
                }
                YEAR -> replaceFragment(YearFragment())
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    private val customizationView: DialogSelectExtraBinding
        get() {
            val binding = DialogSelectExtraBinding.inflate(layoutInflater)
            binding.extraSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
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
        get() = binding!!.taskSummary.text!!.toString().trim { it <= ' ' }

    override val windowType: Int
        get() = if (binding!!.windowTypeSwitch.isChecked) 1 else 0

    override val isExportToCalendar: Boolean
        get() = prefs!!.isCalendarEnabled || prefs!!.isStockCalendarEnabled

    private fun hasGpsPermission(code: Int): Boolean {
        if (!Permissions.checkPermission(this@CreateReminderActivity, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(this@CreateReminderActivity, code, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_reminder)
        isExportToTasks = Google.getInstance(this) != null
        initActionBar()
        initNavigation()
        initLongClick()
        loadReminder()
    }

    private fun initViewModel(id: Int) {
        conversationViewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)

        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                editReminder(reminder)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> finish()
                }
            }
        })
        viewModel!!.defaultGroup.observe(this, { group ->
            if (group != null) {
                binding!!.groupButton.setText(group!!.title)
                this.group = group!!.uuId
            }
        })
    }

    private fun initLongClick() {
        binding!!.customButton.setOnLongClickListener(this)
        binding!!.groupButton.setOnLongClickListener(this)
        binding!!.voiceButton.setOnLongClickListener(this)
        binding!!.exclusionButton.setOnLongClickListener(this)
        binding!!.melodyButton.setOnLongClickListener(this)
        binding!!.repeatButton.setOnLongClickListener(this)
    }

    private fun loadReminder() {
        val intent = intent
        val id = getIntent().getIntExtra(Constants.INTENT_ID, 0)
        initViewModel(id)
        if (id != 0) {
            isEditing = true
        } else if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name!!.scheme
                if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    reminder = BackupTool.getInstance().getReminder(cr, name)
                } else {
                    reminder = BackupTool.getInstance().getReminder(name.path, null)
                }
            } catch (e: IOException) {
                LogUtil.d(TAG, "loadReminder: " + e.localizedMessage)
            } catch (e: IllegalStateException) {
                LogUtil.d(TAG, "loadReminder: " + e.localizedMessage)
            }

        }
    }

    private fun editReminder(reminder: Reminder?) {
        this.reminder = reminder
        if (reminder == null) return
        viewModel!!.pauseReminder(reminder)
        binding!!.taskSummary.setText(reminder.summary)
        showGroup(reminder.group)
        attachment = reminder.attachmentFile
        if (!TextUtils.isEmpty(attachment)) {
            binding!!.attachmentButton.visibility = View.VISIBLE
        }
        binding!!.windowTypeSwitch.isChecked = reminder.windowType == 1
        initParams()
        when (reminder.type) {
            Reminder.BY_DATE, Reminder.BY_DATE_CALL, Reminder.BY_DATE_SMS -> binding!!.navSpinner.setSelection(DATE)
            Reminder.BY_TIME -> binding!!.navSpinner.setSelection(TIMER)
            Reminder.BY_WEEK, Reminder.BY_WEEK_CALL, Reminder.BY_WEEK_SMS -> binding!!.navSpinner.setSelection(WEEK)
            Reminder.BY_LOCATION, Reminder.BY_LOCATION_CALL, Reminder.BY_LOCATION_SMS -> binding!!.navSpinner.setSelection(GPS)
            Reminder.BY_SKYPE, Reminder.BY_SKYPE_CALL, Reminder.BY_SKYPE_VIDEO -> binding!!.navSpinner.setSelection(SKYPE)
            Reminder.BY_DATE_APP, Reminder.BY_DATE_LINK -> binding!!.navSpinner.setSelection(APP)
            Reminder.BY_MONTH, Reminder.BY_MONTH_CALL, Reminder.BY_MONTH_SMS -> binding!!.navSpinner.setSelection(MONTH)
            Reminder.BY_OUT, Reminder.BY_OUT_SMS, Reminder.BY_OUT_CALL -> binding!!.navSpinner.setSelection(GPS_OUT)
            Reminder.BY_DATE_SHOP -> binding!!.navSpinner.setSelection(SHOP)
            Reminder.BY_DATE_EMAIL -> binding!!.navSpinner.setSelection(EMAIL)
            Reminder.BY_DAY_OF_YEAR, Reminder.BY_DAY_OF_YEAR_CALL, Reminder.BY_DAY_OF_YEAR_SMS -> binding!!.navSpinner.setSelection(YEAR)
            else -> if (Module.isPro) {
                when (reminder.type) {
                    Reminder.BY_PLACES, Reminder.BY_PLACES_SMS, Reminder.BY_PLACES_CALL -> binding!!.navSpinner.setSelection(GPS_PLACE)
                }
            }
        }
    }

    private fun initParams() {
        if (reminder != null) {
            useGlobal = reminder!!.isUseGlobal
            auto = reminder!!.isAuto
            wake = reminder!!.isAwake
            unlock = reminder!!.isUnlock
            notificationRepeat = reminder!!.isRepeatNotification
            voice = reminder!!.isNotifyByVoice
            vibration = reminder!!.isVibrate
            volume = reminder!!.volume
            repeatLimit = reminder!!.repeatLimit
            melodyPath = reminder!!.melodyPath
            ledColor = reminder!!.color
            updateMelodyIndicator()
        }
    }

    private fun updateMelodyIndicator() {
        if (melodyPath != null) {
            binding!!.melodyButton.visibility = View.VISIBLE
        } else {
            binding!!.melodyButton.visibility = View.GONE
        }
    }

    private fun initNavigation() {
        val navSpinner = ArrayList<SpinnerItem>()
        if (themeUtil!!.isDark) {
            navSpinner.add(SpinnerItem(getString(R.string.by_date), R.drawable.ic_meeting_deadlines_white))
            navSpinner.add(SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_white))
            navSpinner.add(SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_white))
            navSpinner.add(SpinnerItem(getString(R.string.location), R.drawable.ic_map_white))
            navSpinner.add(SpinnerItem(getString(R.string.skype), R.drawable.ic_skype_white))
            navSpinner.add(SpinnerItem(getString(R.string.launch_application), R.drawable.ic_software_white))
            navSpinner.add(SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar_white))
            navSpinner.add(SpinnerItem(getString(R.string.yearly), R.drawable.ic_confetti_white))
            navSpinner.add(SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp))
            navSpinner.add(SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart_white))
            navSpinner.add(SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email_white))
            if (Module.isPro)
                navSpinner.add(SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker_white))
        } else {
            navSpinner.add(SpinnerItem(getString(R.string.by_date), R.drawable.ic_meeting_deadlines))
            navSpinner.add(SpinnerItem(getString(R.string.timer), R.drawable.ic_timer))
            navSpinner.add(SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm))
            navSpinner.add(SpinnerItem(getString(R.string.location), R.drawable.ic_map))
            navSpinner.add(SpinnerItem(getString(R.string.skype), R.drawable.ic_skype))
            navSpinner.add(SpinnerItem(getString(R.string.launch_application), R.drawable.ic_software))
            navSpinner.add(SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar))
            navSpinner.add(SpinnerItem(getString(R.string.yearly), R.drawable.ic_confetti_black))
            navSpinner.add(SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_black_24dp))
            navSpinner.add(SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart))
            navSpinner.add(SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email))
            if (Module.isPro)
                navSpinner.add(SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker))
        }
        val adapter = TitleNavigationAdapter(applicationContext, navSpinner)
        binding!!.navSpinner.adapter = adapter
        binding!!.navSpinner.onItemSelectedListener = mOnTypeSelectListener
        var lastPos = prefs!!.lastUsedReminder
        if (lastPos >= navSpinner.size) lastPos = 0
        binding!!.navSpinner.setSelection(lastPos)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.voiceButton.setOnClickListener { v -> openRecognizer() }
        binding!!.customButton.setOnClickListener { v -> openCustomizationDialog() }
        binding!!.groupButton.setOnClickListener { v -> changeGroup() }
        binding!!.melodyButton.setOnClickListener { view -> showCurrentMelody() }
        binding!!.attachmentButton.setOnClickListener { view -> showAttachmentSnack() }
    }

    private fun changeGroup() {
        val position = Position()
        val categories = viewModel!!.allGroupsNames
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.choose_group)
        builder.setSingleChoiceItems(ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, categories), position.i) { dialog, which ->
            dialog.dismiss()
            val groups = viewModel!!.allGroups.value
            if (groups != null) showGroup(groups[which])
        }
        val alert = builder.create()
        alert.show()
    }

    private fun showGroup(item: Group?) {
        if (item == null) return
        binding!!.groupButton.text = item.title
        group = item.uuId
    }

    private fun openCustomizationDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.personalization)
        val b = customizationView
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialog, which -> saveExtraResults(b) }
        builder.create().show()
    }

    private fun saveExtraResults(b: DialogSelectExtraBinding) {
        useGlobal = b.extraSwitch.isChecked
        auto = b.autoCheck.isChecked
        wake = b.wakeCheck.isChecked
        unlock = b.unlockCheck.isChecked
        notificationRepeat = b.repeatCheck.isChecked
        voice = b.voiceCheck.isChecked
        vibration = b.vibrationCheck.isChecked
    }

    private fun openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true)
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
        if (reminder != null && prefs!!.isAutoSaveEnabled) {
            if (!reminder!!.isActive) {
                askAboutEnabling()
            } else {
                save()
            }
        } else if (isEditing && reminder != null) {
            if (!reminder!!.isActive) {
                viewModel!!.resumeReminder(reminder!!)
            }
            finish()
        } else {
            finish()
        }
    }

    private fun deleteReminder() {
        if (reminder != null) {
            if (reminder!!.isRemoved) {
                viewModel!!.deleteReminder(reminder!!, true)
            } else {
                viewModel!!.moveToTrash(reminder!!)
            }
        }
    }

    private fun selectVolume() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.loudness)
        val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
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
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            volume = b.seekBar.progress - 1
            val str = String.format(getString(R.string.selected_loudness_x_for_reminder), getVolumeTitle(b.seekBar.progress))
            showSnackbar(str, getString(R.string.cancel)) { v -> volume = -1 }
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
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
        val builder = Dialogues.getDialog(this)
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
                showSnackbar(str, getString(R.string.cancel)) { v -> ledColor = -1 }
                dialog.dismiss()
            }
        }
        builder.setPositiveButton(R.string.ok) { dialog, which -> dialog.dismiss() }
        builder.setNegativeButton(R.string.disable) { dialog, which ->
            ledColor = -1
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun askAboutEnabling() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.this_reminder_is_disabled)
        builder.setMessage(R.string.would_you_like_to_enable_it)
        builder.setPositiveButton(R.string.yes) { dialog, which ->
            dialog.dismiss()
            save()
        }
        builder.setNegativeButton(R.string.no) { dialog, which ->
            dialog.dismiss()
            finish()
        }
        builder.create().show()
    }

    private fun save() {
        if (fragment != null) {
            val reminder = fragment!!.prepare()
            if (reminder != null) {
                Timber.d("save: %s", reminder)
                viewModel!!.saveAndStartReminder(reminder)
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null) {
                val model = conversationViewModel!!.findResults(matches)
                if (model != null) {
                    processModel(model)
                } else {
                    val text = matches[0].toString()
                    binding!!.taskSummary.setText(StringUtils.capitalize(text))
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
            if (attachment != null) {
                binding!!.attachmentButton.visibility = View.VISIBLE
                showAttachmentSnack()
            }
        }
        if (fragment != null) {
            fragment!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showAttachmentSnack() {
        val file = File(attachment!!)
        showSnackbar(String.format(getString(R.string.file_x_attached), file.name),
                getString(R.string.cancel)) { v ->
            attachment = null
            binding!!.attachmentButton.visibility = View.GONE
        }
    }

    private fun showCurrentMelody() {
        if (melodyPath != null) {
            val musicFile = File(melodyPath!!)
            showSnackbar(String.format(getString(R.string.melody_x), musicFile.name),
                    getString(R.string.delete)) { view -> removeMelody() }
        }
    }

    private fun removeMelody() {
        melodyPath = null
        updateMelodyIndicator()
    }

    private fun processModel(model: Reminder) {
        this.reminder = model
        editReminder(model)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (Module.isMarshmallow && fragment != null) {
            fragment!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        if (grantResults.size == 0) return
        when (requestCode) {
            CONTACTS_REQUEST_E -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.navSpinner.setSelection(EMAIL)
            } else {
                binding!!.navSpinner.setSelection(DATE)
            }
            GPS_PLACE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.navSpinner.setSelection(GPS_PLACE)
            } else {
                binding!!.navSpinner.setSelection(DATE)
            }
            GPS_OUT -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.navSpinner.setSelection(GPS_OUT)
            } else {
                binding!!.navSpinner.setSelection(DATE)
            }
            GPS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding!!.navSpinner.setSelection(GPS)
            } else {
                binding!!.navSpinner.setSelection(DATE)
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

    fun showShowcase() {
        if (!prefs!!.isShowcase(SHOWCASE)) {
            prefs!!.setShowcase(SHOWCASE, true)
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
        Snackbar.make(binding!!.mainContainer, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener).show()
    }

    override fun showSnackbar(title: String) {
        Snackbar.make(binding!!.mainContainer, title, Snackbar.LENGTH_SHORT).show()
    }

    override fun setEventHint(hint: String) {
        if (binding != null) binding!!.taskSummary.hint = hint
    }

    override fun setExclusionAction(listener: View.OnClickListener?) {
        if (binding == null) return
        if (listener == null) {
            binding!!.exclusionButton.visibility = View.GONE
        } else {
            binding!!.exclusionButton.visibility = View.VISIBLE
            binding!!.exclusionButton.setOnClickListener(listener)
        }
    }

    override fun setRepeatAction(listener: View.OnClickListener?) {
        if (binding == null) return
        if (listener == null) {
            binding!!.repeatButton.visibility = View.GONE
        } else {
            binding!!.repeatButton.visibility = View.VISIBLE
            binding!!.repeatButton.setOnClickListener(listener)
        }
    }

    override fun setFullScreenMode(b: Boolean) {
        if (b) {
            ViewUtils.collapse(binding!!.toolbar)
        } else {
            ViewUtils.expand(binding!!.toolbar)
        }
    }

    override fun setHasAutoExtra(hasAutoExtra: Boolean, label: String) {
        this.hasAutoExtra = hasAutoExtra
        this.autoLabel = label
    }

    override fun onDestroy() {
        super.onDestroy()
        UpdatesHelper.getInstance(this).updateWidget()
        UpdatesHelper.getInstance(this).updateCalendarWidget()
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

    private class SpinnerItem internal constructor(val title: String, val icon: Int)

    private inner class TitleNavigationAdapter internal constructor(private val context: Context, private val spinnerNavItem: ArrayList<SpinnerItem>) : BaseAdapter() {

        private var txtTitle: TextViewWithIcon? = null

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
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_navigation, null)
            }
            txtTitle = convertView!!.findViewById(R.id.txtTitle)
            txtTitle!!.setIcon(0)
            txtTitle!!.text = spinnerNavItem[position].title
            txtTitle!!.setTextColor(context.resources.getColor(R.color.whitePrimary))
            return convertView
        }


        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_navigation, null)
            }
            val itemBg = convertView!!.findViewById<RelativeLayout>(R.id.itemBg)
            itemBg.setBackgroundColor(themeUtil!!.spinnerStyle)
            txtTitle = convertView.findViewById(R.id.txtTitle)
            txtTitle!!.setIcon(spinnerNavItem[position].icon)
            if (themeUtil!!.isDark) {
                txtTitle!!.setTextColor(themeUtil!!.getColor(R.color.whitePrimary))
            } else {
                txtTitle!!.setTextColor(themeUtil!!.getColor(R.color.blackPrimary))
            }
            txtTitle!!.text = spinnerNavItem[position].title
            return convertView
        }
    }

    companion object {

        private val DATE = 0
        private val TIMER = 1
        private val WEEK = 2
        private val GPS = 3
        private val SKYPE = 4
        private val APP = 5
        private val MONTH = 6
        private val YEAR = 7
        private val GPS_OUT = 8
        private val SHOP = 9
        private val EMAIL = 10
        private val GPS_PLACE = 11

        private val VOICE_RECOGNITION_REQUEST_CODE = 109
        private val MENU_ITEM_DELETE = 12
        private val CONTACTS_REQUEST_E = 501
        private val FILE_REQUEST = 323
        private val TAG = "CreateReminderActivity"
        private val SHOWCASE = "reminder_showcase"
    }
}
