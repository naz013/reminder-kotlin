package com.elementary.tasks.core.app_widgets.buttons

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityWidgetCombinedConfigBinding

class CombinedWidgetConfigActivity : BindingActivity<ActivityWidgetCombinedConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null

  override fun inflateBinding() = ActivityWidgetCombinedConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.fabSave.setOnClickListener { savePrefs() }
    binding.bgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
    binding.bgColorSlider.setListener { position, _ ->
      binding.widgetBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateIcons(position)
    }
    updateIcons(0)
    showCurrentTheme()
  }

  private fun showCurrentTheme() {
    val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
    val headerBg = sp.getInt(WIDGET_BG_COLOR + widgetID, 0)
    binding.bgColorSlider.setSelection(headerBg)
    updateIcons(headerBg)
  }

  private fun updateIcons(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    binding.btnAddReminder.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_alarm_24px, isDark))
    binding.btnAddNote.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_note_24px, isDark))
    binding.btnAddBirthday.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_cake_24px, isDark))
    binding.btnVoice.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_mic_24px, isDark))
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
      .putInt(WIDGET_BG_COLOR + widgetID, binding.bgColorSlider.selectedItem)
      .apply()
    val appWidgetManager = AppWidgetManager.getInstance(this)
    CombinedButtonsWidget.updateWidget(this, appWidgetManager, sp, widgetID)
    setResult(RESULT_OK, resultValue)
    finish()
  }

  companion object {
    const val WIDGET_PREF = "combined_buttons_prefs"
    const val WIDGET_BG_COLOR = "widget_bg_color"
  }
}
