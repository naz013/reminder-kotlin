package com.elementary.tasks.core.app_widgets.singlenote

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Widget
import com.elementary.tasks.core.analytics.WidgetUsedEvent
import com.elementary.tasks.core.app_widgets.BaseWidgetConfigActivity
import com.elementary.tasks.core.data.adapter.note.UiNoteWidgetAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.databinding.ActivityWidgetSingleNoteBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SingleNoteWidgetConfigActivity : BaseWidgetConfigActivity<ActivityWidgetSingleNoteBinding>() {

  private val viewModel by viewModel<SingleNoteWidgetConfigViewModel>()
  private val adapter = SelectableNotesRecyclerAdapter { viewModel.createPreview(it) }

  private val uiNoteWidgetAdapter by inject<UiNoteWidgetAdapter>()
  private val notesDao by inject<NotesDao>()

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: SingleNoteWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetSingleNoteBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.recyclerView.layoutManager = LinearLayoutManager(this)
    binding.recyclerView.adapter = adapter

    binding.fabSave.setOnClickListener { savePrefs() }
    binding.toolbar.setNavigationOnClickListener { finish() }

    lifecycle.addObserver(viewModel)
    viewModel.notes.nonNullObserve(this) {
      adapter.submitList(it)
      prefsProvider.getNoteId()?.also { id -> adapter.autoSelectId(id) }
    }
    viewModel.previewBitmap.nonNullObserve(this) {
      binding.notePreview.setImageBitmap(it.bitmap)
    }
  }

  private fun readIntent() {
    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    prefsProvider = SingleNoteWidgetPrefsProvider(this, widgetID)
    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(RESULT_CANCELED, resultValue)
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  private fun savePrefs() {
    val noteId = adapter.getSelectedId()
    if (noteId == null) {
      toast(getString(R.string.notes_note_not_selected))
      return
    }

    prefsProvider.setNoteId(noteId)

    analyticsEventSender.send(WidgetUsedEvent(Widget.COMBINED))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    SingleNoteWidget.updateWidget(
      context = this,
      appWidgetManager = appWidgetManager,
      prefsProvider = prefsProvider,
      uiNoteWidgetAdapter = uiNoteWidgetAdapter,
      noteWithImages = notesDao.getById(noteId)
    )
    setResult(RESULT_OK, resultValue)
    finish()
  }
}
