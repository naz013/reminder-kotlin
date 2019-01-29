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
import kotlinx.android.synthetic.main.widget_google_tasks_config.*

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
class TasksWidgetConfigActivity : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        setContentView(R.layout.widget_google_tasks_config)

        fabSave.setOnClickListener { savePrefs() }
        bgColorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        bgColorSlider.setListener { position, _ ->
            headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateIcons(position)
        }

        listItemBgColorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        listItemBgColorSlider.setListener { position, _ ->
            listItemCard.setBackgroundResource(WidgetUtils.newWidgetBg(position))
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
            statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_check,
                    ContextCompat.getColor(this, R.color.pureWhite)))
            task.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            note.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_check,
                    ContextCompat.getColor(this, R.color.pureBlack)))
            task.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            note.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
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
        bgColorSlider.setSelection(headerBg)
        updateIcons(headerBg)

        val itemBg = sp.getInt(WIDGET_ITEM_BG + widgetID, 0)
        listItemBgColorSlider.setSelection(itemBg)
        updateText(itemBg)
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            btn_settings.setImageResource(R.drawable.ic_twotone_settings_white)
            btn_add_task.setImageResource(R.drawable.ic_twotone_add_white)
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            btn_settings.setImageResource(R.drawable.ic_twotone_settings_24px)
            btn_add_task.setImageResource(R.drawable.ic_twotone_add_24px)
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)
        sp.edit()
                .putInt(WIDGET_HEADER_BG + widgetID, bgColorSlider.selectedItem)
                .putInt(WIDGET_ITEM_BG + widgetID, listItemBgColorSlider.selectedItem)
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
