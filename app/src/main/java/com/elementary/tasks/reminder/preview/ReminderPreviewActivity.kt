package com.elementary.tasks.reminder.preview

import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TasksConstants
import com.elementary.tasks.googleTasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_reminder_preview.*
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ReminderPreviewActivity : ThemedActivity() {

    private var mGoogleMap: AdvancedMapFragment? = null
    private lateinit var viewModel: ReminderViewModel
    @Inject
    lateinit var backupTool: BackupTool

    private val list = ArrayList<Long>()
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var reminder: Reminder? = null
    private var shoppingAdapter = ShopListRecyclerAdapter()
    private var mSendListener = { isSent: Boolean ->
        if (isSent) {
            finish()
        } else {
            showSendingError()
        }
    }

    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener {
        openFullMap()
        false
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        setContentView(R.layout.activity_reminder_preview)
        initActionBar()
        initViews()
        initViewModel(id)
    }

    private fun showSendingError() {
        Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
    }

    private fun sendSMS(reminder: Reminder) {
        if (TextUtils.isEmpty(reminder.summary)) return
        if (!Permissions.ensurePermissions(this, SMS_PERM, Permissions.SEND_SMS)) {
            return
        }
        val action = "SMS_SENT"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(action), 0)
        registerReceiver(SendReceiver(mSendListener), IntentFilter(action))
        val sms = SmsManager.getDefault()
        try {
            sms.sendTextMessage(reminder.target, null, reminder.summary, sentPI, null)
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.error_sending, Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeCall(reminder: Reminder) {
        if (Permissions.ensurePermissions(this, CALL_PERM, Permissions.CALL_PHONE)) {
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
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
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
    }

    private fun showTask(pair: Pair<GoogleTaskList?, GoogleTask?>) {
        val googleTask = pair.second ?: return
        val googleTaskList = pair.first ?: return
        val binding = GoogleTaskHolder(dataContainer) { _, _, listActions ->
            if (listActions == ListActions.EDIT) {
                TaskActivity.openLogged(this@ReminderPreviewActivity,
                        Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
                                .putExtra(Constants.INTENT_ID, googleTask.taskId)
                                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
            }
        }
        binding.bind(googleTask, mapOf(Pair(googleTask.listId, googleTaskList)))
        this.dataContainer.addView(binding.itemView)
    }

    private fun showNote(note: NoteWithImages) {
        val binding = NoteHolder(dataContainer) { _, _, listActions ->
            if (listActions == ListActions.OPEN) {
                startActivity(Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
                        .putExtra(Constants.INTENT_ID, note.getKey()))
            }
        }
        binding.hasMore = false
        binding.setData(note)
        this.dataContainer.addView(binding.itemView)
    }

    private fun showMapData(reminder: Reminder) {
        mapContainer.visibility = View.VISIBLE
        location.visibility = View.VISIBLE

        var places = ""
        reminder.places.forEach {
            val lat = it.latitude
            val lon = it.longitude
            mGoogleMap?.addMarker(LatLng(lat, lon), reminder.summary, false, false, it.radius)
            places += String.format(Locale.getDefault(), "%.5f %.5f", lat, lon)
            places += "\n"
        }
        location.text = places

        val place = reminder.places[0]
        val lat = place.latitude
        val lon = place.longitude
        mGoogleMap?.moveCamera(LatLng(lat, lon), 0, 0, 0, 0)
    }

    private fun showInfo(reminder: Reminder) {
        this.reminder = reminder

        Timber.d("showInfo: %s", reminder.toString())

        group.text = reminder.groupTitle
        showStatus(reminder)
        window_type_view.text = getWindowType(reminder.windowType)
        taskText.text = reminder.summary
        type.text = ReminderUtils.getTypeString(type.context, reminder.type)
        itemPhoto.setImageResource(themeUtil.getReminderIllustration(reminder.type))

        showDueAndRepeat(reminder)
        showBefore(reminder)
        showPhoneContact(reminder)
        showMelody(reminder)
        showAttachment(reminder)
        if (Reminder.isGpsType(reminder.type)) {
            initMap()
        } else {
            locationView.visibility = View.GONE
            mapContainer.visibility = View.GONE
        }
        if (reminder.shoppings.isNotEmpty()) {
            todoList.visibility = View.VISIBLE
            loadData(reminder)
        } else {
            todoList.visibility = View.GONE
        }
        if (reminder.isActive && !reminder.isRemoved) {
            when {
                Reminder.isKind(reminder.type, Reminder.Kind.SMS) -> {
                    if (prefs.isTelephonyAllowed) {
                        fab.setIconResource(R.drawable.ic_twotone_send_24px)
                        fab.text = getString(R.string.send_sms)
                        fab.visibility = View.VISIBLE
                    } else {
                        fab.visibility = View.GONE
                    }
                }
                Reminder.isKind(reminder.type, Reminder.Kind.CALL) -> {
                    if (prefs.isTelephonyAllowed) {
                        fab.setIconResource(R.drawable.ic_twotone_call_24px)
                        fab.text = getString(R.string.make_call)
                        fab.visibility = View.VISIBLE
                    } else {
                        fab.visibility = View.GONE
                    }
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_APP) -> {
                    fab.setIconResource(R.drawable.ic_twotone_open_in_new_24px)
                    fab.text = getString(R.string.open_app)
                    fab.visibility = View.VISIBLE
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) -> {
                    fab.setIconResource(R.drawable.ic_twotone_open_in_browser_24px)
                    fab.text = getString(R.string.open_link)
                    fab.visibility = View.VISIBLE
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> {
                    fab.setIconResource(R.drawable.ic_twotone_local_post_office_24px)
                    fab.text = getString(R.string.send)
                    fab.visibility = View.VISIBLE
                }
                else -> fab.visibility = View.GONE
            }
        } else {
            fab.visibility = View.GONE
        }

        dataContainer.removeAllViewsInLayout()
    }

    private fun showBefore(reminder: Reminder) {
        val beforeStr = IntervalUtil.getInterval(this, reminder.remindBefore)
        if (beforeStr.isEmpty() || beforeStr == "0") {
            beforeView.visibility = View.GONE
        } else {
            beforeView.visibility = View.VISIBLE
            before.text = beforeStr
        }
    }

    private fun loadData(reminder: Reminder) {
        shoppingAdapter.listener = object : ShopListRecyclerAdapter.ActionListener {
            override fun onItemCheck(position: Int, isChecked: Boolean) {
                val item = shoppingAdapter.getItem(position)
                item.isChecked = !item.isChecked
                shoppingAdapter.updateData()
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder)
            }

            override fun onItemDelete(position: Int) {
                shoppingAdapter.delete(position)
                reminder.shoppings = shoppingAdapter.data
                viewModel.saveReminder(reminder)
            }
        }
        shoppingAdapter.data = reminder.shoppings
        todoList.layoutManager = LinearLayoutManager(this)
        todoList.isNestedScrollingEnabled = false
        todoList.adapter = shoppingAdapter
    }

    private fun showDueAndRepeat(reminder: Reminder) {
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            timeView.visibility = View.VISIBLE
            time.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormat, prefs.appLanguage)
            when {
                Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> repeat.text = String.format(getString(R.string.xM), reminder.repeatInterval.toString())
                Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> repeat.text = ReminderUtils.getRepeatString(this, prefs, reminder.weekdays)
                Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> repeat.text = getString(R.string.yearly)
                else -> repeat.text = IntervalUtil.getInterval(this, reminder.repeatInterval)
            }
            repeatView.visibility = View.VISIBLE
        } else {
            timeView.visibility = View.GONE
            repeatView.visibility = View.GONE
        }
    }

    private fun showPhoneContact(reminder: Reminder) {
        val numberStr = reminder.target
        if (!TextUtils.isEmpty(numberStr)) {
            number.text = numberStr
            numberView.visibility = View.VISIBLE
        } else {
            number.visibility = View.GONE
            numberView.visibility = View.GONE
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
        if (file != null) melody.text = file.name
    }

    private fun showStatus(reminder: Reminder) {
        statusSwitch.isChecked = reminder.isActive
        if (!reminder.isActive) {
            statusText.setText(R.string.disabled)
        } else {
            statusText.setText(R.string.enabled4)
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

    private fun showAttachment(reminder: Reminder) {
        Timber.d("showAttachment: ${reminder.attachmentFile}")
        if (reminder.attachmentFile != "") {
            val uri = Uri.parse(reminder.attachmentFile)
            attachment.text = reminder.attachmentFile
            attachmentView.visibility = View.VISIBLE

            Picasso.get().load(uri).into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Timber.d("onPrepareLoad: ")
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Timber.d("onBitmapFailed: $e")
                    attachmentsView.visibility = View.GONE
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    Timber.d("onBitmapLoaded: ${bitmap != null}")
                    attachmentsView.visibility = View.VISIBLE
                    attachmentImage.setImageBitmap(bitmap)
                    attachmentsView.setOnClickListener {
                        val options = ActivityOptions.makeSceneTransitionAnimation(this@ReminderPreviewActivity, attachmentImage, "image")
                        startActivity(Intent(this@ReminderPreviewActivity, AttachmentPreviewActivity::class.java)
                                .putExtra(Constants.INTENT_ITEM, reminder.attachmentFile),
                                options.toBundle())
                    }
                }
            })
        } else {
            attachmentView.visibility = View.GONE
            attachmentsView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder_preview, menu)

        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isDark)
        ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_share_24px, isDark)
        ViewUtils.tintMenuIcon(this, menu, 2, R.drawable.ic_twotone_file_copy_24px, isDark)
        ViewUtils.tintMenuIcon(this, menu, 3, R.drawable.ic_twotone_delete_24px, isDark)

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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareReminder() {
        val reminder = reminder ?: return
        launchDefault {
            val path = backupTool.exportReminder(reminder)
            if (path != null) {
                withUIContext {
                    TelephonyUtil.sendFile(File(path), this@ReminderPreviewActivity)
                }
            }
        }
    }

    private fun editReminder() {
        val reminder = this.reminder
        if (reminder != null) {
            CreateReminderActivity.openLogged(this, Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId))
        }
    }

    private fun removeReminder() {
        val reminder = reminder
        if (reminder != null) {
            viewModel.moveToTrash(reminder)
        }
    }

    private fun makeCopy() {
        val reminder = reminder
        if (reminder != null) {
            val type = reminder.type
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
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.choose_time)
        builder.setItems(time.toTypedArray()) { dialog, which ->
            dialog.dismiss()
            saveCopy(which)
        }
        val alert = builder.create()
        alert.show()
    }

    private fun saveCopy(which: Int) {
        Timber.d("saveCopy: $which")
        val reminder = reminder ?: return
        viewModel.copyReminder(reminder, list[which], reminder.summary + " - copy")
    }

    private fun initViews() {
        switchWrapper.setOnClickListener { switchClick() }
        fab.setOnClickListener { fabClick() }
        mapContainer.visibility = View.GONE
        attachmentsView.visibility = View.GONE
        fab.visibility = View.GONE
    }

    private fun fabClick() {
        val reminder = this.reminder
        if (reminder != null) {
            if (reminder.isActive && !reminder.isRemoved) {
                when {
                    Reminder.isKind(reminder.type, Reminder.Kind.SMS) -> sendSMS(reminder)
                    Reminder.isKind(reminder.type, Reminder.Kind.CALL) -> makeCall(reminder)
                    Reminder.isSame(reminder.type, Reminder.BY_DATE_APP) -> openApp(reminder)
                    Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) -> openApp(reminder)
                    Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> sendEmail(reminder)
                }
            }
        }
    }

    private fun switchClick() {
        val reminder = this.reminder
        if (reminder != null) {
            viewModel.toggleReminder(reminder)
        }
    }

    private fun initMap() {
        val googleMap = AdvancedMapFragment.newInstance(false, false, false, false,
                false, false, themeUtil.isDark)
        googleMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                googleMap.setSearchEnabled(false)
                googleMap.setOnMapClickListener(GoogleMap.OnMapClickListener {
                    openFullMap()
                })
                googleMap.setOnMarkerClick(mOnMarkerClick)
                if (reminder != null) showMapData(reminder!!)
            }
        })
        supportFragmentManager.beginTransaction()
                .replace(mapContainer.id, googleMap)
                .addToBackStack(null)
                .commit()
        this.mGoogleMap = googleMap
    }

    private fun openFullMap() {
        val reminder = this.reminder
        if (reminder != null) {
            val options = ActivityOptions.makeSceneTransitionAnimation(this, mapContainer, "map")
            startActivity(Intent(this, FullscreenMapActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId), options.toBundle())
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        ViewUtils.listenScrollableView(scrollView) {
            appBar.isSelected = it > 0
        }
        toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                CALL_PERM -> fabClick()
                SMS_PERM -> fabClick()
            }
        }
    }

    companion object {
        private const val CALL_PERM = 612
        private const val SMS_PERM = 613
    }
}
