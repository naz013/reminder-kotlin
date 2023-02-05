package com.elementary.tasks.settings

import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.elementary.tasks.R
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.MelodyPicker
import com.elementary.tasks.core.os.datapicker.NotificationPolicyLauncher
import com.elementary.tasks.core.os.datapicker.RingtonePicker
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.SoundStackHolder
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.SelectionList
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.util.Locale

class NotificationSettingsFragment : BaseSettingsFragment<FragmentSettingsNotificationBinding>() {

  private val cacheUtil by inject<CacheUtil>()
  private val soundStackHolder by inject<SoundStackHolder>()
  private val melodyPicker = MelodyPicker(this) {
    val file = File(it)
    if (file.exists()) {
      prefs.melodyFile = file.toString()
      showMelody()
    }
  }
  private val ringtonePicker = RingtonePicker(this) {
    prefs.melodyFile = it.toString()
    showMelody()
  }
  private val notificationPolicyLauncher = NotificationPolicyLauncher(this)

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsNotificationBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
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
    initMelodyDurationPrefs()
  }

  private fun initMelodyDurationPrefs() {
    binding.melodyDurationPrefs.setOnClickListener { showMelodyDurationDialog() }
    binding.melodyDurationPrefs.setReverseDependentView(binding.infiniteSoundOptionPrefs)
    showMelodyDuration()
  }

  private fun initUnlockPriorityPrefs() {
    if (Module.is10) {
      binding.unlockPriorityPrefs.gone()
    } else {
      binding.unlockPriorityPrefs.visible()
      binding.unlockPriorityPrefs.setOnClickListener { showPriorityDialog() }
      binding.unlockPriorityPrefs.setDependentView(binding.unlockScreenPrefs)
      showPriority()
    }
  }

  private fun showPriority() {
    binding.unlockPriorityPrefs.setDetailText(unlockList()[prefs.unlockPriority])
  }

  private fun showPriorityDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = prefs.unlockPriority,
        title = getString(R.string.priority),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = unlockList()
      ),
      onOk = {
        prefs.unlockPriority = it
        showPriority()
      }
    )
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
    if (Module.is10) {
      binding.ignoreWindowType.gone()
    } else {
      binding.ignoreWindowType.visible()
      binding.ignoreWindowType.setOnClickListener { changeIgnoreWindowTypePrefs() }
      binding.ignoreWindowType.isChecked = prefs.isIgnoreWindowType
    }
  }

  private fun showRepeatTimeDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.interval)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = String.format(
          Locale.getDefault(), getString(R.string.x_minutes),
          value.toInt().toString()
        )
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 60f

      val repeatTime = prefs.notificationRepeatTime
      b.seekBar.value = repeatTime.toFloat()

      b.titleView.text = String.format(
        Locale.getDefault(), getString(R.string.x_minutes),
        repeatTime.toString()
      )
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.notificationRepeatTime = b.seekBar.value.toInt()
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
    binding.repeatIntervalPrefs.setDetailText(
      String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        prefs.notificationRepeatTime.toString()
      )
    )
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
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = prefs.ledColor,
        title = getString(R.string.led_color),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = LED.getAllNames(requireContext())
      ),
      onOk = {
        prefs.ledColor = it
        showLedColor()
      }
    )
  }

  private fun showLedColor() {
    binding.chooseLedColorPrefs.setDetailText(LED.getTitle(requireContext(), prefs.ledColor))
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
    binding.delayForPrefs.setDetailText(
      String.format(
        Locale.getDefault(), getString(R.string.x_minutes),
        prefs.snoozeTime.toString()
      )
    )
  }

  private fun snoozeFormat(progress: Int): String {
    if (!isAdded) return ""
    return String.format(Locale.getDefault(), getString(R.string.x_minutes), progress.toString())
  }

  private fun showSnoozeDialog() {
    dialogues.getNullableDialog(context)?.let { builder ->
      builder.setTitle(R.string.snooze_time)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = snoozeFormat(value.toInt())
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 60f

      val snoozeTime = prefs.snoozeTime
      b.seekBar.value = snoozeTime.toFloat()

      b.titleView.text = snoozeFormat(snoozeTime)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.snoozeTime = b.seekBar.value.toInt()
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
      permissionFlow.askPermission(Permissions.CALL_PHONE) {
        binding.autoCallPrefs.isChecked = true
        prefs.isAutoCallEnabled = true
      }
    } else {
      binding.autoCallPrefs.isChecked = false
      prefs.isAutoCallEnabled = false
    }
  }

  private fun initAutoCallPrefs() {
    if (Module.is10) {
      binding.autoCallPrefs.gone()
    } else {
      binding.autoCallPrefs.visible()
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
    if (Module.is10) {
      binding.autoLaunchPrefs.gone()
    } else {
      binding.autoLaunchPrefs.visible()
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
    if (Module.is10) {
      binding.unlockScreenPrefs.gone()
    } else {
      binding.unlockScreenPrefs.visible()
      binding.unlockScreenPrefs.setOnClickListener { changeUnlockPrefs() }
      binding.unlockScreenPrefs.isChecked = prefs.isDeviceUnlockEnabled
    }
  }

  private fun showTtsLocaleDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = language.getLocalePosition(prefs.ttsLocale),
        title = getString(R.string.language),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = language.getLocaleNames(context)
      ),
      onOk = { saveTtsLocalePrefs(it) }
    )
  }

  private fun showTtsLocale() {
    val locale = prefs.ttsLocale
    val i = language.getLocalePosition(locale)
    binding.localePrefs.setDetailText(language.getLocaleNames(requireContext())[i])
  }

  private fun saveTtsLocalePrefs(i: Int) {
    prefs.ttsLocale = language.getLocaleByPosition(i)
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
    notificationPolicyLauncher.openSettings()
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
    if (!SuperUtil.hasVolumePermission(requireContext())) {
      openNotificationsSettings()
      return
    }
    dialogues.getNullableDialog(context)?.let { builder ->
      builder.setTitle(R.string.loudness)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = value.toInt().toString()
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 25f

      val loudness = prefs.loudness
      b.seekBar.value = loudness.toFloat()

      b.titleView.text = loudness.toString()
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.loudness = b.seekBar.value.toInt()
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
    binding.volumePrefs.setDetailText(
      String.format(Locale.getDefault(), getString(R.string.loudness) + " %d", prefs.loudness)
    )
  }

  private fun showStreamDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = prefs.soundStream - 3,
        title = getString(R.string.sound_stream),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = listOf(
          getString(R.string.music),
          getString(R.string.alarm),
          getString(R.string.notification)
        )
      ),
      onOk = {
        prefs.soundStream = it + 3
        showStream()
      }
    )
  }

  private fun initReminderTypePrefs() {
    if (Module.is10) {
      binding.typePrefs.gone()
    } else {
      binding.typePrefs.visible()
      binding.typePrefs.setOnClickListener { showReminderTypeDialog() }
      showReminderType()
    }
  }

  private fun showReminderTypeDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = prefs.reminderType,
        title = getString(R.string.notification_type),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = listOf(getString(R.string.full_screen), getString(R.string.simple))
      ),
      onOk = {
        prefs.reminderType = it
        showReminderType()
      }
    )
  }

  private fun showReminderType() {
    val types = listOf(getString(R.string.full_screen), getString(R.string.simple))
    binding.typePrefs.setDetailText(types[prefs.reminderType])
  }

  private fun initSoundStreamPrefs() {
    binding.streamPrefs.setOnClickListener { showStreamDialog() }
    binding.streamPrefs.setDependentView(binding.systemPrefs)
    showStream()
  }

  private fun showStream() {
    val types =
      listOf(getString(R.string.music), getString(R.string.alarm), getString(R.string.notification))
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
      Timber.d("Play Click: ${soundStackHolder.sound?.isPlaying}")
      if (soundStackHolder.sound?.isPlaying == true) {
        soundStackHolder.sound?.stop(true)
      } else {
        val melody = ReminderUtils.getSound(requireContext(), prefs, prefs.melodyFile)
        if (melody.melodyType == ReminderUtils.MelodyType.RINGTONE) {
          soundStackHolder.sound?.playRingtone(melody.uri)
        } else {
          permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
            soundStackHolder.sound?.playAlarm(melody.uri, false)
          }
        }
      }
    }
  }

  private fun iconTintColor() =
    if (isDark) colorOf(R.color.pureWhite)
    else colorOf(R.color.pureBlack)

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

  private fun melodyLabels() = listOf(
    getString(R.string.default_string) + ": " + getString(R.string.ringtone),
    getString(R.string.default_string) + ": " + getString(R.string.notification),
    getString(R.string.default_string) + ": " + getString(R.string.alarm),
    getString(R.string.choose_file),
    getString(R.string.choose_ringtone)
  )

  private fun soundPrefPosition() = when (prefs.melodyFile) {
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

  private fun showSoundDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = soundPrefPosition(),
        title = getString(R.string.melody),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = melodyLabels()
      ),
      onOk = { saveMelody(it) }
    )
  }

  private fun saveMelody(i: Int) {
    if (i <= 2 && !isDefaultMelody()) {
      cacheUtil.removeFromCache(prefs.melodyFile)
    }
    when (i) {
      0 -> prefs.melodyFile = Constants.SOUND_RINGTONE
      1 -> prefs.melodyFile = Constants.SOUND_NOTIFICATION
      2 -> prefs.melodyFile = Constants.SOUND_ALARM
      3 -> melodyPicker.pickMelody()
      else -> pickRingtone()
    }
    showMelody()
  }

  private fun pickRingtone() {
    ringtonePicker.pickRingtone()
  }

  private fun isDefaultMelody(): Boolean {
    return listOf(
      Constants.SOUND_RINGTONE,
      Constants.SOUND_NOTIFICATION,
      Constants.SOUND_ALARM
    ).contains(prefs.melodyFile)
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

  private fun durationLabels() = listOf(
    getString(R.string.till_the_end),
    "5 " + getString(R.string.seconds),
    "10 " + getString(R.string.seconds),
    "15 " + getString(R.string.seconds),
    "20 " + getString(R.string.seconds),
    "30 " + getString(R.string.seconds),
    "60 " + getString(R.string.seconds)
  )

  private fun melodyPlaybackDurationPosition() = when (prefs.playbackDuration) {
    5 -> 1
    10 -> 2
    15 -> 3
    20 -> 4
    30 -> 5
    60 -> 6
    else -> 0
  }

  private fun showMelodyDurationDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      SelectionList(
        position = melodyPlaybackDurationPosition(),
        title = getString(R.string.melody_playback_duration),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = durationLabels()
      ),
      onOk = { saveMelodyPlaybackDuration(it) }
    )
  }

  private fun saveMelodyPlaybackDuration(i: Int) {
    prefs.playbackDuration = when (i) {
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
    val isChecked = binding.soundOptionPrefs.isChecked
    binding.soundOptionPrefs.isChecked = !isChecked
    prefs.isSoundInSilentModeEnabled = !isChecked
    if (!SuperUtil.checkNotificationPermission(requireActivity())) {
      SuperUtil.askNotificationPermission(requireActivity(), dialogues)
    } else {
      permissionFlow.askPermission(Permissions.BLUETOOTH) {}
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

  private fun initSbIconPrefs() {
    binding.statusIconPrefs.setOnClickListener { changeSbIconPrefs() }
    binding.statusIconPrefs.isChecked = prefs.isSbIconEnabled
    binding.statusIconPrefs.setDependentView(binding.permanentNotificationPrefs)
  }

  private fun changeSbIconPrefs() {
    val isChecked = binding.statusIconPrefs.isChecked
    binding.statusIconPrefs.isChecked = !isChecked
    prefs.isSbIconEnabled = !isChecked
    PermanentReminderReceiver.show(requireContext())
  }

  private fun initSbPrefs() {
    binding.permanentNotificationPrefs.setOnClickListener { tryChangeSbPrefs() }
    binding.permanentNotificationPrefs.isChecked = prefs.isSbNotificationEnabled
  }

  private fun tryChangeSbPrefs() {
    val isChecked = binding.permanentNotificationPrefs.isChecked
    Timber.d("tryChangeSbPrefs: $isChecked")
    if (!isChecked) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { changeSbPrefs() }
      } else {
        changeSbPrefs()
      }
    } else {
      changeSbPrefs()
    }
  }

  private fun changeSbPrefs() {
    val isChecked = binding.permanentNotificationPrefs.isChecked
    binding.permanentNotificationPrefs.isChecked = !isChecked
    prefs.isSbNotificationEnabled = !isChecked
    if (prefs.isSbNotificationEnabled) {
      PermanentReminderReceiver.show(requireContext())
    } else {
      PermanentReminderReceiver.hide(requireContext())
    }
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

  override fun getTitle() = getString(R.string.notification)

  override fun onPause() {
    super.onPause()
    if (soundStackHolder.sound?.isPlaying == true) {
      soundStackHolder.sound?.stop(true)
    }
  }

  private fun unlockList() = listOf(
    getString(R.string.all),
    getString(R.string.priority_low) + " " + getString(R.string.and_above),
    getString(R.string.priority_normal) + " " + getString(R.string.and_above),
    getString(R.string.priority_high) + " " + getString(R.string.and_above),
    getString(R.string.priority_highest)
  )
}
