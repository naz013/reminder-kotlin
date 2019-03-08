package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.file_explorer.FileExplorerActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding
import java.io.File
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
class NotificationSettingsFragment : BaseSettingsFragment<FragmentSettingsNotificationBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_notification

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initManualPrefs()
        initSbPrefs()
        initSbIconPrefs()
        initVibratePrefs()
        initInfiniteVibratePrefs()
        initSoundInSilentModePrefs()
        initInfiniteSoundPrefs()
        initMelodyPrefs()
        initSystemLoudnessPrefs()
        initSoundStreamPrefs()
        initLoudnessPrefs()
        initIncreasingLoudnessPrefs()
        initTtsPrefs()
        initTtsLocalePrefs()
        initUnlockPrefs()
        initAutoLaunchPrefs()
        initSnoozeTimePrefs()
        initLedPrefs()
        initLedColorPrefs()
        initRepeatPrefs()
        initRepeatTimePrefs()
        initAutoCallPrefs()
        initReminderTypePrefs()
        initIgnoreWindowTypePrefs()
        initSmartFold()
        initWearNotification()
        initUnlockPriorityPrefs()
        initImagePrefs()
    }

    private fun initUnlockPriorityPrefs() {
        binding.unlockPriorityPrefs.setOnClickListener { showPriorityDialog() }
        binding.unlockPriorityPrefs.setDependentView(binding.unlockScreenPrefs)
        showPriority()
    }

    private fun showPriority() {
        binding.unlockPriorityPrefs.setDetailText(unlockList()[prefs.unlockPriority])
    }

    private fun showPriorityDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.priority))
            mItemSelect = prefs.unlockPriority
            builder.setSingleChoiceItems(unlockList(), mItemSelect) { _, which ->
                mItemSelect = which
            }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.unlockPriority = mItemSelect
                showPriority()
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun initSmartFold() {
        binding.smartFoldPrefs.isChecked = prefs.isFoldingEnabled
        binding.smartFoldPrefs.setOnClickListener { changeSmartFoldMode() }
    }

    private fun initWearNotification() {
        binding.wearPrefs.isChecked = prefs.isWearEnabled
        binding.wearPrefs.setOnClickListener { changeWearNotification() }
    }

    private fun changeWearNotification() {
        val isChecked = binding.wearPrefs.isChecked
        prefs.isWearEnabled = !isChecked
        binding.wearPrefs.isChecked = !isChecked
    }

    private fun changeSmartFoldMode() {
        val isChecked = binding.smartFoldPrefs.isChecked
        prefs.isFoldingEnabled = !isChecked
        binding.smartFoldPrefs.isChecked = !isChecked
    }

    private fun changeIgnoreWindowTypePrefs() {
        val isChecked = binding.ignoreWindowType.isChecked
        binding.ignoreWindowType.isChecked = !isChecked
        prefs.isIgnoreWindowType = !isChecked
    }

    private fun initIgnoreWindowTypePrefs() {
        binding.ignoreWindowType.setOnClickListener { changeIgnoreWindowTypePrefs() }
        binding.ignoreWindowType.isChecked = prefs.isIgnoreWindowType
    }

    private fun showRepeatTimeDialog() {
        withActivity {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(R.string.interval)
            val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
            b.seekBar.max = 60
            b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                            progress.toString())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            val repeatTime = prefs.notificationRepeatTime
            b.seekBar.progress = repeatTime
            b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                    repeatTime.toString())
            builder.setView(b.root)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                prefs.notificationRepeatTime = b.seekBar.progress
                showRepeatTime()
                initRepeatTimePrefs()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            Dialogues.setFullWidthDialog(dialog, it)
        }
    }

    private fun initRepeatTimePrefs() {
        binding.repeatIntervalPrefs.setValue(prefs.notificationRepeatTime)
        binding.repeatIntervalPrefs.setOnClickListener { showRepeatTimeDialog() }
        binding.repeatIntervalPrefs.setDependentView(binding.repeatNotificationOptionPrefs)
        showRepeatTime()
    }

    private fun showRepeatTime() {
        binding.repeatIntervalPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.notificationRepeatTime.toString()))
    }

    private fun changeRepeatPrefs() {
        val isChecked = binding.repeatNotificationOptionPrefs.isChecked
        binding.repeatNotificationOptionPrefs.isChecked = !isChecked
        prefs.isNotificationRepeatEnabled = !isChecked
    }

    private fun initRepeatPrefs() {
        binding.repeatNotificationOptionPrefs.setOnClickListener { changeRepeatPrefs() }
        binding.repeatNotificationOptionPrefs.isChecked = prefs.isNotificationRepeatEnabled
    }

    private fun showLedColorDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setTitle(getString(R.string.led_color))
            val colors = LED.getAllNames(it)
            val adapter = ArrayAdapter(it, android.R.layout.simple_list_item_single_choice, colors)
            mItemSelect = prefs.ledColor
            builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.ledColor = mItemSelect
                showLedColor()
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun showLedColor() {
        withContext {
            binding.chooseLedColorPrefs.setDetailText(LED.getTitle(it, prefs.ledColor))
        }
    }

    private fun initLedColorPrefs() {
        binding.chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
        binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs)
        showLedColor()
    }

    private fun changeLedPrefs() {
        val isChecked = binding.ledPrefs.isChecked
        binding.ledPrefs.isChecked = !isChecked
        prefs.isLedEnabled = !isChecked
    }

    private fun initLedPrefs() {
        binding.ledPrefs.setOnClickListener { changeLedPrefs() }
        binding.ledPrefs.isChecked = prefs.isLedEnabled
    }

    private fun initSnoozeTimePrefs() {
        binding.delayForPrefs.setOnClickListener { showSnoozeDialog() }
        binding.delayForPrefs.setValue(prefs.snoozeTime)
        showSnooze()
    }

    private fun showSnooze() {
        binding.delayForPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.snoozeTime.toString()))
    }

    private fun snoozeFormat(progress: Int): String {
        if (!isAdded) return ""
        return String.format(Locale.getDefault(), getString(R.string.x_minutes),
                progress.toString())
    }

    private fun showSnoozeDialog() {
        dialogues.getNullableDialog(context)?.let { builder ->
            builder.setTitle(R.string.snooze_time)
            val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
            b.seekBar.max = 60
            b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    b.titleView.text = snoozeFormat(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            val snoozeTime = prefs.snoozeTime
            b.seekBar.progress = snoozeTime
            b.titleView.text = snoozeFormat(snoozeTime)
            builder.setView(b.root)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                prefs.snoozeTime = b.seekBar.progress
                showSnooze()
                initSnoozeTimePrefs()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            Dialogues.setFullWidthDialog(dialog, activity)
        }
    }

    private fun changeAutoCallPrefs() {
        val isChecked = binding.autoCallPrefs.isChecked
        if (!isChecked) {
            withActivity {
                if (Permissions.ensurePermissions(it, PERM_AUTO_CALL, Permissions.CALL_PHONE)) {
                    binding.autoCallPrefs.isChecked = !isChecked
                    prefs.isAutoCallEnabled = !isChecked
                } else {
                    binding.autoCallPrefs.isChecked = isChecked
                    prefs.isAutoCallEnabled = isChecked
                }
            }
        } else {
            binding.autoCallPrefs.isChecked = !isChecked
            prefs.isAutoCallEnabled = !isChecked
        }
    }

    private fun initAutoCallPrefs() {
        binding.autoCallPrefs.setOnClickListener { changeAutoCallPrefs() }
        binding.autoCallPrefs.isChecked = prefs.isAutoCallEnabled
        binding.autoCallPrefs.isEnabled = prefs.isTelephonyAllowed
    }

    private fun changeAutoLaunchPrefs() {
        val isChecked = binding.autoLaunchPrefs.isChecked
        binding.autoLaunchPrefs.isChecked = !isChecked
        prefs.isAutoLaunchEnabled = !isChecked
    }

    private fun initAutoLaunchPrefs() {
        binding.autoLaunchPrefs.setOnClickListener { changeAutoLaunchPrefs() }
        binding.autoLaunchPrefs.isChecked = prefs.isAutoLaunchEnabled
    }

    private fun changeUnlockPrefs() {
        val isChecked = binding.unlockScreenPrefs.isChecked
        binding.unlockScreenPrefs.isChecked = !isChecked
        prefs.isDeviceUnlockEnabled = !isChecked
    }

    private fun initUnlockPrefs() {
        binding.unlockScreenPrefs.setOnClickListener { changeUnlockPrefs() }
        binding.unlockScreenPrefs.isChecked = prefs.isDeviceUnlockEnabled
    }

    private fun showTtsLocaleDialog() {
        dialogues.getNullableDialog(context)?.let { builder ->
            builder.setTitle(getString(R.string.language))
            val locale = prefs.ttsLocale
            mItemSelect = language.getLocalePosition(locale)
            val names = language.getLocaleNames(context).toTypedArray()
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

    private fun showTtsLocale() {
        val locale = prefs.ttsLocale
        val i = language.getLocalePosition(locale)
        withContext {
            binding.localePrefs.setDetailText(language.getLocaleNames(it)[i])
        }
    }

    private fun saveTtsLocalePrefs() {
        prefs.ttsLocale = language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsLocalePrefs() {
        binding.localePrefs.setOnClickListener { showTtsLocaleDialog() }
        binding.localePrefs.setDependentView(binding.ttsPrefs)
        showTtsLocale()
    }

    private fun changeTtsPrefs() {
        val isChecked = binding.ttsPrefs.isChecked
        binding.ttsPrefs.isChecked = !isChecked
        prefs.isTtsEnabled = !isChecked
    }

    private fun initTtsPrefs() {
        binding.ttsPrefs.setOnClickListener { changeTtsPrefs() }
        binding.ttsPrefs.isChecked = prefs.isTtsEnabled
    }

    private fun changeIncreasePrefs() {
        withContext {
            if (SuperUtil.hasVolumePermission(it)) {
                changeIncrease()
            } else {
                openNotificationsSettings()
            }
        }
    }

    private fun openNotificationsSettings() {
        if (Module.isNougat) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            activity?.startActivityForResult(intent, 1248)
        }
    }

    private fun changeIncrease() {
        val isChecked = binding.increasePrefs.isChecked
        binding.increasePrefs.isChecked = !isChecked
        prefs.isIncreasingLoudnessEnabled = !isChecked
    }

    private fun initIncreasingLoudnessPrefs() {
        binding.increasePrefs.setOnClickListener { changeIncreasePrefs() }
        binding.increasePrefs.isChecked = prefs.isIncreasingLoudnessEnabled
    }

    private fun showLoudnessDialog() {
        if (!SuperUtil.hasVolumePermission(context)) {
            openNotificationsSettings()
            return
        }
        dialogues.getNullableDialog(context)?.let {  builder ->
            builder.setTitle(R.string.loudness)
            val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
            b.seekBar.max = 25
            b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    b.titleView.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
            val loudness = prefs.loudness
            b.seekBar.progress = loudness
            b.titleView.text = loudness.toString()
            builder.setView(b.root)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                prefs.loudness = b.seekBar.progress
                showLoudness()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.show()
            Dialogues.setFullWidthDialog(dialog, activity)
        }
    }

    private fun initLoudnessPrefs() {
        binding.volumePrefs.setOnClickListener { showLoudnessDialog() }
        showLoudness()
    }

    private fun showLoudness() {
        binding.volumePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.loudness) + " %d",
                prefs.loudness))
    }

    private fun showStreamDialog() {
        dialogues.getNullableDialog(context)?.let { builder ->
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.sound_stream))
            val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
            val stream = prefs.soundStream
            mItemSelect = stream - 3
            builder.setSingleChoiceItems(types, mItemSelect) { _, which ->
                if (which != -1) {
                    mItemSelect = which
                }
            }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.soundStream = mItemSelect + 3
                showStream()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun initReminderTypePrefs() {
        binding.typePrefs.setOnClickListener { showReminderTypeDialog() }
        showReminderType()
    }

    private fun showReminderTypeDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(R.string.notification_type)
            val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
            mItemSelect = prefs.reminderType
            builder.setSingleChoiceItems(types, mItemSelect) { _, which ->
                if (which != -1) {
                    mItemSelect = which
                }
            }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                prefs.reminderType = mItemSelect
                showReminderType()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun showReminderType() {
        val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
        binding.typePrefs.setDetailText(types[prefs.reminderType])
    }

    private fun initSoundStreamPrefs() {
        binding.streamPrefs.setOnClickListener { showStreamDialog() }
        binding.streamPrefs.setDependentView(binding.systemPrefs)
        showStream()
    }

    private fun showStream() {
        val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
        binding.streamPrefs.setDetailText(types[prefs.soundStream - 3])
    }

    private fun changeSystemLoudnessPrefs() {
        if (SuperUtil.hasVolumePermission(context)) {
            val isChecked = binding.systemPrefs.isChecked
            binding.systemPrefs.isChecked = !isChecked
            prefs.isSystemLoudnessEnabled = !isChecked
        } else {
            openNotificationsSettings()
        }
    }

    private fun initSystemLoudnessPrefs() {
        binding.systemPrefs.setOnClickListener { changeSystemLoudnessPrefs() }
        binding.systemPrefs.isChecked = prefs.isSystemLoudnessEnabled
    }

    private fun initMelodyPrefs() {
        binding.chooseSoundPrefs.setOnClickListener { showSoundDialog() }
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs.melodyFile
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
            val adapter = ArrayAdapter(it, android.R.layout.simple_list_item_single_choice, types)
            mItemSelect = if (prefs.melodyFile == "" || prefs.melodyFile.matches(Constants.DEFAULT.toRegex())) {
                0
            } else {
                1
            }
            builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                if (mItemSelect == 0) {
                    prefs.melodyFile = Constants.DEFAULT
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

    private fun changeInfiniteSoundPrefs() {
        val isChecked = binding.infiniteSoundOptionPrefs.isChecked
        binding.infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs.isInfiniteSoundEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        binding.infiniteSoundOptionPrefs.setOnClickListener { changeInfiniteSoundPrefs() }
        binding.infiniteSoundOptionPrefs.isChecked = prefs.isInfiniteSoundEnabled
    }

    private fun changeSoundPrefs() {
        withActivity {
            val isChecked = binding.soundOptionPrefs.isChecked
            binding.soundOptionPrefs.isChecked = !isChecked
            prefs.isSoundInSilentModeEnabled = !isChecked
            if (!SuperUtil.checkNotificationPermission(it)) {
                SuperUtil.askNotificationPermission(it, dialogues)
            } else {
                Permissions.ensurePermissions(it, PERM_BT, Permissions.BLUETOOTH)
            }
        }
    }

    private fun initSoundInSilentModePrefs() {
        binding.soundOptionPrefs.setOnClickListener { changeSoundPrefs() }
        binding.soundOptionPrefs.isChecked = prefs.isSoundInSilentModeEnabled
    }

    private fun changeInfiniteVibratePrefs() {
        val isChecked = binding.infiniteVibrateOptionPrefs.isChecked
        binding.infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs.isInfiniteVibrateEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        binding.infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibratePrefs() }
        binding.infiniteVibrateOptionPrefs.isChecked = prefs.isInfiniteVibrateEnabled
        binding.infiniteVibrateOptionPrefs.setDependentView(binding.vibrationOptionPrefs)
    }

    private fun changeVibratePrefs() {
        val isChecked = binding.vibrationOptionPrefs.isChecked
        binding.vibrationOptionPrefs.isChecked = !isChecked
        prefs.isVibrateEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        binding.vibrationOptionPrefs.setOnClickListener { changeVibratePrefs() }
        binding.vibrationOptionPrefs.isChecked = prefs.isVibrateEnabled
    }

    private fun changeSbIconPrefs() {
        val isChecked = binding.statusIconPrefs.isChecked
        binding.statusIconPrefs.isChecked = !isChecked
        prefs.isSbIconEnabled = !isChecked
        notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
    }

    private fun initSbIconPrefs() {
        binding.statusIconPrefs.setOnClickListener { changeSbIconPrefs() }
        binding.statusIconPrefs.isChecked = prefs.isSbIconEnabled
        binding.statusIconPrefs.setDependentView(binding.permanentNotificationPrefs)
    }

    private fun changeSbPrefs() {
        val isChecked = binding.permanentNotificationPrefs.isChecked
        binding.permanentNotificationPrefs.isChecked = !isChecked
        prefs.isSbNotificationEnabled = !isChecked
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        } else {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_HIDE)
        }
    }

    private fun initSbPrefs() {
        binding.permanentNotificationPrefs.setOnClickListener { changeSbPrefs() }
        binding.permanentNotificationPrefs.isChecked = prefs.isSbNotificationEnabled
    }

    private fun changeManualPrefs() {
        val isChecked = binding.notificationDismissPrefs.isChecked
        binding.notificationDismissPrefs.isChecked = !isChecked
        prefs.isManualRemoveEnabled = !isChecked
    }

    private fun initManualPrefs() {
        binding.notificationDismissPrefs.setOnClickListener { changeManualPrefs() }
        binding.notificationDismissPrefs.isChecked = prefs.isManualRemoveEnabled
    }

    private fun initImagePrefs() {
        binding.bgImagePrefs.setOnClickListener { showImageDialog() }
        showImage()
    }

    private fun showImageDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(R.string.background)
            val types = arrayOf(getString(R.string.none), getString(R.string.default_string), getString(R.string.choose_file))
            val adapter = ArrayAdapter(it, android.R.layout.simple_list_item_single_choice, types)
            mItemSelect = when (prefs.screenImage) {
                Constants.NONE -> 0
                Constants.DEFAULT -> 1
                else -> 2
            }
            builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
                if (which != -1) {
                    mItemSelect = which
                }
            }
            builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                when (mItemSelect) {
                    0 -> prefs.screenImage = Constants.NONE
                    1 -> prefs.screenImage = Constants.DEFAULT
                    2 -> openImagePicker()
                }
                showImage()
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun showImage() {
        val title = when (prefs.screenImage) {
            Constants.NONE -> getString(R.string.none)
            Constants.DEFAULT -> getString(R.string.default_string)
            else -> {
                val file = File(prefs.screenImage)
                if (file.exists()) {
                    file.name
                } else {
                    getString(R.string.default_string)
                }
            }
        }
        binding.bgImagePrefs.setDetailText(title)
    }

    override fun getTitle(): String = getString(R.string.notification)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data?.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs.melodyFile = file.toString()
                    }
                }
                showMelody()
            }
            Constants.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data?.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs.screenImage = filePath
                    } else {
                        prefs.screenImage = Constants.DEFAULT
                    }
                    showImage()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                PERM_AUTO_CALL -> changeAutoCallPrefs()
                PERM_IMAGE -> openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        withActivity {
            if (Permissions.ensurePermissions(it, PERM_IMAGE, Permissions.READ_EXTERNAL)) {
                startActivityForResult(Intent(it, FileExplorerActivity::class.java).putExtra(Constants.FILE_TYPE, FileExplorerActivity.TYPE_PHOTO),
                        Constants.ACTION_REQUEST_GALLERY)
            }
        }
    }

    private fun unlockList(): Array<String> {
        return arrayOf(
                getString(R.string.all),
                getString(R.string.priority_low) + " " + getString(R.string.and_above),
                getString(R.string.priority_normal) + " " + getString(R.string.and_above),
                getString(R.string.priority_high) + " " + getString(R.string.and_above),
                getString(R.string.priority_highest)
        )
    }

    companion object {

        private const val MELODY_CODE = 125
        private const val PERM_BT = 1425
        private const val PERM_AUTO_CALL = 1427
        private const val PERM_IMAGE = 1428
    }
}