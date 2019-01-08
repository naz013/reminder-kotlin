package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_notification.*
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
class NotificationSettingsFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0
    private val localeAdapter: ArrayAdapter<String>
        get() = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, language.getLocaleNames(context!!))

    override fun layoutRes(): Int = R.layout.fragment_settings_notification

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
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
        initWakePrefs()
        initUnlockPrefs()
        initAutoSmsPrefs()
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
        if (!Permissions.checkPermission(context!!, Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(activity!!, PERM_SD, Permissions.READ_EXTERNAL)
        }
    }

    private fun initSmartFold() {
        smartFoldPrefs.isChecked = prefs.isFoldingEnabled
        smartFoldPrefs.setOnClickListener { changeSmartFoldMode() }
    }

    private fun initWearNotification() {
        wearPrefs.isChecked = prefs.isWearEnabled
        wearPrefs.setOnClickListener { changeWearNotification() }
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

    private fun changeIgnoreWindowTypePrefs() {
        val isChecked = ignore_window_type.isChecked
        ignore_window_type.isChecked = !isChecked
        prefs.isIgnoreWindowType = !isChecked
    }

    private fun initIgnoreWindowTypePrefs() {
        ignore_window_type.setOnClickListener { changeIgnoreWindowTypePrefs() }
        ignore_window_type.isChecked = prefs.isIgnoreWindowType
    }

    private fun showRepeatTimeDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.interval)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.notificationRepeatTime = b.seekBar.progress
            showRepeatTime()
            initRepeatTimePrefs()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun initRepeatTimePrefs() {
        repeatIntervalPrefs.setValue(prefs.notificationRepeatTime)
        repeatIntervalPrefs.setOnClickListener { showRepeatTimeDialog() }
        repeatIntervalPrefs.setDependentView(repeatNotificationOptionPrefs)
        showRepeatTime()
    }

    private fun showRepeatTime() {
        repeatIntervalPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.notificationRepeatTime.toString()))
    }

    private fun changeRepeatPrefs() {
        val isChecked = repeatNotificationOptionPrefs.isChecked
        repeatNotificationOptionPrefs.isChecked = !isChecked
        prefs.isNotificationRepeatEnabled = !isChecked
    }

    private fun initRepeatPrefs() {
        repeatNotificationOptionPrefs.setOnClickListener { changeRepeatPrefs() }
        repeatNotificationOptionPrefs.isChecked = prefs.isNotificationRepeatEnabled
    }

    private fun showLedColorDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.led_color))
        val colors = LED.getAllNames(context!!)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        mItemSelect = prefs.ledColor
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.ledColor = mItemSelect
            showLedColor()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun showLedColor() {
        chooseLedColorPrefs.setDetailText(LED.getTitle(context!!, prefs.ledColor))
    }

    private fun initLedColorPrefs() {
        chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
        chooseLedColorPrefs.setDependentView(ledPrefs)
        showLedColor()
    }

    private fun changeLedPrefs() {
        val isChecked = ledPrefs.isChecked
        ledPrefs.isChecked = !isChecked
        prefs.isLedEnabled = !isChecked
    }

    private fun initLedPrefs() {
        ledPrefs.setOnClickListener { changeLedPrefs() }
        ledPrefs.isChecked = prefs.isLedEnabled
    }

    private fun initSnoozeTimePrefs() {
        delayForPrefs.setOnClickListener { showSnoozeDialog() }
        delayForPrefs.setValue(prefs.snoozeTime)
        showSnooze()
    }

    private fun showSnooze() {
        delayForPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.snoozeTime.toString()))
    }

    private fun showSnoozeDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.snooze_time)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        val snoozeTime = prefs.snoozeTime
        b.seekBar.progress = snoozeTime
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                snoozeTime.toString())
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.snoozeTime = b.seekBar.progress
            showSnooze()
            initSnoozeTimePrefs()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun changeAutoCallPrefs() {
        val isChecked = autoCallPrefs.isChecked
        autoCallPrefs.isChecked = !isChecked
        prefs.isAutoCallEnabled = !isChecked
    }

    private fun initAutoCallPrefs() {
        autoCallPrefs.setOnClickListener { changeAutoCallPrefs() }
        autoCallPrefs.isChecked = prefs.isAutoCallEnabled
        autoCallPrefs.isEnabled = prefs.isTelephonyAllowed
    }

    private fun changeAutoLaunchPrefs() {
        val isChecked = autoLaunchPrefs.isChecked
        autoLaunchPrefs.isChecked = !isChecked
        prefs.isAutoLaunchEnabled = !isChecked
    }

    private fun initAutoLaunchPrefs() {
        autoLaunchPrefs.setOnClickListener { changeAutoLaunchPrefs() }
        autoLaunchPrefs.isChecked = prefs.isAutoLaunchEnabled
    }

    private fun changeAutoSmsPrefs() {
        val isChecked = silentSMSOptionPrefs.isChecked
        silentSMSOptionPrefs.isChecked = !isChecked
        prefs.isAutoSmsEnabled = !isChecked
    }

    private fun initAutoSmsPrefs() {
        silentSMSOptionPrefs.setOnClickListener { changeAutoSmsPrefs() }
        silentSMSOptionPrefs.isChecked = prefs.isAutoSmsEnabled
        silentSMSOptionPrefs.isEnabled = prefs.isTelephonyAllowed
    }

    private fun changeUnlockPrefs() {
        val isChecked = unlockScreenPrefs.isChecked
        unlockScreenPrefs.isChecked = !isChecked
        prefs.isDeviceUnlockEnabled = !isChecked
    }

    private fun initUnlockPrefs() {
        unlockScreenPrefs.setOnClickListener { changeUnlockPrefs() }
        unlockScreenPrefs.isChecked = prefs.isDeviceUnlockEnabled
    }

    private fun changeWakePrefs() {
        val isChecked = wakeScreenOptionPrefs.isChecked
        wakeScreenOptionPrefs.isChecked = !isChecked
        prefs.isDeviceAwakeEnabled = !isChecked
    }

    private fun initWakePrefs() {
        wakeScreenOptionPrefs.setOnClickListener { changeWakePrefs() }
        wakeScreenOptionPrefs.isChecked = prefs.isDeviceAwakeEnabled
    }

    private fun showTtsLocaleDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locale = prefs.ttsLocale
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

    private fun showTtsLocale() {
        val locale = prefs.ttsLocale
        val i = language.getLocalePosition(locale)
        localePrefs.setDetailText(language.getLocaleNames(context!!)[i])
    }

    private fun saveTtsLocalePrefs() {
        prefs.ttsLocale = language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsLocalePrefs() {
        localePrefs.setOnClickListener { showTtsLocaleDialog() }
        localePrefs.setDependentView(ttsPrefs)
        showTtsLocale()
    }

    private fun changeTtsPrefs() {
        val isChecked = ttsPrefs.isChecked
        ttsPrefs.isChecked = !isChecked
        prefs.isTtsEnabled = !isChecked
    }

    private fun initTtsPrefs() {
        ttsPrefs.setOnClickListener { changeTtsPrefs() }
        ttsPrefs.isChecked = prefs.isTtsEnabled
    }

    private fun changeIncreasePrefs() {
        if (SuperUtil.hasVolumePermission(context!!)) {
            changeIncrease()
        } else {
            openNotificationsSettings()
        }

    }

    private fun openNotificationsSettings() {
        if (Module.isNougat) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            activity!!.startActivityForResult(intent, 1248)
        }
    }

    private fun changeIncrease() {
        val isChecked = increasePrefs.isChecked
        increasePrefs.isChecked = !isChecked
        prefs.isIncreasingLoudnessEnabled = !isChecked
    }

    private fun initIncreasingLoudnessPrefs() {
        increasePrefs.setOnClickListener { changeIncreasePrefs() }
        increasePrefs.isChecked = prefs.isIncreasingLoudnessEnabled
    }

    private fun showLoudnessDialog() {
        if (!SuperUtil.hasVolumePermission(context!!)) {
            openNotificationsSettings()
            return
        }
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.loudness)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.loudness = b.seekBar.progress
            showLoudness()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun initLoudnessPrefs() {
        volumePrefs.setOnClickListener { showLoudnessDialog() }
        showLoudness()
    }

    private fun showLoudness() {
        volumePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.loudness) + " %d",
                prefs.loudness))
    }

    private fun showStreamDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.sound_stream))
        val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        val stream = prefs.soundStream
        mItemSelect = stream - 3
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            if (which != -1) {
                mItemSelect = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.soundStream = mItemSelect + 3
            showStream()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initReminderTypePrefs() {
        typePrefs.setOnClickListener { showReminderTypeDialog() }
        showReminderType()
    }

    private fun showReminderTypeDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(R.string.notification_type)
        val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        mItemSelect = prefs.reminderType
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            if (which != -1) {
                mItemSelect = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.reminderType = mItemSelect
            showReminderType()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun showReminderType() {
        val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
        typePrefs.setDetailText(types[prefs.reminderType])
    }

    private fun initSoundStreamPrefs() {
        streamPrefs.setOnClickListener { showStreamDialog() }
        streamPrefs.setDependentView(systemPrefs)
        showStream()
    }

    private fun showStream() {
        val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
        streamPrefs.setDetailText(types[prefs.soundStream - 3])
    }

    private fun changeSystemLoudnessPrefs() {
        if (SuperUtil.hasVolumePermission(context!!)) {
            val isChecked = systemPrefs.isChecked
            systemPrefs.isChecked = !isChecked
            prefs.isSystemLoudnessEnabled = !isChecked
        } else {
            openNotificationsSettings()
        }
    }

    private fun initSystemLoudnessPrefs() {
        systemPrefs.setOnClickListener { changeSystemLoudnessPrefs() }
        systemPrefs.isChecked = prefs.isSystemLoudnessEnabled
    }

    private fun initMelodyPrefs() {
        chooseSoundPrefs.setOnClickListener { showSoundDialog() }
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs.melodyFile
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
                startActivityForResult(Intent(context, FileExplorerActivity::class.java), MELODY_CODE)
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun changeInfiniteSoundPrefs() {
        val isChecked = infiniteSoundOptionPrefs.isChecked
        infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs.isInfiniteSoundEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        infiniteSoundOptionPrefs.setOnClickListener { changeInfiniteSoundPrefs() }
        infiniteSoundOptionPrefs.isChecked = prefs.isInfiniteSoundEnabled
    }

    private fun changeSoundPrefs() {
        val isChecked = soundOptionPrefs.isChecked
        soundOptionPrefs.isChecked = !isChecked
        prefs.isSoundInSilentModeEnabled = !isChecked
        if (!SuperUtil.checkNotificationPermission(activity!!)) {
            SuperUtil.askNotificationPermission(activity!!, dialogues)
        } else if (!Permissions.checkPermission(context!!, Permissions.BLUETOOTH)) {
            Permissions.requestPermission(activity!!, PERM_BT, Permissions.BLUETOOTH)
        }
    }

    private fun initSoundInSilentModePrefs() {
        soundOptionPrefs.setOnClickListener { changeSoundPrefs() }
        soundOptionPrefs.isChecked = prefs.isSoundInSilentModeEnabled
    }

    private fun changeInfiniteVibratePrefs() {
        val isChecked = infiniteVibrateOptionPrefs.isChecked
        infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs.isInfiniteVibrateEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibratePrefs() }
        infiniteVibrateOptionPrefs.isChecked = prefs.isInfiniteVibrateEnabled
        infiniteVibrateOptionPrefs.setDependentView(vibrationOptionPrefs)
    }

    private fun changeVibratePrefs() {
        val isChecked = vibrationOptionPrefs.isChecked
        vibrationOptionPrefs.isChecked = !isChecked
        prefs.isVibrateEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        vibrationOptionPrefs.setOnClickListener { changeVibratePrefs() }
        vibrationOptionPrefs.isChecked = prefs.isVibrateEnabled
    }

    private fun changeSbIconPrefs() {
        val isChecked = statusIconPrefs.isChecked
        statusIconPrefs.isChecked = !isChecked
        prefs.isSbIconEnabled = !isChecked
        notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
    }

    private fun initSbIconPrefs() {
        statusIconPrefs.setOnClickListener { changeSbIconPrefs() }
        statusIconPrefs.isChecked = prefs.isSbIconEnabled
        statusIconPrefs.setDependentView(permanentNotificationPrefs)
    }

    private fun changeSbPrefs() {
        val isChecked = permanentNotificationPrefs.isChecked
        permanentNotificationPrefs.isChecked = !isChecked
        prefs.isSbNotificationEnabled = !isChecked
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        } else {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_HIDE)
        }
    }

    private fun initSbPrefs() {
        permanentNotificationPrefs.setOnClickListener { changeSbPrefs() }
        permanentNotificationPrefs.isChecked = prefs.isSbNotificationEnabled
    }

    private fun changeManualPrefs() {
        val isChecked = notificationDismissPrefs.isChecked
        notificationDismissPrefs.isChecked = !isChecked
        prefs.isManualRemoveEnabled = !isChecked
    }

    private fun initManualPrefs() {
        notificationDismissPrefs.setOnClickListener { changeManualPrefs() }
        notificationDismissPrefs.isChecked = prefs.isManualRemoveEnabled
    }

    override fun getTitle(): String = getString(R.string.notification)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data!!.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs.melodyFile = file.toString()
                    }
                }
                showMelody()
            }
            Constants.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data!!.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        val uri = UriUtil.getUri(context!!, file)
                        prefs.reminderImage = uri.toString()
                    }
                }
            }
        }
    }

    companion object {

        private const val MELODY_CODE = 125
        private const val PERM_BT = 1425
        private const val PERM_SD = 1426
    }
}