package com.elementary.tasks.core.app_widgets.notes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.databinding.ActivityWidgetNoteConfigBinding

class NotesWidgetConfigActivity : BindingActivity<ActivityWidgetNoteConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null

  override fun inflateBinding() = ActivityWidgetNoteConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.fabSave.setOnClickListener { savePrefs() }
    binding.bgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
    binding.bgColorSlider.setListener { position, _ ->
      binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateIcons(position)
    }
    updateIcons(0)

    showCurrentTheme()
  }

  private fun showCurrentTheme() {
    val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
    val headerBg = sp.getInt(WIDGET_HEADER_BG_COLOR + widgetID, 0)
    binding.bgColorSlider.setSelection(headerBg)
    updateIcons(headerBg)
  }

  private fun updateIcons(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    binding.btnSettings.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_settings_24px, isDark))
    binding.btnAddNote.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_add_24px, isDark))
    if (isDark) {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureBlack))
    }
  }

  private fun readIntent() {
    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID)
    }
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(RESULT_CANCELED, resultValue)
  }

  private fun savePrefs() {
    val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
    sp.edit()
      .putInt(WIDGET_HEADER_BG_COLOR + widgetID, binding.bgColorSlider.selectedItem)
      .apply()
    val appWidgetManager = AppWidgetManager.getInstance(this)
    if (!isFinishing) {
      NotesWidget.updateWidget(this, appWidgetManager, sp, widgetID)
    }
    setResult(RESULT_OK, resultValue)
    finish()
  }

  companion object {
    const val WIDGET_PREF = "new_notes_prefs"
    const val WIDGET_HEADER_BG_COLOR = "widget_header_bg_color"
  }
}
