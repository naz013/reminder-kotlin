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
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.core.views.GridMarginDecoration
import com.elementary.tasks.notes.create.CreateNoteActivity
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

    private var mNote: NoteWithImages? = null
    private var mReminder: Reminder? = null
    private var mId: String = ""

    private val mAdapter = ImagesGridAdapter()
    private lateinit var viewModel: NoteViewModel

    private var mProgress: ProgressDialog? = null
    private val mUiHandler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var backupTool: BackupTool
    @Inject
    lateinit var imagesSingleton: ImagesSingleton

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mId = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        setContentView(R.layout.activity_note_preview)
        initActionBar()
        initImagesList()
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
        mAdapter.actionsListener = object : ActionsListener<ImageFile> {
            override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
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
        imagesSingleton.setCurrent(mAdapter.data)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""

        if (isDark) {
            toolbar.setNavigationIcon(R.drawable.ic_twotone_arrow_white_24px)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_twotone_arrow_back_24px)
        }
    }

    private fun editNote() {
        val noteWithImages = mNote
        if (noteWithImages != null) {
            startActivity(Intent(this, CreateNoteActivity::class.java)
                    .putExtra(Constants.INTENT_ID, noteWithImages.note?.key))
        }
    }

    private fun moveToStatus() {
        val noteWithImages = mNote
        if (noteWithImages != null) {
            notifier.showNoteNotification(noteWithImages)
        }
    }

    override fun onBackPressed() {
        closeWindow()
    }

    private fun showNote(noteWithImages: NoteWithImages?) {
        this.mNote = noteWithImages
        if (noteWithImages != null) {
            showImages(noteWithImages.images)
            noteText.text = noteWithImages.getSummary()
            noteText.typeface = AssetsUtil.getTypeface(this, noteWithImages.getStyle())
            if (Module.isLollipop) {
                window.statusBarColor = themeUtil.getNoteLightColor(noteWithImages.getColor(), noteWithImages.getOpacity())
            }
            windowBackground.setBackgroundColor(themeUtil.getNoteLightColor(noteWithImages.getColor(), noteWithImages.getOpacity()))
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

    private fun showImages(images: List<ImageFile>) {
        if (!images.isEmpty()) {
            mAdapter.setImages(images)
        }
    }

    private fun hideProgress() {
        if (mProgress != null && mProgress?.isShowing == true) {
            mProgress?.dismiss()
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
        launchDefault {
            val file = backupTool.createNote(mNote)
            withUIContext {
                if (file != null) sendNote(file)
            }
        }
    }

    private fun sendNote(file: File) {
        hideProgress()
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        val noteWithImages = mNote
        if (noteWithImages != null) {
            TelephonyUtil.sendNote(file, this, noteWithImages.note?.summary)
            closeWindow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.preview_note_menu, menu)
        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isDark)
        ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_favorite_24px, isDark)
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
        val reminder = mReminder ?: return
        viewModel.deleteReminder(reminder)
        reminderContainer.visibility = View.GONE
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

        private const val SEND_CODE = 25501
    }
}
