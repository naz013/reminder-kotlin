package com.elementary.tasks.navigation.settings.general.theme

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_select_theme.*
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
class SelectThemeActivity : ThemedActivity() {

    private val adapter = ThemesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_theme)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = getString(R.string.theme_color)
        toolbar.navigationIcon = ViewUtils.backIcon(this, false)

        initList()
        addThemes()
    }

    private fun addThemes() {
        val loaded = prefs.appTheme
        val list = mutableListOf<Theme>()
        list.add(autoTheme(loaded))
        list.add(Theme(ThemeUtil.THEME_PURE_WHITE, loaded == ThemeUtil.THEME_PURE_WHITE, !Module.isPro, NAMES[0],
                toColor(R.color.pureWhite), toColor(R.color.pureWhite), toColor(R.color.pureWhite)))

        list.add(Theme(ThemeUtil.THEME_LIGHT_1, loaded == ThemeUtil.THEME_LIGHT_1, false, NAMES[1],
                toColor(R.color.lightPrimaryDark1), toColor(R.color.lightPrimary1), toColor(R.color.lightBg1)))

        list.add(Theme(ThemeUtil.THEME_LIGHT_2, loaded == ThemeUtil.THEME_LIGHT_2, false, NAMES[2],
                toColor(R.color.lightPrimaryDark2), toColor(R.color.lightPrimary2), toColor(R.color.lightBg2)))

        list.add(Theme(ThemeUtil.THEME_LIGHT_3, loaded == ThemeUtil.THEME_LIGHT_3, false, NAMES[3],
                toColor(R.color.lightPrimaryDark3), toColor(R.color.lightPrimary3), toColor(R.color.lightBg3)))

        list.add(Theme(ThemeUtil.THEME_LIGHT_4, loaded == ThemeUtil.THEME_LIGHT_4, false, NAMES[4],
                toColor(R.color.lightPrimaryDark4), toColor(R.color.lightPrimary4), toColor(R.color.lightBg4)))

        list.add(Theme(ThemeUtil.THEME_DARK_1, loaded == ThemeUtil.THEME_DARK_1, true, NAMES[5],
                toColor(R.color.darkPrimaryDark1), toColor(R.color.darkPrimary1), toColor(R.color.darkBg1)))

        list.add(Theme(ThemeUtil.THEME_DARK_2, loaded == ThemeUtil.THEME_DARK_2, true, NAMES[6],
                toColor(R.color.darkPrimaryDark2), toColor(R.color.darkPrimary2), toColor(R.color.darkBg2)))

        list.add(Theme(ThemeUtil.THEME_DARK_3, loaded == ThemeUtil.THEME_DARK_3, true, NAMES[7],
                toColor(R.color.darkPrimaryDark3), toColor(R.color.darkPrimary3), toColor(R.color.darkBg3)))

        list.add(Theme(ThemeUtil.THEME_DARK_4, loaded == ThemeUtil.THEME_DARK_4, true, NAMES[8],
                toColor(R.color.darkPrimaryDark4), toColor(R.color.darkPrimary4), toColor(R.color.darkBg4)))

        list.add(Theme(ThemeUtil.THEME_PURE_BLACK, loaded == ThemeUtil.THEME_PURE_BLACK, true, NAMES[9],
                toColor(R.color.pureBlack), toColor(R.color.pureBlack), toColor(R.color.pureBlack)))

        adapter.setThemes(list)
        themes_list.smoothScrollToPosition(loaded)
    }

    private fun autoTheme(loaded: Int): Theme {
        val calendar = Calendar.getInstance()
        val mTime = System.currentTimeMillis()
        calendar.timeInMillis = mTime
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        val min = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 19)
        val max = calendar.timeInMillis
        val isDark = mTime !in min..max
        return if (isDark) {
            Theme(ThemeUtil.THEME_AUTO, loaded == ThemeUtil.THEME_AUTO, true, getString(R.string.auto),
                    toColor(R.color.darkPrimaryDark1), toColor(R.color.darkPrimary1), toColor(R.color.darkBg1))
        } else {
            Theme(ThemeUtil.THEME_AUTO, loaded == ThemeUtil.THEME_AUTO, false, getString(R.string.auto),
                    toColor(R.color.lightPrimaryDark1), toColor(R.color.lightPrimary1), toColor(R.color.lightBg1))
        }
    }

    private fun toColor(@ColorRes res: Int): Int {
        return ContextCompat.getColor(this, res)
    }

    private fun initList() {
        themes_list.layoutManager = GridLayoutManager(this, 3)
        adapter.selectedListener = {
            onColorSelect(it)
            saveColor(it.id)
        }
        themes_list.adapter = adapter
    }

    private fun saveColor(code: Int) {
        prefs.appTheme = code
        prefs.isUiChanged = true
    }

    override fun onBackPressed() {
        updateNotification()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateNotification() {
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    private fun onColorSelect(theme: Theme) {
        toolbar.setBackgroundColor(theme.barColor)
        if (Module.isLollipop) {
            window.statusBarColor = theme.barColor
        }
        windowBackground.setBackgroundColor(theme.barColor)
        toolbar.navigationIcon = ViewUtils.backIcon(this, theme.isDark)
        if (theme.isDark) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    companion object {
        val NAMES = arrayOf(
                "FFFFFF",
                "F5F5F5",
                "EEEEEE",
                "E0E0E0",
                "9E9E9E",
                "757575",
                "616161",
                "424242",
                "212121",
                "000000"
        )
    }
}
