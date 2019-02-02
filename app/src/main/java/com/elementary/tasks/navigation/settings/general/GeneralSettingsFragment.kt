package com.elementary.tasks.navigation.settings.general

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.general.home.PageIdentifier
import com.elementary.tasks.navigation.settings.general.theme.SelectThemeActivity

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
class GeneralSettingsFragment : BaseSettingsFragment<FragmentSettingsGeneralBinding>() {

    private var mItemSelect: Int = 0
    private val currentTheme: String
        get() {
            val theme = prefs.appTheme
            val accent = themeUtil.accentNames()[prefs.appThemeColor]
            return when (theme) {
                ThemeUtil.THEME_AUTO -> getString(R.string.auto) + " ($accent)"
                else -> SelectThemeActivity.NAMES[theme - 1] + " ($accent)"
            }
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_general

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initAppTheme()
        init24TimePrefs()
        initLanguagePrefs()
        initHomePage()
        initColsPrefs()
    }

    private fun initColsPrefs() {
        binding.twoColsPrefs.isChecked = prefs.isTwoColsEnabled
        binding.twoColsPrefs.setOnClickListener { changeColsPrefs() }
    }

    private fun changeColsPrefs() {
        val b = binding.twoColsPrefs.isChecked
        prefs.isTwoColsEnabled = !b
        binding.twoColsPrefs.isChecked = !b
    }

    private fun initHomePage() {
        binding.homePrefs.setOnClickListener { showHomePageDialog() }
        showHomePage()
    }

    private fun showHomePage() {
        binding.homePrefs.setDetailText(PageIdentifier.name(context!!, prefs.homePage))
    }

    private fun showHomePageDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.start_screen))

        val homePages = PageIdentifier.availablePages(context!!)

        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, homePages.map { it.name })
        val init = prefs.homePage
        mItemSelect = PageIdentifier.index(context!!, init)
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.homePage = homePages[mItemSelect].key
            dialog.dismiss()
            showHomePage()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initLanguagePrefs() {
        binding.languagePrefs.setOnClickListener { showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        binding.languagePrefs.setDetailText(language.getScreenLocaleName(context!!))
    }

    private fun showLanguageDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.application_language))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, resources.getStringArray(R.array.app_languages))
        val init = prefs.appLanguage
        mItemSelect = init
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.appLanguage = mItemSelect
            dialog.dismiss()
            if (init != mItemSelect) restartApp()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun init24TimePrefs() {
        binding.time24hourPrefs.setOnClickListener { showTimeFormatDialog() }
        showTimeFormat()
    }

    private fun showTimeFormat() {
        binding.time24hourPrefs.setDetailText(currentFormat())
    }

    private fun currentFormat(): String? {
        return when (prefs.hourFormat) {
            0 -> getString(R.string.default_string)
            1 -> getString(R.string.use_24_hour_format)
            2 -> getString(R.string.use_12_hour_format)
            else -> getString(R.string.default_string)
        }
    }

    private fun showTimeFormatDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string._24_hour_time_format))

        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice,
                listOf(
                        getString(R.string.default_string),
                        getString(R.string.use_24_hour_format),
                        getString(R.string.use_12_hour_format)
                ))
        mItemSelect = prefs.hourFormat
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.hourFormat = mItemSelect
            dialog.dismiss()
            showTimeFormat()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initAppTheme() {
        binding.appThemePrefs.setDetailText(currentTheme)
        binding.appThemePrefs.setOnClickListener { selectTheme() }
    }

    override fun getTitle(): String = getString(R.string.general)

    private fun restartApp() {
        startActivity(Intent(context, SplashScreen::class.java))
        activity?.finishAffinity()
    }

    private fun selectTheme() {
        startActivity(Intent(context, SelectThemeActivity::class.java))
    }
}
