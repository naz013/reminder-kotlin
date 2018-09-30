package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_settings_birthday_notifications.*
import java.io.File

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

class BirthdayNotificationFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0
    private val localeAdapter: ArrayAdapter<String>
        get() = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice,
                language.getLocaleNames(context!!))

    override fun layoutRes(): Int = R.layout.fragment_settings_birthday_notifications

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

        initGlobalPrefs()
        initVibratePrefs()
        initInfiniteVibratePrefs()
        initSilentPrefs()
        initInfiniteSoundPrefs()
        initWakePrefs()
        initTtsPrefs()
        initTtsLocalePrefs()
        initMelodyPrefs()
        initLedPrefs()
        initLedColorPrefs()
    }

    private fun initLedColorPrefs() {
        chooseLedColorPrefs.setReverseDependentView(globalOptionPrefs)
        chooseLedColorPrefs.setDependentView(ledPrefs)
        chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
        showLedColor()
    }

    private fun showLedColor() {
        chooseLedColorPrefs.setDetailText(LED.getTitle(context!!, prefs.birthdayLedColor))
    }

    private fun showLedColorDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.led_color))
        val colors = LED.getAllNames(context!!)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        mItemSelect = prefs.birthdayLedColor
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.birthdayLedColor = mItemSelect
            showLedColor()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initLedPrefs() {
        ledPrefs.isChecked = prefs.isBirthdayLedEnabled
        ledPrefs.setOnClickListener { changeLedPrefs() }
        ledPrefs.setReverseDependentView(globalOptionPrefs)
    }

    private fun changeLedPrefs() {
        val isChecked = ledPrefs.isChecked
        ledPrefs.isChecked = !isChecked
        prefs.isBirthdayLedEnabled = !isChecked
    }

    private fun initMelodyPrefs() {
        chooseSoundPrefs.setOnClickListener { showSoundDialog() }
        chooseSoundPrefs.setReverseDependentView(globalOptionPrefs)
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs.birthdayMelody
        if (filePath == "" || filePath.matches(Constants.DEFAULT.toRegex())) {
            chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        } else if (!filePath.matches("".toRegex())) {
            val sound = File(filePath)
            val fileName = sound.name
            val pos = fileName.lastIndexOf(".")
            val fileNameS = fileName.substring(0, pos)
            chooseSoundPrefs.setDetailText(fileNameS)
        } else {
            chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        }
    }

    private fun showSoundDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.melody))
        val types = arrayOf(getString(R.string.default_string), getString(R.string.choose_file))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        mItemSelect = if (prefs.birthdayMelody == "" || prefs.birthdayMelody.matches(Constants.DEFAULT.toRegex())) {
            0
        } else {
            1
        }
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            if (mItemSelect == 0) {
                prefs.birthdayMelody = Constants.DEFAULT
                showMelody()
            } else {
                dialog.dismiss()
                startActivityForResult(Intent(context, FileExplorerActivity::class.java), MELODY_CODE)
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initTtsLocalePrefs() {
        localePrefs.setReverseDependentView(globalOptionPrefs)
        localePrefs.setDependentView(ttsPrefs)
        localePrefs.setOnClickListener { showTtsLocaleDialog() }
        showTtsLocale()
    }

    private fun showTtsLocale() {
        val locale = prefs.birthdayTtsLocale
        val i = language.getLocalePosition(locale)
        localePrefs.setDetailText(language.getLocaleNames(context!!)[i])
    }

    private fun showTtsLocaleDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locale = prefs.birthdayTtsLocale
        mItemSelect = language.getLocalePosition(locale)
        builder.setSingleChoiceItems(localeAdapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            saveTtsLocalePrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun saveTtsLocalePrefs() {
        prefs.birthdayTtsLocale = language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsPrefs() {
        ttsPrefs.isChecked = prefs.isBirthdayTtsEnabled
        ttsPrefs.setReverseDependentView(globalOptionPrefs)
        ttsPrefs.setOnClickListener { changeTtsPrefs() }
    }

    private fun changeTtsPrefs() {
        val isChecked = ttsPrefs.isChecked
        ttsPrefs.isChecked = !isChecked
        prefs.isBirthdayTtsEnabled = !isChecked
    }

    private fun initWakePrefs() {
        wakeScreenOptionPrefs.isChecked = prefs.isBirthdayWakeEnabled
        wakeScreenOptionPrefs.setReverseDependentView(globalOptionPrefs)
        wakeScreenOptionPrefs.setOnClickListener { changeWakePrefs() }
    }

    private fun changeWakePrefs() {
        val isChecked = wakeScreenOptionPrefs.isChecked
        wakeScreenOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayWakeEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        infiniteSoundOptionPrefs.setReverseDependentView(globalOptionPrefs)
        infiniteSoundOptionPrefs.isChecked = prefs.isBirthdayInfiniteSoundEnabled
        infiniteSoundOptionPrefs.setOnClickListener { changeInfiniteSoundPrefs() }
    }

    private fun changeInfiniteSoundPrefs() {
        val isChecked = infiniteSoundOptionPrefs.isChecked
        infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayInfiniteSoundEnabled = !isChecked
    }

    private fun initSilentPrefs() {
        soundOptionPrefs.isChecked = prefs.isBirthdaySilentEnabled
        soundOptionPrefs.setOnClickListener { changeSilentPrefs() }
        soundOptionPrefs.setReverseDependentView(globalOptionPrefs)
    }

    private fun changeSilentPrefs() {
        val isChecked = soundOptionPrefs.isChecked
        soundOptionPrefs.isChecked = !isChecked
        prefs.isBirthdaySilentEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        infiniteVibrateOptionPrefs.isChecked = prefs.isBirthdayInfiniteVibrationEnabled
        infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibrationPrefs() }
        infiniteVibrateOptionPrefs.setReverseDependentView(globalOptionPrefs)
    }

    private fun changeInfiniteVibrationPrefs() {
        val isChecked = infiniteVibrateOptionPrefs.isChecked
        infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayInfiniteVibrationEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        vibrationOptionPrefs.isChecked = prefs.isBirthdayVibrationEnabled
        vibrationOptionPrefs.setOnClickListener { changeVibrationPrefs() }
        vibrationOptionPrefs.setReverseDependentView(globalOptionPrefs)
    }

    private fun changeVibrationPrefs() {
        val isChecked = vibrationOptionPrefs.isChecked
        vibrationOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayVibrationEnabled = !isChecked
    }

    private fun initGlobalPrefs() {
        globalOptionPrefs.isChecked = prefs.isBirthdayGlobalEnabled
        globalOptionPrefs.setOnClickListener { changeGlobalPrefs() }
    }

    private fun changeGlobalPrefs() {
        val isChecked = globalOptionPrefs.isChecked
        globalOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayGlobalEnabled = !isChecked
    }

    override fun getTitle(): String = getString(R.string.birthday_notification)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data!!.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs.birthdayMelody = file.toString()
                    }
                }
                showMelody()
            }
        }
    }

    companion object {

        private const val MELODY_CODE = 126
    }
}
