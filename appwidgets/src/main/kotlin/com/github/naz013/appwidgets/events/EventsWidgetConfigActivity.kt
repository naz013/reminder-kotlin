package com.github.naz013.appwidgets.events

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.BaseWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.databinding.ActivityWidgetCurrentTasksConfigBinding
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.databinding.DialogWithSeekAndTitleBinding
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets
import org.koin.android.ext.android.inject

internal class EventsWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetCurrentTasksConfigBinding>() {

  private val dialogues by inject<Dialogues>()

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private var textSize: Int = 14
  private lateinit var prefsProvider: EventsWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetCurrentTasksConfigBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readIntent()
    binding.appBar.applyTopInsets()
    binding.fabSave.applyBottomInsetsMargin()
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
    if (isDark) {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.widgetTitle.setTextColor(colorOf(R.color.pureBlack))
    }
  }

  private fun readIntent() {
    val extras = intent.extras
    Logger.d(TAG, "Read intent extras: ${extras?.keySet()?.toList()}")
    if (extras != null) {
      widgetID = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
      )
    }

    Logger.d(TAG, "Edit events widget with id: $widgetID")

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
      invokeSuspend { savePrefs() }
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, this)
  }

  private suspend fun savePrefs() {
    prefsProvider.setHeaderBackground(binding.bgColorSlider.selectedItem)
    prefsProvider.setItemBackground(binding.listItemBgColorSlider.selectedItem)
    prefsProvider.setTextSize(textSize.toFloat())

    analyticsEventSender.send(WidgetUsedEvent(Widget.EVENTS))

    appWidgetUpdater.updateEventsWidget(widgetID)

    setResult(RESULT_OK, resultValue)
    finish()
  }

  companion object {
    private const val TAG = "EventsWidgetConfigActivity"
  }
}
