package com.elementary.tasks.reminder.preview

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
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

    private val list = ArrayList<Long>()
    private var mNote: Note? = null
    private var mGoogleTask: GoogleTask? = null
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var reminder: Reminder? = null
    private var shoppingAdapter = ShopListRecyclerAdapter()

    @Inject
    lateinit var reminderUtils: ReminderUtils

    private var mSendListener = { isSent: Boolean ->
        if (isSent) {
            finish()
        } else {
            showSendingError()
        }
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener {
        openFullMap()
        false
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
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, SMS_PERM, Permissions.SEND_SMS)
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
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(reminder.target, this)
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
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
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showTask() {
        val task = mGoogleTask
        if (task != null) {
            val binding = GoogleTaskHolder(dataContainer, null)
            binding.bind(task)
            binding.itemView.setOnClickListener {
                startActivity(Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
                        .putExtra(Constants.INTENT_ID, task.taskId)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
            }
            this.dataContainer.addView(binding.itemView)
        }
    }

    private fun showNote() {
        val note = mNote
        if (note != null) {
            val binding = NoteHolder(dataContainer, null)
//            binding.setData(note)
//            binding.itemView.setOnClickListener {
//                startActivity(Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
//                        .putExtra(Constants.INTENT_ID, note.key))
//            }
//            this.dataContainer.addView(binding.itemView)
        }
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

//    private val mReadyCallback = object : ReadyListener {
//        override fun onReady(`object`: Any?) {
//            if (`object` == null) return
//            if (`object` is Note) {
//                mNote = `object`
//                showNote()
//            } else if (`object` is GoogleTask) {
//                mGoogleTask = `object`
//                showTask()
//            }
//        }
//    }

    private fun showInfo(reminder: Reminder) {
        this.reminder = reminder

        Timber.d("showInfo: %s", reminder.toString())

        group.text = reminder.groupTitle
        showStatus(reminder)
        window_type_view.text = getWindowType(reminder.windowType)
        taskText.text = reminder.summary
        type.text = reminderUtils.getTypeString(reminder.type)
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
                    fab.setIconResource(R.drawable.ic_twotone_send_24px)
                    fab.text = getString(R.string.send_sms)
                    fab.visibility = View.VISIBLE
                }
                Reminder.isKind(reminder.type, Reminder.Kind.CALL) -> {
                    fab.setIconResource(R.drawable.ic_twotone_call_24px)
                    fab.text = getString(R.string.make_call)
                    fab.visibility = View.VISIBLE
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
            time.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormatEnabled, false)
            var repeatStr: String? = IntervalUtil.getInterval(this, reminder.repeatInterval)
            if (Reminder.isBase(reminder.type, Reminder.BY_WEEK)) {
                repeatStr = reminderUtils.getRepeatString(reminder.weekdays)
            }
            if (repeatStr != null) {
                repeat.text = repeatStr
                repeatView.visibility = View.VISIBLE
            } else {
                repeatView.visibility = View.GONE
            }
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

    private fun showAttachment(reminder: Reminder?) {
        if (reminder != null) {
            if (reminder.attachmentFile != "") {
                val file = File(reminder.attachmentFile)
                attachment.text = file.name
                attachmentView.visibility = View.VISIBLE
            } else {
                attachmentView.visibility = View.GONE
            }
        } else {
            attachmentView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder_preview, menu)
        val editIcon = ContextCompat.getDrawable(this, R.drawable.ic_twotone_edit_24px)
        val copyIcon = ContextCompat.getDrawable(this, R.drawable.ic_twotone_file_copy_24px)
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_twotone_delete_24px)
        if (isDark) {
            val white = ContextCompat.getColor(this, R.color.whitePrimary)
            DrawableCompat.setTint(editIcon!!, white)
            DrawableCompat.setTint(copyIcon!!, white)
            DrawableCompat.setTint(deleteIcon!!, white)
        } else {
            val black = ContextCompat.getColor(this, R.color.pureBlack)
            DrawableCompat.setTint(editIcon!!, black)
            DrawableCompat.setTint(copyIcon!!, black)
            DrawableCompat.setTint(deleteIcon!!, black)
        }
        menu.getItem(0)?.icon = editIcon
        menu.getItem(1)?.icon = copyIcon
        menu.getItem(2)?.icon = deleteIcon
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val ids = item.itemId
        when (ids) {
            R.id.action_delete -> {
                removeReminder()
                return true
            }
            android.R.id.home -> {
                closeWindow()
                return true
            }
            R.id.action_make_copy -> {
                makeCopy()
                return true
            }
            R.id.action_edit -> editReminder()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editReminder() {
        val reminder = this.reminder
        if (reminder != null) {
            startActivity(Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId))
        }
    }

    private fun removeReminder() {
        if (reminder != null) {
            viewModel.moveToTrash(reminder!!)
        }
    }

    private fun makeCopy() {
        if (reminder != null) {
            val type = reminder!!.type
            if (!Reminder.isGpsType(type) && !Reminder.isSame(type, Reminder.BY_TIME)) {
                showDialog()
            }
        }
    }

    private fun closeWindow() {
        if (Module.isLollipop) {
            mUiHandler.post { this.finishAfterTransition() }
        } else {
            finish()
        }
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
        val is24 = prefs.is24HourFormatEnabled
        do {
            if (hour == 23 && minute == 30) {
                hour = -1
            } else {
                val tmp = calendar.timeInMillis
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
                list.add(tmp)
                time.add(TimeUtil.getTime(calendar.time, is24))
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
        LogUtil.d(TAG, "saveCopy: $which")
        if (reminder != null) {
            viewModel.copyReminder(reminder!!, list[which], reminder!!.summary + " - copy")
        }
    }

    private fun initViews() {
        switchWrapper.setOnClickListener { switchClick() }
        fab.setOnClickListener { fabClick() }
        mapContainer.visibility = View.GONE
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
            startActivity(Intent(this, FullscreenMapActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder.uuId))
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        ViewUtils.listenScrollableView(scrollView) {
            appBar.isSelected = it > 0
        }
        if (isDark) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CALL_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fabClick()
            }
            SMS_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fabClick()
            }
        }
    }

    companion object {
        private const val CALL_PERM = 612
        private const val SMS_PERM = 613
        private const val TAG = "ReminderPreviewActivity"
    }
}
