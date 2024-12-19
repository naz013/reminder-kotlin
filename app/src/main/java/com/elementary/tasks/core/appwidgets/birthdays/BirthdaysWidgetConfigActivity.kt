package com.elementary.tasks.core.appwidgets.birthdays

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.elementary.tasks.core.appwidgets.BaseWidgetConfigActivity
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.applyBottomInsetsMargin
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.databinding.ActivityWidgetBirthdaysConfigBinding

class BirthdaysWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetBirthdaysConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: BirthdaysWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetBirthdaysConfigBinding.inflate(layoutInflater)

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
      binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateIcons(position)
    }

    binding.listItemBgColorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.listItemBgColorSlider.setListener { position, _ ->
      binding.listItemCard.setBackgroundResource(WidgetUtils.newWidgetBg(position))
      updateText(position)
    }

    updateText(0)
    updateIcons(0)

    showCurrentTheme()
  }

  private fun updateText(code: Int) {
    if (WidgetUtils.isDarkBg(code)) {
      binding.statusIconView.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_fluent_food_cake,
          color = colorOf(R.color.pureWhite)
        )
      )
      binding.nameView.setTextColor(colorOf(R.color.pureWhite))
      binding.ageBirthDateView.setTextColor(colorOf(R.color.pureWhite))
      binding.leftTimeView.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.statusIconView.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_fluent_food_cake,
          color = colorOf(R.color.pureBlack)
        )
      )
      binding.nameView.setTextColor(colorOf(R.color.pureBlack))
      binding.ageBirthDateView.setTextColor(colorOf(R.color.pureBlack))
      binding.leftTimeView.setTextColor(colorOf(R.color.pureBlack))
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

    prefsProvider = BirthdaysWidgetPrefsProvider(this, widgetID)

    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(Activity.RESULT_CANCELED, resultValue)

    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  private fun showCurrentTheme() {
    val headerBg = prefsProvider.getHeaderBackground()
    binding.bgColorSlider.setSelection(headerBg)
    binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(headerBg))
    updateIcons(headerBg)

    val itemBg = prefsProvider.getItemBackground()
    binding.listItemBgColorSlider.setSelection(itemBg)
    binding.listItemCard.setBackgroundResource(WidgetUtils.newWidgetBg(itemBg))
    updateText(itemBg)
  }

  private fun updateIcons(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    binding.btnSettings.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_fluent_settings,
        isDark
      )
    )
    binding.btnAddTask.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_fluent_add,
        isDark
      )
    )
    if (isDark) {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureBlack))
    }
  }

  private fun savePrefs() {
    prefsProvider.setHeaderBackground(binding.bgColorSlider.selectedItem)
    prefsProvider.setItemBackground(binding.listItemBgColorSlider.selectedItem)

    analyticsEventSender.send(WidgetUsedEvent(Widget.BIRTHDAYS))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    BirthdaysWidget.updateWidget(this, appWidgetManager, prefsProvider)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }
}
