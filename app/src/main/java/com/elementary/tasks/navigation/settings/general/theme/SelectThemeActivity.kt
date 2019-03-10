package com.elementary.tasks.navigation.settings.general.theme

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivitySelectThemeBinding
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
class SelectThemeActivity : ThemedActivity<ActivitySelectThemeBinding>() {

    private lateinit var themes: List<Theme>

    override fun layoutRes(): Int = R.layout.activity_select_theme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themes = createThemes()
        initToolbar()

        binding.warningCard.visibility = View.GONE

        binding.colorSliderBg.setColors(themeUtil.themeColorsForSlider())
        binding.colorSliderBg.setListener { position, _ ->
            prefs.appTheme = position
            prefs.isUiChanged = true
            onColorSelect(themes[position])
            checkCompat()
        }
        binding.colorSliderBg.setSelection(0)
        binding.colorSliderBg.setSelection(prefs.appTheme)

        binding.colorSlider.setColors(themeUtil.accentColorsForSlider())
        binding.colorSlider.setListener { position, color ->
            prefs.appThemeColor = position
            prefs.isUiChanged = true
            binding.bgTitle.setTextColor(color)
            binding.accentTitle.setTextColor(color)
            checkCompat()
        }
        binding.colorSlider.setSelection(0)
        binding.colorSlider.setSelection(prefs.appThemeColor)

