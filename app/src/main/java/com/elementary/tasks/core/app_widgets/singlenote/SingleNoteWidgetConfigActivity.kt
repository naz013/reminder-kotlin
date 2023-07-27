package com.elementary.tasks.core.app_widgets.singlenote

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Widget
import com.elementary.tasks.core.analytics.WidgetUsedEvent
import com.elementary.tasks.core.app_widgets.BaseWidgetConfigActivity
import com.elementary.tasks.core.data.adapter.note.UiNoteWidgetAdapter
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.views.drawable.NoteDrawableParams
import com.elementary.tasks.databinding.ActivityWidgetSingleNoteBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SingleNoteWidgetConfigActivity : BaseWidgetConfigActivity<ActivityWidgetSingleNoteBinding>() {

  private val viewModel by viewModel<SingleNoteWidgetConfigViewModel>()
  private val adapter = SelectableNotesRecyclerAdapter {
    viewModel.createPreview(
      id = it,
      horizontalAlignment = getHorizontalAlignment(),
      verticalAlignment = getVerticalAlignment(),
      textSize = getTextSize()
    )
  }

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

    binding.horGroup.setOnCheckedChangeListener { group, checkedId ->
      adapter.getSelectedId()?.also {
        viewModel.createPreview(
          id = it,
          horizontalAlignment = getHorizontalAlignment(),
          verticalAlignment = getVerticalAlignment(),
          textSize = getTextSize()
        )
      }
    }
    binding.verGroup.setOnCheckedChangeListener { group, checkedId ->
      adapter.getSelectedId()?.also {
        viewModel.createPreview(
          id = it,
          horizontalAlignment = getHorizontalAlignment(),
          verticalAlignment = getVerticalAlignment(),
          textSize = getTextSize()
        )
      }
    }

    binding.fontSizeBar.addOnChangeListener { _, value, _ ->
      adapter.getSelectedId()?.also {
        viewModel.createPreview(
          id = it,
          horizontalAlignment = getHorizontalAlignment(),
          verticalAlignment = getVerticalAlignment(),
          textSize = value
        )
      }
    }
    binding.fontSizeBar.setLabelFormatter { "${it.toInt()}" }

    lifecycle.addObserver(viewModel)
    viewModel.notes.nonNullObserve(this) {
      adapter.submitList(it)
      prefsProvider.getNoteId()?.also { id -> adapter.autoSelectId(id) }
    }
    viewModel.previewBitmap.nonNullObserve(this) {
      binding.notePreview.setImageBitmap(it.bitmap)
    }
  }

  private fun getTextSize(): Float {
    return binding.fontSizeBar.value
  }

  private fun getHorizontalAlignment(): NoteDrawableParams.HorizontalAlignment {
    return when {
      binding.horLeft.isChecked -> NoteDrawableParams.HorizontalAlignment.LEFT
      binding.horRight.isChecked -> NoteDrawableParams.HorizontalAlignment.RIGHT
      else -> NoteDrawableParams.HorizontalAlignment.CENTER
    }
  }

  private fun getVerticalAlignment(): NoteDrawableParams.VerticalAlignment {
    return when {
      binding.verTop.isChecked -> NoteDrawableParams.VerticalAlignment.TOP
      binding.verBottom.isChecked -> NoteDrawableParams.VerticalAlignment.BOTTOM
      else -> NoteDrawableParams.VerticalAlignment.CENTER
    }
  }

  @IdRes
  private fun getHorizontalCheck(horizontalAlignment: NoteDrawableParams.HorizontalAlignment): Int {
    return when (horizontalAlignment) {
      NoteDrawableParams.HorizontalAlignment.CENTER -> binding.horCenter.id
      NoteDrawableParams.HorizontalAlignment.LEFT -> binding.horLeft.id
      NoteDrawableParams.HorizontalAlignment.RIGHT -> binding.horRight.id
    }
  }

  @IdRes
  private fun getVerticalCheck(verticalAlignment: NoteDrawableParams.VerticalAlignment): Int {
    return when (verticalAlignment) {
      NoteDrawableParams.VerticalAlignment.CENTER -> binding.verCenter.id
      NoteDrawableParams.VerticalAlignment.BOTTOM -> binding.verBottom.id
      NoteDrawableParams.VerticalAlignment.TOP -> binding.verTop.id
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

    prefsProvider.getTextSize().takeIf { it > 0f && it < 250f }
      ?.also { binding.fontSizeBar.value = it }
    binding.verGroup.check(getVerticalCheck(prefsProvider.getVerticalAlignment()))
    binding.horGroup.check(getHorizontalCheck(prefsProvider.getHorizontalAlignment()))
  }

  private fun savePrefs() {
    val noteId = adapter.getSelectedId()
    if (noteId == null) {
      toast(getString(R.string.widget_note_note_not_selected))
      return
    }

    prefsProvider.setNoteId(noteId)
    prefsProvider.setHorizontalAlignment(getHorizontalAlignment())
    prefsProvider.setVerticalAlignment(getVerticalAlignment())
    prefsProvider.setTextSize(getTextSize())

    analyticsEventSender.send(WidgetUsedEvent(Widget.SINGLE_NOTE))

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
