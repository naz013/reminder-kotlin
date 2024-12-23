package com.github.naz013.appwidgets.googletasks

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.github.naz013.analytics.Widget
import com.github.naz013.analytics.WidgetUsedEvent
import com.github.naz013.appwidgets.BaseWidgetConfigActivity
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.databinding.ActivityWidgetGoogleTasksConfigBinding
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets
import org.koin.android.ext.android.get

internal class TasksWidgetConfigActivity :
  BaseWidgetConfigActivity<ActivityWidgetGoogleTasksConfigBinding>() {

  private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
  private var resultValue: Intent? = null
  private lateinit var prefsProvider: GoogleTasksWidgetPrefsProvider

  override fun inflateBinding() = ActivityWidgetGoogleTasksConfigBinding.inflate(layoutInflater)

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

    if (!get<GoogleTasksAuthManager>().isAuthorized()) {
      Toast.makeText(this, getString(R.string.you_not_logged_to_google_tasks), Toast.LENGTH_SHORT)
        .show()
      finish()
    }
  }

  private fun updateText(code: Int) {
    if (WidgetUtils.isDarkBg(code)) {
      binding.statusIcon.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_builder_google_task_list,
          color = colorOf(R.color.pureWhite)
        )
      )
      binding.task.setTextColor(colorOf(R.color.pureWhite))
      binding.note.setTextColor(colorOf(R.color.pureWhite))
      binding.taskDate.setTextColor(colorOf(R.color.pureWhite))
    } else {
      binding.statusIcon.setImageBitmap(
        ViewUtils.createIcon(
          context = this,
          res = R.drawable.ic_builder_google_task_list,
          color = colorOf(R.color.pureBlack)
        )
      )
      binding.task.setTextColor(colorOf(R.color.pureBlack))
      binding.note.setTextColor(colorOf(R.color.pureBlack))
      binding.taskDate.setTextColor(colorOf(R.color.pureBlack))
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

    prefsProvider = GoogleTasksWidgetPrefsProvider(this, widgetID)

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

  private fun savePrefs() {
    prefsProvider.setHeaderBackground(binding.bgColorSlider.selectedItem)
    prefsProvider.setItemBackground(binding.listItemBgColorSlider.selectedItem)

    analyticsEventSender.send(WidgetUsedEvent(Widget.GOOGLE_TASKS))

    val appWidgetManager = AppWidgetManager.getInstance(this)
    TasksWidget.updateWidget(this, appWidgetManager, prefsProvider)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }
}