        onColorSelect(themes[prefs.appTheme])
        checkCompat()
    }

    private fun checkCompat() {
        val bgColor = prefs.appTheme
        val accent = prefs.appThemeColor

        if (isBad(bgColor, accent)) {
            binding.warningCard.visibility = View.VISIBLE
        } else {
            binding.warningCard.visibility = View.GONE
        }
    }

    private fun isBad(bgColor: Int, accent: Int): Boolean {
        return when (bgColor) {
            ThemeUtil.THEME_PURE_WHITE, ThemeUtil.THEME_LIGHT_1, ThemeUtil.THEME_LIGHT_2,
            ThemeUtil.THEME_LIGHT_3, ThemeUtil.THEME_LIGHT_4 -> accent == ThemeUtil.Color.WHITE
            ThemeUtil.THEME_PURE_BLACK, ThemeUtil.THEME_DARK_3, ThemeUtil.THEME_DARK_4 -> accent == ThemeUtil.Color.BLACK
            ThemeUtil.THEME_RETRO_YELLOW -> accent == ThemeUtil.Color.WHITE
                    || accent == ThemeUtil.Color.YELLOW
                    || accent == ThemeUtil.Color.AMBER
                    || accent == ThemeUtil.Color.LIME
            ThemeUtil.THEME_RETRO_ORANGE -> accent == ThemeUtil.Color.YELLOW
                    || accent == ThemeUtil.Color.AMBER
                    || accent == ThemeUtil.Color.LIME
                    || accent == ThemeUtil.Color.ORANGE
                    || accent == ThemeUtil.Color.LIGHT_GREEN
            ThemeUtil.THEME_RETRO_GREEN -> accent == ThemeUtil.Color.WHITE
                    || accent == ThemeUtil.Color.YELLOW
                    || accent == ThemeUtil.Color.LIME
                    || accent == ThemeUtil.Color.GREEN
                    || accent == ThemeUtil.Color.LIGHT_GREEN
                    || accent == ThemeUtil.Color.TEAL
                    || accent == ThemeUtil.Color.LIGHT_BLUE
                    || accent == ThemeUtil.Color.CYAN
            ThemeUtil.THEME_RETRO_RED -> accent == ThemeUtil.Color.RED
                    || accent == ThemeUtil.Color.PINK
                    || accent == ThemeUtil.Color.PURPLE
                    || accent == ThemeUtil.Color.ORANGE
                    || accent == ThemeUtil.Color.DEEP_ORANGE
                    || accent == ThemeUtil.Color.LIVING_CORAL
            ThemeUtil.THEME_RETRO_BROWN -> accent == ThemeUtil.Color.LIGHT_BLUE
                    || accent == ThemeUtil.Color.CYAN
                    || accent == ThemeUtil.Color.TEAL
                    || accent == ThemeUtil.Color.GREEN
                    || accent == ThemeUtil.Color.LIGHT_GREEN
                    || accent == ThemeUtil.Color.LIME
            else -> false
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = getString(R.string.theme_color)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, false)
    }

    private fun createThemes(): List<Theme> {
        val loaded = prefs.appTheme
        return listOf(
                autoTheme(loaded),
                Theme(ThemeUtil.THEME_PURE_WHITE, loaded == ThemeUtil.THEME_PURE_WHITE, false, NAMES[0],
                        toColor(R.color.pureWhite), toColor(R.color.pureWhite), toColor(R.color.pureWhite)),
                Theme(ThemeUtil.THEME_LIGHT_1, loaded == ThemeUtil.THEME_LIGHT_1, false, NAMES[1],
                        toColor(R.color.lightPrimaryDark1), toColor(R.color.lightPrimary1), toColor(R.color.lightBg1)),
                Theme(ThemeUtil.THEME_LIGHT_2, loaded == ThemeUtil.THEME_LIGHT_2, false, NAMES[2],
                        toColor(R.color.lightPrimaryDark2), toColor(R.color.lightPrimary2), toColor(R.color.lightBg2)),
                Theme(ThemeUtil.THEME_LIGHT_3, loaded == ThemeUtil.THEME_LIGHT_3, false, NAMES[3],
                        toColor(R.color.lightPrimaryDark3), toColor(R.color.lightPrimary3), toColor(R.color.lightBg3)),
                Theme(ThemeUtil.THEME_LIGHT_4, loaded == ThemeUtil.THEME_LIGHT_4, false, NAMES[4],
                        toColor(R.color.lightPrimaryDark4), toColor(R.color.lightPrimary4), toColor(R.color.lightBg4)),
                Theme(ThemeUtil.THEME_DARK_1, loaded == ThemeUtil.THEME_DARK_1, true, NAMES[5],
                        toColor(R.color.darkPrimaryDark1), toColor(R.color.darkPrimary1), toColor(R.color.darkBg1)),
                Theme(ThemeUtil.THEME_DARK_2, loaded == ThemeUtil.THEME_DARK_2, true, NAMES[6],
                        toColor(R.color.darkPrimaryDark2), toColor(R.color.darkPrimary2), toColor(R.color.darkBg2)),
                Theme(ThemeUtil.THEME_DARK_3, loaded == ThemeUtil.THEME_DARK_3, true, NAMES[7],
                        toColor(R.color.darkPrimaryDark3), toColor(R.color.darkPrimary3), toColor(R.color.darkBg3)),
                Theme(ThemeUtil.THEME_DARK_4, loaded == ThemeUtil.THEME_DARK_4, true, NAMES[8],
                        toColor(R.color.darkPrimaryDark4), toColor(R.color.darkPrimary4), toColor(R.color.darkBg4)),
                Theme(ThemeUtil.THEME_PURE_BLACK, loaded == ThemeUtil.THEME_PURE_BLACK, true, NAMES[9],
                        toColor(R.color.pureBlack), toColor(R.color.pureBlack), toColor(R.color.pureBlack)),
                Theme(ThemeUtil.THEME_RETRO_YELLOW, loaded == ThemeUtil.THEME_RETRO_YELLOW, false, NAMES[10],
                        toColor(R.color.retroYellowDark), toColor(R.color.retroYellow), toColor(R.color.retroYellowBg)),
                Theme(ThemeUtil.THEME_RETRO_ORANGE, loaded == ThemeUtil.THEME_RETRO_ORANGE, false, NAMES[11],
                        toColor(R.color.retroOrangeDark), toColor(R.color.retroOrange), toColor(R.color.retroOrangeBg)),
                Theme(ThemeUtil.THEME_RETRO_GREEN, loaded == ThemeUtil.THEME_RETRO_GREEN, false, NAMES[12],
                        toColor(R.color.retroGreenDark), toColor(R.color.retroGreen), toColor(R.color.retroGreenBg)),
                Theme(ThemeUtil.THEME_RETRO_RED, loaded == ThemeUtil.THEME_RETRO_RED, false, NAMES[13],
                        toColor(R.color.retroRedDark), toColor(R.color.retroRed), toColor(R.color.retroRedBg)),
                Theme(ThemeUtil.THEME_RETRO_BROWN, loaded == ThemeUtil.THEME_RETRO_BROWN, false, NAMES[14],
                        toColor(R.color.retroBrownDark), toColor(R.color.retroBrown), toColor(R.color.retroBrownBg))

        )
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

    override fun onDestroy() {
        super.onDestroy()
        if (prefs.isSbNotificationEnabled && prefs.isUiChanged) {
            notifier.showReminderPermanent()
        }
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
        binding.bgTitle.text = getString(R.string.background) + " - " + theme.name
        binding.warningCard.setCardBackgroundColor(theme.bgColor)
        binding.toolbar.setBackgroundColor(theme.barColor)
        window.statusBarColor = theme.barColor
        binding.windowBackground.setBackgroundColor(theme.barColor)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, theme.isDark)
        if (theme.isDark) {
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.warningText.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.warningText.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
        binding.colorSliderBg.setSelectorColorResource(if (theme.isDark) R.color.pureWhite else R.color.pureBlack)
        binding.colorSlider.setSelectorColorResource(if (theme.isDark) R.color.pureWhite else R.color.pureBlack)
    }

    companion object {
        val NAMES = arrayOf(
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14",
                "15"
        )
    }
}
