package com.elementary.tasks.notes

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import kotlinx.android.synthetic.main.view_note_card.view.*
import kotlinx.android.synthetic.main.view_note_reminder_card.view.*
import kotlinx.android.synthetic.main.view_note_status_card.view.*
import java.util.*

/**
 * Copyright 2017 Nazar Suhovich
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
class QuickNoteCoordinator(private val context: Context, private val parent: ViewGroup,
                           private val noteList: ViewGroup,
                           private var noteViewModel: NoteViewModel,
                           private val prefs: Prefs,
                           private val notifier: Notifier) {

    val isNoteVisible: Boolean
        get() = parent.visibility == View.VISIBLE

    init {
        parent.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (isNoteVisible) {
                    hideNoteView()
                }
                return@setOnTouchListener true
            }
            false
        }
        parent.visibility = View.GONE
    }

    fun switchQuickNote() {
        if (isNoteVisible) {
            hideNoteView()
        } else {
            showNoteView()
        }
    }

    fun hideNoteView() {
        parent.visibility = View.GONE
        noteList.removeAllViewsInLayout()
    }

    private fun showNoteView() {
        parent.visibility = View.VISIBLE
        Handler().postDelayed({ this.addFirstCard() }, 250)
    }

    private fun addFirstCard() {
        val binding = LayoutInflater.from(context).inflate(R.layout.view_note_card, noteList, false)
        binding.buttonSave.setOnClickListener { saveNote(binding) }
        binding.noteCard.visibility = View.GONE

        noteList.addView(binding)
        binding.noteCard.visibility = View.VISIBLE
    }

    private fun saveNote(binding: View) {
        val text = binding.quickNote.text.toString().trim()
        if (TextUtils.isEmpty(text)) {
            binding.nameLayout.error = context.getString(R.string.must_be_not_empty)
            binding.nameLayout.isErrorEnabled = true
            return
        }
        binding.quickNote.isEnabled = false
        binding.buttonSave.isEnabled = false
        val item = Note()
        item.summary = text
        item.date = TimeUtil.gmtDateTime
        if (prefs.isNoteColorRememberingEnabled) {
            item.color = prefs.lastNoteColor
        } else {
            item.color = Random().nextInt(16)
        }
        val noteWithImages = NoteWithImages()
        noteWithImages.note = item

        if (prefs.isNoteReminderEnabled) {
            addReminderCard(noteWithImages)
        } else {
            noteViewModel.saveNote(noteWithImages)
            addNotificationCard(noteWithImages)
        }
    }

    private fun addReminderCard(item: NoteWithImages) {
        val cardBinding = LayoutInflater.from(context).inflate(R.layout.view_note_reminder_card, noteList, false)

        cardBinding.buttonYes.setOnClickListener {
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            addReminderToNote(item)
        }
        cardBinding.buttonNo.setOnClickListener {
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            noteViewModel.saveNote(item)
            addNotificationCard(item)
        }
        cardBinding.noteReminderCard.visibility = View.GONE

        noteList.addView(cardBinding)
        cardBinding.noteReminderCard.visibility = View.VISIBLE
    }

    private fun addReminderToNote(item: NoteWithImages) {
        val note = item.note
        if (note == null) {
            hideNoteView()
            return
        }

        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.useGlobal = true
        reminder.noteId = note.key
        reminder.isActive = true
        reminder.isRemoved = false
        reminder.summary = note.summary
        val prefsTime = prefs.noteReminderTime * TimeCount.MINUTE
        val startTime = System.currentTimeMillis() + prefsTime
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)

        noteViewModel.saveNote(item, reminder)
        addNotificationCard(item)
    }

    private fun addNotificationCard(item: NoteWithImages) {
        val cardBinding = LayoutInflater.from(context).inflate(R.layout.view_note_status_card, noteList, false)

        cardBinding.buttonYesStatus.setOnClickListener {
            cardBinding.buttonNoStatus.isEnabled = false
            cardBinding.buttonYesStatus.isEnabled = false
            showInStatusBar(item)
        }
        cardBinding.buttonNoStatus.setOnClickListener { hideNoteView() }
        cardBinding.noteStatusCard.visibility = View.GONE

        noteList.addView(cardBinding)
        cardBinding.noteStatusCard.visibility = View.VISIBLE
    }

    private fun showInStatusBar(item: NoteWithImages) {
        notifier.showNoteNotification(item)
        hideNoteView()
    }
}
