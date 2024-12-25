package com.github.naz013.appwidgets.singlenote

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.BaseWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.databinding.ActivityWidgetSingleNoteBinding
import com.github.naz013.appwidgets.singlenote.adapter.SelectableNotesRecyclerAdapter
import com.github.naz013.appwidgets.singlenote.data.UiNoteWidgetAdapter
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.usecase.notes.GetNoteByIdUseCase
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class SingleNoteWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetSingleNoteBinding>() {

  private val viewModel by viewModel<SingleNoteWidgetConfigViewModel>()
  private val adapter = SelectableNotesRecyclerAdapter {
    viewModel.createPreview(
      id = it,
      horizontalAlignment = getHorizontalAlignment(),
      verticalAlignment = getVerticalAlignment(),
      textSize = getTextSize(),
      textColor = getTextColor(),
      textColorOpacity = getTextColorOpacity(),
      overlayColor = getOverlayColor(),
      overlayOpacity = getOverlayOpacity()
    )
  }

  private val uiNoteWidgetAdapter by inject<UiNoteWidgetAdapter>()
  private val getNoteByIdUseCase by inject<GetNoteByIdUseCase>()

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: SingleNoteWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetSingleNoteBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.appBar.applyTopInsets()
    binding.fabSave.applyBottomInsetsMargin()
    binding.scrollView.applyBottomInsets()

    binding.recyclerView.layoutManager = LinearLayoutManager(this)
    binding.recyclerView.adapter = adapter

    binding.fabSave.setOnClickListener { savePrefs() }
    binding.toolbar.setNavigationOnClickListener { finish() }

    binding.horGroup.setOnCheckedChangeListener { _, _ -> updatePreview() }
    binding.verGroup.setOnCheckedChangeListener { _, _ -> updatePreview() }

    binding.fontSizeBar.addOnChangeListener { _, value, _ -> updatePreview(textSize = value) }
    binding.fontSizeBar.setLabelFormatter { "${it.toInt()}" }

    initTextColor()
    initOverlayColor()

    lifecycle.addObserver(viewModel)
    viewModel.notes.nonNullObserve(this) {
      adapter.submitList(it)
      prefsProvider.getNoteId()?.also { id -> adapter.autoSelectId(id) }
    }
    viewModel.previewBitmap.nonNullObserve(this) {
      binding.notePreview.setImageBitmap(it.bitmap)
    }
  }

  private fun initOverlayColor() {
    binding.overlayColorSlider.setColors(ThemeProvider.colorsForNoteWidgetSlider(this))
    binding.overlayColorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.overlayColorSlider.setListener { _, color ->
      updatePreview(overlayColor = color)
    }
    binding.overlayColorSlider.setSelection(prefsProvider.getOverlayColorPosition())

    binding.overlayOpacityBar.addOnChangeListener { _, value, _ ->
      updatePreview(overlayOpacity = value)
    }
    binding.overlayOpacityBar.setLabelFormatter { "${it.toInt()}%" }
    binding.overlayOpacityBar.value = prefsProvider.getOverlayColorOpacity()
  }

  private fun initTextColor() {
    binding.textColorSlider.setColors(ThemeProvider.colorsForNoteWidgetSlider(this))
    binding.textColorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.textColorSlider.setListener { _, color ->
      updatePreview(textColor = color)
    }
    binding.textColorSlider.setSelection(prefsProvider.getTextColorPosition())

    binding.textOpacityBar.addOnChangeListener { _, value, _ ->
      updatePreview(textColorOpacity = value)
    }
    binding.textOpacityBar.setLabelFormatter { "${it.toInt()}%" }
    binding.textOpacityBar.value = prefsProvider.getTextColorOpacity()
  }

  private fun updatePreview(
    horizontalAlignment: NoteDrawableParams.HorizontalAlignment = getHorizontalAlignment(),
    verticalAlignment: NoteDrawableParams.VerticalAlignment = getVerticalAlignment(),
    textSize: Float = getTextSize(),
    textColor: Int = getTextColor(),
    textColorOpacity: Float = getTextColorOpacity(),
    overlayColor: Int = getOverlayColor(),
    overlayOpacity: Float = getOverlayOpacity()
  ) {
    adapter.getSelectedId()?.also {
      viewModel.createPreview(
        id = it,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        textSize = textSize,
        textColor = textColor,
        textColorOpacity = textColorOpacity,
        overlayColor = overlayColor,
        overlayOpacity = overlayOpacity
      )
    }
  }

  @ColorInt
  private fun getTextColor(): Int {
    return binding.textColorSlider.selectedColor
  }

  private fun getTextColorPosition(): Int {
    return binding.textColorSlider.selectedItem
  }

  @ColorInt
  private fun getOverlayColor(): Int {
    return binding.overlayColorSlider.selectedColor
  }

  private fun getOverlayColorPosition(): Int {
    return binding.overlayColorSlider.selectedItem
  }

  private fun getOverlayOpacity(): Float {
    return binding.overlayOpacityBar.value
  }

  private fun getTextColorOpacity(): Float {
    return binding.textOpacityBar.value
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
      widgetID = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )
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
    prefsProvider.setTextColorPosition(getTextColorPosition())
    prefsProvider.setTextColorOpacity(getTextColorOpacity())
    prefsProvider.setOverlayColorPosition(getOverlayColorPosition())
    prefsProvider.setOverlayColorOpacity(getOverlayOpacity())

    analyticsEventSender.send(WidgetUsedEvent(Widget.SINGLE_NOTE))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    SingleNoteWidget.updateWidget(
      context = this,
      appWidgetManager = appWidgetManager,
      prefsProvider = prefsProvider,
      uiNoteWidgetAdapter = uiNoteWidgetAdapter,
      noteWithImages = invokeSuspend { getNoteByIdUseCase(noteId) }
    )
    setResult(RESULT_OK, resultValue)
    finish()
  }
}
