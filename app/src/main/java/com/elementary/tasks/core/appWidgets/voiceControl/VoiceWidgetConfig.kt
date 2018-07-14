package com.elementary.tasks.core.appWidgets.voiceControl

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.WidgetUtils
import com.elementary.tasks.core.utils.Module
import kotlinx.android.synthetic.main.widget_voice.view.*
import kotlinx.android.synthetic.main.widget_voice_config.*
import java.util.*

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
class VoiceWidgetConfig : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID

    private var resultValue: Intent? = null
    private var color: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        setContentView(R.layout.widget_voice_config)
        initActionBar()

        val headerBgColor = findViewById<Spinner>(R.id.headerBgColor)
        val isPro = Module.isPro
        val spinnerArray = ArrayList<String>()
        val colorsArray = resources.getStringArray(R.array.color_list)
        Collections.addAll(spinnerArray, *colorsArray)
        if (isPro) {
            spinnerArray.add(getString(R.string.dark_purple))
            spinnerArray.add(getString(R.string.dark_orange))
            spinnerArray.add(getString(R.string.lime))
            spinnerArray.add(getString(R.string.indigo))
        }
        val spinnerArrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        headerBgColor.adapter = spinnerArrayAdapter
        headerBgColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                color = WidgetUtils.getDrawable(i)
                widgetLayout.widgetBg.setBackgroundResource(color)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.voice_control)
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
        resultValue!!.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        setResult(Activity.RESULT_CANCELED, resultValue)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.widget_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                savePrefs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(VOICE_WIDGET_PREF, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(VOICE_WIDGET_COLOR + widgetID, color)
        editor.apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        VoiceWidget.updateWidget(this@VoiceWidgetConfig, appWidgetManager, sp, widgetID)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {

        const val VOICE_WIDGET_PREF = "widget_pref"
        const val VOICE_WIDGET_COLOR = "widget_color_"
    }
}
