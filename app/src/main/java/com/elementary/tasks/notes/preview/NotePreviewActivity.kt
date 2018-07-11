package com.elementary.tasks.notes.preview

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.databinding.ActivityNotePreviewBinding
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.create.NoteImage
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity

import java.io.File
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

class NotePreviewActivity : ThemedActivity() {

    private var mNote: Note? = null
    private var mReminder: Reminder? = null
    private var mId: String? = null

    private val mAdapter = ImagesGridAdapter()
    private var binding: ActivityNotePreviewBinding? = null
    private var viewModel: NoteViewModel? = null

    private var mProgress: ProgressDialog? = null

    private val mUiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mId = intent.getStringExtra(Constants.INTENT_ID)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_preview)
        initActionBar()
        initImagesList()
        initScrollView()
        initReminderCard()

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(application, mId)).get(NoteViewModel::class.java)
        viewModel!!.note.observe(this, { note ->
            if (note != null) {
                showNote(note)
            }
        })
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showReminder(reminder)
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

    private fun initReminderCard() {
        binding!!.reminderContainer.visibility = View.GONE
        binding!!.editReminder.setOnClickListener { v -> editReminder() }
        binding!!.deleteReminder.setOnClickListener { v -> showReminderDeleteDialog() }
    }

    private fun editReminder() {
        if (mReminder != null) {
            startActivity(Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, mReminder!!.uuId))
        }
    }

    private fun initImagesList() {
        mAdapter.actionsListener = { view, position, noteImage, actions ->
            when (actions) {
                ListActions.OPEN -> openImagePreview(position)
            }
        }
        binding!!.imagesList.layoutManager = KeepLayoutManager(this, 6, mAdapter)
        binding!!.imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        binding!!.imagesList.adapter = mAdapter
    }

    private fun openImagePreview(position: Int) {
        val note = Note()
        note.key = PREVIEW_IMAGES
        note.images = mAdapter.data
        viewModel!!.saveNote(note)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, note.key)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun initScrollView() {
        binding!!.scrollContent.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val scrollY = binding!!.scrollContent.scrollY
                if (!mNote!!.images.isEmpty()) {
                    binding!!.appBar.background.alpha = getAlphaForActionBar(scrollY)
                } else {
                    binding!!.appBar.background.alpha = 255
                }
            }

            private fun getAlphaForActionBar(scrollY: Int): Int {
                val minDist = 0
                val maxDist = MeasureUtils.dp2px(this@NotePreviewActivity, 200)
                return if (scrollY > maxDist) {
                    255
                } else if (scrollY < minDist) {
                    0
                } else {
                    (255.0 / maxDist * scrollY).toInt()
                }
            }
        })
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.toolbar.title = ""
    }

    private fun editNote() {
        startActivity(Intent(this@NotePreviewActivity, CreateNoteActivity::class.java)
                .putExtra(Constants.INTENT_ID, mNote!!.key))
    }

    private fun moveToStatus() {
        if (mNote != null) {
            Notifier(this).showNoteNotification(mNote!!)
        }
    }

    override fun onBackPressed() {
        closeWindow()
    }

    private fun showNote(note: Note?) {
        this.mNote = note
        if (note != null) {
            binding!!.noteText.text = note.summary
            binding!!.noteText.typeface = AssetsUtil.getTypeface(this, note.style)
            if (Module.isLollipop) {
                window.statusBarColor = themeUtil!!.getNoteDarkColor(note.color)
            }
            binding!!.scrollContent.setBackgroundColor(themeUtil!!.getNoteLightColor(note.color))
            showImages(note.images)
        }
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null) {
            val dateTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime, prefs!!.is24HourFormatEnabled)
            binding!!.reminderTime.text = dateTime
            binding!!.reminderContainer.visibility = View.VISIBLE
        }
    }

    private fun showImages(images: List<NoteImage>) {
        if (!images.isEmpty()) {
            mAdapter.setImages(images)
            binding!!.appBar.setBackgroundColor(themeUtil!!.getNoteColor(mNote!!.color))
            binding!!.appBar.background.alpha = 0
        } else {
            binding!!.appBar.setBackgroundColor(themeUtil!!.getNoteColor(mNote!!.color))
            binding!!.appBar.background.alpha = 255
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
        showProgress()
        val callback = BackupTool.CreateCallback { this.sendNote(it) }
        Thread { BackupTool.getInstance().createNote(mNote, callback) }.start()
    }

    private fun sendNote(file: File) {
        hideProgress()
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        TelephonyUtil.sendNote(file, this, mNote!!.summary)
        closeWindow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.preview_note_menu, menu)
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
        val builder = Dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_note))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            dialog.dismiss()
            if (mNote != null) viewModel!!.deleteNote(mNote!!)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showReminderDeleteDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(R.string.delete_this_reminder)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, which ->
            dialog.dismiss()
            deleteReminder()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteReminder() {
        if (mReminder != null) {
            viewModel!!.deleteReminder(mReminder!!)
            binding!!.reminderContainer.visibility = View.GONE
        }
    }

    companion object {

        val PREVIEW_IMAGES = "preview_image_key"
    }
}
