package com.elementary.tasks.core.appwidgets.events

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Widget
import com.elementary.tasks.core.analytics.WidgetUsedEvent
import com.elementary.tasks.core.appwidgets.BaseWidgetConfigActivity
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.ActivityWidgetCurrentTasksConfigBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

class EventsWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetCurrentTasksConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private var textSize: Int = 14
  private lateinit var prefsProvider: EventsWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetCurrentTasksConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()
    binding.fabSave.setOnClickListener { showTextSizeDialog() }
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

  private fun showCurrentTheme() {
    textSize = prefsProvider.getTextSize().takeIf { it != 0f }?.toInt() ?: 14
    val headerBg = prefsProvider.getHeaderBackground()
    binding.bgColorSlider.setSelection(headerBg)
    binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(headerBg))
    updateIcons(headerBg)

    val itemBg = prefsProvider.getItemBackground()
    binding.listItemBgColorSlider.setSelection(itemBg)
    updateText(itemBg)
  }

  private fun updateText(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    if (isDark) {
      binding.statusIcon.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_fluent_clock_alarm,
          color = colorOf(R.color.pureWhite)
        )
      )
      binding.taskText.setTextColor(colorOf(R.color.pureWhite))
      binding.taskDate.setTextColor(colorOf(R.color.pureWhite))
      binding.taskNumber.setTextColor(colorOf(R.color.pureWhite))
      binding.taskTime.setTextColor(colorOf(R.color.pureWhite))
      binding.leftTime.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.statusIcon.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_fluent_clock_alarm,
          color = colorOf(R.color.pureBlack)
        )
      )
      binding.taskText.setTextColor(colorOf(R.color.pureBlack))
      binding.taskDate.setTextColor(colorOf(R.color.pureBlack))
      binding.taskNumber.setTextColor(colorOf(R.color.pureBlack))
      binding.taskTime.setTextColor(colorOf(R.color.pureBlack))
      binding.leftTime.setTextColor(colorOf(R.color.pureBlack))
    }
  }

  private fun updateIcons(code: Int) {
    val isDark = WidgetUtils.isDarkBg(code)
    binding.btnSettings.setImageDrawable(
      ViewUtils.tintIcon(
        context = this,
        resource = R.drawable.ic_fluent_settings,
        isDark = isDark
      )
    )
    binding.btnAddTask.setImageDrawable(
      ViewUtils.tintIcon(
        context = this,
        resource = R.drawable.ic_fluent_add,
        isDark = isDark
      )
    )
    binding.btnVoice.setImageDrawable(
      ViewUtils.tintIcon(
        context = this,
        resource = R.drawable.ic_builder_mic_on,
        isDark = isDark
      )
    )
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
      widgetID = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )
    }

    prefsProvider = EventsWidgetPrefsProvider(this, widgetID)

    resultValue = Intent()
    resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
    setResult(RESULT_CANCELED, resultValue)
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
    }
  }

  private fun showTextSizeDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.text_size)
    val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater, null, false)

    b.seekBar.addOnChangeListener { _, value, _ ->
      textSize = value.toInt()
      b.titleView.text = textSize.toString()
    }
    b.seekBar.stepSize = 1f
    b.seekBar.valueFrom = 12f
    b.seekBar.valueTo = 25f
    b.seekBar.value = textSize.toFloat()

    b.titleView.text = textSize.toString()
    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
      dialogInterface.dismiss()
      savePrefs()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, this)
  }

  private fun savePrefs() {
    prefsProvider.setHeaderBackground(binding.bgColorSlider.selectedItem)
    prefsProvider.setItemBackground(binding.listItemBgColorSlider.selectedItem)
    prefsProvider.setTextSize(textSize.toFloat())

    analyticsEventSender.send(WidgetUsedEvent(Widget.EVENTS))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    EventsWidget.updateWidget(this, appWidgetManager, prefsProvider)
    setResult(RESULT_OK, resultValue)
    finish()
  }
}
