package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.file_explorer.FileExplorerActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsBirthdayNotificationsBinding
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
class BirthdayNotificationFragment : BaseSettingsFragment<FragmentSettingsBirthdayNotificationsBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_birthday_notifications

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
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
        binding.chooseLedColorPrefs.setReverseDependentView(binding.globalOptionPrefs)
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs)
        binding.chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
        showLedColor()
    }

    private fun showLedColor() {
        withContext {
            binding.chooseLedColorPrefs.setDetailText(LED.getTitle(it, prefs.birthdayLedColor))
        }
    }

    private fun showLedColorDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.led_color))
            val colors = LED.getAllNames(it).toTypedArray()
            mItemSelect = prefs.birthdayLedColor
            builder.setSingleChoiceItems(colors, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.birthdayLedColor = mItemSelect
                showLedColor()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun initLedPrefs() {
        binding.ledPrefs.isChecked = prefs.isBirthdayLedEnabled
        binding.ledPrefs.setOnClickListener { changeLedPrefs() }
        binding.ledPrefs.setReverseDependentView(binding.globalOptionPrefs)
    }

    private fun changeLedPrefs() {
        val isChecked = binding.ledPrefs.isChecked
        binding.ledPrefs.isChecked = !isChecked
        prefs.isBirthdayLedEnabled = !isChecked
    }

    private fun initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener { showSoundDialog() }
        binding.chooseSoundPrefs.setReverseDependentView(binding.globalOptionPrefs)
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs.birthdayMelody
        if (filePath == "" || filePath.matches(Constants.DEFAULT.toRegex())) {
            binding.chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        } else if (!filePath.matches("".toRegex())) {
            val sound = File(filePath)
            val fileName = sound.name
            val pos = fileName.lastIndexOf(".")
            val fileNameS = fileName.substring(0, pos)
            binding.chooseSoundPrefs.setDetailText(fileNameS)
        } else {
            binding.chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        }
    }

    private fun showSoundDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.melody))
            val types = arrayOf(getString(R.string.default_string), getString(R.string.choose_file))
            mItemSelect = if (prefs.birthdayMelody == "" || prefs.birthdayMelody.matches(Constants.DEFAULT.toRegex())) {
                0
            } else {
                1
            }
            builder.setSingleChoiceItems(types, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                if (mItemSelect == 0) {
                    prefs.birthdayMelody = Constants.DEFAULT
                    showMelody()
                } else {
                    dialog.dismiss()
                    startActivityForResult(Intent(it, FileExplorerActivity::class.java), MELODY_CODE)
                }
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun initTtsLocalePrefs() {
        binding.localePrefs.setReverseDependentView(binding.globalOptionPrefs)
        binding.localePrefs.setDependentView(binding.ttsPrefs)
        binding.localePrefs.setOnClickListener { showTtsLocaleDialog() }
        showTtsLocale()
    }

    private fun showTtsLocale() {
        val locale = prefs.birthdayTtsLocale
        val i = language.getLocalePosition(locale)
        withContext {
            binding.localePrefs.setDetailText(language.getLocaleNames(it)[i])
        }
    }

    private fun showTtsLocaleDialog() {
        withContext {
            val names = language.getLocaleNames(it).toTypedArray()
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.language))
            val locale = prefs.birthdayTtsLocale
            mItemSelect = language.getLocalePosition(locale)
            builder.setSingleChoiceItems(names, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                saveTtsLocalePrefs()
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun saveTtsLocalePrefs() {
        prefs.birthdayTtsLocale = language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsPrefs() {
        binding.ttsPrefs.isChecked = prefs.isBirthdayTtsEnabled
        binding.ttsPrefs.setReverseDependentView(binding.globalOptionPrefs)
        binding.ttsPrefs.setOnClickListener { changeTtsPrefs() }
    }

    private fun changeTtsPrefs() {
        val isChecked = binding.ttsPrefs.isChecked
        binding.ttsPrefs.isChecked = !isChecked
        prefs.isBirthdayTtsEnabled = !isChecked
    }

    private fun initWakePrefs() {
        binding.wakeScreenOptionPrefs.isChecked = prefs.isBirthdayWakeEnabled
        binding.wakeScreenOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
        binding.wakeScreenOptionPrefs.setOnClickListener { changeWakePrefs() }
    }

    private fun changeWakePrefs() {
        val isChecked = binding.wakeScreenOptionPrefs.isChecked
        binding.wakeScreenOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayWakeEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
        binding.infiniteSoundOptionPrefs.isChecked = prefs.isBirthdayInfiniteSoundEnabled
        binding.infiniteSoundOptionPrefs.setOnClickListener { changeInfiniteSoundPrefs() }
    }

    private fun changeInfiniteSoundPrefs() {
        val isChecked = binding.infiniteSoundOptionPrefs.isChecked
        binding.infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayInfiniteSoundEnabled = !isChecked
    }

    private fun initSilentPrefs() {
        binding.soundOptionPrefs.isChecked = prefs.isBirthdaySilentEnabled
        binding.soundOptionPrefs.setOnClickListener { changeSilentPrefs() }
        binding.soundOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
    }

    private fun changeSilentPrefs() {
        val isChecked = binding.soundOptionPrefs.isChecked
        binding.soundOptionPrefs.isChecked = !isChecked
        prefs.isBirthdaySilentEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.isChecked = prefs.isBirthdayInfiniteVibrationEnabled
        binding.infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibrationPrefs() }
        binding.infiniteVibrateOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
    }

    private fun changeInfiniteVibrationPrefs() {
        val isChecked = binding.infiniteVibrateOptionPrefs.isChecked
        binding.infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayInfiniteVibrationEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        binding.vibrationOptionPrefs.isChecked = prefs.isBirthdayVibrationEnabled
        binding.vibrationOptionPrefs.setOnClickListener { changeVibrationPrefs() }
        binding.vibrationOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
    }

    private fun changeVibrationPrefs() {
        val isChecked = binding.vibrationOptionPrefs.isChecked
        binding.vibrationOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayVibrationEnabled = !isChecked
    }

    private fun initGlobalPrefs() {
        binding.globalOptionPrefs.isChecked = prefs.isBirthdayGlobalEnabled
        binding.globalOptionPrefs.setOnClickListener { changeGlobalPrefs() }
    }

    private fun changeGlobalPrefs() {
        val isChecked = binding.globalOptionPrefs.isChecked
        binding.globalOptionPrefs.isChecked = !isChecked
        prefs.isBirthdayGlobalEnabled = !isChecked
    }

    override fun getTitle(): String = getString(R.string.birthday_notification)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data?.getStringExtra(Constants.FILE_PICKED)
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
