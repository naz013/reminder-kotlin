package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.elementary.tasks.R
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.databinding.FragmentBirthdayNotificationsBinding

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

    private var binding: FragmentBirthdayNotificationsBinding? = null
    private var mItemSelect: Int = 0


    private val localeAdapter: ArrayAdapter<String>
        get() = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice,
                Language.getLocaleNames(context!!))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBirthdayNotificationsBinding.inflate(inflater, container, false)
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
        return binding!!.root
    }

    private fun initLedColorPrefs() {
        binding!!.chooseLedColorPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        binding!!.chooseLedColorPrefs.setDependentView(binding!!.ledPrefs)
        binding!!.chooseLedColorPrefs.setOnClickListener { view -> showLedColorDialog() }
        showLedColor()
    }

    private fun showLedColor() {
        binding!!.chooseLedColorPrefs.setDetailText(LED.getTitle(context, prefs!!.birthdayLedColor))
    }

    private fun showLedColorDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.led_color))
        val colors = LED.getAllNames(context)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        mItemSelect = prefs!!.birthdayLedColor
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.birthdayLedColor = mItemSelect
            showLedColor()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun initLedPrefs() {
        binding!!.ledPrefs.isChecked = prefs!!.isBirthdayLedEnabled
        binding!!.ledPrefs.setOnClickListener { view -> changeLedPrefs() }
        binding!!.ledPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
    }

    private fun changeLedPrefs() {
        val isChecked = binding!!.ledPrefs.isChecked
        binding!!.ledPrefs.isChecked = !isChecked
        prefs!!.isBirthdayLedEnabled = !isChecked
    }

    private fun initMelodyPrefs() {
        binding!!.chooseSoundPrefs.setOnClickListener { view -> showSoundDialog() }
        binding!!.chooseSoundPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs!!.birthdayMelody
        if (filePath == null || filePath.matches(Constants.DEFAULT.toRegex())) {
            binding!!.chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        } else if (!filePath.matches("".toRegex())) {
            val sound = File(filePath)
            val fileName = sound.name
            val pos = fileName.lastIndexOf(".")
            val fileNameS = fileName.substring(0, pos)
            binding!!.chooseSoundPrefs.setDetailText(fileNameS)
        } else {
            binding!!.chooseSoundPrefs.setDetailText(resources.getString(R.string.default_string))
        }
    }

    private fun showSoundDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.melody))
        val types = arrayOf(getString(R.string.default_string), getString(R.string.choose_file))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        if (prefs!!.birthdayMelody == null || prefs!!.birthdayMelody!!.matches(Constants.DEFAULT.toRegex())) {
            mItemSelect = 0
        } else {
            mItemSelect = 1
        }
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            if (mItemSelect == 0) {
                prefs!!.birthdayMelody = Constants.DEFAULT
                showMelody()
            } else {
                dialog.dismiss()
                startActivityForResult(Intent(context, FileExplorerActivity::class.java), MELODY_CODE)
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun initTtsLocalePrefs() {
        binding!!.localePrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        binding!!.localePrefs.setDependentView(binding!!.ttsPrefs)
        binding!!.localePrefs.setOnClickListener { view -> showTtsLocaleDialog() }
        showTtsLocale()
    }

    private fun showTtsLocale() {
        val locale = prefs!!.birthdayTtsLocale
        val i = Language.getLocalePosition(locale)
        binding!!.localePrefs.setDetailText(Language.getLocaleNames(context!!)[i])
    }

    private fun showTtsLocaleDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locale = prefs!!.birthdayTtsLocale
        mItemSelect = Language.getLocalePosition(locale)
        builder.setSingleChoiceItems(localeAdapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            saveTtsLocalePrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun saveTtsLocalePrefs() {
        prefs!!.birthdayTtsLocale = Language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsPrefs() {
        binding!!.ttsPrefs.isChecked = prefs!!.isBirthdayTtsEnabled
        binding!!.ttsPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        binding!!.ttsPrefs.setOnClickListener { view -> changeTtsPrefs() }
    }

    private fun changeTtsPrefs() {
        val isChecked = binding!!.ttsPrefs.isChecked
        binding!!.ttsPrefs.isChecked = !isChecked
        prefs!!.isBirthdayTtsEnabled = !isChecked
    }

    private fun initWakePrefs() {
        binding!!.wakeScreenOptionPrefs.isChecked = prefs!!.isBirthdayWakeEnabled
        binding!!.wakeScreenOptionPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        binding!!.wakeScreenOptionPrefs.setOnClickListener { view -> changeWakePrefs() }
    }

    private fun changeWakePrefs() {
        val isChecked = binding!!.wakeScreenOptionPrefs.isChecked
        binding!!.wakeScreenOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdayWakeEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        binding!!.infiniteSoundOptionPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
        binding!!.infiniteSoundOptionPrefs.isChecked = prefs!!.isBirthdayInfiniteSoundEnabled
        binding!!.infiniteSoundOptionPrefs.setOnClickListener { view -> changeInfiniteSoundPrefs() }
    }

    private fun changeInfiniteSoundPrefs() {
        val isChecked = binding!!.infiniteSoundOptionPrefs.isChecked
        binding!!.infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdayInfiniteSoundEnabled = !isChecked
    }

    private fun initSilentPrefs() {
        binding!!.soundOptionPrefs.isChecked = prefs!!.isBirthdaySilentEnabled
        binding!!.soundOptionPrefs.setOnClickListener { view -> changeSilentPrefs() }
        binding!!.soundOptionPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
    }

    private fun changeSilentPrefs() {
        val isChecked = binding!!.soundOptionPrefs.isChecked
        binding!!.soundOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdaySilentEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        binding!!.infiniteVibrateOptionPrefs.isChecked = prefs!!.isBirthdayInfiniteVibrationEnabled
        binding!!.infiniteVibrateOptionPrefs.setOnClickListener { view -> changeInfiniteVibrationPrefs() }
        binding!!.infiniteVibrateOptionPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
    }

    private fun changeInfiniteVibrationPrefs() {
        val isChecked = binding!!.infiniteVibrateOptionPrefs.isChecked
        binding!!.infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdayInfiniteVibrationEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        binding!!.vibrationOptionPrefs.isChecked = prefs!!.isBirthdayVibrationEnabled
        binding!!.vibrationOptionPrefs.setOnClickListener { view -> changeVibrationPrefs() }
        binding!!.vibrationOptionPrefs.setReverseDependentView(binding!!.globalOptionPrefs)
    }

    private fun changeVibrationPrefs() {
        val isChecked = binding!!.vibrationOptionPrefs.isChecked
        binding!!.vibrationOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdayVibrationEnabled = !isChecked
    }

    private fun initGlobalPrefs() {
        binding!!.globalOptionPrefs.isChecked = prefs!!.isBirthdayGlobalEnabled
        binding!!.globalOptionPrefs.setOnClickListener { view -> changeGlobalPrefs() }
    }

    private fun changeGlobalPrefs() {
        val isChecked = binding!!.globalOptionPrefs.isChecked
        binding!!.globalOptionPrefs.isChecked = !isChecked
        prefs!!.isBirthdayGlobalEnabled = !isChecked
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.birthday_notification))
            callback!!.onFragmentSelect(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data!!.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs!!.birthdayMelody = file.toString()
                    }
                }
                showMelody()
            }
        }
    }

    companion object {

        private val MELODY_CODE = 126
    }
}
