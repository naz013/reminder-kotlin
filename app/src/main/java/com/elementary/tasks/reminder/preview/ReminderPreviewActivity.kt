package com.elementary.tasks.reminder.preview

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding
import com.elementary.tasks.databinding.ListItemTaskBinding
import com.elementary.tasks.databinding.ListItemNoteBinding
import com.elementary.tasks.google_tasks.TaskActivity
import com.elementary.tasks.google_tasks.TasksConstants
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

import java.io.File
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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

    private var binding: ActivityReminderPreviewBinding? = null
    private var mGoogleMap: AdvancedMapFragment? = null
    private var viewModel: ReminderViewModel? = null

    private val list = ArrayList<Long>()
    private var mNote: Note? = null
    private var mGoogleTask: GoogleTask? = null
    private val mUiHandler = Handler(Looper.getMainLooper())
    private var reminder: Reminder? = null

    private val mReadyCallback = { `object` ->
        if (`object` == null) return
        if (`object` is Note) {
            this.mNote = `object`
            showNote()
        } else if (`object` is GoogleTask) {
            this.mGoogleTask = `object`
            showTask()
        }
    }
    private val mMapReadyCallback = MapCallback {
        mGoogleMap!!.setSearchEnabled(false)
        if (reminder != null) showMapData(reminder!!)
    }
    private val mOnMarkerClick = GoogleMap.OnMarkerClickListener { marker ->
        mGoogleMap!!.moveCamera(marker.position, 0, 0, 0, 0)
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(Constants.INTENT_ID, 0)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_preview)
        initActionBar()
        initViews()

        initViewModel(id)
    }

    private fun initViewModel(id: Int) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showInfo(reminder)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showTask() {
        if (mGoogleTask != null) {
            val binding = ListItemTaskBinding.inflate(LayoutInflater.from(this))
            binding.googleTask = mGoogleTask
            binding.setClick { v ->
                startActivity(Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
                        .putExtra(Constants.INTENT_ID, mGoogleTask!!.taskId)
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
            }
            this.binding!!.dataContainer.addView(binding.root)
        }
    }

    private fun showNote() {
        if (mNote != null) {
            val binding = ListItemNoteBinding.inflate(LayoutInflater.from(this))
            binding.note = mNote
            binding.noteClick.setOnClickListener { v ->
                startActivity(Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
                        .putExtra(Constants.INTENT_ID, mNote!!.key))
            }
            this.binding!!.dataContainer.addView(binding.root)
        }
    }

    private fun showMapData(reminder: Reminder) {
        val place = reminder.places[0]
        val lat = place.latitude
        val lon = place.longitude
        binding!!.mapContainer.visibility = View.VISIBLE
        binding!!.location.visibility = View.VISIBLE
        binding!!.location.text = String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.latitude, place.longitude, reminder.places.size)
        mGoogleMap!!.addMarker(LatLng(lat, lon), reminder.summary, true, true, place.radius)
    }

    private fun showInfo(reminder: Reminder?) {
        this.reminder = reminder
        if (reminder != null) {
            binding!!.statusSwitch.isChecked = reminder.isActive
            if (!reminder.isActive) {
                binding!!.statusText.setText(R.string.disabled)
            } else {
                binding!!.statusText.setText(R.string.enabled4)
            }
            binding!!.windowTypeView.text = getWindowType(reminder.windowType)
            binding!!.taskText.text = reminder.summary
            binding!!.type.text = ReminderUtils.getTypeString(this, reminder.type)
            binding!!.itemPhoto.setImageResource(themeUtil!!.getReminderIllustration(reminder.type))
            val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            if (due > 0) {
                binding!!.time.text = TimeUtil.getFullDateTime(due, prefs!!.is24HourFormatEnabled, false)
                var repeatStr: String? = IntervalUtil.getInterval(this, reminder.repeatInterval)
                if (Reminder.isBase(reminder.type, Reminder.BY_WEEK)) {
                    repeatStr = ReminderUtils.getRepeatString(this, reminder.weekdays)
                }
                if (repeatStr != null) {
                    binding!!.repeat.text = repeatStr
                } else {
                    binding!!.repeat.visibility = View.GONE
                }
            } else {
                binding!!.time.visibility = View.GONE
                binding!!.repeat.visibility = View.GONE
            }
            if (Reminder.isGpsType(reminder.type)) {
                initMap()
            } else {
                binding!!.location.visibility = View.GONE
                binding!!.mapContainer.visibility = View.GONE
            }
            val numberStr = reminder.target
            if (!TextUtils.isEmpty(numberStr)) {
                binding!!.number.text = numberStr
            } else {
                binding!!.number.visibility = View.GONE
            }

            var file: File? = null
            if (!TextUtils.isEmpty(reminder.melodyPath)) {
                file = File(reminder.melodyPath!!)
            } else {
                val path = prefs!!.melodyFile
                if (path != null && !Sound.isDefaultMelody(path)) {
                    file = File(path)
                } else {
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (soundUri != null && soundUri.path != null) {
                        file = File(soundUri.path!!)
                    }
                }
            }
            if (file != null) binding!!.melody.text = file.name

            var catColor = 0
            if (reminder.group != null) {
                binding!!.group.text = reminder.group!!.title
                catColor = reminder.group!!.color
            }
            val mColor = themeUtil!!.getColor(themeUtil!!.getCategoryColor(catColor))
            binding!!.appBar.setBackgroundColor(mColor)
            if (Module.isLollipop) {
                window.statusBarColor = themeUtil!!.getNoteDarkColor(catColor)
            }
            binding!!.dataContainer.removeAllViewsInLayout()
            showAttachment(reminder)
            Thread(NoteThread(mReadyCallback, reminder.noteId)).start()
            Thread(TaskThread(mReadyCallback, reminder.uuId)).start()
        }
    }

    private fun getWindowType(reminderWType: Int): String {
        var windowType = Prefs.getInstance(this).reminderType
        val ignore = Prefs.getInstance(this).isIgnoreWindowType
        if (!ignore) {
            windowType = reminderWType
        }
        return if (windowType == 0) getString(R.string.full_screen) else getString(R.string.simple)
    }

    private fun showAttachment(reminder: Reminder?) {
        if (reminder != null) {
            if (reminder.attachmentFile != null) {
                val file = File(reminder.attachmentFile!!)
                binding!!.attachmentView.text = file.name
                binding!!.attachmentView.visibility = View.VISIBLE
            } else {
                binding!!.attachmentView.visibility = View.GONE
            }
        } else {
            binding!!.attachmentView.visibility = View.GONE
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
            startActivity(Intent(this, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, reminder!!.uniqueId))
        }
    }

    private fun removeReminder() {
        if (reminder != null) {
            viewModel!!.moveToTrash(reminder!!)
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

    fun showDialog() {
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
        val is24 = prefs!!.is24HourFormatEnabled
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
        val builder = Dialogues.getDialog(this)
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
            viewModel!!.copyReminder(reminder!!, list[which], reminder!!.summary!! + " - cope")
        }
    }

    private fun initViews() {
        binding!!.switchWrapper.setOnClickListener { v -> switchClick() }
        binding!!.mapContainer.visibility = View.GONE
    }

    private fun switchClick() {
        if (reminder != null) {
            viewModel!!.toggleReminder(reminder!!)
        }
    }

    private fun initMap() {
        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false, prefs!!.markerStyle, themeUtil!!.isDark)
        mGoogleMap!!.setCallback(mMapReadyCallback)
        mGoogleMap!!.setOnMarkerClick(mOnMarkerClick)
        supportFragmentManager.beginTransaction()
                .replace(binding!!.mapContainer.id, mGoogleMap!!)
                .addToBackStack(null)
                .commit()
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
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

        private val TAG = "ReminderPreviewActivity"
    }
}
