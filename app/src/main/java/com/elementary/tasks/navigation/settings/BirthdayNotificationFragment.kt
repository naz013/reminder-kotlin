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
        initMelodyDurationPrefs()
    }

    private fun initMelodyDurationPrefs() {
        binding.melodyDurationPrefs.setOnClickListener { showMelodyDurationDialog() }
        binding.melodyDurationPrefs.setReverseDependentView(binding.infiniteSoundOptionPrefs)
        showMelodyDuration()
    }

    private fun showMelodyDuration() {
        val label = when (prefs.birthdayPlaybackDuration) {
            5 -> durationLabels()[1]
            10 -> durationLabels()[2]
            15 -> durationLabels()[3]
            20 -> durationLabels()[4]
            30 -> durationLabels()[5]
            60 -> durationLabels()[6]
            else -> durationLabels()[0]
        }
        binding.melodyDurationPrefs.setDetailText(label)
    }

    private fun durationLabels(): Array<String> {
        return arrayOf(
                getString(R.string.till_the_end),
                "5 " + getString(R.string.seconds),
                "10 " + getString(R.string.seconds),
                "15 " + getString(R.string.seconds),
                "20 " + getString(R.string.seconds),
                "30 " + getString(R.string.seconds),
                "60 " + getString(R.string.seconds)
        )
    }

    private fun showMelodyDurationDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.melody_playback_duration))
            mItemSelect = when(prefs.birthdayPlaybackDuration) {
                5 -> 1
                10 -> 2
                15 -> 3
                20 -> 4
                30 -> 5
                60 -> 6
                else -> 0
            }
            builder.setSingleChoiceItems(durationLabels(), mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                prefs.birthdayPlaybackDuration = when (mItemSelect) {
                    1 -> 5
                    2 -> 10
                    3 -> 15
                    4 -> 20
                    5 -> 30
                    6 -> 60
                    else -> 0
                }
                showMelodyDuration()
            }
            builder.create().show()
        }
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
        val labels = melodyLabels()
        val label = when(filePath) {
            Constants.SOUND_RINGTONE -> labels[0]
            Constants.SOUND_NOTIFICATION, Constants.DEFAULT -> labels[1]
            Constants.SOUND_ALARM -> labels[2]
            else -> {
                if (!filePath.matches("".toRegex())) {
                    val sound = File(filePath)
                    val fileName = sound.name
                    val pos = fileName.lastIndexOf(".")
                    val fileNameS = fileName.substring(0, pos)
                    fileNameS
                } else {
                    labels[1]
                }
            }
        }
        binding.chooseSoundPrefs.setDetailText(label)
    }

    private fun melodyLabels(): Array<String> {
        return arrayOf(
                getString(R.string.default_string) + ": " + getString(R.string.ringtone),
                getString(R.string.default_string) + ": " + getString(R.string.notification),
                getString(R.string.default_string) + ": " + getString(R.string.alarm),
                getString(R.string.choose_file)
        )
    }

    private fun showSoundDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.melody))
            mItemSelect = when(prefs.birthdayMelody) {
                Constants.SOUND_RINGTONE -> 0
                Constants.SOUND_NOTIFICATION, Constants.DEFAULT -> 1
                Constants.SOUND_ALARM -> 2
                else -> 3
            }
            builder.setSingleChoiceItems(melodyLabels(), mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                when (mItemSelect) {
                    0 -> prefs.birthdayMelody = Constants.SOUND_RINGTONE
                    1 -> prefs.birthdayMelody = Constants.SOUND_NOTIFICATION
                    2 -> prefs.birthdayMelody = Constants.SOUND_ALARM
                    else -> {
                        startActivityForResult(Intent(it, FileExplorerActivity::class.java), MELODY_CODE)
                    }
                }
                showMelody()
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
