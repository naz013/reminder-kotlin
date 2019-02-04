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
class ReminderPreviewActivity : ThemedActivity<ActivityReminderPreviewBinding>() {

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

    override fun layoutRes(): Int = R.layout.activity_reminder_preview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        binding.dataContainer.removeAllViewsInLayout()
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
        binding.mapContainer.visibility = View.VISIBLE
        binding.location.visibility = View.VISIBLE

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
        binding.itemPhoto.setImageResource(themeUtil.getReminderIllustration(reminder.type))
        binding.idView.setText(reminder.uuId)

        showDueAndRepeat(reminder)
        showBefore(reminder)
        showPhoneContact(reminder)
        showMelody(reminder)
        showAttachment(reminder)
        if (Reminder.isGpsType(reminder.type)) {
            initMap()
        } else {
            binding.locationView.visibility = View.GONE
            binding.mapContainer.visibility = View.GONE
        }
        if (reminder.shoppings.isNotEmpty()) {
            binding.todoList.visibility = View.VISIBLE
            loadData(reminder)
        } else {
            binding.todoList.visibility = View.GONE
        }
        if (reminder.isActive && !reminder.isRemoved) {
            when {
                Reminder.isKind(reminder.type, Reminder.Kind.SMS) -> {
                    if (prefs.isTelephonyAllowed) {
                        binding.fab.setIconResource(R.drawable.ic_twotone_send_24px)
                        binding.fab.text = getString(R.string.send_sms)
                        binding.fab.visibility = View.VISIBLE
                    } else {
                        binding.fab.visibility = View.GONE
                    }
                }
                Reminder.isKind(reminder.type, Reminder.Kind.CALL) -> {
                    if (prefs.isTelephonyAllowed) {
                        binding.fab.setIconResource(R.drawable.ic_twotone_call_24px)
                        binding.fab.text = getString(R.string.make_call)
                        binding.fab.visibility = View.VISIBLE
                    } else {
                        binding.fab.visibility = View.GONE
                    }
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_APP) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_open_in_new_24px)
                    binding.fab.text = getString(R.string.open_app)
                    binding.fab.visibility = View.VISIBLE
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_open_in_browser_24px)
                    binding.fab.text = getString(R.string.open_link)
                    binding.fab.visibility = View.VISIBLE
                }
                Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> {
                    binding.fab.setIconResource(R.drawable.ic_twotone_local_post_office_24px)
                    binding.fab.text = getString(R.string.send)
                    binding.fab.visibility = View.VISIBLE
                }
                else -> binding.fab.visibility = View.GONE
            }
        } else {
            binding.fab.visibility = View.GONE
        }

        binding.dataContainer.removeAllViewsInLayout()
    }

    private fun showBefore(reminder: Reminder) {
        val beforeStr = IntervalUtil.getInterval(this, reminder.remindBefore)
        if (beforeStr.isEmpty() || beforeStr == "0") {
            binding.beforeView.visibility = View.GONE
        } else {
            binding.beforeView.visibility = View.VISIBLE
            binding.before.text = beforeStr
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
        binding.todoList.layoutManager = LinearLayoutManager(this)
        binding.todoList.isNestedScrollingEnabled = false
        binding.todoList.adapter = shoppingAdapter
    }

    private fun showDueAndRepeat(reminder: Reminder) {
        val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        if (due > 0) {
            binding.timeView.visibility = View.VISIBLE
            binding.time.text = TimeUtil.getFullDateTime(due, prefs.is24HourFormat, prefs.appLanguage)
            when {
                Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> binding.repeat.text = String.format(getString(R.string.xM), reminder.repeatInterval.toString())
                Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> binding.repeat.text = ReminderUtils.getRepeatString(this, prefs, reminder.weekdays)
                Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> binding.repeat.text = getString(R.string.yearly)
                else -> binding.repeat.text = IntervalUtil.getInterval(this, reminder.repeatInterval)
            }
            binding.repeatView.visibility = View.VISIBLE
        } else {
            binding.timeView.visibility = View.GONE
            binding.repeatView.visibility = View.GONE
        }
    }

    private fun showPhoneContact(reminder: Reminder) {
        val numberStr = reminder.target
        if (!TextUtils.isEmpty(numberStr)) {
            binding.number.text = numberStr
            binding.numberView.visibility = View.VISIBLE
        } else {
            binding.number.visibility = View.GONE
            binding.numberView.visibility = View.GONE
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
            binding.attachmentsView.visibility = View.GONE
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            Timber.d("onBitmapLoaded: ${bitmap != null}")
            binding.attachmentsView.visibility = View.VISIBLE
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
            binding.attachmentView.visibility = View.VISIBLE
            val file = File(reminder.attachmentFile)
            if (file.exists()) {
                Picasso.get().load(file).into(imageTarget)
            } else {
                val uri = Uri.parse(reminder.attachmentFile)
                Picasso.get().load(uri).into(imageTarget)
            }
        } else {
            binding.attachmentView.visibility = View.GONE
            binding.attachmentsView.visibility = View.GONE
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
        if (!Permissions.ensurePermissions(this, SD_PERM, Permissions.WRITE_EXTERNAL)) {
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
        reminder?.let { viewModel.moveToTrash(it) }
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
        reminder?.let {
            viewModel.copyReminder(it, list[which], it.summary + " - " + getString(R.string.copy))
        }
    }

    private fun initViews() {
        binding.switchWrapper.setOnClickListener { switchClick() }
        binding.fab.setOnClickListener { fabClick() }
        binding.mapContainer.visibility = View.GONE
        binding.attachmentsView.visibility = View.GONE
        binding.fab.visibility = View.GONE
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
        reminder?.let { viewModel.toggleReminder(it) }
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
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
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
                SD_PERM -> shareReminder()
            }
        }
    }

    companion object {
        private const val CALL_PERM = 612
        private const val SMS_PERM = 613
        private const val SD_PERM = 614
    }
}
