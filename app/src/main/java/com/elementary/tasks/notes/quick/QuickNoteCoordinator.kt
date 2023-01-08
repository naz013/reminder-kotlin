package com.elementary.tasks.notes.quick

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ViewNoteCardBinding
import com.elementary.tasks.databinding.ViewNoteReminderCardBinding
import com.elementary.tasks.databinding.ViewNoteStatusCardBinding
import org.threeten.bp.LocalDateTime
import java.util.Random

@SuppressLint("ClickableViewAccessibility")
class QuickNoteCoordinator(
  private val context: Context,
  private val parent: ViewGroup,
  private val noteList: ViewGroup,
  private var quickNoteViewModel: QuickNoteViewModel,
  private val prefs: Prefs,
  private val notifier: Notifier,
  private val dateTimeManager: DateTimeManager
) {

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
    parent.gone()
    noteList.removeAllViewsInLayout()
  }

  private fun showNoteView() {
    parent.visible()
    Handler(Looper.getMainLooper()).postDelayed({ this.addFirstCard() }, 250)
  }

  private fun addFirstCard() {
    val binding = ViewNoteCardBinding.inflate(LayoutInflater.from(context), noteList, false)
    binding.buttonSave.setOnClickListener { saveNote(binding) }
    binding.noteCard.gone()

    noteList.addView(binding.root)
    binding.noteCard.visible()
  }

  private fun saveNote(binding: ViewNoteCardBinding) {
    val text = binding.quickNote.trimmedText()
    if (TextUtils.isEmpty(text)) {
      binding.nameLayout.error = context.getString(R.string.must_be_not_empty)
      binding.nameLayout.isErrorEnabled = true
      return
    }
    binding.quickNote.isEnabled = false
    binding.buttonSave.isEnabled = false
    val item = Note()
    item.summary = text
    item.date = dateTimeManager.getNowGmtDateTime()
    if (prefs.isNoteColorRememberingEnabled) {
      item.color = prefs.lastNoteColor
    } else {
      item.color = Random().nextInt(ThemeProvider.NOTE_COLORS)
    }
    item.palette = prefs.notePalette
    val noteWithImages = NoteWithImages()
    noteWithImages.note = item

    if (prefs.isNoteReminderEnabled) {
      addReminderCard(noteWithImages)
    } else {
      quickNoteViewModel.saveNote(noteWithImages, null)
      addNotificationCard(noteWithImages)
    }
  }

  private fun addReminderCard(item: NoteWithImages) {
    val cardBinding = ViewNoteReminderCardBinding.inflate(LayoutInflater.from(context), noteList, false)

    cardBinding.buttonYes.setOnClickListener {
      cardBinding.buttonNo.isEnabled = false
      cardBinding.buttonYes.isEnabled = false
      addReminderToNote(item)
    }
    cardBinding.buttonNo.setOnClickListener {
      cardBinding.buttonNo.isEnabled = false
      cardBinding.buttonYes.isEnabled = false
      quickNoteViewModel.saveNote(item, null)
      addNotificationCard(item)
    }
    cardBinding.noteReminderCard.gone()

    noteList.addView(cardBinding.root)
    cardBinding.noteReminderCard.visible()
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
    reminder.summary = SuperUtil.normalizeSummary(note.summary)
    val startTime = LocalDateTime.now().plusMinutes(prefs.noteReminderTime.toLong())
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)

    quickNoteViewModel.saveNote(item, reminder)
    addNotificationCard(item)
  }

  private fun addNotificationCard(item: NoteWithImages) {
    val cardBinding = ViewNoteStatusCardBinding.inflate(LayoutInflater.from(context), noteList, false)

    cardBinding.buttonYesStatus.setOnClickListener {
      cardBinding.buttonNoStatus.isEnabled = false
      cardBinding.buttonYesStatus.isEnabled = false
      showInStatusBar(item)
    }
    cardBinding.buttonNoStatus.setOnClickListener { hideNoteView() }
    cardBinding.noteStatusCard.gone()

    noteList.addView(cardBinding.root)
    cardBinding.noteStatusCard.visible()
  }

  private fun showInStatusBar(item: NoteWithImages) {
    val uniqueId = item.note?.uniqueId ?: 0
    val image = item.images.firstOrNull()?.image
    notifier.showNoteNotification(item.getSummary(), uniqueId, image)
    hideNoteView()
  }
}