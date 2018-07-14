package com.elementary.tasks.core.appWidgets.notes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import kotlinx.android.synthetic.main.widget_note_config.*

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
class NotesWidgetConfig : ThemedActivity() {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null

    private var mThemes: List<NotesTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()
        setContentView(R.layout.widget_note_config)
        initActionBar()
        loadThemes()
        showCurrentTheme()
    }

    private fun showCurrentTheme() {
        val sp = getSharedPreferences(NOTES_WIDGET_PREF, MODE_PRIVATE)
        val theme = sp.getInt(NOTES_WIDGET_THEME + widgetID, 0)
        themePager.setCurrentItem(theme, true)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.notes)
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
        setResult(RESULT_CANCELED, resultValue)
    }

    private fun loadThemes() {
        mThemes = NotesTheme.getThemes(this)
        val adapter = MyFragmentPagerAdapter(supportFragmentManager, mThemes!!)
        themePager.adapter = adapter
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
        val sp = getSharedPreferences(NOTES_WIDGET_PREF, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(NOTES_WIDGET_THEME + widgetID, themePager.currentItem)
        editor.apply()
        val appWidgetManager = AppWidgetManager.getInstance(this)
        NotesWidget.updateWidget(this@NotesWidgetConfig, appWidgetManager, sp, widgetID)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    inner class MyFragmentPagerAdapter internal constructor(fm: FragmentManager, private val arrayList: List<NotesTheme>) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return NotesThemeFragment.newInstance(position, mThemes!!)
        }

        override fun getCount(): Int {
            return arrayList.size
        }
    }

    companion object {
        const val NOTES_WIDGET_PREF = "notes_pref"
        const val NOTES_WIDGET_THEME = "notes_theme_"
    }
}
