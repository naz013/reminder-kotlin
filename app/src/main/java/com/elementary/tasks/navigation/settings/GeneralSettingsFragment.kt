package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.elementary.tasks.R
import com.elementary.tasks.core.SplashScreen
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding
import com.elementary.tasks.navigation.settings.images.MainImageActivity
import com.elementary.tasks.navigation.settings.theme.SelectThemeActivity

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

    private var binding: FragmentSettingsGeneralBinding? = null
    private val mFoldingClick = { view -> changeSmartFoldMode() }
    private val mWearClick = { view -> changeWearNotification() }
    private val mThemeClick = { view -> selectTheme() }
    private val mMainImageClick = { view -> selectMainImage() }

    private var mItemSelect: Int = 0

    private val currentTheme: String
        get() {
            val theme = prefs!!.appTheme
            return if (theme == ThemeUtil.THEME_AUTO)
                getString(R.string.auto)
            else if (theme == ThemeUtil.THEME_WHITE)
                getString(R.string.light)
            else if (theme == ThemeUtil.THEME_AMOLED)
                getString(R.string.amoled)
            else
                getString(R.string.dark)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false)
        initAppTheme()
        initThemeColor()
        initMainImage()
        initSmartFold()
        initWearNotification()
        init24TimePrefs()
        initSavePrefs()
        initLanguagePrefs()
        return binding!!.root
    }

    private fun initLanguagePrefs() {
        binding!!.languagePrefs.setOnClickListener { v -> showLanguageDialog() }
        showLanguage()
    }

    private fun showLanguage() {
        binding!!.languagePrefs.setDetailText(Language.getScreenLocaleName(context))
    }

    private fun showLanguageDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.application_language))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, resources.getStringArray(R.array.app_languages))
        val init = prefs!!.appLanguage
        mItemSelect = init
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.appLanguage = mItemSelect
            dialog.dismiss()
            if (init != mItemSelect) restartApp()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun initSavePrefs() {
        binding!!.savePrefs.isChecked = prefs!!.isAutoSaveEnabled
        binding!!.savePrefs.setOnClickListener { v -> changeSavePrefs() }
    }

    private fun changeSavePrefs() {
        val b = binding!!.savePrefs.isChecked
        prefs!!.isAutoSaveEnabled = !b
        binding!!.savePrefs.isChecked = !b
    }

    private fun init24TimePrefs() {
        binding!!.time24hourPrefs.isChecked = prefs!!.is24HourFormatEnabled
        binding!!.time24hourPrefs.setOnClickListener { view -> change24Prefs() }
    }

    private fun change24Prefs() {
        val is24 = binding!!.time24hourPrefs.isChecked
        prefs!!.is24HourFormatEnabled = !is24
        binding!!.time24hourPrefs.isChecked = !is24
    }

    private fun initAppTheme() {
        binding!!.appThemePrefs.setDetailText(currentTheme)
        binding!!.appThemePrefs.setOnClickListener { view -> showThemeDialog() }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.general))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun showThemeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.theme))
        val colors = arrayOf(getString(R.string.auto), getString(R.string.light), getString(R.string.dark), getString(R.string.amoled))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        val initTheme = prefs!!.appTheme
        mItemSelect = initTheme
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.appTheme = mItemSelect
            dialog.dismiss()
            if (initTheme != mItemSelect) restartApp()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun restartApp() {
        startActivity(Intent(context, SplashScreen::class.java))
        activity!!.finishAffinity()
    }

    private fun selectMainImage() {
        startActivity(Intent(context, MainImageActivity::class.java))
    }

    private fun initMainImage() {
        binding!!.mainImagePrefs.setOnClickListener(mMainImageClick)
    }

    private fun selectTheme() {
        startActivity(Intent(context, SelectThemeActivity::class.java))
    }

    private fun initThemeColor() {
        binding!!.themePrefs.setViewResource(ThemeUtil.getInstance(context).getIndicator(prefs!!.appThemeColor))
        binding!!.themePrefs.setOnClickListener(mThemeClick)
    }

    private fun initSmartFold() {
        binding!!.smartFoldPrefs.isChecked = prefs!!.isFoldingEnabled
        binding!!.smartFoldPrefs.setOnClickListener(mFoldingClick)
    }

    private fun initWearNotification() {
        binding!!.wearPrefs.isChecked = prefs!!.isWearEnabled
        binding!!.wearPrefs.setOnClickListener(mWearClick)
    }

    private fun changeWearNotification() {
        val isChecked = binding!!.wearPrefs.isChecked
        prefs!!.isWearEnabled = !isChecked
        binding!!.wearPrefs.isChecked = !isChecked
    }

    private fun changeSmartFoldMode() {
        val isChecked = binding!!.smartFoldPrefs.isChecked
        prefs!!.isFoldingEnabled = !isChecked
        binding!!.smartFoldPrefs.isChecked = !isChecked
    }
}
