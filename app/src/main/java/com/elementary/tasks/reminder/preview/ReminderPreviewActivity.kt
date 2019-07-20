package com.elementary.tasks.reminder.preview

import android.app.ActivityOptions
import android.app.AlarmManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.QrShareProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.util.*

class ReminderPreviewActivity : BindingActivity<ActivityReminderPreviewBinding>(R.layout.activity_reminder_preview) {

    private var mGoogleMap: AdvancedMapFragment? = null
    private lateinit var viewModel: ReminderViewModel
    private val backupTool: BackupTool by inject()

    private val list = ArrayList<Long>()
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var reminder: Reminder? = null
    private var shoppingAdapter = ShopListRecyclerAdapter()

    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener {
        openFullMap()
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        binding.dataContainer.removeAllViewsInLayout()
        initActionBar()
        initViews()
        initViewModel(id)
    }

    private fun sendSMS(reminder: Reminder) {
        if (TextUtils.isEmpty(reminder.summary)) return
        TelephonyUtil.sendSms(this, reminder.target, reminder.summary)
    }

    private fun makeCall(reminder: Reminder) {
        if (Permissions.checkPermission(this, CALL_PERM, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(reminder.target, this)
        }
    }

    private fun openApp(reminder: Reminder) {
        if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
            TelephonyUtil.openApp(reminder.target, this)
        } else {
            TelephonyUtil.openLink(reminder.target, this)
        }
    }

