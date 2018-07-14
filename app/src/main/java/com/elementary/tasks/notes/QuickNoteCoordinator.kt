package com.elementary.tasks.notes

import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import kotlinx.android.synthetic.main.activity_main.view.*
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
class QuickNoteCoordinator(private val mContext: FragmentActivity, private val binding: View, private val mCallback: Callback?) {
    private val themeUtil: ThemeUtil = ThemeUtil.getInstance(mContext)
    private var reminderViewModel: ReminderViewModel? = null
    private var noteViewModel: NoteViewModel? = null

    private var mNote: Note? = null

    val isNoteVisible: Boolean
        get() = binding.quickNoteContainer.visibility == View.VISIBLE

    init {
        this.binding.quickNoteContainer.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (isNoteVisible) {
                    hideNoteView()
                }
                return@setOnTouchListener true
            }
            false
        }
        this.binding.quickNoteContainer.visibility = View.GONE

        initReminderViewModel()
        initNoteViewModel()
    }

    private fun initNoteViewModel() {
        noteViewModel = ViewModelProviders.of(mContext).get(NoteViewModel::class.java)
        noteViewModel!!.result.observe(mContext, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> if (Prefs.getInstance(mContext).isNoteReminderEnabled) {
                        if (mNote != null) addReminderCard(mNote!!)
                    } else {
                        if (mNote != null) addNotificationCard(mNote!!)
                    }
                }
            }
        })
    }

    private fun initReminderViewModel() {
        reminderViewModel = ViewModelProviders.of(mContext).get(ReminderViewModel::class.java)
        reminderViewModel!!.result.observe(mContext, Observer{ commands ->
            if (commands != null && commands == Commands.SAVED) {
                if (mNote != null) addNotificationCard(mNote!!)
            }
        })
    }

    fun switchQuickNote() {
        if (isNoteVisible) {
            hideNoteView()
        } else {
            showNoteView()
        }
    }

    fun hideNoteView() {
        ViewUtils.hideReveal(binding.quickNoteContainer)
        binding.quickNoteView.removeAllViewsInLayout()
        mCallback?.onClose()
    }

    private fun showNoteView() {
        ViewUtils.showReveal(binding.quickNoteContainer)
        Handler().postDelayed({ this.addFirstCard() }, 250)
    }

    private fun addFirstCard() {
        val binding = LayoutInflater.from(mContext).inflate(R.layout.view_note_card, this.binding.quickNoteView, false)
        binding.buttonSave.setOnClickListener { saveNote(binding) }
        binding.noteCard.visibility = View.GONE
        if (Module.isLollipop) {
            binding.noteCard.elevation = Configs.CARD_ELEVATION
        }
        binding.noteCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        binding.bgView.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(binding)
        ViewUtils.slideInUp(mContext, binding.noteCard)
        mCallback?.onOpen()
    }

    private fun saveNote(binding: View) {
        val text = binding.quickNote.text!!.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(text)) {
            binding.quickNote.error = mContext.getString(R.string.must_be_not_empty)
            return
        }
        binding.quickNote.isEnabled = false
        binding.buttonSave.isEnabled = false
        val item = Note()
        item.summary = text
        item.date = TimeUtil.gmtDateTime
        if (Prefs.getInstance(mContext).isNoteColorRememberingEnabled) {
            item.color = Prefs.getInstance(mContext).lastNoteColor
        } else {
            item.color = Random().nextInt(16)
        }
        mNote = item
        noteViewModel!!.saveNote(item)
    }

    private fun addReminderCard(item: Note) {
        val cardBinding = LayoutInflater.from(mContext).inflate(R.layout.view_note_reminder_card, this.binding.quickNoteView, false)
        if (Module.isLollipop) {
            cardBinding.noteReminderCard.elevation = Configs.CARD_ELEVATION
        }
        cardBinding.buttonYes.setOnClickListener {
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            addReminderToNote(item)
        }
        cardBinding.buttonNo.setOnClickListener {
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            addNotificationCard(item)
        }
        cardBinding.noteReminderCard.visibility = View.GONE
        cardBinding.noteReminderCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        cardBinding.bgViewReminder.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(cardBinding)
        Handler().postDelayed({ ViewUtils.slideInUp(mContext, cardBinding.noteReminderCard) }, 250)
    }

    private fun addReminderToNote(item: Note) {
        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.useGlobal = true
        reminder.noteId = item.key
        reminder.isActive = true
        reminder.isRemoved = false
        reminder.summary = item.summary
        val def = reminderViewModel!!.defaultGroup.value
        if (def != null) {
            reminder.groupUuId = def.uuId
        }
        val prefsTime = Prefs.getInstance(mContext).noteReminderTime * TimeCount.MINUTE
        val startTime = System.currentTimeMillis() + prefsTime
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        reminderViewModel!!.saveAndStartReminder(reminder)
    }

    private fun addNotificationCard(item: Note) {
        val cardBinding = LayoutInflater.from(mContext).inflate(R.layout.view_note_status_card, binding.quickNoteView, false)
        if (Module.isLollipop) {
            cardBinding.noteStatusCard.elevation = Configs.CARD_ELEVATION
        }
        cardBinding.buttonYesStatus.setOnClickListener {
            cardBinding.buttonNoStatus.isEnabled = false
            cardBinding.buttonYesStatus.isEnabled = false
            showInStatusBar(item)
        }
        cardBinding.buttonNoStatus.setOnClickListener { hideNoteView() }
        cardBinding.noteStatusCard.visibility = View.GONE
        cardBinding.noteStatusCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        cardBinding.bgViewStatus.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(cardBinding)
        Handler().postDelayed({ ViewUtils.slideInUp(mContext, cardBinding.noteStatusCard) }, 250)
    }

    private fun showInStatusBar(item: Note) {
        Notifier(mContext).showNoteNotification(item)
        hideNoteView()
    }

    interface Callback {
        fun onOpen()

        fun onClose()
    }
}
