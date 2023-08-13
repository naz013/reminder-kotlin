package com.elementary.tasks.reminder.dialog

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseNotificationActivity
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.buildIntent
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.startActivity
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.ActivityDialogReminderBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File

class ReminderDialogActivity : BaseNotificationActivity<ActivityDialogReminderBinding>() {

  private val viewModel by viewModel<ReminderViewModel> { parametersOf(getId()) }
  private val jobScheduler by inject<JobScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val contactsReader by inject<ContactsReader>()

  private var shoppingAdapter = ShopListRecyclerAdapter()

  private val moreActionParams = MoreActionParams()

  private var mReminder: Reminder? = null
  private var mControl: EventControl? = null
  private var isMockedTest = false
  private var isReminderShowed = false
  override var isScreenResumed: Boolean = false
    private set

  override val groupName: String
    get() = "reminder"

  private val isAppType: Boolean
    get() {
      val reminder = mReminder ?: return false
      return Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) ||
        Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)
    }

  private val isAutoCallEnabled: Boolean
    get() {
      val reminder = mReminder ?: return false
      var has = prefs.isAutoCallEnabled
      if (!isGlobal) {
        has = reminder.auto
      }
      return has
    }

  private val isAutoLaunchEnabled: Boolean
    get() {
      val reminder = mReminder ?: return false
      var has = prefs.isAutoLaunchEnabled
      if (!isGlobal) {
        has = reminder.auto
      }
      return has
    }

  private val isRepeatEnabled: Boolean
    get() {
      val reminder = mReminder ?: return false
      var isRepeat = prefs.isNotificationRepeatEnabled
      if (!isGlobal) {
        isRepeat = reminder.repeatNotification
      }
      return isRepeat
    }

  private val isTtsEnabled: Boolean
    get() {
      val reminder = mReminder ?: return false
      var isTTS = prefs.isTtsEnabled
      if (!isGlobal) {
        isTTS = reminder.notifyByVoice
      }
      Timber.d("isTtsEnabled: $isTTS")
      return isTTS
    }

  override val melody: String
    get() = if (mReminder == null) "" else mReminder?.melodyPath ?: ""

  override val isVibrate: Boolean
    get() {
      val reminder = mReminder ?: return false
      var isVibrate = prefs.isVibrateEnabled
      if (!isGlobal) isVibrate = reminder.vibrate
      return isVibrate
    }

  override val summary: String
    get() = mReminder?.summary ?: ""

  override val uuId: String
    get() = mReminder?.uuId ?: ""

  override val id: Int
    get() = mReminder?.uniqueId ?: 2121

  override val ledColor: Int
    get() {
      val reminder = mReminder ?: return 0
      return if (Module.isPro) {
        if (reminder.color != -1) {
          LED.getLED(reminder.color)
        } else {
          LED.getLED(prefs.ledColor)
        }
      } else {
        LED.getLED(0)
      }
    }

  override val isGlobal: Boolean
    get() = mReminder != null && mReminder?.useGlobal ?: false

  override val isUnlockDevice: Boolean
    get() {
      val reminder = mReminder ?: return false
      var has = prefs.isDeviceUnlockEnabled
      if (!isGlobal) has = reminder.unlock
      return has
    }

  override val maxVolume: Int
    get() {
      val reminder = mReminder ?: return 25
      return if (!isGlobal && reminder.volume != -1) {
        reminder.volume
      } else {
        prefs.loudness
      }
    }

  override val priority: Int
    get() {
      val reminder = mReminder ?: return 0
      return reminder.priority
    }

  private val isRateDialogShowed: Boolean
    get() {
      var count = prefs.rateCount
      count++
      prefs.rateCount = count
      return count == 10
    }
  private var mWasStopped = false
  private val mLocalReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action ?: ""
      val mId = getId()
      Timber.d("onReceive: $action, $mId")
      if (mWasStopped && action == ACTION_STOP_BG_ACTIVITY && uuId == mId) {
        finish()
      }
    }
  }

  override fun inflateBinding() = ActivityDialogReminderBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    drawBehindSystemBars(binding.rootView)

    isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)

    binding.container.gone()
    binding.progressOverlay.gone()
    binding.progressOverlay.setOnTouchListener { v, _ -> v.performClick() }
    binding.subjectContainer.gone()
    binding.contactBlock.transparent()

    initButtons()

    if (savedInstanceState != null) {
      isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
    }

    initViewModel()
    LocalBroadcastManager.getInstance(this)
      .registerReceiver(mLocalReceiver, IntentFilter(ACTION_STOP_BG_ACTIVITY))
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_IS_ROTATED, true)
    super.onSaveInstanceState(outState)
  }

  private fun initButtons() {
    binding.buttonOk.setOnClickListener { ok() }
    binding.buttonMore.setOnClickListener { showMoreDialog(getActions()) }
    binding.buttonAction.setOnClickListener { call() }
  }

  private fun getActions(): Map<String, () -> Unit> {
    val map = mutableMapOf<String, () -> Unit>()
    if (moreActionParams.canStartAgain) {
      map[getString(R.string.start_again)] = { startAgain() }
    }
    if (moreActionParams.canCancel) {
      map[getString(R.string.cancel)] = { cancel() }
    }
    if (moreActionParams.canOpenAttachment) {
      map[getString(R.string.acc_open_attachment)] = { showFile() }
    }
    if (moreActionParams.canSnooze) {
      map[getString(R.string.acc_button_snooze)] = { delay() }
      map[getString(R.string.acc_button_snooze_for)] = {
        jobScheduler.cancelReminder(mReminder?.uniqueId ?: 0)
        showDialog()
        discardNotification(id)
      }
    }
    if (moreActionParams.canMoveToStatusBar) {
      map[getString(R.string.acc_button_move_to_status_bar)] = { favourite() }
    }
    if (moreActionParams.canEdit) {
      map[getString(R.string.acc_button_edit)] = { editReminder() }
    }
    return map
  }

  private fun showMoreDialog(actionMap: Map<String, () -> Unit>) {
    val keys = actionMap.keys.toTypedArray()
    dialogues.getMaterialDialog(this)
      .setItems(keys) { dialog, which ->
        dialog.dismiss()
        actionMap[keys[which]]?.invoke()
      }
      .create()
      .show()
  }

  override fun onResume() {
    super.onResume()
    if (isRateDialogShowed) {
      showRateDialog()
    }
  }

  private fun showRateDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.rate)
    builder.setMessage(R.string.can_you_rate_this_application)
    builder.setPositiveButton(R.string.rate) { dialogInterface, _ ->
      dialogInterface.dismiss()
      SuperUtil.launchMarket(this)
    }
    builder.setNegativeButton(R.string.never) { dialogInterface, _ -> dialogInterface.dismiss() }
    builder.setNeutralButton(R.string.later) { dialogInterface, _ ->
      dialogInterface.dismiss()
      prefs.rateCount = 0
    }
    builder.create().show()
  }

  private val mReminderObserver: Observer<in Reminder?> = Observer { reminder ->
    if (reminder != null) {
      if (!isReminderShowed) showInfo(reminder)
    }
  }

  private fun initViewModel() {
    Timber.d("initViewModel: ${getId()}")
    viewModel.reminder.observeForever(mReminderObserver)
    viewModel.result.observe(this) { commands ->
      if (commands != null) {
        when (commands) {
          Commands.DELETED -> {
          }

          else -> {
          }
        }
      }
    }
    lifecycle.addObserver(viewModel)
    if (getId() == "" && BuildConfig.DEBUG) {
      loadTest()
    }
  }

  private fun loadTest() {
    isMockedTest = intentBoolean(ARG_TEST)
    if (isMockedTest) {
      val reminder = intentParcelable(ARG_TEST_ITEM, Reminder::class.java)
      if (reminder != null) showInfo(reminder)
    }
  }

  private fun showInfo(reminder: Reminder) {
    this.mReminder = reminder
    if (!isMockedTest) {
      this.mControl = get<EventControlFactory>().getController(reminder)
    }
    Timber.d("showInfo: ${dateTimeManager.logDateTime(reminder.eventTime)}")

    moreActionParams.canOpenAttachment = reminder.attachmentFile != ""

    val contactPhoto = binding.contactPhoto
    contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
    contactPhoto.gone()

    binding.todoList.layoutManager = LinearLayoutManager(this)
    binding.todoList.gone()

    binding.remText.text = ""

    if (!TextUtils.isEmpty(reminder.eventTime) && !Reminder.isGpsType(reminder.type)) {
      binding.reminderTime.text = dateTimeManager.getFullDateTime(reminder.eventTime)
      binding.timeBlock.visible()
    } else {
      binding.timeBlock.gone()
    }

    if (Reminder.isKind(reminder.type, Reminder.Kind.CALL)) {
      contactPhoto.visible()
      val conID = if (Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
        contactsReader.getIdFromNumber(reminder.target)
      } else {
        0L
      }

      val name = contactsReader.getNameFromNumber(reminder.target)
      binding.remText.setText(R.string.make_call)
      val userTitle = (name ?: "") + "\n" + reminder.target

      val photo = contactsReader.getPhotoBitmap(conID)
      if (photo != null) {
        contactPhoto.setImageBitmap(photo)
      } else {
        BitmapUtils.imageFromName(name ?: reminder.target) {
          contactPhoto.setImageDrawable(it)
        }
      }

      binding.contactInfo.text = userTitle
      binding.contactInfo.contentDescription = userTitle
      binding.messageView.text = summary
      binding.messageView.contentDescription = summary

      binding.contactName.text = name
      binding.contactNumber.text = reminder.target

      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.make_call)
      binding.buttonAction.visibleGone(prefs.isTelephonyAllowed)
      binding.container.visible()
    } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
      contactPhoto.visible()
      val conID = contactsReader.getIdFromNumber(reminder.target)
      val name = contactsReader.getNameFromNumber(reminder.target)
      binding.remText.setText(R.string.send_sms)
      val userInfo = (name ?: "") + "\n" + reminder.target
      binding.contactInfo.text = userInfo
      binding.contactInfo.contentDescription = userInfo
      binding.messageView.text = summary
      binding.messageView.contentDescription = summary

      val photo = contactsReader.getPhotoBitmap(conID)
      if (photo != null) {
        contactPhoto.setImageBitmap(photo)
      } else {
        BitmapUtils.imageFromName(name ?: reminder.target) {
          contactPhoto.setImageDrawable(it)
        }
      }

      binding.contactName.text = name
      binding.contactNumber.text = reminder.target
      binding.buttonAction.text = getString(R.string.send)
      binding.buttonAction.visibleGone(prefs.isTelephonyAllowed)
      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.send)
      binding.buttonAction.contentDescription = getString(R.string.acc_button_send_message)
      binding.container.visible()
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL)) {
      binding.remText.setText(R.string.e_mail)
      val conID = contactsReader.getIdFromMail(reminder.target)
      if (conID != 0L) {
        val photo = contactsReader.getPhotoBitmap(conID)
        if (photo != null) {
          contactPhoto.setImageBitmap(photo)
        } else {
          contactPhoto.gone()
        }
        val name = contactsReader.getNameFromMail(reminder.target)
        val userInfo = (name ?: "") + "\n" + reminder.target
        binding.contactInfo.text = userInfo
        binding.contactInfo.contentDescription = userInfo
        binding.contactName.text = name
        binding.contactNumber.text = reminder.target
      } else {
        binding.contactInfo.text = reminder.target
        binding.contactInfo.contentDescription = reminder.target
        binding.contactName.text = reminder.target
        binding.contactNumber.text = reminder.target
      }
      binding.messageView.text = summary
      binding.messageView.contentDescription = summary
      binding.subjectView.text = reminder.subject
      binding.subjectView.contentDescription = reminder.subject
      binding.container.visible()
      binding.subjectContainer.visible()
      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.send)
      binding.buttonAction.visible()
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
      val packageManager = packageManager
      var applicationInfo: ApplicationInfo? = null
      try {
        applicationInfo = packageManager.getApplicationInfo(reminder.target, 0)
      } catch (ignored: PackageManager.NameNotFoundException) {
      }

      val nameA = if (applicationInfo != null) {
        packageManager.getApplicationLabel(applicationInfo).toString()
      } else {
        "???"
      }
      val label = summary + "\n\n" + nameA + "\n" + reminder.target
      binding.remText.text = summary
      binding.remText.contentDescription = label
      binding.contactName.text = nameA
      binding.contactNumber.text = reminder.target
      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.open)
      binding.buttonAction.visible()
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)) {
      val label = summary + "\n\n" + reminder.target
      binding.remText.text = summary
      binding.remText.contentDescription = label
      binding.contactName.text = reminder.target
      binding.contactNumber.text = reminder.target
      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.open)
      binding.buttonAction.visible()
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP)) {
      binding.remText.text = summary
      binding.remText.contentDescription = summary
      binding.contactBlock.transparent()
      binding.buttonAction.gone()
      loadData()
    } else {
      binding.remText.text = summary
      binding.remText.contentDescription = summary
      binding.contactBlock.transparent()
      binding.buttonAction.gone()
    }

    moreActionParams.canStartAgain = Reminder.isBase(reminder.type, Reminder.BY_TIME)
    moreActionParams.canCancel = canSkip()

    if (!Reminder.isGpsType(reminder.type)) {
      moreActionParams.canSnooze = true
    }

    init()

    if (Reminder.isKind(reminder.type, Reminder.Kind.CALL) && isAutoCallEnabled) {
      call()
    } else if (isAppType && isAutoLaunchEnabled) {
      openApplication(reminder)
    } else {
      showNotification()
      if (isRepeatEnabled) {
        jobScheduler.scheduleReminderRepeat(reminder)
      }
      if (isTtsEnabled) {
        startTts()
      }
    }
  }

  private fun canSkip(): Boolean {
    return mControl?.canSkip() ?: false
  }

  private fun startAgain() {
    doActions({
      it.next()
      it.onOff()
    }, { finish() })
  }

  private fun showFile() {
    val reminder = mReminder ?: return
    val path = reminder.attachmentFile
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      val uri =
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(path))
      intent.data = uri
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
      startActivity(intent)
    } catch (e: Exception) {
      toast(R.string.cant_find_app_for_that_file_type)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver)
    viewModel.reminder.removeObserver(mReminderObserver)
    lifecycle.removeObserver(viewModel)
    removeFlags()
  }

  override fun handleBackPress(): Boolean {
    discardMedia()
    if (prefs.isFoldingEnabled) {
      jobScheduler.cancelReminder(mReminder?.uniqueId ?: 0)
      removeFlags()
      finish()
    } else {
      toast(R.string.select_one_of_item)
    }
    return true
  }

  private fun openApplication(reminder: Reminder) {
    if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
      TelephonyUtil.openApp(reminder.target, this)
    } else {
      TelephonyUtil.openLink(reminder.target, this)
    }
    finish()
  }

  private fun cancelTasks() {
    discardNotification(id)
  }

  private fun showDialog() {
    val items = arrayOf<CharSequence>(
      String.format(getString(R.string.x_minutes), 5.toString()),
      String.format(getString(R.string.x_minutes), 10.toString()),
      String.format(getString(R.string.x_minutes), 15.toString()),
      String.format(getString(R.string.x_minutes), 30.toString()),
      String.format(getString(R.string.x_minutes), 45.toString()),
      String.format(getString(R.string.x_minutes), 60.toString()),
      String.format(getString(R.string.x_minutes), 90.toString()),
      String.format(getString(R.string.x_hours), 2.toString()),
      String.format(getString(R.string.x_hours), 6.toString()),
      String.format(getString(R.string.x_hours), 24.toString()),
      String.format(getString(R.string.x_days), 2.toString()),
      String.format(getString(R.string.x_days), 7.toString())
    )
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(getString(R.string.choose_time))
    builder.setItems(items) { dialog, item1 ->
      dialog.dismiss()
      var x = 0
      when (item1) {
        0 -> x = 5
        1 -> x = 10
        2 -> x = 15
        3 -> x = 30
        4 -> x = 45
        5 -> x = 60
        6 -> x = 90
        7 -> x = 120
        8 -> x = 60 * 6
        9 -> x = 60 * 24
        10 -> x = 60 * 24 * 2
        11 -> x = 60 * 24 * 7
      }
      delay(x)
    }
    builder.create().show()
  }

  private fun sendSMS() {
    val reminder = mReminder ?: return
    if (TextUtils.isEmpty(summary)) return
    TelephonyUtil.sendSms(this, reminder.target, summary)
  }

  private fun showNotification() {
    if (isMockedTest || isReminderShowed) return
    if (!isTtsEnabled) {
      showReminderNotification()
    } else {
      showTTSNotification()
    }
  }

  private fun editReminder() {
    doActions({ it.stop() }, {
      PinLoginActivity.openLogged(this, CreateReminderActivity::class.java) {
        putExtra(Constants.INTENT_ID, it.uuId)
      }
      finish()
    })
  }

  private fun loadData() {
    val reminder = mReminder ?: return
    shoppingAdapter = ShopListRecyclerAdapter()
    shoppingAdapter.listener = object : ShopListRecyclerAdapter.ActionListener {
      override fun onItemCheck(position: Int, isChecked: Boolean) {
        val item = shoppingAdapter.getItem(position)
        item.isChecked = !item.isChecked
        shoppingAdapter.updateData()
        reminder.shoppings = shoppingAdapter.data
        viewModel.saveReminder(reminder)
      }

      override fun onItemDelete(position: Int) {
        shoppingAdapter.delete(position)
        reminder.shoppings = shoppingAdapter.data
        viewModel.saveReminder(reminder)
      }
    }
    shoppingAdapter.data = reminder.shoppings
    binding.todoList.adapter = shoppingAdapter
    binding.todoList.visible()
  }

  private fun call() {
    doActions({ it.next() }, {
      when {
        Reminder.isKind(it.type, Reminder.Kind.SMS) -> sendSMS()
        isAppType -> openApplication(it)
        Reminder.isSame(it.type, Reminder.BY_DATE_EMAIL) -> TelephonyUtil.sendMail(
          context = this,
          email = it.target,
          subject = it.subject,
          message = summary,
          filePath = it.attachmentFile
        )

        else -> makeCall()
      }
      if (!Reminder.isKind(it.type, Reminder.Kind.SMS)) {
        finish()
      }
    })
  }

  private fun makeCall() {
    val reminder = mReminder ?: return
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      TelephonyUtil.makeCall(reminder.target, this)
    }
  }

  private fun delay(minutes: Int = prefs.snoozeTime) {
    doActions({ it.setDelay(minutes) }, {
      toast(R.string.reminder_snoozed)
      finish()
    })
  }

  private fun cancel() {
    doActions({ it.stop() }, { finish() })
  }

  private fun favourite() {
    doActions({ it.next() }, {
      showFavouriteNotification()
      finish()
    })
  }

  private fun ok() {
    doActions({ it.next() }, { finish() })
  }

  private fun showFavouriteNotification() {
    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
    builder.setContentTitle(summary)
    val appName: String = if (Module.isPro) {
      getString(R.string.app_name_pro)
    } else {
      getString(R.string.app_name)
    }
    builder.setContentText(appName)
    builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
    builder.color = colorOf(R.color.secondaryBlue)
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("GROUP")
      builder.setGroupSummary(true)
    }
    notifier.notify(id, builder.build())
    if (isWear) {
      showWearNotification(appName)
    }
  }

  private fun showReminderNotification() {
    Timber.d("showReminderNotification: $id")

    val notificationIntent = getLaunchIntent(this, uuId)
    val intent = PendingIntentWrapper.getActivity(
      this,
      id,
      notificationIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )

    val builder: NotificationCompat.Builder
    if (isScreenResumed) {
      builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
      builder.priority = NotificationCompat.PRIORITY_LOW
    } else {
      builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
      builder.priority = priority()
      val playDefaultMelody = !SuperUtil.isDoNotDisturbEnabled(this) ||
        (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled)
      if (playDefaultMelody) {
        val soundUri = soundUri
        Timber.d("showReminderNotification: $soundUri")
        sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled, prefs.playbackDuration)
      }
      if (prefs.isVibrateEnabled) {
        val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
          longArrayOf(150, 86400000)
        } else {
          longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
        }
        builder.setVibrate(pattern)
      }
    }
    builder.setContentTitle(summary)
    builder.setContentIntent(intent)
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
    builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
    builder.color = colorOf(R.color.secondaryBlue)
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

  private fun priority(): Int {
    return when (mReminder?.priority ?: 2) {
      0 -> NotificationCompat.PRIORITY_MIN
      1 -> NotificationCompat.PRIORITY_LOW
      2 -> NotificationCompat.PRIORITY_DEFAULT
      3 -> NotificationCompat.PRIORITY_HIGH
      else -> NotificationCompat.PRIORITY_MAX
    }
  }

  private fun showTTSNotification() {
    Timber.d("showTTSNotification: ")
    val builder: NotificationCompat.Builder
    if (isScreenResumed) {
      builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
      builder.priority = NotificationCompat.PRIORITY_LOW
    } else {
      builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
      builder.priority = priority()
      val playDefaultMelody = !SuperUtil.isDoNotDisturbEnabled(this) ||
        (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled)
      if (playDefaultMelody) {
        playDefaultMelody()
      }
      if (prefs.isVibrateEnabled) {
        val pattern: LongArray = if (prefs.isInfiniteVibrateEnabled) {
          longArrayOf(150, 86400000)
        } else {
          longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
        }
        builder.setVibrate(pattern)
      }
    }
    builder.setContentTitle(summary)

    val notificationIntent = getLaunchIntent(this, uuId)
    val intent = PendingIntentWrapper.getActivity(
      this,
      id,
      notificationIntent,
      PendingIntent.FLAG_CANCEL_CURRENT
    )

    builder.setContentIntent(intent)
    builder.setAutoCancel(false)
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
    builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
    builder.color = colorOf(R.color.secondaryBlue)
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

  override fun onStop() {
    super.onStop()
    mWasStopped = true
  }

  private fun doActions(onControl: (EventControl) -> Unit, onEnd: (Reminder) -> Unit) {
    isReminderShowed = true
    viewModel.reminder.removeObserver(mReminderObserver)
    val reminder = mReminder
    if (reminder == null) {
      removeFlags()
      cancelTasks()
      finish()
      return
    }
    jobScheduler.cancelReminder(reminder.uniqueId)
    val control = mControl
    launchDefault {
      if (control != null) {
        onControl.invoke(control)
      }
      withUIContext {
        removeFlags()
        cancelTasks()
        onEnd.invoke(reminder)
      }
    }
  }

  companion object {

    private const val ARG_TEST = "arg_test"
    private const val ARG_TEST_ITEM = "arg_test_item"
    private const val ARG_IS_ROTATED = "arg_rotated"
    const val ACTION_STOP_BG_ACTIVITY = "action.STOP.BG"

    fun mockTest(context: Context, reminder: Reminder) {
      context.startActivity(ReminderDialogActivity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(ARG_TEST_ITEM, reminder)
      }
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      return context.buildIntent(ReminderDialogActivity::class.java) {
        putExtra(Constants.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
    }
  }
}
