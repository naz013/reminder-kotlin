package com.elementary.tasks.core.appwidgets.buttons

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Widget
import com.elementary.tasks.core.analytics.WidgetUsedEvent
import com.elementary.tasks.core.appwidgets.BaseWidgetConfigActivity
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.ActivityWidgetCombinedConfigBinding

class CombinedWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetCombinedConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: CombinedWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetCombinedConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.fabSave.setOnClickListener { savePrefs() }
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.bgColorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.bgColorSlider.setListener { position, _ ->
      binding.widgetBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateIcons(position)
    }
    updateIcons(0)
    showCurrentTheme()
  }

  private fun showCurrentTheme() {
    val position = prefsProvider.getWidgetBackground()
    binding.bgColorSlider.setSelection(position)
    binding.widgetBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
    updateIcons(position)
  }

  private fun updateIcons(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    binding.btnAddReminder.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_twotone_alarm_24px,
        isDark
      )
    )
    binding.btnAddNote.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_twotone_note_24px,
        isDark
      )
    )
    binding.btnAddBirthday.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_twotone_cake_24px,
        isDark
      )
    )
    binding.btnVoice.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_twotone_mic_black_24px,
        isDark
      )
    )
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
    prefsProvider = CombinedWidgetPrefsProvider(this, widgetID)
    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(RESULT_CANCELED, resultValue)
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  private fun savePrefs() {
    prefsProvider.setWidgetBackground(binding.bgColorSlider.selectedItem)

    analyticsEventSender.send(WidgetUsedEvent(Widget.COMBINED))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    CombinedButtonsWidget.updateWidget(this, appWidgetManager, prefsProvider)
    setResult(RESULT_OK, resultValue)
    finish()
  }
}
