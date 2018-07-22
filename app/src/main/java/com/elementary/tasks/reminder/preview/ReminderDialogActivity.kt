package com.elementary.tasks.reminder.preview

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.RepeatNotificationReceiver
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter
import com.elementary.tasks.reminder.work.BackupReminderTask
import kotlinx.android.synthetic.main.activity_reminder_dialog.*
import java.io.File
import java.io.IOException

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
class ReminderDialogActivity : BaseNotificationActivity() {

    private lateinit var viewModel: ReminderViewModel

    private var shoppingAdapter: ShopListRecyclerAdapter = ShopListRecyclerAdapter()

    private val repeater = RepeatNotificationReceiver()
    private var sentReceiver: BroadcastReceiver? = null

    private var mReminder: Reminder? = null
    private var mControl: EventControl? = null
    override var isScreenResumed: Boolean = false
        private set

    private val isAppType: Boolean
        get() {
            val reminder = mReminder ?: return false
            return Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)
                    || Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)
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

    private val isAutoEnabled: Boolean
        get() {
            val reminder = mReminder ?: return false
            var has = prefs.isAutoSmsEnabled
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
            LogUtil.d(TAG, "isTtsEnabled: $isTTS")
            return isTTS
        }

    override val melody: String
        get() = if (mReminder == null) "" else mReminder!!.melodyPath

    override val isVibrate: Boolean
        get() {
            val reminder = mReminder ?: return false
            var isVibrate = prefs.isVibrateEnabled
            if (!isGlobal) isVibrate = reminder.vibrate
            return isVibrate
        }

    override val summary: String
        get() = if (mReminder == null) "" else mReminder!!.summary

    override val uuId: String
        get() = if (mReminder == null) "" else mReminder!!.uuId

    override val id: Int
        get() = if (mReminder == null) 0 else mReminder!!.uniqueId

    override val ledColor: Int
        get() {
            val reminder = mReminder ?: return 0
            return if (Module.isPro) {
                if (reminder.color != -1) {
                    LED.getLED(reminder.color)
                } else {
                    LED.getLED(prefs.ledColor)
                }
            } else LED.getLED(0)
        }

    override val isAwakeDevice: Boolean
        get() {
            val reminder = mReminder ?: return false
            var has = prefs.isDeviceAwakeEnabled
            if (!isGlobal) has = reminder.awake
            return has
        }

    override val isGlobal: Boolean
        get() = mReminder != null && mReminder!!.useGlobal

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
            return if (!isGlobal && reminder.volume != -1)
                reminder.volume
            else
                prefs.loudness
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        val id = intent.getIntExtra(Constants.INTENT_ID, 0)

        setContentView(R.layout.activity_reminder_dialog)

        card.setCardBackgroundColor(themeUtil.cardStyle)
        if (Module.isLollipop) card.cardElevation = Configs.CARD_ELEVATION
        container.visibility = View.GONE
        subjectContainer.visibility = View.GONE
        loadImage(bgImage)
        colorify(buttonOk, buttonCall, buttonCancel, buttonDelay,
                buttonDelayFor, buttonNotification, buttonEdit)
        setTextDrawable(buttonDelay, prefs.snoozeTime.toString())
        setTextDrawable(buttonDelayFor, "...")
        buttonOk.setImageResource(R.drawable.ic_done_black_24dp)
        buttonEdit.setImageResource(R.drawable.ic_create_black_24dp)
        buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp)
        button_refresh.hide()
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp)
        buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp)

        initViewModel(id)
    }

    private fun initViewModel(id: Int) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this,Observer { reminder ->
            if (reminder != null) {
                showInfo(reminder)
            } else {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()

            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                }
            }
        })
    }

    private fun showInfo(reminder: Reminder?) {
        this.mReminder = reminder
        this.mControl = EventControlFactory.getController(reminder!!)
        LogUtil.d(TAG, "showInfo: " + TimeUtil.getFullDateTime(reminder.eventTime))
        if (reminder.attachmentFile != "") showAttachmentButton()

        val contactPhoto = contactPhoto
        contactPhoto.borderColor = themeUtil.getColor(themeUtil.colorPrimary())
        contactPhoto.visibility = View.GONE

        todoList.layoutManager = LinearLayoutManager(this)
        todoList.visibility = View.GONE

        remText.text = ""

        if (!TextUtils.isEmpty(reminder.eventTime) && Reminder.isGpsType(reminder.type)) {
            reminder_time.text = TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime),
                    Prefs.getInstance(this).is24HourFormatEnabled, false)
            reminder_time.visibility = View.VISIBLE
        } else {
            reminder_time.visibility = View.GONE
        }

        if (Reminder.isKind(reminder.type, Reminder.Kind.CALL) || Reminder.isSame(reminder.type, Reminder.BY_SKYPE_VIDEO)) {
            if (!Reminder.isBase(reminder.type, Reminder.BY_SKYPE)) {
                contactPhoto.visibility = View.VISIBLE
                val conID = Contacts.getIdFromNumber(reminder.target, this).toLong()
                val photo = Contacts.getPhoto(conID)
                if (photo != null)
                    contactPhoto.setImageURI(photo)
                else
                    contactPhoto.visibility = View.GONE
                val name = Contacts.getNameFromNumber(reminder.target, this)
                remText.setText(R.string.make_call)
                val userTitle = (name ?: "") + "\n" + reminder.target
                contactInfo.text = userTitle
                contactInfo.contentDescription = userTitle
                messageView.text = summary
                messageView.contentDescription = summary
            } else {
                if (Reminder.isSame(reminder.type, Reminder.BY_SKYPE_VIDEO)) {
                    remText.setText(R.string.video_call)
                } else {
                    remText.setText(R.string.skype_call)
                }
                contactInfo.text = reminder.target
                contactInfo.contentDescription = reminder.target
                messageView.text = summary
                messageView.contentDescription = summary
                if (TextUtils.isEmpty(summary)) {
                    messageView.visibility = View.GONE
                    someView.visibility = View.GONE
                }
            }
            container.visibility = View.VISIBLE
        } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS) || Reminder.isSame(reminder.type, Reminder.BY_SKYPE)) {
            if (!Reminder.isSame(reminder.type, Reminder.BY_SKYPE)) {
                contactPhoto.visibility = View.VISIBLE
                val conID = Contacts.getIdFromNumber(reminder.target, this).toLong()
                val photo = Contacts.getPhoto(conID)
                if (photo != null)
                    contactPhoto.setImageURI(photo)
                else
                    contactPhoto.visibility = View.GONE
                val name = Contacts.getNameFromNumber(reminder.target, this)
                remText.setText(R.string.send_sms)
                val userInfo = (name ?: "") + "\n" + reminder.target
                contactInfo.text = userInfo
                contactInfo.contentDescription = userInfo
                messageView.text = summary
                messageView.contentDescription = summary
            } else {
                remText.setText(R.string.skype_chat)
                contactInfo.text = reminder.target
                contactInfo.contentDescription = reminder.target
                messageView.text = summary
                messageView.contentDescription = summary
            }
            if (!prefs.isAutoSmsEnabled) {
                buttonCall.show()
                buttonCall.setImageResource(R.drawable.ic_send_black_24dp)
                buttonCall.contentDescription = getString(R.string.acc_button_send_message)
            } else {
                buttonCall.hide()
                buttonDelay.hide()
                buttonDelayFor.hide()
            }
            container.visibility = View.VISIBLE
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL)) {
            buttonCall.show()
            buttonCall.setImageResource(R.drawable.ic_send_black_24dp)
            buttonCall.contentDescription = getString(R.string.acc_button_send_message)
            remText.setText(R.string.e_mail)
            val conID = Contacts.getIdFromMail(reminder.target, this)
            if (conID != 0) {
                val photo = Contacts.getPhoto(conID.toLong())
                if (photo != null)
                    contactPhoto.setImageURI(photo)
                else
                    contactPhoto.visibility = View.GONE
                val name = Contacts.getNameFromMail(reminder.target, this)
                val userInfo = (name ?: "") + "\n" + reminder.target
                contactInfo.text = userInfo
                contactInfo.contentDescription = userInfo
            } else {
                contactInfo.text = reminder.target
                contactInfo.contentDescription = reminder.target
            }
            messageView.text = summary
            messageView.contentDescription = summary
            subjectView.text = reminder.subject
            subjectView.contentDescription = reminder.subject
            container.visibility = View.VISIBLE
            subjectContainer.visibility = View.VISIBLE
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
            val packageManager = packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(reminder.target, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val nameA = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            val label = summary + "\n\n" + nameA + "\n" + reminder.target
            remText.text = label
            remText.contentDescription = label
            buttonCall.show()
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp)
            buttonCall.contentDescription = getString(R.string.acc_button_open_application)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)) {
            val label = summary + "\n\n" + reminder.target
            remText.text = label
            remText.contentDescription = label
            buttonCall.show()
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp)
            buttonCall.contentDescription = getString(R.string.acc_button_open_link_in_browser)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP)) {
            remText.text = summary
            remText.contentDescription = summary
            buttonCall.hide()
            loadData()
        } else {
            remText.text = summary
            remText.contentDescription = summary
            buttonCall.hide()
        }

        if (Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
            button_refresh.show()
            button_refresh.setOnClickListener { startAgain() }
        } else {
            button_refresh.hide()
        }

        if (Reminder.isGpsType(reminder.type)) {
            buttonDelay.hide()
            buttonDelayFor.hide()
        }

        if (!mControl!!.canSkip()) {
            buttonCancel.hide()
        } else {
            buttonCancel.show()
        }

        buttonCancel.setOnClickListener { cancel() }
        buttonNotification.setOnClickListener { favourite() }
        buttonOk.setOnClickListener { ok() }
        buttonEdit.setOnClickListener { editReminder() }
        buttonDelay.setOnClickListener { delay() }
        buttonDelayFor.setOnClickListener {
            showDialog()
            repeater.cancelAlarm(this, id)
            discardNotification(id)
        }
        buttonCall.setOnClickListener { call() }
        if (Reminder.isKind(reminder.type, Reminder.Kind.SMS) && isAutoEnabled) {
            sendSMS()
        } else if (Reminder.isKind(reminder.type, Reminder.Kind.CALL) && isAutoCallEnabled) {
            call()
        } else if (isAppType && isAutoLaunchEnabled) {
            openApplication()
        } else {
            showReminder()
        }
        if (isRepeatEnabled) {
            repeater.setAlarm(this, id)
        }
        if (isTtsEnabled) {
            startTts()
        }
    }

    private fun startAgain() {
        if (mControl != null) {
            mControl?.next()
            mControl?.onOff()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    private fun showAttachmentButton() {
        if (buttonAttachment != null) {
            buttonAttachment.show()
            buttonAttachment.setOnClickListener { showFile() }
        }
    }

    private fun showFile() {
        val reminder = mReminder ?: return
        val path = reminder.attachmentFile ?: return
        val mime = MimeTypeMap.getSingleton()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Module.isNougat) {
            val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(path))
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        } else {
            intent.setDataAndType(Uri.parse("file://$path"),
                    mime.getMimeTypeFromExtension(fileExt(reminder.attachmentFile).substring(1)))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.cant_find_app_for_that_file_type, Toast.LENGTH_LONG).show()
        }
    }

    private fun fileExt(url: String?): String {
        var url: String? = url ?: return ""
        if (url!!.contains("?")) {
            url = url.substring(0, url.indexOf("?"))
        }
        return if (url.lastIndexOf(".") == -1) {
            ""
        } else {
            var ext = url.substring(url.lastIndexOf(".") + 1)
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"))
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"))
            }
            ext.toLowerCase()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver)
        }
        removeFlags()
        if (prefs.isAutoBackupEnabled) {
            BackupTask(this).execute()
        }
        BackupReminderTask(this).execute()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs.isFoldingEnabled) {
            repeater.cancelAlarm(this, id)
            removeFlags()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApplication() {
        val reminder = mReminder ?: return
        if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
            TelephonyUtil.openApp(reminder.target, this)
        } else {
            TelephonyUtil.openLink(reminder.target, this)
        }
        cancelTasks()
        finish()
    }

    private fun cancelTasks() {
        discardNotification(id)
        repeater.cancelAlarm(this, id)
    }

    private fun showDialog() {
        val items = arrayOf<CharSequence>(String.format(getString(R.string.x_minutes), 5.toString()), String.format(getString(R.string.x_minutes), 10.toString()), String.format(getString(R.string.x_minutes), 15.toString()), String.format(getString(R.string.x_minutes), 30.toString()), String.format(getString(R.string.x_minutes), 45.toString()), String.format(getString(R.string.x_minutes), 60.toString()), String.format(getString(R.string.x_minutes), 90.toString()), String.format(getString(R.string.x_hours), 2.toString()), String.format(getString(R.string.x_hours), 6.toString()), String.format(getString(R.string.x_hours), 24.toString()), String.format(getString(R.string.x_days), 2.toString()), String.format(getString(R.string.x_days), 7.toString()))
        val builder = Dialogues.getDialog(this)
        builder.setTitle(getString(R.string.choose_time))
        builder.setItems(items) { dialog, item1 ->
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
            mControl?.setDelay(x)
            Toast.makeText(this, getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            removeFlags()
            finish()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun sendSMS() {
        val reminder = mReminder ?: return
        if (TextUtils.isEmpty(summary)) return
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, SMS_PERM, Permissions.SEND_SMS)
            return
        }
        showProgressDialog(getString(R.string.sending_message))
        val action = "SMS_SENT"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(action), 0)
        registerReceiver(SendReceiver(mSendListener), IntentFilter(action))
        val sms = SmsManager.getDefault()
        try {
            sms.sendTextMessage(reminder.target, null, summary, sentPI, null)
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.error_sending, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReminder() {
        if (!isTtsEnabled) {
            showReminderNotification(this)
        } else {
            showTTSNotification(this)
        }
    }

    private fun editReminder() {
        val reminder = mReminder ?: return
        if (mControl == null) return
        mControl?.stop()
        removeFlags()
        cancelTasks()
        startActivity(Intent(this, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, reminder.uuId))
        finish()
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
        todoList.adapter = shoppingAdapter
        todoList.visibility = View.VISIBLE
    }

    override fun call() {
        val reminder = mReminder ?: return
        if (mControl == null) return
        mControl?.next()
        removeFlags()
        cancelTasks()
        when {
            Reminder.isKind(reminder.type, Reminder.Kind.SMS) -> sendSMS()
            Reminder.isBase(reminder.type, Reminder.BY_SKYPE) -> {
                if (!SuperUtil.isSkypeClientInstalled(this)) {
                    showInstallSkypeDialog()
                    return
                }
                when {
                    Reminder.isSame(reminder.type, Reminder.BY_SKYPE_CALL) -> TelephonyUtil.skypeCall(reminder.target, this)
                    Reminder.isSame(reminder.type, Reminder.BY_SKYPE_VIDEO) -> TelephonyUtil.skypeVideoCall(reminder.target, this)
                    Reminder.isSame(reminder.type, Reminder.BY_SKYPE) -> TelephonyUtil.skypeChat(reminder.target, this)
                }
            }
            isAppType -> openApplication()
            Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> TelephonyUtil.sendMail(this, reminder.target,
                    reminder.subject, summary, reminder.attachmentFile)
            else -> makeCall()
        }
        if (!Reminder.isKind(reminder.type, Reminder.Kind.SMS)) {
            finish()
        }
    }

    private fun showInstallSkypeDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(R.string.skype_is_not_installed)
        builder.setPositiveButton(R.string.yes) { dialogInterface, _ ->
            dialogInterface.dismiss()
            SuperUtil.installSkype(this)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun makeCall() {
        val reminder = mReminder ?: return
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(reminder.target, this)
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
        }
    }

    override fun delay() {
        if (mControl != null) {
            val delay = prefs.snoozeTime
            mControl?.setDelay(delay)
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun cancel() {
        if (mControl != null) {
            mControl?.stop()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun favourite() {
        if (mControl != null) {
            mControl?.next()
            removeFlags()
            cancelTasks()
            showFavouriteNotification()
        }
        finish()
    }

    override fun ok() {
        if (mControl != null) {
            mControl?.next()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun showSendingError() {
        showReminder()
        remText.text = getString(R.string.error_sending)
        remText.contentDescription = getString(R.string.error_sending)
        buttonCall.setImageResource(R.drawable.ic_refresh)
        buttonCall.contentDescription = getString(R.string.acc_button_retry_to_send_message)
        if (buttonCall.visibility == View.GONE) {
            buttonCall.show()
        }
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
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp)
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        val isWear = prefs.isWearEnabled
        if (isWear && Module.isJellyMR2) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        val mNotifyMgr = NotificationManagerCompat.from(this)
        mNotifyMgr.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun showReminderNotification(activity: Activity) {
        LogUtil.d(TAG, "showReminderNotification: ")
        val notificationIntent = Intent(this, activity.javaClass)
        notificationIntent.putExtra(Constants.INTENT_ID, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
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
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp)
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        if (sound != null && !isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this)
                        || SuperUtil.checkNotificationPermission(this)
                        && prefs.isSoundInSilentModeEnabled)) {
            val soundUri = soundUri
            LogUtil.d(TAG, "showReminderNotification: $soundUri")
            sound!!.playAlarm(soundUri, prefs.isInfiniteSoundEnabled)
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
        if (isWear && Module.isJellyMR2) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        val mNotifyMgr = NotificationManagerCompat.from(this)
        mNotifyMgr.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun showTTSNotification(activityClass: Activity) {
        LogUtil.d(TAG, "showTTSNotification: ")
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(summary)
        val notificationIntent = Intent(this, activityClass.javaClass)
        notificationIntent.putExtra(Constants.INTENT_ID, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
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
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp)
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        if (!isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this) || SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled)) {
            playDefaultMelody()
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
        if (isWear && Module.isJellyMR2) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        val mNotifyMgr = NotificationManagerCompat.from(this)
        mNotifyMgr.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun playDefaultMelody() {
        if (sound == null) return
        LogUtil.d(TAG, "playDefaultMelody: ")
        try {
            val afd = assets.openFd("sounds/beep.mp3")
            sound?.playAlarm(afd)
        } catch (e: IOException) {
            e.printStackTrace()
            sound?.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CALL_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall()
            }
            SMS_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS()
            }
        }
    }

    companion object {

        private const val TAG = "ReminderDialogActivity"
        private const val CALL_PERM = 612
        private const val SMS_PERM = 613

        fun getLaunchIntent(context: Context, id: Int): Intent {
            val resultIntent = Intent(context, ReminderDialogActivity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
