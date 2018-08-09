package com.elementary.tasks.reminder.preview

import android.app.AlarmManager
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_reminder_preview.*
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

    @Inject
    lateinit var reminderUtils: ReminderUtils

    init {
        ReminderApp.appComponent.inject(this)
    }

    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener { marker ->
        mGoogleMap?.moveCamera(marker.position, 0, 0, 0, 0)
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

    private fun initViewModel(id: String) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this, Observer{ reminder ->
            if (reminder != null) {
                showInfo(reminder)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showTask() {
        if (mGoogleTask != null) {
            val binding = GoogleTaskHolder(dataContainer, null)
            binding.bind(mGoogleTask!!)
            binding.itemView.onClick {
                startActivity(Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
                        .putExtra(Constants.INTENT_ID, mGoogleTask!!.taskId)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
            }
            this.dataContainer.addView(binding.itemView)
        }
    }

    private fun showNote() {
        if (mNote != null) {
            val binding = NoteHolder(dataContainer, null)
            binding.setData(mNote!!)
            binding.itemView.setOnClickListener {
                startActivity(Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
                        .putExtra(Constants.INTENT_ID, mNote!!.key))
            }
            this.dataContainer.addView(binding.itemView)
        }
    }

    private fun showMapData(reminder: Reminder) {
        val place = reminder.places[0]
        val lat = place.latitude
        val lon = place.longitude
        mapContainer.visibility = View.VISIBLE
        location.visibility = View.VISIBLE
        location.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, reminder.places.size)
        mGoogleMap?.addMarker(LatLng(lat, lon), reminder.summary, true, true, place.radius)
    }

    private val mReadyCallback = object : ReadyListener {
        override fun onReady(it: Any?) {
            if (it == null) return
            if (it is Note) {
                mNote = it
                showNote()
            } else if (it is GoogleTask) {
                mGoogleTask = it
                showTask()
            }
        }
    }

    private fun showInfo(reminder: Reminder) {
        this.reminder = reminder
        statusSwitch.isChecked = reminder.isActive
        if (!reminder.isActive) {
            statusText.setText(R.string.disabled)
        } else {
            statusText.setText(R.string.enabled4)
        }
        window_type_view.text = getWindowType(reminder.windowType)
        taskText.text = reminder.summary
        type.text = reminderUtils.getTypeString(reminder.type)
        itemPhoto.setImageResource(themeUtil.getReminderIllustration(reminder.type))
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            time.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormatEnabled, false)
            var repeatStr: String? = IntervalUtil.getInterval(this, reminder.repeatInterval)
            if (Reminder.isBase(reminder.type, Reminder.BY_WEEK)) {
                repeatStr = reminderUtils.getRepeatString(reminder.weekdays)
            }
            if (repeatStr != null) {
                repeat.text = repeatStr
            } else {
                repeat.visibility = View.GONE
            }
        } else {
            time.visibility = View.GONE
            repeat.visibility = View.GONE
        }
        if (Reminder.isGpsType(reminder.type)) {
            initMap()
        } else {
            location.visibility = View.GONE
            mapContainer.visibility = View.GONE
        }
        val numberStr = reminder.target
        if (!TextUtils.isEmpty(numberStr)) {
            number.text = numberStr
        } else {
            number.visibility = View.GONE
        }

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

        var catColor = 0
        if (reminder.group != null) {
            group.text = reminder.group!!.title
            catColor = reminder.group!!.color
        }
        val mColor = themeUtil.getColor(themeUtil.getCategoryColor(catColor))
        appBar.setBackgroundColor(mColor)
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getNoteDarkColor(catColor)
        }
        dataContainer.removeAllViewsInLayout()
        showAttachment(reminder)
        Thread(NoteThread(mReadyCallback, reminder.noteId)).start()
        Thread(TaskThread(mReadyCallback, reminder.uuId)).start()
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
                attachment_view.text = file.name
                attachment_view.visibility = View.VISIBLE
            } else {
                attachment_view.visibility = View.GONE
            }
        } else {
            attachment_view.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder_preview, menu)
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
        if (reminder != null) {
            startActivity(Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, reminder!!.uniqueId))
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
            viewModel.copyReminder(reminder!!, list[which], reminder!!.summary + " - cope")
        }
    }

    private fun initViews() {
        switchWrapper.setOnClickListener { switchClick() }
        mapContainer.visibility = View.GONE
    }

    private fun switchClick() {
        if (reminder != null) {
            viewModel.toggleReminder(reminder!!)
        }
    }

    private fun initMap() {
        val googleMap = AdvancedMapFragment.newInstance(false, false, false,
                false, prefs.markerStyle, themeUtil.isDark)
        googleMap.setCallback(object : MapCallback {
            override fun onMapReady() {
                googleMap.setSearchEnabled(false)
                if (reminder != null) showMapData(reminder!!)
            }
        })
        googleMap.setOnMarkerClick(mOnMarkerClick)
        supportFragmentManager.beginTransaction()
                .replace(mapContainer.id, googleMap)
                .addToBackStack(null)
                .commit()
        this.mGoogleMap = googleMap
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onBackPressed() {
        finish()
    }

    private inner class NoteThread internal constructor(private val listener: ReadyListener?, private val uuId: String) : Runnable {

        override fun run() {
            val item = AppDb.getAppDatabase(this@ReminderPreviewActivity).notesDao().getById(uuId)
            runOnUiThread { if (listener != null && item != null) listener.onReady(item) }
        }
    }

    private inner class TaskThread internal constructor(private val listener: ReadyListener?, private val uuId: String) : Runnable {

        override fun run() {
            val item = AppDb.getAppDatabase(this@ReminderPreviewActivity).googleTasksDao().getByReminderId(uuId)
            runOnUiThread { if (listener != null && item != null) listener.onReady(item) }
        }
    }

    internal interface ReadyListener {
        fun onReady(`object`: Any?)
    }

    companion object {
        private const val TAG = "ReminderPreviewActivity"
    }
}
