package com.elementary.tasks.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.CacheUtil
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.SoundStackHolder
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.databinding.FragmentSettingsBirthdayNotificationsBinding
import org.koin.android.ext.android.inject
import java.io.File

class BirthdayNotificationFragment : BaseSettingsFragment<FragmentSettingsBirthdayNotificationsBinding>() {

  private val cacheUtil by inject<CacheUtil>()
  private val soundStackHolder by inject<SoundStackHolder>()

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsBirthdayNotificationsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
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
      mItemSelect = when (prefs.birthdayPlaybackDuration) {
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
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
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
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

  private fun iconTintColor() =
    if (isDark) colorOf(R.color.pureWhite)
    else colorOf(R.color.pureBlack)

  private fun showMelody() {
    val filePath = prefs.birthdayMelody
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
    return listOf(Constants.SOUND_RINGTONE, Constants.SOUND_NOTIFICATION, Constants.SOUND_ALARM).contains(prefs.birthdayMelody)
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
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setCancelable(true)
      builder.setTitle(getString(R.string.melody))
      mItemSelect = when (prefs.birthdayMelody) {
        Constants.SOUND_RINGTONE -> 0
        Constants.SOUND_NOTIFICATION, Constants.DEFAULT -> 1
        Constants.SOUND_ALARM -> 2
        else -> {
          val musicFile = File(prefs.birthdayMelody)
          if (musicFile.exists()) {
            3
          } else {
            val ringtone = RingtoneManager.getRingtone(context, prefs.birthdayMelody.toUri())
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
          cacheUtil.removeFromCache(prefs.birthdayMelody)
        }
        when (mItemSelect) {
          0 -> prefs.birthdayMelody = Constants.SOUND_RINGTONE
          1 -> prefs.birthdayMelody = Constants.SOUND_NOTIFICATION
          2 -> prefs.birthdayMelody = Constants.SOUND_ALARM
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
    if (Module.is10) {
      binding.wakeScreenOptionPrefs.hide()
    } else {
      binding.wakeScreenOptionPrefs.show()
      binding.wakeScreenOptionPrefs.isChecked = prefs.isBirthdayWakeEnabled
      binding.wakeScreenOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
      binding.wakeScreenOptionPrefs.setOnClickListener { changeWakePrefs() }
    }
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
        if (Permissions.checkPermission(requireContext(), Permissions.READ_EXTERNAL)) {
          val filePath = cacheUtil.cacheFile(data)
          if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
              prefs.birthdayMelody = file.toString()
            }
          }
          showMelody()
        }
      }
      RINGTONE_CODE -> if (resultCode == Activity.RESULT_OK) {
        val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
          prefs.birthdayMelody = uri.toString()
          showMelody()
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    if (soundStackHolder.sound?.isPlaying == true) {
      soundStackHolder.sound?.stop(true)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        PERM_MELODY -> pickMelody()
      }
    }
  }

  companion object {
    private const val MELODY_CODE = 125
    private const val RINGTONE_CODE = 126
    private const val PERM_MELODY = 1429
  }
}
