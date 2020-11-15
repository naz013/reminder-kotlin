package com.elementary.tasks.navigation.settings

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CacheUtil
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.SoundStackHolder
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*

class NotificationSettingsFragment : BaseSettingsFragment<FragmentSettingsNotificationBinding>() {

  private val cacheUtil by inject<CacheUtil>()
  private val soundStackHolder by inject<SoundStackHolder>()

  private var mItemSelect: Int = 0

  override fun layoutRes(): Int = R.layout.fragment_settings_notification

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
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
    initMelodyDurationPrefs()
  }

  private fun initMelodyDurationPrefs() {
    binding.melodyDurationPrefs.setOnClickListener { showMelodyDurationDialog() }
    binding.melodyDurationPrefs.setReverseDependentView(binding.infiniteSoundOptionPrefs)
    showMelodyDuration()
  }

  private fun initUnlockPriorityPrefs() {
    if (Module.isQ) {
      binding.unlockPriorityPrefs.hide()
    } else {
      binding.unlockPriorityPrefs.show()
      binding.unlockPriorityPrefs.setOnClickListener { showPriorityDialog() }
      binding.unlockPriorityPrefs.setDependentView(binding.unlockScreenPrefs)
      showPriority()
    }
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
    if (Module.isQ) {
      binding.ignoreWindowType.hide()
    } else {
      binding.ignoreWindowType.show()
      binding.ignoreWindowType.setOnClickListener { changeIgnoreWindowTypePrefs() }
      binding.ignoreWindowType.isChecked = prefs.isIgnoreWindowType
    }
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
        if (Permissions.checkPermission(it, PERM_AUTO_CALL, Permissions.CALL_PHONE)) {
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
    if (Module.isQ) {
      binding.autoCallPrefs.hide()
    } else {
      binding.autoCallPrefs.show()
      binding.autoCallPrefs.setOnClickListener { changeAutoCallPrefs() }
      binding.autoCallPrefs.isChecked = prefs.isAutoCallEnabled
      binding.autoCallPrefs.isEnabled = prefs.isTelephonyAllowed
    }
  }

  private fun changeAutoLaunchPrefs() {
    val isChecked = binding.autoLaunchPrefs.isChecked
    binding.autoLaunchPrefs.isChecked = !isChecked
    prefs.isAutoLaunchEnabled = !isChecked
  }

  private fun initAutoLaunchPrefs() {
    if (Module.isQ) {
      binding.autoLaunchPrefs.hide()
    } else {
      binding.autoLaunchPrefs.show()
      binding.autoLaunchPrefs.setOnClickListener { changeAutoLaunchPrefs() }
      binding.autoLaunchPrefs.isChecked = prefs.isAutoLaunchEnabled
    }
  }

  private fun changeUnlockPrefs() {
    val isChecked = binding.unlockScreenPrefs.isChecked
    binding.unlockScreenPrefs.isChecked = !isChecked
    prefs.isDeviceUnlockEnabled = !isChecked
  }

  private fun initUnlockPrefs() {
    if (Module.isQ) {
      binding.unlockScreenPrefs.hide()
    } else {
      binding.unlockScreenPrefs.show()
      binding.unlockScreenPrefs.setOnClickListener { changeUnlockPrefs() }
      binding.unlockScreenPrefs.isChecked = prefs.isDeviceUnlockEnabled
    }
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
    if (SuperUtil.hasVolumePermission(requireContext())) {
      changeIncrease()
    } else {
      openNotificationsSettings()
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
    dialogues.getNullableDialog(context)?.let { builder ->
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun initReminderTypePrefs() {
    if (Module.isQ) {
      binding.typePrefs.hide()
    } else {
      binding.typePrefs.show()
      binding.typePrefs.setOnClickListener { showReminderTypeDialog() }
      showReminderType()
    }
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
    try {
      binding.streamPrefs.setDetailText(types[prefs.soundStream - 3])
    } catch (e: Exception) {
      binding.streamPrefs.setDetailText(types[0])
    }
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
    binding.chooseSoundPrefs.setViewTintColor(iconTintColor())
    showMelody()
    soundStackHolder.initParams()
    soundStackHolder.onlyPlay = true
    soundStackHolder.playbackCallback = object : Sound.PlaybackCallback {
      override fun onFinish() {
        binding.chooseSoundPrefs.setViewResource(R.drawable.ic_twotone_play_circle_filled_24px)
        binding.chooseSoundPrefs.setLoading(false)
      }

      override fun onStart() {
        binding.chooseSoundPrefs.setViewResource(R.drawable.ic_twotone_stop_24px)
        binding.chooseSoundPrefs.setLoading(true)
      }
    }
    binding.chooseSoundPrefs.setViewResource(R.drawable.ic_twotone_play_circle_filled_24px)
    binding.chooseSoundPrefs.setCustomViewClickListener {
      if (soundStackHolder.sound?.isPlaying == true) {
        soundStackHolder.sound?.stop(true)
      } else {
        val melody = ReminderUtils.getSound(requireContext(), prefs, prefs.melodyFile)
        if (melody.melodyType == ReminderUtils.MelodyType.RINGTONE) {
          soundStackHolder.sound?.playRingtone(melody.uri)
        } else {
          soundStackHolder.sound?.playAlarm(melody.uri, false)
        }
      }
    }
  }

  private fun iconTintColor(): Int {
    return if (isDark) ContextCompat.getColor(requireContext(), R.color.pureWhite)
    else ContextCompat.getColor(requireContext(), R.color.pureBlack)
  }

  private fun showMelody() {
    val filePath = prefs.melodyFile
    val labels = melodyLabels()
    val label = when (filePath) {
      Constants.SOUND_RINGTONE -> labels[0]
      Constants.SOUND_NOTIFICATION, Constants.DEFAULT -> labels[1]
      Constants.SOUND_ALARM -> labels[2]
      else -> {
        if (!filePath.matches("".toRegex())) {
          val musicFile = File(filePath)
          if (musicFile.exists()) {
            musicFile.name
          } else {
            val ringtone = RingtoneManager.getRingtone(context, filePath.toUri())
            if (ringtone != null) {
              ringtone.getTitle(context)
            } else {
              labels[1]
            }
          }
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
      getString(R.string.choose_file),
      getString(R.string.choose_ringtone)
    )
  }

  private fun showSoundDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setCancelable(true)
      builder.setTitle(getString(R.string.melody))
      mItemSelect = when (prefs.melodyFile) {
        Constants.SOUND_RINGTONE -> 0
        Constants.SOUND_NOTIFICATION, Constants.DEFAULT -> 1
        Constants.SOUND_ALARM -> 2
        else -> {
          val musicFile = File(prefs.melodyFile)
          if (musicFile.exists()) {
            3
          } else {
            val ringtone = RingtoneManager.getRingtone(context, prefs.melodyFile.toUri())
            if (ringtone != null) {
              4
            } else {
              1
            }
          }
        }
      }
      builder.setSingleChoiceItems(melodyLabels(), mItemSelect) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        dialog.dismiss()
        if (mItemSelect <= 2 && !isDefaultMelody()) {
          cacheUtil.removeFromCache(prefs.melodyFile)
        }
        when (mItemSelect) {
          0 -> prefs.melodyFile = Constants.SOUND_RINGTONE
          1 -> prefs.melodyFile = Constants.SOUND_NOTIFICATION
          2 -> prefs.melodyFile = Constants.SOUND_ALARM
          3 -> pickMelody()
          else -> pickRingtone()
        }
        showMelody()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun pickRingtone() {
    withActivity {
      val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_ringtone_for_notifications))
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
      intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
      startActivityForResult(intent, RINGTONE_CODE)
    }
  }

  private fun pickMelody() {
    withActivity {
      if (Permissions.checkPermission(it, PERM_MELODY, Permissions.READ_EXTERNAL)) {
        cacheUtil.pickMelody(it, MELODY_CODE)
      }
    }
  }

  private fun isDefaultMelody(): Boolean {
    return listOf(Constants.SOUND_RINGTONE, Constants.SOUND_NOTIFICATION, Constants.SOUND_ALARM).contains(prefs.melodyFile)
  }

  private fun showMelodyDuration() {
    val label = when (prefs.playbackDuration) {
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
      mItemSelect = when (prefs.playbackDuration) {
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
        prefs.playbackDuration = when (mItemSelect) {
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
        Permissions.checkPermission(it, PERM_BT, Permissions.BLUETOOTH)
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
    Notifier.updateReminderPermanent(requireContext(), PermanentReminderReceiver.ACTION_SHOW)
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
      Notifier.updateReminderPermanent(requireContext(), PermanentReminderReceiver.ACTION_SHOW)
    } else {
      Notifier.updateReminderPermanent(requireContext(), PermanentReminderReceiver.ACTION_HIDE)
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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
    binding.bgImagePrefs.setViewDrawable(null)
    if (prefs.screenImage != Constants.NONE) {
      if (prefs.screenImage == Constants.DEFAULT) {
        binding.bgImagePrefs.setViewResource(R.drawable.widget_preview_bg)
      } else {
        val imageFile = File(prefs.screenImage)
        if (Permissions.checkPermission(requireContext(), Permissions.READ_EXTERNAL) && imageFile.exists()) {
          Glide.with(requireContext())
            .load(imageFile)
            .override(200, 200)
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {
              override fun onLoadCleared(placeholder: Drawable?) {

              }

              override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                binding.bgImagePrefs.setViewDrawable(resource)
              }
            })
        } else {
          binding.bgImagePrefs.setViewResource(R.drawable.widget_preview_bg)
        }
      }
    }
  }

  override fun getTitle(): String = getString(R.string.notification)

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
        if (Permissions.checkPermission(requireContext(), Permissions.READ_EXTERNAL)) {
          val filePath = cacheUtil.cacheFile(data)
          if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
              prefs.melodyFile = file.toString()
            }
          }
          showMelody()
        }
      }
      RINGTONE_CODE -> if (resultCode == Activity.RESULT_OK) {
        val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
          prefs.melodyFile = uri.toString()
          showMelody()
        }
      }
      Constants.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
        if (Permissions.checkPermission(requireContext(), Permissions.READ_EXTERNAL)) {
          val filePath = cacheUtil.cacheFile(data)
          if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
              prefs.screenImage = filePath
            } else {
              prefs.screenImage = Constants.DEFAULT
            }
          }
          showImage()
        }
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        PERM_AUTO_CALL -> changeAutoCallPrefs()
        PERM_IMAGE -> openImagePicker()
        PERM_MELODY -> pickMelody()
      }
    }
  }

  override fun onPause() {
    super.onPause()
    if (soundStackHolder.sound?.isPlaying == true) {
      soundStackHolder.sound?.stop(true)
    }
  }

  private fun openImagePicker() {
    withActivity {
      if (Permissions.checkPermission(it, PERM_IMAGE, Permissions.READ_EXTERNAL)) {
        cacheUtil.pickImage(it, Constants.ACTION_REQUEST_GALLERY)
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
    private const val RINGTONE_CODE = 126
    private const val PERM_BT = 1425
    private const val PERM_AUTO_CALL = 1427
    private const val PERM_IMAGE = 1428
    private const val PERM_MELODY = 1429
  }
}