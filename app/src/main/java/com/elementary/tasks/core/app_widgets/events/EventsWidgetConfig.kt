package com.elementary.tasks.core.app_widgets.events

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.databinding.WidgetCurrentTasksConfigBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

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
class EventsWidgetConfig : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    private var textSize: Int = 0

    private var binding: WidgetCurrentTasksConfigBinding? = null
    private var mThemes: List<EventsTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        binding = DataBindingUtil.setContentView(this, R.layout.widget_current_tasks_config)
        initActionBar()
        loadThemes()
        showCurrentTheme()
    }

    private fun showCurrentTheme() {
        val sp = getSharedPreferences(EVENTS_WIDGET_PREF, Context.MODE_PRIVATE)
        val theme = sp.getInt(EVENTS_WIDGET_THEME + widgetID, 0)
        binding!!.themePager.setCurrentItem(theme, true)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.toolbar.title = getString(R.string.active_reminders)
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

    private fun loadThemes() {
        mThemes = EventsTheme.getThemes(this)
        val adapter = MyFragmentPagerAdapter(supportFragmentManager, mThemes)
        binding!!.themePager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.widget_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                showTextSizeDialog()
                return true
            }
            android.R.id.home -> finish()
        }
        return true
    }

    private fun showTextSizeDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(R.string.text_size)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(this))
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
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            dialogInterface.dismiss()
            updateWidget()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun updateWidget() {
        val sp = getSharedPreferences(EVENTS_WIDGET_PREF, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(EVENTS_WIDGET_THEME + widgetID, binding!!.themePager.currentItem)
        editor.putFloat(EVENTS_WIDGET_TEXT_SIZE + widgetID, textSize.toFloat())
        editor.apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        EventsWidget.updateWidget(this@EventsWidgetConfig, appWidgetManager, sp, widgetID)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private inner class MyFragmentPagerAdapter internal constructor(fm: FragmentManager, private val arrayList: List<EventsTheme>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return EventsThemeFragment.newInstance(position, mThemes)
        }

        override fun getCount(): Int {
            return arrayList.size
        }
    }

    companion object {
        val EVENTS_WIDGET_PREF = "widget_pref"
        val EVENTS_WIDGET_TEXT_SIZE = "widget_text_size_"
        val EVENTS_WIDGET_THEME = "widget_theme_"
    }
}
