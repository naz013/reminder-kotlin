package com.elementary.tasks.core.appWidgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.widget_current_tasks_config.*

/**
 * Copyright 2015 Nazar Suhovich
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
class EventsWidgetConfigActivity : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null
    private var textSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        setContentView(R.layout.widget_current_tasks_config)

        fabSave.setOnClickListener { showTextSizeDialog() }
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

    private fun updateText(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_alarm_24px,
                    ContextCompat.getColor(this, R.color.pureWhite)))
            taskText.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            taskNumber.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            taskTime.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            leftTime.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_alarm_24px,
                    ContextCompat.getColor(this, R.color.pureBlack)))
            taskText.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            taskNumber.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            taskTime.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            leftTime.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            btn_settings.setImageResource(R.drawable.ic_twotone_settings_white)
            btn_add_task.setImageResource(R.drawable.ic_twotone_add_white)
            btn_voice.setImageResource(R.drawable.ic_twotone_mic_white)
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            btn_settings.setImageResource(R.drawable.ic_twotone_settings_24px)
            btn_add_task.setImageResource(R.drawable.ic_twotone_add_24px)
            btn_voice.setImageResource(R.drawable.ic_twotone_mic_24px)
            widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
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

    private fun showTextSizeDialog() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(R.string.text_size)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null, false)
        b.seekBar.max = 13
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textSize = progress + 12
                b.titleView.text = textSize.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = 2
        textSize = 2 + 12
        b.titleView.text = textSize.toString()
        builder.setView(b)
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
        val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)

        sp.edit()
                .putInt(WIDGET_HEADER_BG + widgetID, bgColorSlider.selectedItem)
                .putInt(WIDGET_ITEM_BG + widgetID, listItemBgColorSlider.selectedItem)
                .putFloat(WIDGET_TEXT_SIZE + widgetID, textSize.toFloat())
                .apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        EventsWidget.updateWidget(this, appWidgetManager, sp, widgetID)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val WIDGET_PREF = "new_events_pref"
        const val WIDGET_HEADER_BG = "new_events_header_bg"
        const val WIDGET_ITEM_BG = "new_events_item_bg"
        const val WIDGET_TEXT_SIZE = "new_events_text_size"
    }
}