    private fun sendEmail(reminder: Reminder) {
        TelephonyUtil.sendMail(this, reminder.target,
                reminder.subject, reminder.summary, reminder.attachmentFile)
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(id)).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                showInfo(reminder)
                viewModel.loadExtra(reminder)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                    Commands.FAILED -> {
                        Toast.makeText(this, getString(R.string.reminder_is_outdated), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                    }
                }
            }
        })
        viewModel.googleTask.observe(this, Observer {
            if (it != null) {
                showTask(it)
            }
        })
        viewModel.note.observe(this, Observer {
            if (it != null) {
                showNote(it)
            }
        })
        viewModel.calendarEvent.observe(this, Observer {
            if (it != null) {
                showCalendarEvents(it)
            }
        })
        viewModel.clearExtraData.observe(this, Observer {
            if (it != null && it) {
                binding.dataContainer.removeAllViewsInLayout()
            }
        })
    }

    private fun showCalendarEvents(events: List<CalendarUtils.EventItem>) {
        Timber.d("showCalendarEvents: $events")
        for (e in events) {
            val binding = GoogleEventHolder(binding.dataContainer) { _, event, listActions ->
                if (listActions == ListActions.OPEN && event != null) {
                    openCalendar(event.id)
                } else if (listActions == ListActions.REMOVE && event != null) {
                    reminder?.let { viewModel.deleteEvent(event, it) }
                }
            }
            binding.bind(e)
            this.binding.dataContainer.addView(binding.itemView)
        }
    }

    private fun openCalendar(id: Long) {
        if (id <= 0L) return
        val uri = Uri.parse("content://com.android.calendar/events/$id")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showTask(pair: Pair<GoogleTaskList?, GoogleTask?>) {
        val googleTask = pair.second ?: return
        val googleTaskList = pair.first ?: return
        val binding = GoogleTaskHolder(binding.dataContainer) { _, _, listActions ->
            if (listActions == ListActions.EDIT) {
                TaskActivity.openLogged(this@ReminderPreviewActivity,
                        Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
                                .putExtra(Constants.INTENT_ID, googleTask.taskId)
                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
            }
        }
        binding.bind(googleTask, mapOf(Pair(googleTask.listId, googleTaskList)))
        this.binding.dataContainer.addView(binding.itemView)
    }

    private fun showNote(note: NoteWithImages) {
        val binding = NoteHolder(binding.dataContainer) { _, _, listActions ->
            if (listActions == ListActions.OPEN) {
                startActivity(Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
                        .putExtra(Constants.INTENT_ID, note.getKey()))
            }
        }
        binding.hasMore = false
        binding.setData(note)
        this.binding.dataContainer.addView(binding.itemView)
    }

    private fun showMapData(reminder: Reminder) {
        binding.mapContainer.show()
        binding.location.show()

        var places = ""
        reminder.places.forEach {
            val lat = it.latitude
            val lon = it.longitude
            mGoogleMap?.addMarker(LatLng(lat, lon), reminder.summary, false, false, it.radius)
            places += String.format(Locale.getDefault(), "%.5f %.5f", lat, lon)
            places += "\n"
        }
        binding.location.text = places

        val place = reminder.places[0]
        val lat = place.latitude
        val lon = place.longitude
        mGoogleMap?.moveCamera(LatLng(lat, lon), 0, 0, 0, 0)
    }

    private fun showInfo(reminder: Reminder) {
        this.reminder = reminder

        Timber.d("showInfo: %s", reminder.toString())

        binding.group.text = reminder.groupTitle
        showStatus(reminder)
        binding.windowTypeView.text = getWindowType(reminder.windowType)
        binding.taskText.text = reminder.summary
        binding.type.text = ReminderUtils.getTypeString(this, reminder.type)
        binding.itemPhoto.setImageResource(ThemeUtil.getReminderIllustration(reminder.type))
        binding.idView.text = reminder.uuId

        showDueAndRepeat(reminder)
        showBefore(reminder)
        showPhoneContact(reminder)
        showMelody(reminder)
        showAttachment(reminder)
        if (Reminder.isGpsType(reminder.type)) {
            initMap()
        } else {
            binding.locationView.hide()
            binding.mapContainer.hide()
        }
        if (reminder.shoppings.isNotEmpty()) {
            binding.todoList.show()
            loadData(reminder)
        } else {
            binding.todoList.hide()
        }
        if (reminder.isActive && !reminder.isRemoved) {
            when {
                Reminder.isKind(reminder.type, Reminder.Kind.SMS) -> {
                    if (prefs.isTelephonyAllowed) {
                        binding.fab.setIconResource(R.drawable.ic_twotone_send_24px)
                        binding.fab.text = getString(R.string.send_sms)
                        binding.fab.show()
                    } else {
                        binding.fab.hide()
                    }
                }
                Reminder.isKind(reminder.type, Reminder.Kind.CALL) -> {
                    if (prefs.isTelephonyAllowed) {
                        binding.fab.setIconResource(R.drawable.ic_twotone_call_24px)
                        binding.fab.text = getString(R.string.make_call)
                        binding.fab.show()
                    } else {
                        binding.fab.hide()
                    }
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_APP) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_open_in_new_24px)
                    binding.fab.text = getString(R.string.open_app)
                    binding.fab.show()
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_open_in_browser_24px)
                    binding.fab.text = getString(R.string.open_link)
                    binding.fab.show()
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_local_post_office_24px)
                    binding.fab.text = getString(R.string.send)
                    binding.fab.show()
                }
                else -> binding.fab.hide()
            }
        } else {
            binding.fab.hide()
        }
    }

    private fun showBefore(reminder: Reminder) {
        if (reminder.remindBefore == 0L) {
            binding.beforeView.hide()
        } else {
            binding.beforeView.show()
            binding.before.text = IntervalUtil.getBeforeTime(this, reminder.remindBefore)
        }
    }

    private fun loadData(reminder: Reminder) {
        shoppingAdapter.listener = object : ShopListRecyclerAdapter.ActionListener {
            override fun onItemCheck(position: Int, isChecked: Boolean) {
                val item = shoppingAdapter.getItem(position)
                item.isChecked = !item.isChecked
                shoppingAdapter.updateData()
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder, this@ReminderPreviewActivity)
            }

            override fun onItemDelete(position: Int) {
                shoppingAdapter.delete(position)
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder, this@ReminderPreviewActivity)
            }
        }
        shoppingAdapter.data = reminder.shoppings
        binding.todoList.layoutManager = LinearLayoutManager(this)
        binding.todoList.isNestedScrollingEnabled = false
        binding.todoList.adapter = shoppingAdapter
    }

    private fun showDueAndRepeat(reminder: Reminder) {
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            binding.timeView.show()
            binding.time.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormat, prefs.appLanguage)
            when {
                Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> binding.repeat.text = String.format(getString(R.string.xM), reminder.repeatInterval.toString())
                Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> binding.repeat.text = ReminderUtils.getRepeatString(this, prefs, reminder.weekdays)
                Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> binding.repeat.text = getString(R.string.yearly)
                else -> binding.repeat.text = IntervalUtil.getInterval(this, reminder.repeatInterval)
            }
            binding.repeatView.show()
        } else {
            binding.timeView.hide()
            binding.repeatView.hide()
        }
    }

    private fun showPhoneContact(reminder: Reminder) {
        val numberStr = reminder.target
        if (!TextUtils.isEmpty(numberStr)) {
            binding.number.text = numberStr
            binding.numberView.show()
        } else {
            binding.number.hide()
            binding.numberView.hide()
        }
    }

    private fun showMelody(reminder: Reminder) {
        var file: File? = null
        if (!TextUtils.isEmpty(reminder.melodyPath)) {
            file = File(reminder.melodyPath)
        } else {
            val path = prefs.melodyFile
            if (path != "" && !Sound.isDefaultMelody(path)) {
                file = File(path)
            } else {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (soundUri != null && soundUri.path != null) {
                    file = File(soundUri.path!!)
                }
            }
        }
        if (file != null) binding.melody.text = file.name
    }

    private fun showStatus(reminder: Reminder) {
        binding.statusSwitch.isChecked = reminder.isActive
        if (!reminder.isActive) {
            binding.statusText.setText(R.string.disabled)
        } else {
            binding.statusText.setText(R.string.enabled4)
        }
    }

    private fun getWindowType(reminderWType: Int): String {
        var windowType = prefs.reminderType
        val ignore = prefs.isIgnoreWindowType
        if (!ignore) {
            windowType = reminderWType
        }
        return if (windowType == 0) getString(R.string.full_screen) else getString(R.string.simple)
    }

    private val imageTarget: Target = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            Timber.d("onPrepareLoad: ")
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            Timber.d("onBitmapFailed: $e")
            binding.attachmentsView.hide()
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            Timber.d("onBitmapLoaded: ${bitmap != null}")
            binding.attachmentsView.show()
            binding.attachmentImage.setImageBitmap(bitmap)
            binding.attachmentsView.setOnClickListener {
                reminder?.let {
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@ReminderPreviewActivity,
                            binding.attachmentImage, "image")
                    startActivity(Intent(this@ReminderPreviewActivity, AttachmentPreviewActivity::class.java)
                            .putExtra(Constants.INTENT_ITEM, it.attachmentFile),
                            options.toBundle())
                }
            }
        }
    }

    private fun showAttachment(reminder: Reminder) {
        Timber.d("showAttachment: ${reminder.attachmentFile}")
        if (reminder.attachmentFile != "") {
            binding.attachment.text = reminder.attachmentFile
            binding.attachmentView.show()
            val file = File(reminder.attachmentFile)
            if (file.exists()) {
                Picasso.get().load(file).into(imageTarget)
            } else {
                val uri = Uri.parse(reminder.attachmentFile)
                Picasso.get().load(uri).into(imageTarget)
            }
        } else {
            binding.attachmentView.hide()
            binding.attachmentsView.hide()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder_preview, menu)

        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isDarkMode)
        ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_share_24px, isDarkMode)
        ViewUtils.tintMenuIcon(this, menu, 2, R.drawable.ic_twotone_file_copy_24px, isDarkMode)
        ViewUtils.tintMenuIcon(this, menu, 3, R.drawable.ic_twotone_delete_24px, isDarkMode)

        if (Module.isPro && QrShareProvider.hasQrSupport()) {
            menu.add(Menu.NONE, MENU_ITEM_IN_APP_SHARE, 100, getString(R.string.in_app_sharing))
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ids = item.itemId
        when (ids) {
            R.id.action_delete -> removeReminder()
            android.R.id.home -> closeWindow()
            R.id.action_make_copy -> makeCopy()
            R.id.action_share -> shareReminder()
            R.id.action_edit -> editReminder()
            MENU_ITEM_IN_APP_SHARE -> {
                openShareScreen()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openShareScreen() {
        reminder?.let {
            launchDefault {
                val data = QrShareProvider.generateEncryptedData(it)
                withUIContext {
                    if (data != null) {
                        QrShareProvider.openShareScreen(this@ReminderPreviewActivity, data, QrShareProvider.TYPE_REMINDER)
                    }
                }
            }
        }
    }

    private fun shareReminder() {
        if (!Permissions.checkPermission(this, SD_PERM, Permissions.WRITE_EXTERNAL)) {
            return
        }
        reminder?.let {
            launchDefault {
                val path = backupTool.exportReminder(it)
                Timber.d("shareReminder: $path")
                if (path != null) {
                    withUIContext {
                        TelephonyUtil.sendFile(File(path), this@ReminderPreviewActivity)
                    }
                }
            }
        }
    }

    private fun editReminder() {
        reminder?.let {
            CreateReminderActivity.openLogged(this, Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, it.uuId))
        }
    }

    private fun removeReminder() {
        reminder?.let { reminder ->
            if (reminder.isActive && !reminder.isRemoved) {
                dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
                    if (it) viewModel.moveToTrash(reminder)
                }
            } else {
                dialogues.askConfirmation(this, getString(R.string.delete)) {
                    if (it) viewModel.deleteReminder(reminder, true)
                }
            }
        }
    }

    private fun makeCopy() {
        reminder?.let {
            val type = it.type
            if (!Reminder.isGpsType(type) && !Reminder.isSame(type, Reminder.BY_TIME)) {
                showDialog()
            }
        }
    }

    private fun closeWindow() {
        mUiHandler.post { this.finishAfterTransition() }
    }

    private fun showDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var hour = 0
        var minute = 0
        list.clear()
        val time = ArrayList<String>()
        val is24 = prefs.is24HourFormat
        do {
            if (hour == 23 && minute == 30) {
                hour = -1
            } else {
                val tmp = calendar.timeInMillis
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
                list.add(tmp)
                time.add(TimeUtil.getTime(calendar.time, is24, prefs.appLanguage))
                calendar.timeInMillis = tmp + AlarmManager.INTERVAL_HALF_HOUR
            }
        } while (hour != -1)
        val builder = dialogues.getMaterialDialog(this)
        builder.setTitle(R.string.choose_time)
        builder.setItems(time.toTypedArray()) { dialog, which ->
            dialog.dismiss()
            saveCopy(which)
        }
        builder.create().show()
    }

    private fun saveCopy(which: Int) {
        Timber.d("saveCopy: $which")
        reminder?.let {
            viewModel.copyReminder(it, list[which], it.summary + " - " + getString(R.string.copy))
        }
    }

    private fun initViews() {
        binding.switchWrapper.setOnClickListener { switchClick() }
        binding.fab.setOnClickListener { fabClick() }
        binding.mapContainer.hide()
        binding.attachmentsView.hide()
        binding.fab.hide()
    }

    private fun fabClick() {
        reminder?.let {
            if (it.isActive && !it.isRemoved) {
                when {
                    Reminder.isKind(it.type, Reminder.Kind.SMS) -> sendSMS(it)
                    Reminder.isKind(it.type, Reminder.Kind.CALL) -> makeCall(it)
                    Reminder.isSame(it.type, Reminder.BY_DATE_APP) -> openApp(it)
                    Reminder.isSame(it.type, Reminder.BY_DATE_LINK) -> openApp(it)
                    Reminder.isSame(it.type, Reminder.BY_DATE_EMAIL) -> sendEmail(it)
                }
            }
        }
    }

    private fun switchClick() {
        reminder?.let {
            if (Reminder.isGpsType(it.type)) {
                if (Permissions.ensureForeground(this@ReminderPreviewActivity, 1142)) {
                    viewModel.toggleReminder(it)
                }
            } else {
                viewModel.toggleReminder(it)
            }
        }
    }

    private fun initMap() {
        val googleMap = AdvancedMapFragment.newInstance(false, false, false, false,
                false, false, isDarkMode)
        googleMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                googleMap.setSearchEnabled(false)
                googleMap.setOnMapClickListener(GoogleMap.OnMapClickListener {
                    openFullMap()
                })
                googleMap.setOnMarkerClick(mOnMarkerClick)
                reminder?.let { showMapData(it) }
            }
        })
        supportFragmentManager.beginTransaction()
                .replace(binding.mapContainer.id, googleMap)
                .addToBackStack(null)
                .commit()
        this.mGoogleMap = googleMap
    }

    private fun openFullMap() {
        reminder?.let {
            val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.mapContainer, "map")
            startActivity(Intent(this, FullscreenMapActivity::class.java)
                    .putExtra(Constants.INTENT_ID, it.uuId), options.toBundle())
        }
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        ViewUtils.listenScrollableView(binding.scrollView) {
            binding.appBar.isSelected = it > 0
        }
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.checkPermission(grantResults)) {
            when (requestCode) {
                CALL_PERM -> fabClick()
                SD_PERM -> shareReminder()
            }
        }
    }

    companion object {
        private const val CALL_PERM = 612
        private const val SD_PERM = 614
        private const val MENU_ITEM_IN_APP_SHARE = 12
    }
}
