package com.github.naz013.appwidgets.combinedbuttons

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.BaseWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.databinding.ActivityWidgetCombinedConfigBinding
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets

internal class CombinedWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetCombinedConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: CombinedWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetCombinedConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()

    binding.appBar.applyTopInsets()
    binding.fabSave.applyBottomInsetsMargin()
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
        R.drawable.ic_fluent_clock_alarm,
        isDark
      )
    )
    binding.btnAddNote.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_fluent_note,
        isDark
      )
    )
    binding.btnAddBirthday.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_fluent_food_cake,
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
