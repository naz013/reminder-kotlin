package com.elementary.tasks.core.app_widgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.databinding.ActivityWidgetCalendarConfigBinding
import java.util.*

class CalendarWidgetConfigActivity : BindingActivity<ActivityWidgetCalendarConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null

  override fun inflateBinding() = ActivityWidgetCalendarConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()
    binding.fabSave.setOnClickListener { savePrefs() }
    binding.bgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
    binding.bgColorSlider.setListener { position, _ ->
      updateContent(position)
    }

    binding.headerBgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
    binding.headerBgColorSlider.setListener { position, _ ->
      binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateHeader(position)
    }

    updateContent(0)
    updateHeader(0)

    showCurrentTheme()
  }

  private fun showCurrentTheme() {
    val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)

    val headerBg = sp.getInt(WIDGET_HEADER_BG + widgetID, 0)
    binding.headerBgColorSlider.setSelection(headerBg)
    updateHeader(headerBg)

    val itemBg = sp.getInt(WIDGET_BG + widgetID, 0)
    binding.bgColorSlider.setSelection(itemBg)
    updateContent(itemBg)
  }

  private fun updateContent(code: Int) {
    binding.widgetBg.setBackgroundResource(WidgetUtils.newWidgetBg(code))
  }

  private fun updateHeader(code: Int) {
    val color = if (WidgetUtils.isDarkBg(code)) {
      colorOf(R.color.pureWhite)
    } else {
      colorOf(R.color.pureBlack)
    }

    binding.btnSettings.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_settings_24px, color))
    binding.btnAddTask.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_add_24px, color))
    binding.btnVoice.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_mic_24px, color))
    binding.btnNext.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_keyboard_arrow_right_24px, color))
    binding.btnPrev.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_keyboard_arrow_left_24px, color))
    binding.widgetTitle.setTextColor(color)
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

    val cal = GregorianCalendar()
    cal.timeInMillis = System.currentTimeMillis()
    val month = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)

    sp.edit()
      .putInt(WIDGET_HEADER_BG + widgetID, binding.headerBgColorSlider.selectedItem)
      .putInt(WIDGET_BG + widgetID, binding.bgColorSlider.selectedItem)
      .putInt(CALENDAR_WIDGET_MONTH + widgetID, month)
      .putInt(CALENDAR_WIDGET_YEAR + widgetID, year)
      .apply()

    val appWidgetManager = AppWidgetManager.getInstance(this)
    CalendarWidget.updateWidget(this, appWidgetManager, sp, widgetID)
    setResult(RESULT_OK, resultValue)
    finish()
  }

  companion object {
    const val WIDGET_PREF = "new_calendar_pref"
    const val WIDGET_HEADER_BG = "new_calendar_header_bg"
    const val WIDGET_BG = "new_calendar_bg"
    const val CALENDAR_WIDGET_MONTH = "new_calendar_month_"
    const val CALENDAR_WIDGET_YEAR = "new_calendar_year_"
  }
}
