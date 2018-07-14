package com.elementary.tasks.core.appWidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.databinding.WidgetCalendarConfigBinding

import java.util.Calendar
import java.util.GregorianCalendar

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
class CalendarWidgetConfig : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    private var binding: WidgetCalendarConfigBinding? = null
    private var mThemes: List<CalendarTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        val sp = getSharedPreferences(CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        val theme = sp.getInt(CALENDAR_WIDGET_THEME + widgetID, 0)
        binding = DataBindingUtil.setContentView(this, R.layout.widget_calendar_config)
        initActionBar()
        loadThemes()
        binding!!.themePager.setCurrentItem(theme, true)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        binding!!.toolbar.title = getString(R.string.calendar)
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
        mThemes = CalendarTheme.getThemes(this)
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
                updateWidget()
                return true
            }
            android.R.id.home -> finish()
        }
        return true
    }

    private fun updateWidget() {
        val sp = getSharedPreferences(CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        val editor = sp.edit()
        val cal = GregorianCalendar()
        cal.timeInMillis = System.currentTimeMillis()
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val position = binding!!.themePager.currentItem
        editor.putInt(CALENDAR_WIDGET_THEME + widgetID, position)
        editor.putInt(CALENDAR_WIDGET_MONTH + widgetID, month)
        editor.putInt(CALENDAR_WIDGET_YEAR + widgetID, year)
        editor.apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        CalendarWidget.updateWidget(this@CalendarWidgetConfig, appWidgetManager, sp, widgetID)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private inner class MyFragmentPagerAdapter internal constructor(fm: FragmentManager, private val arrayList: List<CalendarTheme>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return CalendarThemeFragment.newInstance(position, mThemes)
        }

        override fun getCount(): Int {
            return arrayList.size
        }
    }

    companion object {
        val CALENDAR_WIDGET_PREF = "calendar_pref"
        val CALENDAR_WIDGET_THEME = "calendar_theme_"
        val CALENDAR_WIDGET_MONTH = "calendar_month_"
        val CALENDAR_WIDGET_YEAR = "calendar_year_"
    }
}
