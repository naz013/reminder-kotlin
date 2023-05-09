package com.elementary.tasks.birthdays.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseNotificationActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ActivityDialogBirthdayBinding
import com.elementary.tasks.reminder.dialog.ReminderDialogActivity
import com.squareup.picasso.Picasso
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.util.Locale

class ShowBirthdayActivity : BaseNotificationActivity<ActivityDialogBirthdayBinding>() {

  private val viewModel by viewModel<ShowBirthdayViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  override var isScreenResumed: Boolean = false
    private set
  override var summary: String = ""
    private set
  override val groupName: String = "birthdays"

  private val isBirthdaySilentEnabled: Boolean
    get() {
      var isEnabled = prefs.isSoundInSilentModeEnabled
      if (Module.isPro && !isGlobal) {
        isEnabled = prefs.isBirthdaySilentEnabled
      }
      return isEnabled
    }

  private val isTtsEnabled: Boolean
    get() {
      var isEnabled = prefs.isTtsEnabled
      if (Module.isPro && !isGlobal) {
        isEnabled = prefs.isBirthdayTtsEnabled
      }
      return isEnabled
    }

  override val ttsLocale: Locale?
    get() {
      var locale = language.getLocale(false)
      if (Module.isPro && !isGlobal) {
        locale = language.getLocale(true)
      }
      return locale
    }

  override val melody: String
    get() = if (Module.isPro && !isGlobal) {
      prefs.birthdayMelody
    } else {
      prefs.melodyFile
    }

  override val isBirthdayInfiniteVibration: Boolean
    get() {
      var vibrate = prefs.isInfiniteVibrateEnabled
      if (Module.isPro && !isGlobal) {
        vibrate = prefs.isBirthdayInfiniteVibrationEnabled
      }
      return vibrate
    }

  override val isBirthdayInfiniteSound: Boolean
    get() {
      var isLooping = prefs.isInfiniteSoundEnabled
      if (Module.isPro && !isGlobal) {
        isLooping = prefs.isBirthdayInfiniteSoundEnabled
      }
      return isLooping
    }

  override val isVibrate: Boolean
    get() {
      var vibrate = prefs.isVibrateEnabled
      if (Module.isPro && !isGlobal) {
        vibrate = prefs.isBirthdayVibrationEnabled
      }
      return vibrate
    }

  override val uuId: String
    get() = viewModel.getId()

  override val id: Int
    get() = viewModel.getUniqueId()

  override val ledColor: Int
    get() {
      var ledColor = LED.getLED(prefs.ledColor)
      if (Module.isPro && !isGlobal) {
        ledColor = LED.getLED(prefs.birthdayLedColor)
      }
      return ledColor
    }

  override val maxVolume: Int
    get() = prefs.loudness

  override val isGlobal: Boolean
    get() = prefs.isBirthdayGlobalEnabled

  override val isUnlockDevice: Boolean
    get() {
      var isWake = prefs.isDeviceUnlockEnabled
      if (Module.isPro && !isGlobal) {
        isWake = prefs.isBirthdayWakeEnabled
      }
      return isWake
    }

  override val priority: Int
    get() = prefs.birthdayPriority

