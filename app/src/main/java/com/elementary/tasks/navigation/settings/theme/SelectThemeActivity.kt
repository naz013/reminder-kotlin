package com.elementary.tasks.navigation.settings.theme

import android.os.Bundle
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.views.ColorPickerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import androidx.appcompat.widget.Toolbar

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

class SelectThemeActivity : ThemedActivity(), ColorPickerView.OnColorListener {

    private var mFab: FloatingActionButton? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_theme)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toolbar!!.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar!!.title = getString(R.string.theme_color)
        val loaded = prefs!!.appThemeColor
        val pickerView = findViewById<ColorPickerView>(R.id.pickerView)
        pickerView.setListener(this)
        pickerView.setSelectedColor(loaded)

        mFab = findViewById(R.id.fab)
        mFab!!.backgroundTintList = ViewUtils.getFabState(this, themeUtil!!.colorAccent(), themeUtil!!.colorPrimary())
    }

    private fun saveColor(code: Int) {
        prefs!!.appThemeColor = code
        prefs!!.isUiChanged = true
    }

    override fun onBackPressed() {
        updateNotification()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                updateNotification()
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateNotification() {
        if (prefs!!.isSbNotificationEnabled) {
            Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    private fun refreshUi() {
        toolbar!!.setBackgroundColor(ViewUtils.getColor(this, themeUtil!!.colorPrimary()))
        if (Module.isLollipop) {
            window.statusBarColor = ViewUtils.getColor(this, themeUtil!!.colorPrimaryDark())
        }
        mFab!!.backgroundTintList = ViewUtils.getFabState(this, themeUtil!!.colorAccent(), themeUtil!!.colorPrimary())
        mFab!!.rippleColor = ViewUtils.getColor(this, themeUtil!!.colorPrimary())
    }

    override fun onColorSelect(code: Int) {
        saveColor(code)
        refreshUi()
    }
}
