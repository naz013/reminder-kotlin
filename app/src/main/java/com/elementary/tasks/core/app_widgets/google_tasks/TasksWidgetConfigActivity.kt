package com.elementary.tasks.core.app_widgets.google_tasks

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityWidgetGoogleTasksConfigBinding

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class TasksWidgetConfigActivity : ThemedActivity<ActivityWidgetGoogleTasksConfigBinding>() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    override fun layoutRes(): Int = R.layout.activity_widget_google_tasks_config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()

        binding.fabSave.setOnClickListener { savePrefs() }
        binding.bgColorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        binding.bgColorSlider.setListener { position, _ ->
            binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateIcons(position)
        }

        binding.listItemBgColorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        binding.listItemBgColorSlider.setListener { position, _ ->
            binding.listItemCard.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateText(position)
        }

        updateText(0)
        updateIcons(0)

        showCurrentTheme()

        if (GTasks.getInstance(this)?.isLogged != true) {
            Toast.makeText(this, getString(R.string.you_not_logged_to_google_tasks), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateText(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            binding.statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_check,
                    ContextCompat.getColor(this, R.color.pureWhite)))
            binding.task.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.note.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_check,
                    ContextCompat.getColor(this, R.color.pureBlack)))
            binding.task.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.note.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
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
        setResult(Activity.RESULT_CANCELED, resultValue)
    }

    private fun showCurrentTheme() {
        val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)

        val headerBg = sp.getInt(WIDGET_HEADER_BG + widgetID, 0)
        binding.bgColorSlider.setSelection(headerBg)
        updateIcons(headerBg)

        val itemBg = sp.getInt(WIDGET_ITEM_BG + widgetID, 0)
        binding.listItemBgColorSlider.setSelection(itemBg)
        updateText(itemBg)
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_white)
            binding.btnAddTask.setImageResource(R.drawable.ic_twotone_add_white)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_24px)
            binding.btnAddTask.setImageResource(R.drawable.ic_twotone_add_24px)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        sp.edit()
                .putInt(WIDGET_HEADER_BG + widgetID, binding.bgColorSlider.selectedItem)
                .putInt(WIDGET_ITEM_BG + widgetID, binding.listItemBgColorSlider.selectedItem)
                .apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        TasksWidget.updateWidget(this, appWidgetManager, sp, widgetID)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val WIDGET_PREF = "new_tasks_pref"
        const val WIDGET_HEADER_BG = "new_tasks_header_bg"
        const val WIDGET_ITEM_BG = "new_tasks_item_bg"
    }
}