  private var wasStopped = false
  private val localReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action ?: ""
      val mId = intent?.getStringExtra(Constants.INTENT_ID) ?: ""
      Timber.d("onReceive: $action, $mId")
      if (wasStopped && action == ACTION_STOP_BG_ACTIVITY && uuId == mId) {
        finish()
      }
    }
  }

  override fun inflateBinding() = ActivityDialogBirthdayBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    drawBehindSystemBars(binding.rootView)

    isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)

    binding.buttonOk.setOnClickListener { ok() }
    binding.buttonCall.setOnClickListener { makeCall() }
    binding.buttonSms.setOnClickListener { sendSMS() }

    binding.contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
    binding.contactPhoto.gone()

    if (savedInstanceState != null) {
      isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
    }

    initViewModel()
    LocalBroadcastManager.getInstance(this)
      .registerReceiver(localReceiver, IntentFilter(ReminderDialogActivity.ACTION_STOP_BG_ACTIVITY))
  }

  private fun getId() = intentString(Constants.INTENT_ID, "")

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_IS_ROTATED, true)
    super.onSaveInstanceState(outState)
  }

  private fun initViewModel() {
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.SAVED -> close()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
    if (getId().isEmpty() && BuildConfig.DEBUG) {
      loadTest()
    }
  }

  private fun loadTest() {
    val isMocked = intentBoolean(ARG_TEST, false)
    if (isMocked) {
      viewModel.onTestLoad(intentParcelable(ARG_TEST_ITEM, Birthday::class.java))
    }
  }

  private fun showBirthday(birthday: UiBirthdayShow) {
    if (viewModel.isEventShowed) return

    birthday.photo?.also {
      Picasso.get().load(it).into(binding.contactPhoto)
      binding.contactPhoto.visible()
    } ?: run { binding.contactPhoto.gone() }

    binding.userName.text = birthday.name
    binding.userName.contentDescription = birthday.name

    binding.userYears.text = birthday.ageFormatted
    binding.userYears.contentDescription = birthday.ageFormatted

    summary = birthday.name + "\n" + birthday.ageFormatted

    if (birthday.number.isEmpty()) {
      binding.buttonCall.transparent()
      binding.buttonSms.transparent()
      binding.userNumber.gone()
    } else {
      binding.userNumber.text = birthday.number
      binding.userNumber.contentDescription = birthday.number
      binding.userNumber.visible()
      if (prefs.isTelephonyAllowed) {
        binding.buttonCall.visible()
        binding.buttonSms.visible()
      } else {
        binding.buttonCall.transparent()
        binding.buttonSms.transparent()
      }
    }
    init()

    if (isTtsEnabled) {
      showTTSNotification(birthday)
      startTts()
    } else {
      showNotification(birthday)
    }
  }

  private fun showNotification(birthday: UiBirthdayShow) {
    if (isScreenResumed) {
      return
    }
    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
    builder.setContentTitle(birthday.name)
    builder.setContentText(birthday.ageFormatted)
    builder.setSmallIcon(R.drawable.ic_twotone_cake_white)
    builder.color = colorOf(R.color.secondaryBlue)
    if (!isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this)
        || SuperUtil.checkNotificationPermission(this) && isBirthdaySilentEnabled)) {
      val sound = sound
      sound?.playAlarm(soundUri, isBirthdayInfiniteSound, prefs.birthdayPlaybackDuration)
    }
    if (isVibrate) {
      var pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
      if (isBirthdayInfiniteVibration) {
        pattern = longArrayOf(150, 86400000)
      }
      builder.setVibrate(pattern)
    }
    if (Module.isPro) {
      builder.setLights(ledColor, 500, 1000)
    }
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup(groupName)
      builder.setGroupSummary(true)
    }
    notifier.notify(id, builder.build())
    if (isWear) {
      showWearNotification(birthday.name)
    }
  }

  private fun showTTSNotification(birthday: UiBirthdayShow) {
    if (isScreenResumed) {
      return
    }
    Timber.d("showTTSNotification: ")
    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
    builder.setContentTitle(birthday.name)
    builder.setContentText(birthday.ageFormatted)
    builder.setSmallIcon(R.drawable.ic_twotone_cake_white)
    builder.color = colorOf(R.color.secondaryBlue)
    if (isScreenResumed) {
      builder.priority = NotificationCompat.PRIORITY_LOW
    } else {
      builder.priority = priority
      if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
          (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
        playDefaultMelody()
      }
      if (isVibrate) {
        var pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
        if (isBirthdayInfiniteVibration) {
          pattern = longArrayOf(150, 86400000)
        }
        builder.setVibrate(pattern)
      }
    }
    if (Module.isPro) {
      builder.setLights(ledColor, 500, 1000)
    }
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup(groupName)
      builder.setGroupSummary(true)
    }
    notifier.notify(id, builder.build())
    if (isWear) {
      showWearNotification(birthday.name)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver)
    lifecycle.removeObserver(viewModel)
    removeFlags()
  }

  override fun handleBackPress(): Boolean {
    discardMedia()
    if (prefs.isFoldingEnabled) {
      removeFlags()
      finish()
    } else {
      toast(R.string.select_one_of_item)
    }
    return true
  }

  private fun makeCall() {
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      viewModel.getNumber()?.also {
        TelephonyUtil.makeCall(it, this)
        updateBirthday()
      } ?: run { ok() }
    }
  }

  private fun sendSMS() {
    viewModel.getNumber()?.also {
      TelephonyUtil.sendSms(it, this)
      updateBirthday()
    } ?: run { ok() }
  }

  private fun ok() {
    updateBirthday()
  }

  private fun updateBirthday() {
    discardNotification(viewModel.getUniqueId())
    viewModel.isEventShowed = true
    viewModel.saveBirthday()
  }

  private fun close() {
    removeFlags()
    discardNotification(id)
    finish()
  }

  override fun onStop() {
    super.onStop()
    wasStopped = true
  }

  companion object {

    private const val ARG_TEST = "arg_test"
    private const val ARG_TEST_ITEM = "arg_test_item"
    private const val ARG_IS_ROTATED = "arg_rotated"
    const val ACTION_STOP_BG_ACTIVITY = "action.birthday.STOP.BG"

    fun mockTest(context: Context, birthday: Birthday) {
      val intent = Intent(context, ShowBirthdayActivity::class.java)
      intent.putExtra(ARG_TEST, true)
      intent.putExtra(ARG_TEST_ITEM, birthday)
      context.startActivity(intent)
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      val resultIntent = Intent(context, ShowBirthdayActivity::class.java)
      resultIntent.putExtra(Constants.INTENT_ID, id)
      resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      return resultIntent
    }
  }
}
