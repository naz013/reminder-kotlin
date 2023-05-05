package com.elementary.tasks.missed_calls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseNotificationActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.data.ui.missedcall.UiMissedCallShow
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
import com.elementary.tasks.databinding.ActivityDialogMissedCallBinding
import com.squareup.picasso.Picasso
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@Deprecated("After S")
class MissedCallDialogActivity : BaseNotificationActivity<ActivityDialogMissedCallBinding>() {

  private val viewModel by viewModel<MissedCallViewModel> { parametersOf(getNumber()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  override var isScreenResumed: Boolean = false
    private set

  override val melody: String
    get() = ""

  override val isVibrate: Boolean
    get() = prefs.isVibrateEnabled

  override val summary: String
    get() = viewModel.getNumber() ?: ""

  override val uuId: String
    get() = ""

  override val id: Int
    get() = viewModel.missedCall.value?.uniqueId ?: 2122

  override val ledColor: Int
    get() = LED.getLED(prefs.ledColor)

  override val isGlobal: Boolean
    get() = false

  override val isUnlockDevice: Boolean
    get() = prefs.isDeviceUnlockEnabled

  override val maxVolume: Int
    get() = prefs.loudness

  override val priority: Int
    get() = prefs.missedCallPriority

  override val groupName: String
    get() = "missed_call"

  override fun inflateBinding() = ActivityDialogMissedCallBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    drawBehindSystemBars(binding.rootView)

    isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)

    binding.contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
    binding.contactPhoto.gone()

    initButtons()

    if (savedInstanceState != null) {
      isScreenResumed = intentBoolean(ARG_IS_ROTATED)
    }

    initViewModel()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_IS_ROTATED, true)
    super.onSaveInstanceState(outState)
  }

  private fun initButtons() {
    binding.buttonOk.setOnClickListener { removeMissed() }
    binding.buttonSms.setOnClickListener { sendSMS() }
    binding.buttonCall.setOnClickListener { makeCall() }
    if (prefs.isTelephonyAllowed) {
      binding.buttonSms.visibility = View.VISIBLE
      binding.buttonCall.visibility = View.VISIBLE
    } else {
      binding.buttonSms.visibility = View.INVISIBLE
      binding.buttonCall.visibility = View.INVISIBLE
    }
  }

  private fun loadTest() {
    if (intentBoolean(ARG_TEST)) {
      viewModel.loadTest(intentParcelable(ARG_TEST_ITEM, MissedCall::class.java))
    }
  }

  private fun getNumber() = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    viewModel.missedCall.nonNullObserve(this) { showInfo(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> closeWindow()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
    if (getNumber() == "" && BuildConfig.DEBUG) {
      loadTest()
    }
  }

  private fun showInfo(missedCall: UiMissedCallShow) {
    if (viewModel.isEventShowed) return

    val name: String
    if (missedCall.number.isNotEmpty()) {
      name = missedCall.name ?: missedCall.number
      missedCall.photo?.also {
        Picasso.get().load(it).into(binding.contactPhoto)
      } ?: run {
        binding.contactPhoto.setImageDrawable(missedCall.avatar)
      }
    } else {
      name = missedCall.number
      binding.contactPhoto.transparent()
    }

    binding.remText.setText(R.string.last_called)
    binding.reminderTime.text = missedCall.formattedTime

    binding.contactName.text = name
    binding.contactNumber.text = missedCall.number

    showMissedReminder(name)
    init()
  }

  private fun closeWindow() {
    discardNotification(id)
    removeFlags()
    finish()
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.removeObserver(viewModel)
    removeFlags()
  }

  override fun handleBackPress(): Boolean {
    discardMedia()
    if (prefs.isFoldingEnabled) {
      closeWindow()
    } else {
      toast(R.string.select_one_of_item)
    }
    return true
  }

  private fun makeCall() {
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      TelephonyUtil.makeCall(viewModel.getNumber() ?: "", this)
      removeMissed()
    }
  }

  private fun sendSMS() {
    val sendIntent = Intent(Intent.ACTION_VIEW)
    sendIntent.type = "vnd.android-dir/mms-sms"
    sendIntent.putExtra("address", viewModel.getNumber())
    startActivity(Intent.createChooser(sendIntent, "SMS:"))
    removeMissed()
  }

  private fun removeMissed() {
    viewModel.isEventShowed = true
    viewModel.deleteMissedCall()
  }

  private fun showMissedReminder(name: String?) {
    if (isScreenResumed) {
      return
    }
    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
    builder.setContentTitle(name)
    builder.setAutoCancel(false)
    builder.priority = NotificationCompat.PRIORITY_MAX
    if (prefs.isManualRemoveEnabled) {
      builder.setOngoing(false)
    } else {
      builder.setOngoing(true)
    }
    val appName: String
    if (Module.isPro) {
      appName = getString(R.string.app_name_pro)
      if (prefs.isLedEnabled) {
        builder.setLights(ledColor, 500, 1000)
      }
    } else {
      appName = getString(R.string.app_name)
    }
    builder.setContentText(appName)
    builder.setSmallIcon(R.drawable.ic_twotone_call_white)
    builder.color = colorOf(R.color.secondaryBlue)
    if (sound != null && !isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this)
        || SuperUtil.checkNotificationPermission(this)
        && prefs.isSoundInSilentModeEnabled)) {
      val soundUri = soundUri
      sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled, prefs.playbackDuration)
    }
    if (isVibrate) {
      val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
        longArrayOf(150, 86400000)
      } else {
        longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
      }
      builder.setVibrate(pattern)
    }
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup(groupName)
      builder.setGroupSummary(true)
    }
    notifier.notify(id, builder.build())
    if (isWear) {
      showWearNotification(appName)
    }
  }

  companion object {
    private const val ARG_TEST = "arg_test"
    private const val ARG_IS_ROTATED = "arg_rotated"
    private const val ARG_TEST_ITEM = "arg_test_item"

    fun mockTest(context: Context, missedCall: MissedCall) {
      val intent = Intent(context, MissedCallDialogActivity::class.java)
      intent.putExtra(ARG_TEST, true)
      intent.putExtra(ARG_TEST_ITEM, missedCall)
      context.startActivity(intent)
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      val resultIntent = Intent(context, MissedCallDialogActivity::class.java)
      resultIntent.putExtra(Constants.INTENT_ID, id)
      resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      return resultIntent
    }
  }
}
