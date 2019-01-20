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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.RepeatNotificationReceiver
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import kotlinx.android.synthetic.main.activity_reminder_dialog.*
import timber.log.Timber
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
    private var isMockedTest = false
    private var isReminderShowed = false
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
        get() = if (mReminder == null) "" else mReminder?.summary ?: ""

    override val uuId: String
        get() = if (mReminder == null) "" else mReminder?.uuId ?: ""

    override val id: Int
        get() = if (mReminder == null) 0 else mReminder?.uniqueId ?: 2121

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
            return if (!isGlobal && reminder.volume != -1)
                reminder.volume
            else
                prefs.loudness
        }
    private val isRateDialogShowed: Boolean
        get() {
            var count = prefs.rateCount
            count++
            prefs.rateCount = count
            return count == 10
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""

        setContentView(R.layout.activity_reminder_dialog)

        container.visibility = View.GONE
        subjectContainer.visibility = View.GONE
        contactBlock.visibility = View.INVISIBLE

        initButtons()

        if (savedInstanceState != null) {
            isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
        }

        initViewModel(id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_IS_ROTATED, true)
        super.onSaveInstanceState(outState)
    }

    private fun initButtons() {
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
        buttonAction.setOnClickListener { call() }
        buttonRefresh.hide()
    }

    override fun onResume() {
        super.onResume()
        if (isRateDialogShowed) {
            showRateDialog()
        }
    }

    private fun showRateDialog() {
        val builder = dialogues.getDialog(this)
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

    private fun initViewModel(id: String) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel.reminder.observe(this,Observer { reminder ->
            if (reminder != null) {
                if (!isReminderShowed) showInfo(reminder)
            } else {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                    else -> {
                    }
                }
            }
        })

        if (id == "" && BuildConfig.DEBUG) {
            loadTest()
        }
    }

    private fun loadTest() {
        isMockedTest = intent.getBooleanExtra(ARG_TEST, false)
        if (isMockedTest) {
            val reminder = intent.getSerializableExtra(ARG_TEST_ITEM) as Reminder?
            if (reminder != null) showInfo(reminder)
        }
    }

    private fun showInfo(reminder: Reminder) {
        this.mReminder = reminder
        if (!isMockedTest) {
            this.mControl = EventControlFactory.getController(reminder)
        }
        Timber.d("showInfo: ${TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime), true)}")
        if (reminder.attachmentFile != "") showAttachmentButton()
        else buttonAttachment?.hide()

        val contactPhoto = contactPhoto
        contactPhoto.borderColor = themeUtil.getNoteLightColor()
        contactPhoto.visibility = View.GONE

        todoList.layoutManager = LinearLayoutManager(this)
        todoList.visibility = View.GONE

        remText.text = ""

        if (!TextUtils.isEmpty(reminder.eventTime) && !Reminder.isGpsType(reminder.type)) {
            reminder_time.text = TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime),
                    prefs.is24HourFormatEnabled, prefs.appLanguage)
            timeBlock.visibility = View.VISIBLE
        } else {
            timeBlock.visibility = View.GONE
        }

        if (Reminder.isKind(reminder.type, Reminder.Kind.CALL) || Reminder.isSame(reminder.type, Reminder.BY_SKYPE_VIDEO)) {
            if (!Reminder.isBase(reminder.type, Reminder.BY_SKYPE)) {
                contactPhoto.visibility = View.VISIBLE
                val conID = Contacts.getIdFromNumber(reminder.target, this)

                val name = Contacts.getNameFromNumber(reminder.target, this)
                remText.setText(R.string.make_call)
                val userTitle = (name ?: "") + "\n" + reminder.target

                val photo = Contacts.getPhoto(conID)
                if (photo != null) {
                    contactPhoto.setImageURI(photo)
                } else {
                    contactPhoto.setImageDrawable(BitmapUtils.imageFromName(name ?: reminder.target))
                }

                contactInfo.text = userTitle
                contactInfo.contentDescription = userTitle
                messageView.text = summary
                messageView.contentDescription = summary

                contactName.text = name
                contactNumber.text = reminder.target

                contactBlock.visibility = View.VISIBLE
                buttonAction.text = getString(R.string.make_call)
                if (prefs.isTelephonyAllowed) {
                    buttonAction.visibility = View.VISIBLE
                } else {
                    buttonAction.visibility = View.INVISIBLE
                }
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

                contactName.text = reminder.target
                contactNumber.text = reminder.target

                contactBlock.visibility = View.VISIBLE
                buttonAction.text = getString(R.string.make_call)
                buttonAction.visibility = View.VISIBLE
                if (TextUtils.isEmpty(summary)) {
                    messageView.visibility = View.GONE
                    someView.visibility = View.GONE
                }
            }
            container.visibility = View.VISIBLE
        } else if (Reminder.isKind(reminder.type, Reminder.Kind.SMS) || Reminder.isSame(reminder.type, Reminder.BY_SKYPE)) {
            if (!Reminder.isSame(reminder.type, Reminder.BY_SKYPE)) {
                contactPhoto.visibility = View.VISIBLE
                val conID = Contacts.getIdFromNumber(reminder.target, this)
                val name = Contacts.getNameFromNumber(reminder.target, this)
                remText.setText(R.string.send_sms)
                val userInfo = (name ?: "") + "\n" + reminder.target
                contactInfo.text = userInfo
                contactInfo.contentDescription = userInfo
                messageView.text = summary
                messageView.contentDescription = summary

                val photo = Contacts.getPhoto(conID)
                if (photo != null) {
                    contactPhoto.setImageURI(photo)
                } else {
                    contactPhoto.setImageDrawable(BitmapUtils.imageFromName(name ?: reminder.target))
                }

                contactName.text = name
                contactNumber.text = reminder.target
                buttonAction.text = getString(R.string.send)
                if (prefs.isTelephonyAllowed) {
                    buttonAction.visibility = View.VISIBLE
                } else {
                    buttonAction.visibility = View.INVISIBLE
                }
            } else {
                remText.setText(R.string.skype_chat)
                contactInfo.text = reminder.target
                contactInfo.contentDescription = reminder.target
                messageView.text = summary
                messageView.contentDescription = summary

                contactName.text = reminder.target
                contactNumber.text = reminder.target
            }
            if (!prefs.isAutoSmsEnabled) {
                contactBlock.visibility = View.VISIBLE
                buttonAction.text = getString(R.string.send)
                buttonAction.contentDescription = getString(R.string.acc_button_send_message)
                buttonAction.visibility = View.VISIBLE
            } else {
                contactBlock.visibility = View.INVISIBLE
                buttonAction.visibility = View.INVISIBLE
                buttonDelay.hide()
                buttonDelayFor.hide()
            }
            container.visibility = View.VISIBLE
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL)) {
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

                contactName.text = name
                contactNumber.text = reminder.target
            } else {
                contactInfo.text = reminder.target
                contactInfo.contentDescription = reminder.target

                contactName.text = reminder.target
                contactNumber.text = reminder.target
            }
            messageView.text = summary
            messageView.contentDescription = summary
            subjectView.text = reminder.subject
            subjectView.contentDescription = reminder.subject
            container.visibility = View.VISIBLE
            subjectContainer.visibility = View.VISIBLE

            contactBlock.visibility = View.VISIBLE
            buttonAction.text = getString(R.string.send)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
            val packageManager = packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(reminder.target, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val nameA = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            val label = summary + "\n\n" + nameA + "\n" + reminder.target
            remText.text = summary
            remText.contentDescription = label

            contactName.text = nameA
            contactNumber.text = reminder.target
            contactBlock.visibility = View.VISIBLE
            buttonAction.text = getString(R.string.open)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)) {
            val label = summary + "\n\n" + reminder.target
            remText.text = summary
            remText.contentDescription = label

            contactName.text = reminder.target
            contactNumber.text = reminder.target
            contactBlock.visibility = View.VISIBLE
            buttonAction.text = getString(R.string.open)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP)) {
            remText.text = summary
            remText.contentDescription = summary
            contactBlock.visibility = View.INVISIBLE
            loadData()
        } else {
            remText.text = summary
            remText.contentDescription = summary
            contactBlock.visibility = View.INVISIBLE
        }

        if (Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
            buttonRefresh.show()
            buttonRefresh.setOnClickListener { startAgain() }
        } else {
            buttonRefresh.hide()
        }

        if (Reminder.isGpsType(reminder.type)) {
            buttonDelay.hide()
            buttonDelayFor.hide()
        }

        if (!canSkip()) {
            buttonCancel.hide()
        } else {
            buttonCancel.show()
        }

        if (Reminder.isKind(reminder.type, Reminder.Kind.SMS) && isAutoEnabled) {
            sendSMS()
        } else if (Reminder.isKind(reminder.type, Reminder.Kind.CALL) && isAutoCallEnabled) {
            call()
        } else if (isAppType && isAutoLaunchEnabled) {
            openApplication(reminder)
        } else {
            showNotification()
            if (isRepeatEnabled) {
                repeater.setAlarm(this, id)
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

    private fun showAttachmentButton() {
        if (buttonAttachment != null) {
            buttonAttachment.show()
            buttonAttachment.setOnClickListener { showFile() }
        }
    }

    private fun showFile() {
        val reminder = mReminder ?: return
        val path = reminder.attachmentFile
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

    private fun fileExt(urlNullable: String?): String {
        var url: String = urlNullable ?: return ""
        if (url.contains("?")) {
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
        repeater.cancelAlarm(this, id)
    }

    private fun showDialog() {
        val items = arrayOf<CharSequence>(String.format(getString(R.string.x_minutes), 5.toString()), String.format(getString(R.string.x_minutes), 10.toString()), String.format(getString(R.string.x_minutes), 15.toString()), String.format(getString(R.string.x_minutes), 30.toString()), String.format(getString(R.string.x_minutes), 45.toString()), String.format(getString(R.string.x_minutes), 60.toString()), String.format(getString(R.string.x_minutes), 90.toString()), String.format(getString(R.string.x_hours), 2.toString()), String.format(getString(R.string.x_hours), 6.toString()), String.format(getString(R.string.x_hours), 24.toString()), String.format(getString(R.string.x_days), 2.toString()), String.format(getString(R.string.x_days), 7.toString()))
        val builder = dialogues.getDialog(this)
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
        if (!Permissions.ensurePermissions(this, SMS_PERM, Permissions.SEND_SMS)) {
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

    private fun showNotification() {
        if (isMockedTest || isReminderShowed) return
        if (!isTtsEnabled) {
            showReminderNotification(this)
        } else {
            showTTSNotification(this)
        }
    }

    private fun editReminder() {
        doActions({ it.stop() }, {
            CreateReminderActivity.openLogged(this, Intent(this, CreateReminderActivity::class.java)
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
        todoList.adapter = shoppingAdapter
        todoList.visibility = View.VISIBLE
    }

    private fun call() {
        doActions({ it.next() }, {
            when {
                Reminder.isKind(it.type, Reminder.Kind.SMS) -> sendSMS()
                Reminder.isBase(it.type, Reminder.BY_SKYPE) -> {
                    if (!SuperUtil.isSkypeClientInstalled(this)) {
                        showInstallSkypeDialog()
                        return@doActions
                    }
                    when {
                        Reminder.isSame(it.type, Reminder.BY_SKYPE_CALL) -> TelephonyUtil.skypeCall(it.target, this)
                        Reminder.isSame(it.type, Reminder.BY_SKYPE_VIDEO) -> TelephonyUtil.skypeVideoCall(it.target, this)
                        Reminder.isSame(it.type, Reminder.BY_SKYPE) -> TelephonyUtil.skypeChat(it.target, this)
                    }
                }
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

    private fun showInstallSkypeDialog() {
        val builder = dialogues.getDialog(this)
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
        if (Permissions.ensurePermissions(this, CALL_PERM, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(reminder.target, this)
        }
    }

    private fun delay() {
        doActions({ it.setDelay(prefs.snoozeTime) }, { finish() })
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

    override fun showSendingError() {
        showNotification()
        remText.text = getString(R.string.error_sending)
        remText.contentDescription = getString(R.string.error_sending)
        buttonAction.text = getString(R.string.retry)
        if (buttonAction.visibility == View.INVISIBLE) {
            buttonAction.visibility = View.VISIBLE
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
            builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
            builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun showReminderNotification(activity: Activity) {
        if (isScreenResumed) {
            return
        }
        Timber.d("showReminderNotification: $id")
        val notificationIntent = Intent(this, activity.javaClass)
        notificationIntent.putExtra(Constants.INTENT_ID, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        val builder: NotificationCompat.Builder
        if (isScreenResumed) {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
            builder.priority = NotificationCompat.PRIORITY_LOW
        } else {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
            builder.priority = priority()
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
                val soundUri = soundUri
                Timber.d("showReminderNotification: $soundUri")
                sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled)
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
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
            builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun priority(): Int {
        val priority = mReminder?.priority ?: 2
        return when (priority) {
            0 -> NotificationCompat.PRIORITY_MIN
            1 -> NotificationCompat.PRIORITY_LOW
            2 -> NotificationCompat.PRIORITY_DEFAULT
            3 -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_MAX
        }
    }

    private fun showTTSNotification(activityClass: Activity) {
        if (isScreenResumed) {
            return
        }
        Timber.d("showTTSNotification: ")
        val builder: NotificationCompat.Builder
        if (isScreenResumed) {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
            builder.priority = NotificationCompat.PRIORITY_LOW
        } else {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
            builder.priority = priority()
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
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

        val notificationIntent = Intent(this, activityClass.javaClass)
        notificationIntent.putExtra(Constants.INTENT_ID, id)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
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
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
            builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white)
        }
        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun playDefaultMelody() {
        if (sound == null) return
        Timber.d("playDefaultMelody: ")
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
        when (requestCode) {
            CALL_PERM -> if (Permissions.isAllGranted(grantResults)) {
                makeCall()
            }
            SMS_PERM -> if (Permissions.isAllGranted(grantResults)) {
                sendSMS()
            }
        }
    }

    private fun doActions(onControl: (EventControl) -> Unit, onEnd: (Reminder) -> Unit) {
        isReminderShowed = true
        val reminder = mReminder
        if (reminder == null) {
            removeFlags()
            cancelTasks()
            finish()
            return
        }
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
        private const val CALL_PERM = 612
        private const val SMS_PERM = 613
        private const val ARG_TEST = "arg_test"
        private const val ARG_TEST_ITEM = "arg_test_item"
        private const val ARG_IS_ROTATED = "arg_rotated"

        fun mockTest(context: Context, reminder: Reminder) {
            val intent = Intent(context, ReminderDialogActivity::class.java)
            intent.putExtra(ARG_TEST, true)
            intent.putExtra(ARG_TEST_ITEM, reminder)
            context.startActivity(intent)
        }

        fun getLaunchIntent(context: Context, id: String): Intent {
            val resultIntent = Intent(context, ReminderDialogActivity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
