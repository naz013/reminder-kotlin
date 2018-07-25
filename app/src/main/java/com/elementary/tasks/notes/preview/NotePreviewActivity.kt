package com.elementary.tasks.notes.preview

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.create.NoteImage
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import kotlinx.android.synthetic.main.activity_note_preview.*
import java.io.File
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

class NotePreviewActivity : ThemedActivity() {

    private var mNote: Note? = null
    private var mReminder: Reminder? = null
    private var mId: String = ""

    private val mAdapter = ImagesGridAdapter()
    private lateinit var viewModel: NoteViewModel

    private var mProgress: ProgressDialog? = null
    private val mUiHandler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mId = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        setContentView(R.layout.activity_note_preview)
        initActionBar()
        initImagesList()
        initScrollView()
        initReminderCard()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(application, mId)).get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer{ note ->
            if (note != null) {
                showNote(note)
            }
        })
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                showReminder(reminder)
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

    private fun initReminderCard() {
        reminderContainer.visibility = View.GONE
        editReminder.setOnClickListener { editReminder() }
        deleteReminder.setOnClickListener { showReminderDeleteDialog() }
    }

    private fun editReminder() {
        if (mReminder != null) {
            startActivity(Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, mReminder?.uuId))
        }
    }

    private fun initImagesList() {
        mAdapter.actionsListener = object : ActionsListener<NoteImage> {
            override fun onAction(view: View, position: Int, t: NoteImage?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> openImagePreview(position)
                }
            }
        }
        imagesList.layoutManager = KeepLayoutManager(this, 6, mAdapter)
        imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        imagesList.adapter = mAdapter
    }

    private fun openImagePreview(position: Int) {
        val note = Note()
        note.key = PREVIEW_IMAGES
        note.images = mAdapter.data
        viewModel.saveNote(note)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, note.key)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun initScrollView() {
        scrollContent.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val scrollY = scrollContent.scrollY
                if (!mNote!!.images.isEmpty()) {
                    appBar.background.alpha = getAlphaForActionBar(scrollY)
                } else {
                    appBar.background.alpha = 255
                }
            }

            private fun getAlphaForActionBar(scrollY: Int): Int {
                val minDist = 0
                val maxDist = MeasureUtils.dp2px(this@NotePreviewActivity, 200)
                return when {
                    scrollY > maxDist -> 255
                    scrollY < minDist -> 0
                    else -> (255.0 / maxDist * scrollY).toInt()
                }
            }
        })
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = ""
    }

    private fun editNote() {
        startActivity(Intent(this, CreateNoteActivity::class.java)
                .putExtra(Constants.INTENT_ID, mNote?.key))
    }

    private fun moveToStatus() {
        if (mNote != null) {
            notifier.showNoteNotification(mNote!!)
        }
    }

    override fun onBackPressed() {
        closeWindow()
    }

    private fun showNote(note: Note?) {
        this.mNote = note
        if (note != null) {
            noteText.text = note.summary
            noteText.typeface = AssetsUtil.getTypeface(this, note.style)
            if (Module.isLollipop) {
                window.statusBarColor = themeUtil.getNoteDarkColor(note.color)
            }
            scrollContent.setBackgroundColor(themeUtil.getNoteLightColor(note.color))
            showImages(note.images)
        }
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null) {
            val dateTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime, prefs.is24HourFormatEnabled)
            reminderTime.text = dateTime
            reminderContainer.visibility = View.VISIBLE
        }
    }

    private fun showImages(images: List<NoteImage>) {
        if (!images.isEmpty()) {
            mAdapter.setImages(images)
            appBar.setBackgroundColor(themeUtil.getNoteColor(mNote!!.color))
            appBar.background.alpha = 0
        } else {
            appBar.setBackgroundColor(themeUtil.getNoteColor(mNote!!.color))
            appBar.background.alpha = 255
        }
    }

    private fun hideProgress() {
        if (mProgress != null && mProgress!!.isShowing) {
            mProgress!!.dismiss()
        }
    }

    private fun showProgress() {
        mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    private fun shareNote() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, SEND_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
        showProgress()
        Thread { backupTool.createNote(mNote, object : BackupTool.CreateCallback {
            override fun onReady(file: File?) {
                if (file != null) sendNote(file)
            }
        }) }.start()
    }

    private fun sendNote(file: File) {
        hideProgress()
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        TelephonyUtil.sendNote(file, this, mNote!!.summary)
        closeWindow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.preview_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                closeWindow()
                return true
            }
            R.id.action_share -> {
                shareNote()
                return true
            }
            R.id.action_delete -> {
                showDeleteDialog()
                return true
            }
            R.id.action_status -> {
                moveToStatus()
                return true
            }
            R.id.action_edit -> {
                editNote()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun closeWindow() {
        if (Module.isLollipop) {
            mUiHandler.post { this.finishAfterTransition() }
        } else {
            finish()
        }
    }

    private fun showDeleteDialog() {
        val builder = dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_note))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            if (mNote != null) viewModel.deleteNote(mNote!!)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showReminderDeleteDialog() {
        val builder = dialogues.getDialog(this)
        builder.setMessage(R.string.delete_this_reminder)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteReminder()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteReminder() {
        if (mReminder != null) {
            viewModel.deleteReminder(mReminder!!)
            reminderContainer.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            SEND_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareNote()
            }
        }
    }

    companion object {

        const val PREVIEW_IMAGES = "preview_image_key"
        private const val SEND_CODE = 25501
    }
}
