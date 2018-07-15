package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.fileExplorer.FileExplorerActivity
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding

import java.io.File
import java.util.Locale

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

    private var binding: FragmentSettingsNotificationBinding? = null
    private var mItemSelect: Int = 0

    private val mImageClick = { view -> showImageDialog() }
    private val mBlurClick = { view -> changeBlurPrefs() }
    private val mManualClick = { view -> changeManualPrefs() }
    private val mSbClick = { view -> changeSbPrefs() }
    private val mSbIconClick = { view -> changeSbIconPrefs() }
    private val mVibrateClick = { view -> changeVibratePrefs() }
    private val mInfiniteVibrateClick = { view -> changeInfiniteVibratePrefs() }
    private val mSoundClick = { view -> changeSoundPrefs() }
    private val mInfiniteSoundClick = { view -> changeInfiniteSoundPrefs() }
    private val mMelodyClick = { view -> showSoundDialog() }
    private val mSystemLoudnessClick = { view -> changeSystemLoudnessPrefs() }
    private val mStreamClick = { view -> showStreamDialog() }
    private val mLoudnessClick = { view -> showLoudnessDialog() }
    private val mIncreaseClick = { view -> changeIncreasePrefs() }
    private val mTtsClick = { view -> changeTtsPrefs() }
    private val mTtsLocaleClick = { view -> showTtsLocaleDialog() }
    private val mWakeClick = { view -> changeWakePrefs() }
    private val mUnlockClick = { view -> changeUnlockPrefs() }
    private val mAutoSmsClick = { view -> changeAutoSmsPrefs() }
    private val mAutoLaunchClick = { view -> changeAutoLaunchPrefs() }
    private val mSnoozeClick = { view -> showSnoozeDialog() }
    private val mRepeatClick = { view -> changeRepeatPrefs() }
    private val mLedColorClick = { view -> showLedColorDialog() }
    private val mLedClick = { view -> changeLedPrefs() }
    private val mRepeatTimeClick = { view -> showRepeatTimeDialog() }
    private val mAutoCallClick = { view -> changeAutoCallPrefs() }

    private val localeAdapter: ArrayAdapter<String>
        get() = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, Language.getLocaleNames(context!!))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsNotificationBinding.inflate(inflater, container, false)
        binding!!.imagePrefs.setOnClickListener(mImageClick)
        initBlurPrefs()
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
        if (!Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(activity, PERM_SD, Permissions.READ_EXTERNAL)
        }
        return binding!!.root
    }

    private fun changeIgnoreWindowTypePrefs() {
        val isChecked = binding!!.ignoreWindowType.isChecked
        binding!!.ignoreWindowType.isChecked = !isChecked
        prefs!!.isIgnoreWindowType = !isChecked
    }

    private fun initIgnoreWindowTypePrefs() {
        binding!!.ignoreWindowType.setOnClickListener { v -> changeIgnoreWindowTypePrefs() }
        binding!!.ignoreWindowType.isChecked = prefs!!.isIgnoreWindowType
    }

    private fun showRepeatTimeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.interval)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val repeatTime = prefs!!.notificationRepeatTime
        b.seekBar.progress = repeatTime
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                repeatTime.toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.notificationRepeatTime = b.seekBar.progress
            showRepeatTime()
            initRepeatTimePrefs()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun initRepeatTimePrefs() {
        binding!!.repeatIntervalPrefs.setValue(prefs!!.notificationRepeatTime)
        binding!!.repeatIntervalPrefs.setOnClickListener(mRepeatTimeClick)
        binding!!.repeatIntervalPrefs.setDependentView(binding!!.repeatNotificationOptionPrefs)
        showRepeatTime()
    }

    private fun showRepeatTime() {
        binding!!.repeatIntervalPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs!!.notificationRepeatTime.toString()))
    }

    private fun changeRepeatPrefs() {
        val isChecked = binding!!.repeatNotificationOptionPrefs.isChecked
        binding!!.repeatNotificationOptionPrefs.isChecked = !isChecked
        prefs!!.isNotificationRepeatEnabled = !isChecked
    }

    private fun initRepeatPrefs() {
        binding!!.repeatNotificationOptionPrefs.setOnClickListener(mRepeatClick)
        binding!!.repeatNotificationOptionPrefs.isChecked = prefs!!.isNotificationRepeatEnabled
    }

    private fun showLedColorDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.led_color))
        val colors = LED.getAllNames(context)
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, colors)
        mItemSelect = prefs!!.ledColor
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.ledColor = mItemSelect
            showLedColor()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun showLedColor() {
        binding!!.chooseLedColorPrefs.setDetailText(LED.getTitle(context, prefs!!.ledColor))
    }

    private fun initLedColorPrefs() {
        binding!!.chooseLedColorPrefs.setOnClickListener(mLedColorClick)
        binding!!.chooseLedColorPrefs.setDependentView(binding!!.ledPrefs)
        showLedColor()
    }

    private fun changeLedPrefs() {
        val isChecked = binding!!.ledPrefs.isChecked
        binding!!.ledPrefs.isChecked = !isChecked
        prefs!!.isLedEnabled = !isChecked
    }

    private fun initLedPrefs() {
        binding!!.ledPrefs.setOnClickListener(mLedClick)
        binding!!.ledPrefs.isChecked = prefs!!.isLedEnabled
    }

    private fun initSnoozeTimePrefs() {
        binding!!.delayForPrefs.setOnClickListener(mSnoozeClick)
        binding!!.delayForPrefs.setValue(prefs!!.snoozeTime)
        showSnooze()
    }

    private fun showSnooze() {
        binding!!.delayForPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs!!.snoozeTime.toString()))
    }

    private fun showSnoozeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.snooze_time)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val snoozeTime = prefs!!.snoozeTime
        b.seekBar.progress = snoozeTime
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                snoozeTime.toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.snoozeTime = b.seekBar.progress
            showSnooze()
            initSnoozeTimePrefs()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun changeAutoCallPrefs() {
        val isChecked = binding!!.autoCallPrefs.isChecked
        binding!!.autoCallPrefs.isChecked = !isChecked
        prefs!!.isAutoCallEnabled = !isChecked
    }

    private fun initAutoCallPrefs() {
        binding!!.autoCallPrefs.setOnClickListener(mAutoCallClick)
        binding!!.autoCallPrefs.isChecked = prefs!!.isAutoCallEnabled
    }

    private fun changeAutoLaunchPrefs() {
        val isChecked = binding!!.autoLaunchPrefs.isChecked
        binding!!.autoLaunchPrefs.isChecked = !isChecked
        prefs!!.isAutoLaunchEnabled = !isChecked
    }

    private fun initAutoLaunchPrefs() {
        binding!!.autoLaunchPrefs.setOnClickListener(mAutoLaunchClick)
        binding!!.autoLaunchPrefs.isChecked = prefs!!.isAutoLaunchEnabled
    }

    private fun changeAutoSmsPrefs() {
        val isChecked = binding!!.silentSMSOptionPrefs.isChecked
        binding!!.silentSMSOptionPrefs.isChecked = !isChecked
        prefs!!.isAutoSmsEnabled = !isChecked
    }

    private fun initAutoSmsPrefs() {
        binding!!.silentSMSOptionPrefs.setOnClickListener(mAutoSmsClick)
        binding!!.silentSMSOptionPrefs.isChecked = prefs!!.isAutoSmsEnabled
    }

    private fun changeUnlockPrefs() {
        val isChecked = binding!!.unlockScreenPrefs.isChecked
        binding!!.unlockScreenPrefs.isChecked = !isChecked
        prefs!!.isDeviceUnlockEnabled = !isChecked
    }

    private fun initUnlockPrefs() {
        binding!!.unlockScreenPrefs.setOnClickListener(mUnlockClick)
        binding!!.unlockScreenPrefs.isChecked = prefs!!.isDeviceUnlockEnabled
    }

    private fun changeWakePrefs() {
        val isChecked = binding!!.wakeScreenOptionPrefs.isChecked
        binding!!.wakeScreenOptionPrefs.isChecked = !isChecked
        prefs!!.isDeviceAwakeEnabled = !isChecked
    }

    private fun initWakePrefs() {
        binding!!.wakeScreenOptionPrefs.setOnClickListener(mWakeClick)
        binding!!.wakeScreenOptionPrefs.isChecked = prefs!!.isDeviceAwakeEnabled
    }

    private fun showTtsLocaleDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locale = prefs!!.ttsLocale
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

    private fun showTtsLocale() {
        val locale = prefs!!.ttsLocale
        val i = Language.getLocalePosition(locale)
        binding!!.localePrefs.setDetailText(Language.getLocaleNames(context!!)[i])
    }

    private fun saveTtsLocalePrefs() {
        prefs!!.ttsLocale = Language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsLocalePrefs() {
        binding!!.localePrefs.setOnClickListener(mTtsLocaleClick)
        binding!!.localePrefs.setDependentView(binding!!.ttsPrefs)
        showTtsLocale()
    }

    private fun changeTtsPrefs() {
        val isChecked = binding!!.ttsPrefs.isChecked
        binding!!.ttsPrefs.isChecked = !isChecked
        prefs!!.isTtsEnabled = !isChecked
    }

    private fun initTtsPrefs() {
        binding!!.ttsPrefs.setOnClickListener(mTtsClick)
        binding!!.ttsPrefs.isChecked = prefs!!.isTtsEnabled
    }

    private fun changeIncreasePrefs() {
        if (SuperUtil.hasVolumePermission(context)) {
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
        val isChecked = binding!!.increasePrefs.isChecked
        binding!!.increasePrefs.isChecked = !isChecked
        prefs!!.isIncreasingLoudnessEnabled = !isChecked
    }

    private fun initIncreasingLoudnessPrefs() {
        binding!!.increasePrefs.setOnClickListener(mIncreaseClick)
        binding!!.increasePrefs.isChecked = prefs!!.isIncreasingLoudnessEnabled
    }

    private fun showLoudnessDialog() {
        if (!SuperUtil.hasVolumePermission(context)) {
            openNotificationsSettings()
            return
        }
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.loudness)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val loudness = prefs!!.loudness
        b.seekBar.progress = loudness
        b.titleView.text = loudness.toString()
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.loudness = b.seekBar.progress
            showLoudness()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun initLoudnessPrefs() {
        binding!!.volumePrefs.setOnClickListener(mLoudnessClick)
        showLoudness()
    }

    private fun showLoudness() {
        binding!!.volumePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.loudness) + " %d",
                prefs!!.loudness))
    }

    private fun showStreamDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.sound_stream))
        val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        val stream = prefs!!.soundStream
        mItemSelect = stream - 3
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which ->
            if (which != -1) {
                mItemSelect = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.soundStream = mItemSelect + 3
            showStream()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun initReminderTypePrefs() {
        binding!!.typePrefs.setOnClickListener { v -> showReminderTypeDialog() }
        showReminderType()
    }

    private fun showReminderTypeDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(R.string.notification_type)
        val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        mItemSelect = prefs!!.reminderType
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which ->
            if (which != -1) {
                mItemSelect = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            prefs!!.reminderType = mItemSelect
            showReminderType()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun showReminderType() {
        val types = arrayOf(getString(R.string.full_screen), getString(R.string.simple))
        binding!!.typePrefs.setDetailText(types[prefs!!.reminderType])
    }

    private fun initSoundStreamPrefs() {
        binding!!.streamPrefs.setOnClickListener(mStreamClick)
        binding!!.streamPrefs.setDependentView(binding!!.systemPrefs)
        showStream()
    }

    private fun showStream() {
        val types = arrayOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
        binding!!.streamPrefs.setDetailText(types[prefs!!.soundStream - 3])
    }

    private fun changeSystemLoudnessPrefs() {
        if (SuperUtil.hasVolumePermission(context)) {
            val isChecked = binding!!.systemPrefs.isChecked
            binding!!.systemPrefs.isChecked = !isChecked
            prefs!!.isSystemLoudnessEnabled = !isChecked
        } else
            openNotificationsSettings()
    }

    private fun initSystemLoudnessPrefs() {
        binding!!.systemPrefs.setOnClickListener(mSystemLoudnessClick)
        binding!!.systemPrefs.isChecked = prefs!!.isSystemLoudnessEnabled
    }

    private fun initMelodyPrefs() {
        binding!!.chooseSoundPrefs.setOnClickListener(mMelodyClick)
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs!!.melodyFile
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
        if (prefs!!.melodyFile == null || prefs!!.melodyFile!!.matches(Constants.DEFAULT.toRegex())) {
            mItemSelect = 0
        } else {
            mItemSelect = 1
        }
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            if (mItemSelect == 0) {
                prefs!!.melodyFile = Constants.DEFAULT
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

    private fun changeInfiniteSoundPrefs() {
        val isChecked = binding!!.infiniteSoundOptionPrefs.isChecked
        binding!!.infiniteSoundOptionPrefs.isChecked = !isChecked
        prefs!!.isInfiniteSoundEnabled = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        binding!!.infiniteSoundOptionPrefs.setOnClickListener(mInfiniteSoundClick)
        binding!!.infiniteSoundOptionPrefs.isChecked = prefs!!.isInfiniteSoundEnabled
    }

    private fun changeSoundPrefs() {
        val isChecked = binding!!.soundOptionPrefs.isChecked
        binding!!.soundOptionPrefs.isChecked = !isChecked
        prefs!!.isSoundInSilentModeEnabled = !isChecked
        if (!SuperUtil.checkNotificationPermission(activity!!)) {
            SuperUtil.askNotificationPermission(activity)
        } else if (!Permissions.checkPermission(context, Permissions.BLUETOOTH)) {
            Permissions.requestPermission(activity, PERM_BT, Permissions.BLUETOOTH)
        }
    }

    private fun initSoundInSilentModePrefs() {
        binding!!.soundOptionPrefs.setOnClickListener(mSoundClick)
        binding!!.soundOptionPrefs.isChecked = prefs!!.isSoundInSilentModeEnabled
    }

    private fun changeInfiniteVibratePrefs() {
        val isChecked = binding!!.infiniteVibrateOptionPrefs.isChecked
        binding!!.infiniteVibrateOptionPrefs.isChecked = !isChecked
        prefs!!.isInfiniteVibrateEnabled = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        binding!!.infiniteVibrateOptionPrefs.setOnClickListener(mInfiniteVibrateClick)
        binding!!.infiniteVibrateOptionPrefs.isChecked = prefs!!.isInfiniteVibrateEnabled
        binding!!.infiniteVibrateOptionPrefs.setDependentView(binding!!.vibrationOptionPrefs)
    }

    private fun changeVibratePrefs() {
        val isChecked = binding!!.vibrationOptionPrefs.isChecked
        binding!!.vibrationOptionPrefs.isChecked = !isChecked
        prefs!!.isVibrateEnabled = !isChecked
    }

    private fun initVibratePrefs() {
        binding!!.vibrationOptionPrefs.setOnClickListener(mVibrateClick)
        binding!!.vibrationOptionPrefs.isChecked = prefs!!.isVibrateEnabled
    }

    private fun changeSbIconPrefs() {
        val isChecked = binding!!.statusIconPrefs.isChecked
        binding!!.statusIconPrefs.isChecked = !isChecked
        prefs!!.isSbIconEnabled = !isChecked
        Notifier.updateReminderPermanent(context!!, PermanentReminderReceiver.ACTION_SHOW)
    }

    private fun initSbIconPrefs() {
        binding!!.statusIconPrefs.setOnClickListener(mSbIconClick)
        binding!!.statusIconPrefs.isChecked = prefs!!.isSbIconEnabled
        binding!!.statusIconPrefs.setDependentView(binding!!.permanentNotificationPrefs)
    }

    private fun changeSbPrefs() {
        val isChecked = binding!!.permanentNotificationPrefs.isChecked
        binding!!.permanentNotificationPrefs.isChecked = !isChecked
        prefs!!.isSbNotificationEnabled = !isChecked
        if (prefs!!.isSbNotificationEnabled) {
            Notifier.updateReminderPermanent(context!!, PermanentReminderReceiver.ACTION_SHOW)
        } else {
            Notifier.updateReminderPermanent(context!!, PermanentReminderReceiver.ACTION_HIDE)
        }
    }

    private fun initSbPrefs() {
        binding!!.permanentNotificationPrefs.setOnClickListener(mSbClick)
        binding!!.permanentNotificationPrefs.isChecked = prefs!!.isSbNotificationEnabled
    }

    private fun changeManualPrefs() {
        val isChecked = binding!!.notificationDismissPrefs.isChecked
        binding!!.notificationDismissPrefs.isChecked = !isChecked
        prefs!!.isManualRemoveEnabled = !isChecked
    }

    private fun initManualPrefs() {
        binding!!.notificationDismissPrefs.setOnClickListener(mManualClick)
        binding!!.notificationDismissPrefs.isChecked = prefs!!.isManualRemoveEnabled
    }

    private fun initBlurPrefs() {
        binding!!.blurPrefs.setOnClickListener(mBlurClick)
        binding!!.blurPrefs.isChecked = prefs!!.isBlurEnabled
    }

    private fun changeBlurPrefs() {
        val isChecked = binding!!.blurPrefs.isChecked
        binding!!.blurPrefs.isChecked = !isChecked
        prefs!!.isBlurEnabled = !isChecked
    }

    private fun showImageDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.background))
        val types = arrayOf(getString(R.string.none), getString(R.string.default_string), getString(R.string.choose_file))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, types)
        val image = prefs!!.reminderImage
        if (image!!.matches(Constants.NONE.toRegex())) {
            mItemSelect = 0
        } else if (image.matches(Constants.DEFAULT.toRegex())) {
            mItemSelect = 1
        } else {
            mItemSelect = 2
        }
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            saveImagePrefs(mItemSelect)
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun saveImagePrefs(which: Int) {
        if (which == 0) {
            prefs!!.reminderImage = Constants.NONE
        } else if (which == 1) {
            prefs!!.reminderImage = Constants.DEFAULT
        } else if (which == 2) {
            startActivityForResult(Intent(context, FileExplorerActivity::class.java)
                    .putExtra(Constants.FILE_TYPE, FileExplorerActivity.TYPE_PHOTO), Constants.ACTION_REQUEST_GALLERY)
        }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.notification))
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
                        prefs!!.melodyFile = file.toString()
                    }
                }
                showMelody()
            }
            Constants.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data!!.getStringExtra(Constants.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        val uri = UriUtil.getUri(context, file)
                        prefs!!.reminderImage = uri.toString()
                    }
                }
            }
        }
    }

    companion object {

        private val MELODY_CODE = 125
        private val PERM_BT = 1425
        private val PERM_SD = 1426
    }
}