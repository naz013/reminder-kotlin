package com.elementary.tasks.reminder.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.databinding.ActivityDialogReminderBinding
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.elementary.tasks.reminder.scheduling.ReminderBehaviorStrategy
import com.github.naz013.common.Permissions
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class ReminderDialog29Activity : BindingActivity<ActivityDialogReminderBinding>() {

  private val viewModel by viewModel<ReminderViewModel> { parametersOf(getId()) }
  private val jobScheduler by inject<JobScheduler>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val contactsReader by inject<ContactsReader>()
  private val prefs by inject<Prefs>()
  private val dialogues by inject<Dialogues>()
  private val notifier by inject<Notifier>()
  private val navigator by inject<Navigator>()
  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)

  private var shoppingAdapter = ShopListRecyclerAdapter()

  private val moreActionParams = MoreActionParams()

  private var mReminder: Reminder? = null
  private var reminderStrategy: ReminderBehaviorStrategy? = null
  private var isMockedTest = false
  private var isReminderShowed = false


  private val isRateDialogShowed: Boolean
    get() {
      var count = prefs.rateCount
      count++
      prefs.rateCount = count
      return count == 10
    }
  private val summary: String
    get() = mReminder?.summary ?: ""
  private val groupName: String
    get() = "reminder"

  private var mWasStopped = false

  private val mLocalReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action ?: ""
      Logger.d("onReceive: $action, ${getId()}")
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
    LocalBroadcastManager.getInstance(this).registerReceiver(
      mLocalReceiver,
      IntentFilter(ACTION_STOP_BG_ACTIVITY)
    )
  }

  private fun getId() = intentString(IntentKeys.INTENT_ID)

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
    Logger.d("initViewModel: ${getId()}")
    viewModel.reminder.observeForever(mReminderObserver)
    viewModel.resultEvent.observeEvent(this) { commands ->
      when (commands) {
        Commands.DELETED -> {
        }

        else -> {
        }
      }
    }
    viewModel.redirectEvent.observeEvent(this) { event ->
      handleRedirect(event)
    }
    viewModel.showToast.observeEvent(this) {
      Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
    viewModel.showFavoriteNotification.observeEvent(this) {
      showFavouriteNotification(it.text, it.notificationId)
    }
    lifecycle.addObserver(viewModel)
    if (getId() == "" && BuildConfig.DEBUG) {
      loadTest()
    }
  }

  private fun handleRedirect(event: ReminderViewModel.Redirect) {
    when (event) {
      is ReminderViewModel.Redirect.Edit -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.ReminderCreate,
            extras = Bundle().apply {
              putString(IntentKeys.INTENT_ID, event.id)
            },
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
        finish()
      }

      is ReminderViewModel.Redirect.Finish -> {
        finish()
      }

      is ReminderViewModel.Redirect.SendEmail -> {
        TelephonyUtil.sendMail(
          context = this,
          email = event.email,
          subject = event.subject,
          message = event.message,
          filePath = event.filePath
        )
        finish()
      }

      is ReminderViewModel.Redirect.OpenApp -> {
        TelephonyUtil.openApp(event.target, this)
        finish()
      }

      is ReminderViewModel.Redirect.OpenLink -> {
        TelephonyUtil.openLink(event.target, this)
        finish()
      }

      is ReminderViewModel.Redirect.SendSms -> {
        TelephonyUtil.sendSms(this, event.target, summary)
      }

      is ReminderViewModel.Redirect.MakeCall -> {
        permissionFlowDelegate.permissionFlow.askPermission(Permissions.CALL_PHONE) {
          TelephonyUtil.makeCall(event.target, this)
          finish()
        }
      }
    }
  }

  private fun loadTest() {
    isMockedTest = intentBoolean(ARG_TEST)
    if (isMockedTest) {
      val reminder = intentSerializable(ARG_TEST_ITEM, Reminder::class.java)
      if (reminder != null) showInfo(reminder)
    }
  }

  private fun showInfo(reminder: Reminder) {
    this.mReminder = reminder
    Logger.d("showInfo: ${dateTimeManager.logDateTime(reminder.eventTime)}")

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

    if (reminder.readType().hasCallAction()) {
      contactPhoto.visible()
      val conID = contactsReader.getIdFromNumber(reminder.target)

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
    } else if (reminder.readType().hasSmsAction()) {
      contactPhoto.visibility = View.VISIBLE
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
    } else if (reminder.readType().hasEmailAction()) {
      binding.remText.setText(R.string.e_mail)
      val conID = if (Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
        contactsReader.getIdFromMail(reminder.target)
      } else {
        0
      }
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
    } else if (reminder.readType().hasApplicationAction()) {
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
    } else if (reminder.readType().hasLinkAction()) {
      val label = summary + "\n\n" + reminder.target
      binding.remText.text = summary
      binding.remText.contentDescription = label
      binding.contactName.text = reminder.target
      binding.contactNumber.text = reminder.target
      binding.contactBlock.visible()
      binding.buttonAction.text = getString(R.string.open)
      binding.buttonAction.visible()
    } else if (reminder.readType().hasSubTasks()) {
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

    if (!reminder.readType().isGpsType()) {
      moreActionParams.canSnooze = true
    }
  }

  private fun canSkip(): Boolean {
    return reminderStrategy?.canSkip(mReminder!!) ?: false
  }

  private fun startAgain() {
    viewModel.startAgain()
  }

  private fun showFile() {
    val reminder = mReminder ?: return
    val path = reminder.attachmentFile
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      val uri = FileProvider.getUriForFile(
        /* context = */ this,
        /* authority = */ BuildConfig.APPLICATION_ID + ".provider",
        /* file = */ File(path)
      )
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
  }

  override fun handleBackPress(): Boolean {
    Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
    return true
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

  private fun editReminder() {
    viewModel.editReminder()
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
    viewModel.onActionButtonClick()
  }

  private fun delay(minutes: Int = prefs.snoozeTime) {
    viewModel.onSnoozeClicked(minutes)
  }

  private fun cancel() {
    viewModel.onCancelClicked()
  }

  private fun favourite() {
    viewModel.onFavoriteClicked()
  }

  private fun ok() {
    viewModel.onOkClicked()
  }

  private fun showFavouriteNotification(text: String, notificationId: Int) {
    val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
    builder.setContentTitle(text)
    val appName: String = if (BuildParams.isPro) {
      getString(R.string.app_name_pro)
    } else {
      getString(R.string.app_name)
    }
    builder.setContentText(appName)
    builder.setSmallIcon(R.drawable.ic_fluent_alert)
    builder.color = colorOf(R.color.secondaryBlue)
    val isWear = prefs.isWearEnabled
    if (isWear) {
      builder.setOnlyAlertOnce(true)
      builder.setGroup("GROUP")
      builder.setGroupSummary(true)
    }
    notifier.notify(notificationId, builder.build())
    if (isWear) {
      showWearNotification(
        text,
        appName,
        notificationId
      )
    }
  }

  private fun showWearNotification(
    text: String,
    secondaryText: String,
    notificationId: Int
  ) {
    Logger.d("showWearNotification: $secondaryText")
    val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
    wearableNotificationBuilder.setSmallIcon(R.drawable.ic_fluent_alert)
    wearableNotificationBuilder.setContentTitle(text)
    wearableNotificationBuilder.setContentText(secondaryText)
    wearableNotificationBuilder.color = colorOf(R.color.secondaryBlue)
    wearableNotificationBuilder.setOngoing(false)
    wearableNotificationBuilder.setOnlyAlertOnce(true)
    wearableNotificationBuilder.setGroup(groupName)
    wearableNotificationBuilder.setGroupSummary(false)
    notifier.notify(notificationId, wearableNotificationBuilder.build())
  }

  override fun onStop() {
    super.onStop()
    mWasStopped = true
  }

  companion object {

    private const val ARG_TEST = "arg_test"
    private const val ARG_TEST_ITEM = "arg_test_item"
    const val ACTION_STOP_BG_ACTIVITY = "action.STOP.BG"

    fun mockTest(context: Context, reminder: Reminder) {
      context.startActivity(ReminderDialog29Activity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(ARG_TEST_ITEM, reminder)
      }
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      return context.buildIntent(ReminderDialog29Activity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
    }
  }
}
