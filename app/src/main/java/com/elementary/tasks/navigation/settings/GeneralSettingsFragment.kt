package com.elementary.tasks.navigation.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.navigation.settings.images.MainImageActivity
import com.elementary.tasks.navigation.settings.theme.SelectThemeActivity
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.fragment_settings_general.*

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
class GeneralSettingsFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0
    private val currentTheme: String
        get() {
            val theme = prefs.appTheme
            return when (theme) {
                ThemeUtil.THEME_AUTO -> getString(R.string.auto)
                ThemeUtil.THEME_WHITE -> getString(R.string.light)
                ThemeUtil.THEME_AMOLED -> getString(R.string.amoled)
                else -> getString(R.string.dark)
            }
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_general

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainImagePrefs.onClick { selectMainImage() }
        initAppTheme()
        initThemeColor()
        initSmartFold()
        initWearNotification()
        init24TimePrefs()
        initSavePrefs()
        initLanguagePrefs()
    }

    private fun initLanguagePrefs() {
        language_prefs.onClick { showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        language_prefs.setDetailText(language.getScreenLocaleName(context!!))
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

    private fun initSavePrefs() {
        savePrefs.isChecked = prefs.isAutoSaveEnabled
        savePrefs.onClick { changeSavePrefs() }
    }

    private fun changeSavePrefs() {
        val b = savePrefs.isChecked
        prefs.isAutoSaveEnabled = !b
        savePrefs.isChecked = !b
    }

    private fun init24TimePrefs() {
        time24hourPrefs.isChecked = prefs.is24HourFormatEnabled
        time24hourPrefs.onClick { change24Prefs() }
    }

    private fun change24Prefs() {
        val is24 = time24hourPrefs.isChecked
        prefs.is24HourFormatEnabled = !is24
        time24hourPrefs.isChecked = !is24
    }

    private fun initAppTheme() {
        appThemePrefs.setDetailText(currentTheme)
        appThemePrefs.onClick { showThemeDialog() }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.general))
            callback?.onFragmentSelect(this)
        }
    }

    private fun showThemeDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.theme))
        val colors = arrayOf(getString(R.string.auto), getString(R.string.light), getString(R.string.dark), getString(R.string.amoled))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        val initTheme = prefs.appTheme
        mItemSelect = initTheme
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.appTheme = mItemSelect
            dialog.dismiss()
            if (initTheme != mItemSelect) restartApp()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun restartApp() {
        startActivity(Intent(context, SplashScreen::class.java))
        activity!!.finishAffinity()
    }

    private fun selectMainImage() {
        startActivity(Intent(context, MainImageActivity::class.java))
    }

    private fun selectTheme() {
        startActivity(Intent(context, SelectThemeActivity::class.java))
    }

    private fun initThemeColor() {
        themePrefs.setViewResource(themeUtil.getIndicator(prefs.appThemeColor))
        themePrefs.onClick { selectTheme() }
    }

    private fun initSmartFold() {
        smartFoldPrefs.isChecked = prefs.isFoldingEnabled
        smartFoldPrefs.onClick { changeSmartFoldMode() }
    }

    private fun initWearNotification() {
        wearPrefs.isChecked = prefs.isWearEnabled
        wearPrefs.onClick { changeWearNotification() }
    }

    private fun changeWearNotification() {
        val isChecked = wearPrefs.isChecked
        prefs.isWearEnabled = !isChecked
        wearPrefs.isChecked = !isChecked
    }

    private fun changeSmartFoldMode() {
        val isChecked = smartFoldPrefs.isChecked
        prefs.isFoldingEnabled = !isChecked
        smartFoldPrefs.isChecked = !isChecked
    }
}
