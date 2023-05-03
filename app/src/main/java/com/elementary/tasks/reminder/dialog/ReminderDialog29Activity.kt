package com.elementary.tasks.reminder.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.contacts.Contacts
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.ActivityDialogReminderBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File

class ReminderDialog29Activity : BindingActivity<ActivityDialogReminderBinding>() {

  private val viewModel by viewModel<ReminderViewModel> { parametersOf(getId()) }
  private val jobScheduler by inject<JobScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val permissionFlow = PermissionFlow(this, dialogues)

  private var shoppingAdapter = ShopListRecyclerAdapter()

  private val moreActionParams = MoreActionParams()

  private var mReminder: Reminder? = null
  private var mControl: EventControl? = null
  private var mWakeLock: PowerManager.WakeLock? = null
  private var isMockedTest = false
  private var isReminderShowed = false
  private val isAppType: Boolean
    get() {
      val reminder = mReminder ?: return false
      return Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)
        || Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)
    }

  private val isRateDialogShowed: Boolean
    get() {
      var count = prefs.rateCount
      count++
      prefs.rateCount = count
      return count == 10
    }
  private val isGlobal: Boolean
    get() = mReminder != null && mReminder?.useGlobal ?: false
  private val isUnlockDevice: Boolean
    get() {
      val reminder = mReminder ?: return false
      var has = prefs.isDeviceUnlockEnabled
      if (!isGlobal) has = reminder.unlock
      return has
    }
  private val id: Int
    get() = mReminder?.uniqueId ?: 2121
  private val summary: String
    get() = mReminder?.summary ?: ""
  private val groupName: String
    get() = "reminder"
  private val priority: Int
    get() {
      val reminder = mReminder ?: return 0
      return reminder.priority
    }

  private var mWasStopped = false

  private val mLocalReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action ?: ""
      Timber.d("onReceive: $action, ${getId()}")
      if (mWasStopped && action == ACTION_STOP_BG_ACTIVITY && mReminder?.uuId == getId()) {
        finish()
      }
    }
  }

  override fun inflateBinding() = ActivityDialogReminderBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    drawBehindSystemBars(binding.rootView)

    binding.container.gone()
    binding.progressOverlay.gone()
    binding.progressOverlay.setOnTouchListener { v, _ -> v.performClick() }
    binding.subjectContainer.gone()
    binding.contactBlock.transparent()

    initButtons()
    initViewModel()
    LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, IntentFilter(
      ACTION_STOP_BG_ACTIVITY
    ))
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  private fun init() {
    setUpScreenOptions()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (MotionEvent.ACTION_DOWN == event.action) {
      discardMedia()
    }
    return super.onTouchEvent(event)
  }

  private fun canUnlockScreen(): Boolean {
    return if (isGlobal) {
      if (prefs.isDeviceUnlockEnabled) {
        priority >= prefs.unlockPriority
      } else {
        false
      }
    } else {
      isUnlockDevice
    }
  }

  private fun setUpScreenOptions() {
    Timber.d("setUpScreenOptions: ${canUnlockScreen()}")
    if (canUnlockScreen()) {
      SuperUtil.turnScreenOn(this, window)
      SuperUtil.unlockOn(this, window)
    }
    mWakeLock = SuperUtil.wakeDevice(this)
  }

  private fun removeFlags() {
    if (canUnlockScreen()) {
      SuperUtil.unlockOff(this, window)
      SuperUtil.turnScreenOff(this, window, mWakeLock)
    }
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
        jobScheduler.cancelReminder(mReminder?.uuId ?: "")
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
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> {
        }

        else -> {
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
      contactPhoto.visibility = View.VISIBLE
      val conID = Contacts.getIdFromNumber(reminder.target, this)

      val name = Contacts.getNameFromNumber(reminder.target, this)
      binding.remText.setText(R.string.make_call)
      val userTitle = (name ?: "") + "\n" + reminder.target

      val photo = Contacts.getPhoto(conID)
      if (photo != null) {
        Picasso.get().load(photo).into(contactPhoto)
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

      binding.contactBlock.visibility = View.VISIBLE
      binding.buttonAction.text = getString(R.string.make_call)
      binding.buttonAction.visibleGone(prefs.isTelephonyAllowed)
      binding.container.visibility = View.VISIBLE
    } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
      contactPhoto.visibility = View.VISIBLE
      val conID = Contacts.getIdFromNumber(reminder.target, this)
      val name = Contacts.getNameFromNumber(reminder.target, this)
      binding.remText.setText(R.string.send_sms)
      val userInfo = (name ?: "") + "\n" + reminder.target
      binding.contactInfo.text = userInfo
      binding.contactInfo.contentDescription = userInfo
      binding.messageView.text = summary
      binding.messageView.contentDescription = summary

      val photo = Contacts.getPhoto(conID)
      if (photo != null) {
        Picasso.get().load(photo).into(contactPhoto)
      } else {
        BitmapUtils.imageFromName(name ?: reminder.target) {
          contactPhoto.setImageDrawable(it)
        }
      }

      binding.contactName.text = name
      binding.contactNumber.text = reminder.target
      binding.buttonAction.text = getString(R.string.send)
      binding.buttonAction.visibleGone(prefs.isTelephonyAllowed)

      binding.contactBlock.visibility = View.VISIBLE
      binding.buttonAction.text = getString(R.string.send)
      binding.buttonAction.contentDescription = getString(R.string.acc_button_send_message)

      binding.container.visibility = View.VISIBLE
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL)) {
      binding.remText.setText(R.string.e_mail)
      val conID = if (Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
        Contacts.getIdFromMail(reminder.target, this)
      } else {
        0
      }
      if (conID != 0L) {
        val photo = Contacts.getPhoto(conID)
        if (photo != null) {
          Picasso.get().load(photo).into(contactPhoto)
        } else {
          contactPhoto.visibility = View.GONE
        }
        val name = Contacts.getNameFromMail(reminder.target, this)
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
      binding.container.visibility = View.VISIBLE
      binding.subjectContainer.visibility = View.VISIBLE
      binding.contactBlock.visibility = View.VISIBLE
      binding.buttonAction.text = getString(R.string.send)
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
      val packageManager = packageManager
      var applicationInfo: ApplicationInfo? = null
      try {
        applicationInfo = packageManager.getApplicationInfo(reminder.target, 0)
      } catch (ignored: PackageManager.NameNotFoundException) {
      }

      val nameA = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
      val label = summary + "\n\n" + nameA + "\n" + reminder.target
      binding.remText.text = summary
      binding.remText.contentDescription = label
      binding.contactName.text = nameA
      binding.contactNumber.text = reminder.target
      binding.contactBlock.visibility = View.VISIBLE
      binding.buttonAction.text = getString(R.string.open)
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)) {
      val label = summary + "\n\n" + reminder.target
      binding.remText.text = summary
      binding.remText.contentDescription = label
      binding.contactName.text = reminder.target
      binding.contactNumber.text = reminder.target
      binding.contactBlock.visibility = View.VISIBLE
      binding.buttonAction.text = getString(R.string.open)
    } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP)) {
      binding.remText.text = summary
      binding.remText.contentDescription = summary
      binding.contactBlock.visibility = View.INVISIBLE
      loadData()
    } else {
      binding.remText.text = summary
      binding.remText.contentDescription = summary
      binding.contactBlock.visibility = View.INVISIBLE
    }

    moreActionParams.canStartAgain = Reminder.isBase(reminder.type, Reminder.BY_TIME)
    moreActionParams.canCancel = canSkip()

    if (!Reminder.isGpsType(reminder.type)) {
      moreActionParams.canSnooze = true
    }

    init()
  }

  private fun canSkip(): Boolean {
    return mControl?.canSkip() ?: false
  }

  private fun startAgain() {
    discardNotification(id)
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
      val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(path))
      intent.data = uri
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
      startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(this, R.string.cant_find_app_for_that_file_type, Toast.LENGTH_LONG).show()
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
      jobScheduler.cancelReminder(mReminder?.uuId ?: "")
      removeFlags()
      finish()
    } else {
      Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
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

  private fun editReminder() {
    discardNotification(id)
    doActions({ it.stop() }, {
      PinLoginActivity.openLogged(this, Intent(this, CreateReminderActivity::class.java)
        .putExtra(Constants.INTENT_ID, it.uuId))
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
    binding.todoList.visibility = View.VISIBLE
  }

  private fun call() {
    discardNotification(id)
    doActions({ it.next() }, {
      when {
        Reminder.isKind(it.type, Reminder.Kind.SMS) -> sendSMS()
        isAppType -> openApplication(it)
        Reminder.isSame(it.type, Reminder.BY_DATE_EMAIL) -> TelephonyUtil.sendMail(this, it.target,
          it.subject, summary, it.attachmentFile)
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
    discardNotification(id)
    doActions({ it.setDelay(minutes) }, {
      Toast.makeText(this, getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show()
      finish()
    })
  }

  private fun cancel() {
    discardNotification(id)
    doActions({ it.stop() }, { finish() })
  }

  private fun favourite() {
    discardNotification(id)
    doActions({ it.next() }, {
      showFavouriteNotification()
      finish()
    })
  }

  private fun ok() {
    discardNotification(id)
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

  private fun discardNotification(id: Int) {
    Timber.d("discardNotification: $id")
    discardMedia()
    notifier.cancel(id)
  }

  private fun discardMedia() {
    ContextCompat.startForegroundService(this,
      EventOperationalService.getIntent(this, mReminder?.uuId ?: "",
        EventOperationalService.TYPE_REMINDER,
        EventOperationalService.ACTION_STOP,
        id))
  }

  private fun showWearNotification(secondaryText: String) {
    Timber.d("showWearNotification: $secondaryText")
    val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
    wearableNotificationBuilder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
    wearableNotificationBuilder.setContentTitle(summary)
    wearableNotificationBuilder.setContentText(secondaryText)
    wearableNotificationBuilder.color = colorOf(R.color.secondaryBlue)
    wearableNotificationBuilder.setOngoing(false)
    wearableNotificationBuilder.setOnlyAlertOnce(true)
    wearableNotificationBuilder.setGroup(groupName)
    wearableNotificationBuilder.setGroupSummary(false)
    notifier.notify(id, wearableNotificationBuilder.build())
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
    jobScheduler.cancelReminder(reminder.uuId)
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
    const val ACTION_STOP_BG_ACTIVITY = "action.STOP.BG"

    @RequiresApi(29)
    fun mockTest(context: Context, reminder: Reminder) {
      val intent = Intent(context, ReminderDialog29Activity::class.java)
      intent.putExtra(ARG_TEST, true)
      intent.putExtra(ARG_TEST_ITEM, reminder)
      context.startActivity(intent)
    }

    @RequiresApi(29)
    fun getLaunchIntent(context: Context, id: String): Intent {
      val resultIntent = Intent(context, ReminderDialog29Activity::class.java)
      resultIntent.putExtra(Constants.INTENT_ID, id)
      resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      return resultIntent
    }
  }
}
