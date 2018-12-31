package com.elementary.tasks.core.appWidgets.buttons

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.WidgetUtils
import kotlinx.android.synthetic.main.widget_combined_config.*

/**
 * Copyright 2018 Nazar Suhovich
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
class CombinedWidgetConfigActivity : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        setContentView(R.layout.widget_combined_config)

        fabSave.setOnClickListener { savePrefs() }
        bgColorSlider.setListener { position, _ ->
            widgetBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateIcons(position)
        }
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            btn_add_reminder.setImageResource(R.drawable.ic_twotone_alarm_white)
            btn_add_note.setImageResource(R.drawable.ic_twotone_note_white)
            btn_add_birthday.setImageResource(R.drawable.ic_twotone_cake_white)
            btn_voice.setImageResource(R.drawable.ic_twotone_mic_white)
        } else {
            btn_add_reminder.setImageResource(R.drawable.ic_twotone_alarm_24px)
            btn_add_note.setImageResource(R.drawable.ic_twotone_note_24px)
            btn_add_birthday.setImageResource(R.drawable.ic_twotone_cake_24px)
            btn_voice.setImageResource(R.drawable.ic_twotone_mic_24px)
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
        setResult(RESULT_CANCELED, resultValue)
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)
        sp.edit()
                .putInt(WIDGET_BG_COLOR + widgetID, bgColorSlider.selectedItem)
                .apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        CombinedButtonsWidget.updateWidget(this, appWidgetManager, sp, widgetID)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val WIDGET_PREF = "combined_buttons_prefs"
        const val WIDGET_BG_COLOR = "widget_bg_color"
    }
}
