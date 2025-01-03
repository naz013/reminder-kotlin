package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.BaseWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetPrefsHolder
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.databinding.ActivityWidgetCalendarConfigBinding
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets
import org.koin.android.ext.android.inject
import java.util.Calendar
import java.util.GregorianCalendar

internal class CalendarWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetCalendarConfigBinding>() {

  private val widgetPrefsHolder by inject<WidgetPrefsHolder>()

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: CalendarWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetCalendarConfigBinding.inflate(layoutInflater)

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
      updateContent(position)
    }

    binding.headerBgColorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.headerBgColorSlider.setListener { position, _ ->
      binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateHeader(position)
    }

    updateContent(0)
    updateHeader(0)

    showCurrentTheme()
  }

  private fun showCurrentTheme() {
    val headerBg = prefsProvider.getHeaderBackground()
    binding.headerBgColorSlider.setSelection(headerBg)
    binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(headerBg))
    updateHeader(headerBg)

    val itemBg = prefsProvider.getBackground()
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

    binding.btnSettings.setImageBitmap(
      ViewUtils.createIcon(
        context = this,
        res = R.drawable.ic_fluent_settings,
        color = color
      )
    )
    binding.btnAddTask.setImageBitmap(
      ViewUtils.createIcon(
        context = this,
        res = R.drawable.ic_fluent_add,
        color = color
      )
    )
    binding.btnVoice.setImageBitmap(
      ViewUtils.createIcon(
        context = this,
        res = R.drawable.ic_builder_mic_on,
        color = color
      )
    )
    binding.btnNext.setImageBitmap(
      ViewUtils.createIcon(
        context = this,
        res = R.drawable.ic_fluent_chevron_right,
        color = color
      )
    )
    binding.btnPrev.setImageBitmap(
      ViewUtils.createIcon(
        context = this,
        res = R.drawable.ic_fluent_chevron_left,
        color = color
      )
    )
    binding.widgetTitle.setTextColor(color)
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
    prefsProvider = widgetPrefsHolder.findOrCreate(
      widgetID,
      CalendarWidgetPrefsProvider::class.java
    )
    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(RESULT_CANCELED, resultValue)
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  private fun savePrefs() {
    val cal = GregorianCalendar()
    cal.timeInMillis = System.currentTimeMillis()
    val month = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)

    prefsProvider.setBackground(binding.bgColorSlider.selectedItem)
    prefsProvider.setHeaderBackground(binding.headerBgColorSlider.selectedItem)
    prefsProvider.setMonth(month)
    prefsProvider.setYear(year)

    analyticsEventSender.send(WidgetUsedEvent(Widget.CALENDAR))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    CalendarWidget.updateWidget(this, appWidgetManager, prefsProvider)
    setResult(RESULT_OK, resultValue)
    finish()
  }
}
