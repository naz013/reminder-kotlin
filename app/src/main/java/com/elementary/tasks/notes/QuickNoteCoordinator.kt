package com.elementary.tasks.notes

import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel
import com.elementary.tasks.databinding.ActivityMainBinding
import com.elementary.tasks.databinding.ViewNoteCardBindingImpl
import com.elementary.tasks.databinding.ViewNoteReminderCardBinding
import com.elementary.tasks.databinding.ViewNoteStatusCardBinding

import java.util.Random
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders

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

class QuickNoteCoordinator(private val mContext: FragmentActivity, private val binding: ActivityMainBinding, private val mCallback: Callback?) {
    private val themeUtil: ThemeUtil
    private var reminderViewModel: ReminderViewModel? = null
    private var noteViewModel: NoteViewModel? = null

    private var mNote: Note? = null

    val isNoteVisible: Boolean
        get() = binding.quickNoteContainer.visibility == View.VISIBLE

    init {
        this.themeUtil = ThemeUtil.getInstance(mContext)
        this.binding.quickNoteContainer.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (isNoteVisible) {
                    hideNoteView()
                }
                return @this.binding.quickNoteContainer.setOnTouchListener true
            }
            false
        }
        this.binding.quickNoteContainer.visibility = View.GONE

        initReminderViewModel()
        initNoteViewModel()
    }

    private fun initNoteViewModel() {
        noteViewModel = ViewModelProviders.of(mContext).get(NoteViewModel::class.java)
        noteViewModel!!.result.observe(mContext, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> if (Prefs.getInstance(mContext).isNoteReminderEnabled) {
                        if (mNote != null) addReminderCard(mNote)
                    } else {
                        if (mNote != null) addNotificationCard(mNote!!)
                    }
                }
            }
        })
    }

    private fun initReminderViewModel() {
        reminderViewModel = ViewModelProviders.of(mContext).get(ReminderViewModel::class.java)
        reminderViewModel!!.result.observe(mContext, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> if (mNote != null) addNotificationCard(mNote!!)
                }
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
        val binding = ViewNoteCardBindingImpl.inflate(LayoutInflater.from(mContext), this.binding.quickNoteView, false)
        binding.buttonSave.setOnClickListener { view -> saveNote(binding) }
        binding.noteCard.visibility = View.GONE
        if (Module.isLollipop) {
            binding.noteCard.elevation = Configs.CARD_ELEVATION
        }
        binding.noteCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        binding.bgView.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(binding.root)
        ViewUtils.slideInUp(mContext, binding.noteCard)
        mCallback?.onOpen()
    }

    private fun saveNote(binding: ViewNoteCardBindingImpl) {
        val text = binding.quickNote.getText()!!.toString().trim({ it <= ' ' })
        if (TextUtils.isEmpty(text)) {
            binding.quickNote.setError(mContext.getString(R.string.must_be_not_empty))
            return
        }
        binding.quickNote.setEnabled(false)
        binding.buttonSave.setEnabled(false)
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
        val cardBinding = ViewNoteReminderCardBinding.inflate(LayoutInflater.from(mContext), this.binding.quickNoteView, false)
        if (Module.isLollipop) {
            cardBinding.noteReminderCard.elevation = Configs.CARD_ELEVATION
        }
        cardBinding.buttonYes.setOnClickListener { view ->
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            addReminderToNote(item)
        }
        cardBinding.buttonNo.setOnClickListener { view ->
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            addNotificationCard(item)
        }
        cardBinding.noteReminderCard.visibility = View.GONE
        cardBinding.noteReminderCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        cardBinding.bgView.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(cardBinding.root)
        Handler().postDelayed({ ViewUtils.slideInUp(mContext, cardBinding.noteReminderCard) }, 250)
    }

    private fun addReminderToNote(item: Note) {
        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.isUseGlobal = true
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
        val cardBinding = ViewNoteStatusCardBinding.inflate(LayoutInflater.from(mContext), binding.quickNoteView, false)
        if (Module.isLollipop) {
            cardBinding.noteStatusCard.elevation = Configs.CARD_ELEVATION
        }
        cardBinding.buttonYes.setOnClickListener { view ->
            cardBinding.buttonNo.isEnabled = false
            cardBinding.buttonYes.isEnabled = false
            showInStatusBar(item)
        }
        cardBinding.buttonNo.setOnClickListener { view -> hideNoteView() }
        cardBinding.noteStatusCard.visibility = View.GONE
        cardBinding.noteStatusCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()))
        cardBinding.bgView.setBackgroundColor(themeUtil.backgroundStyle)
        this.binding.quickNoteView.addView(cardBinding.root)
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
