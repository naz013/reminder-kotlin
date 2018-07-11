package com.elementary.tasks.reminder.preview

import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.RepeatNotificationReceiver
import com.elementary.tasks.core.services.SendReceiver
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.reminder.work.BackupReminderTask
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.elementary.tasks.databinding.ActivityReminderDialogBinding
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter

import java.io.File
import java.io.IOException
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

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
    private var binding: ActivityReminderDialogBinding? = null
    private var viewModel: ReminderViewModel? = null

    private var shoppingAdapter: ShopListRecyclerAdapter? = null

    private val repeater = RepeatNotificationReceiver()
    private var sentReceiver: BroadcastReceiver? = null

    private var mReminder: Reminder? = null
    private var mControl: EventControl? = null
    protected override var isScreenResumed: Boolean = false
        private set

    private val isAppType: Boolean
        get() = mReminder != null && (Reminder.isSame(mReminder!!.type, Reminder.BY_DATE_LINK) || Reminder.isSame(mReminder!!.type, Reminder.BY_DATE_APP))

    private val isAutoCallEnabled: Boolean
        get() {
            if (mReminder == null) return false
            var `is` = prefs!!.isAutoCallEnabled
            if (!isGlobal) {
                `is` = mReminder!!.isAuto
            }
            return `is`
        }

    private val isAutoLaunchEnabled: Boolean
        get() {
            if (mReminder == null) return false
            var `is` = prefs!!.isAutoLaunchEnabled
            if (!isGlobal) {
                `is` = mReminder!!.isAuto
            }
            return `is`
        }

    private val isAutoEnabled: Boolean
        get() {
            if (mReminder == null) return false
            var `is` = prefs!!.isAutoSmsEnabled
            if (!isGlobal) {
                `is` = mReminder!!.isAuto
            }
            return `is`
        }

    private val isRepeatEnabled: Boolean
        get() {
            if (mReminder == null) return false
            var isRepeat = prefs!!.isNotificationRepeatEnabled
            if (!isGlobal) {
                isRepeat = mReminder!!.isRepeatNotification
            }
            return isRepeat
        }

    private val isTtsEnabled: Boolean
        get() {
            if (mReminder == null) return false
            var isTTS = prefs!!.isTtsEnabled
            if (!isGlobal) {
                isTTS = mReminder!!.isNotifyByVoice
            }
            LogUtil.d(TAG, "isTtsEnabled: $isTTS")
            return isTTS
        }

    protected override val melody: String?
        get() = if (mReminder == null) "" else mReminder!!.melodyPath

    protected override val isVibrate: Boolean
        get() {
            if (mReminder == null) return false
            var isVibrate = prefs!!.isVibrateEnabled
            if (!isGlobal) isVibrate = mReminder!!.isVibrate
            return isVibrate
        }

    protected override val summary: String?
        get() = if (mReminder == null) "" else mReminder!!.summary

    protected override val uuId: String?
        get() = if (mReminder == null) "" else mReminder!!.uuId

    protected override val id: Int
        get() = if (mReminder == null) 0 else mReminder!!.uniqueId

    protected override val ledColor: Int
        get() {
            if (mReminder == null) return 0
            return if (Module.isPro) {
                if (mReminder!!.color != -1) {
                    LED.getLED(mReminder!!.color)
                } else {
                    LED.getLED(prefs!!.ledColor)
                }
            } else LED.getLED(0)
        }

    protected override val isAwakeDevice: Boolean
        get() {
            if (mReminder == null) return false
            var `is` = prefs!!.isDeviceAwakeEnabled
            if (!isGlobal) `is` = mReminder!!.isAwake
            return `is`
        }

    protected override val isGlobal: Boolean
        get() = mReminder != null && mReminder!!.isUseGlobal

    protected override val isUnlockDevice: Boolean
        get() {
            if (mReminder == null) return false
            var `is` = prefs!!.isDeviceUnlockEnabled
            if (!isGlobal) `is` = mReminder!!.isUnlock
            return `is`
        }

    protected override val maxVolume: Int
        get() {
            if (mReminder == null) return 25
            return if (!isGlobal && mReminder!!.volume != -1)
                mReminder!!.volume
            else
                prefs!!.loudness
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        val id = intent.getIntExtra(Constants.INTENT_ID, 0)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog)

        binding!!.card.setCardBackgroundColor(themeUtil!!.cardStyle)
        if (Module.isLollipop) binding!!.card.cardElevation = Configs.CARD_ELEVATION
        binding!!.container.visibility = View.GONE
        binding!!.subjectContainer.visibility = View.GONE
        loadImage(binding!!.bgImage)
        colorify(binding!!.buttonOk, binding!!.buttonCall, binding!!.buttonCancel, binding!!.buttonDelay,
                binding!!.buttonDelayFor, binding!!.buttonNotification, binding!!.buttonEdit)
        setTextDrawable(binding!!.buttonDelay, prefs!!.snoozeTime.toString())
        setTextDrawable(binding!!.buttonDelayFor, "...")
        binding!!.buttonOk.setImageResource(R.drawable.ic_done_black_24dp)
        binding!!.buttonEdit.setImageResource(R.drawable.ic_create_black_24dp)
        binding!!.buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp)
        binding!!.buttonRefresh.hide()
        binding!!.buttonCall.setImageResource(R.drawable.ic_call_black_24dp)
        binding!!.buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp)

        initViewModel(id)
    }

    private fun initViewModel(id: Int) {
        val factory = ReminderViewModel.Factory(application, id)
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel::class.java)
        viewModel!!.reminder.observe(this, { reminder ->
            if (reminder != null) {
                showInfo(reminder)
            } else {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()

            }
        })
        viewModel!!.result.observe(this, { commands ->
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
        if (reminder.attachmentFile != null) showAttachmentButton()

        val contactPhoto = binding!!.contactPhoto
        contactPhoto.borderColor = themeUtil!!.getColor(themeUtil!!.colorPrimary())
        contactPhoto.visibility = View.GONE

        binding!!.todoList.layoutManager = LinearLayoutManager(this)
        binding!!.todoList.visibility = View.GONE

        binding!!.remText.text = ""

        if (!TextUtils.isEmpty(reminder.eventTime) && Reminder.isGpsType(reminder.type)) {
            binding!!.reminderTime.text = TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(reminder.eventTime),
                    Prefs.getInstance(this).is24HourFormatEnabled, false)
            binding!!.reminderTime.visibility = View.VISIBLE
        } else {
            binding!!.reminderTime.visibility = View.GONE
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
                binding!!.remText.setText(R.string.make_call)
                val userTitle = (name ?: "") + "\n" + reminder.target
                binding!!.contactInfo.text = userTitle
                binding!!.contactInfo.contentDescription = userTitle
                binding!!.messageView.text = summary
                binding!!.messageView.contentDescription = summary
            } else {
                if (Reminder.isSame(reminder.type, Reminder.BY_SKYPE_VIDEO)) {
                    binding!!.remText.setText(R.string.video_call)
                } else {
                    binding!!.remText.setText(R.string.skype_call)
                }
                binding!!.contactInfo.text = reminder.target
                binding!!.contactInfo.contentDescription = reminder.target
                binding!!.messageView.text = summary
                binding!!.messageView.contentDescription = summary
                if (TextUtils.isEmpty(summary)) {
                    binding!!.messageView.visibility = View.GONE
                    binding!!.someView.visibility = View.GONE
                }
            }
            binding!!.container.visibility = View.VISIBLE
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
                binding!!.remText.setText(R.string.send_sms)
                val userInfo = (name ?: "") + "\n" + reminder.target
                binding!!.contactInfo.text = userInfo
                binding!!.contactInfo.contentDescription = userInfo
                binding!!.messageView.text = summary
                binding!!.messageView.contentDescription = summary
            } else {
                binding!!.remText.setText(R.string.skype_chat)
                binding!!.contactInfo.text = reminder.target
                binding!!.contactInfo.contentDescription = reminder.target
                binding!!.messageView.text = summary
                binding!!.messageView.contentDescription = summary
            }
            if (!prefs!!.isAutoSmsEnabled) {
                binding!!.buttonCall.show()
                binding!!.buttonCall.setImageResource(R.drawable.ic_send_black_24dp)
                binding!!.buttonCall.contentDescription = getString(R.string.acc_button_send_message)
            } else {
                binding!!.buttonCall.hide()
                binding!!.buttonDelay.hide()
                binding!!.buttonDelayFor.hide()
            }
            binding!!.container.visibility = View.VISIBLE
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL)) {
            binding!!.buttonCall.show()
            binding!!.buttonCall.setImageResource(R.drawable.ic_send_black_24dp)
            binding!!.buttonCall.contentDescription = getString(R.string.acc_button_send_message)
            binding!!.remText.setText(R.string.e_mail)
            val conID = Contacts.getIdFromMail(reminder.target, this)
            if (conID != 0) {
                val photo = Contacts.getPhoto(conID.toLong())
                if (photo != null)
                    contactPhoto.setImageURI(photo)
                else
                    contactPhoto.visibility = View.GONE
                val name = Contacts.getNameFromMail(reminder.target, this)
                val userInfo = (name ?: "") + "\n" + reminder.target
                binding!!.contactInfo.text = userInfo
                binding!!.contactInfo.contentDescription = userInfo
            } else {
                binding!!.contactInfo.text = reminder.target
                binding!!.contactInfo.contentDescription = reminder.target
            }
            binding!!.messageView.text = summary
            binding!!.messageView.contentDescription = summary
            binding!!.subjectView.text = reminder.subject
            binding!!.subjectView.contentDescription = reminder.subject
            binding!!.container.visibility = View.VISIBLE
            binding!!.subjectContainer.visibility = View.VISIBLE
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
            val packageManager = packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(reminder.target, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }

            val nameA = (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
            val label = summary + "\n\n" + nameA + "\n" + reminder.target
            binding!!.remText.text = label
            binding!!.remText.contentDescription = label
            binding!!.buttonCall.show()
            binding!!.buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp)
            binding!!.buttonCall.contentDescription = getString(R.string.acc_button_open_application)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK)) {
            val label = summary + "\n\n" + reminder.target
            binding!!.remText.text = label
            binding!!.remText.contentDescription = label
            binding!!.buttonCall.show()
            binding!!.buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp)
            binding!!.buttonCall.contentDescription = getString(R.string.acc_button_open_link_in_browser)
        } else if (Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP)) {
            binding!!.remText.text = summary
            binding!!.remText.contentDescription = summary
            binding!!.buttonCall.hide()
            loadData()
        } else {
            binding!!.remText.text = summary
            binding!!.remText.contentDescription = summary
            binding!!.buttonCall.hide()
        }

        if (Reminder.isBase(reminder.type, Reminder.BY_TIME)) {
            binding!!.buttonRefresh.show()
            binding!!.buttonRefresh.setOnClickListener { v -> startAgain() }
        } else {
            binding!!.buttonRefresh.hide()
        }

        if (Reminder.isGpsType(reminder.type)) {
            binding!!.buttonDelay.hide()
            binding!!.buttonDelayFor.hide()
        }

        if (!mControl!!.canSkip()) {
            binding!!.buttonCancel.hide()
        } else {
            binding!!.buttonCancel.show()
        }

        binding!!.buttonCancel.setOnClickListener { v -> cancel() }
        binding!!.buttonNotification.setOnClickListener { v -> favourite() }
        binding!!.buttonOk.setOnClickListener { v -> ok() }
        binding!!.buttonEdit.setOnClickListener { v -> editReminder() }
        binding!!.buttonDelay.setOnClickListener { v -> delay() }
        binding!!.buttonDelayFor.setOnClickListener { v ->
            showDialog()
            repeater.cancelAlarm(this, id)
            discardNotification(id)
        }
        binding!!.buttonCall.setOnClickListener { v -> call() }
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
            mControl!!.next()
            mControl!!.onOff()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    private fun showAttachmentButton() {
        if (binding!!.buttonAttachment != null) {
            binding!!.buttonAttachment.show()
            binding!!.buttonAttachment.setOnClickListener { view -> showFile() }
        }
    }

    private fun showFile() {
        if (mReminder == null) return
        val path = mReminder!!.attachmentFile ?: return
        val mime = MimeTypeMap.getSingleton()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Module.isNougat) {
            val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", File(path))
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        } else {
            intent.setDataAndType(Uri.parse("file://$path"), mime.getMimeTypeFromExtension(fileExt(mReminder!!.attachmentFile).substring(1)))
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
        if (url.lastIndexOf(".") == -1) {
            return ""
        } else {
            var ext = url.substring(url.lastIndexOf(".") + 1)
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"))
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"))
            }
            return ext.toLowerCase()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver)
        }
        removeFlags()
        if (prefs!!.isAutoBackupEnabled) {
            BackupTask(this).execute()
        }
        BackupReminderTask(this).execute()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs!!.isFoldingEnabled) {
            repeater.cancelAlarm(this, id)
            removeFlags()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    fun openApplication() {
        if (mReminder == null) return
        if (Reminder.isSame(mReminder!!.type, Reminder.BY_DATE_APP)) {
            TelephonyUtil.openApp(mReminder!!.target!!, this)
        } else {
            TelephonyUtil.openLink(mReminder!!.target!!, this)
        }
        cancelTasks()
        finish()
    }

    private fun cancelTasks() {
        discardNotification(id)
        repeater.cancelAlarm(this, id)
    }

    fun showDialog() {
        val items = arrayOf<CharSequence>(String.format(getString(R.string.x_minutes), 5.toString()), String.format(getString(R.string.x_minutes), 10.toString()), String.format(getString(R.string.x_minutes), 15.toString()), String.format(getString(R.string.x_minutes), 30.toString()), String.format(getString(R.string.x_minutes), 45.toString()), String.format(getString(R.string.x_minutes), 60.toString()), String.format(getString(R.string.x_minutes), 90.toString()), String.format(getString(R.string.x_hours), 2.toString()), String.format(getString(R.string.x_hours), 6.toString()), String.format(getString(R.string.x_hours), 24.toString()), String.format(getString(R.string.x_days), 2.toString()), String.format(getString(R.string.x_days), 7.toString()))
        val builder = Dialogues.getDialog(this)
        builder.setTitle(getString(R.string.choose_time))
        builder.setItems(items) { dialog, item1 ->
            var x = 0
            if (item1 == 0) {
                x = 5
            } else if (item1 == 1) {
                x = 10
            } else if (item1 == 2) {
                x = 15
            } else if (item1 == 3) {
                x = 30
            } else if (item1 == 4) {
                x = 45
            } else if (item1 == 5) {
                x = 60
            } else if (item1 == 6) {
                x = 90
            } else if (item1 == 7) {
                x = 120
            } else if (item1 == 8) {
                x = 60 * 6
            } else if (item1 == 9) {
                x = 60 * 24
            } else if (item1 == 10) {
                x = 60 * 24 * 2
            } else if (item1 == 11) {
                x = 60 * 24 * 7
            }
            if (mControl != null) mControl!!.setDelay(x)
            Toast.makeText(this, getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            removeFlags()
            finish()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun sendSMS() {
        if (mReminder == null || TextUtils.isEmpty(summary)) return
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, SMS_PERM, Permissions.SEND_SMS)
            return
        }
        showProgressDialog(getString(R.string.sending_message))
        val SENT = "SMS_SENT"
        val sentPI = PendingIntent.getBroadcast(this, 0, Intent(SENT), 0)
        registerReceiver(SendReceiver(mSendListener), IntentFilter(SENT))
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(mReminder!!.target, null, summary, sentPI, null)
    }

    private fun showReminder() {
        if (!isTtsEnabled) {
            showReminderNotification(this)
        } else {
            showTTSNotification(this)
        }
    }

    private fun editReminder() {
        if (mReminder == null || mControl == null) return
        mControl!!.stop()
        removeFlags()
        cancelTasks()
        startActivity(Intent(this, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, mReminder!!.uuId))
        finish()
    }

    private fun loadData() {
        if (mReminder == null) return
        shoppingAdapter = ShopListRecyclerAdapter(this, mReminder!!.shoppings,
                object : ShopListRecyclerAdapter.ActionListener {
                    override fun onItemCheck(position: Int, isChecked: Boolean) {
                        val item = shoppingAdapter!!.getItem(position)
                        item.isChecked = !item.isChecked
                        shoppingAdapter!!.updateData()
                        viewModel!!.saveReminder(mReminder!!.setShoppings(shoppingAdapter!!.data))
                    }

                    override fun onItemDelete(position: Int) {
                        shoppingAdapter!!.delete(position)
                        viewModel!!.saveReminder(mReminder!!.setShoppings(shoppingAdapter!!.data))
                    }
                })
        binding!!.todoList.adapter = shoppingAdapter
        binding!!.todoList.visibility = View.VISIBLE
    }

    override fun call() {
        if (mReminder == null || mControl == null) return
        mControl!!.next()
        removeFlags()
        cancelTasks()
        if (Reminder.isKind(mReminder!!.type, Reminder.Kind.SMS)) {
            sendSMS()
        } else if (Reminder.isBase(mReminder!!.type, Reminder.BY_SKYPE)) {
            if (!SuperUtil.isSkypeClientInstalled(this)) {
                showInstallSkypeDialog()
                return
            }
            if (Reminder.isSame(mReminder!!.type, Reminder.BY_SKYPE_CALL)) {
                TelephonyUtil.skypeCall(mReminder!!.target, this)
            } else if (Reminder.isSame(mReminder!!.type, Reminder.BY_SKYPE_VIDEO)) {
                TelephonyUtil.skypeVideoCall(mReminder!!.target, this)
            } else if (Reminder.isSame(mReminder!!.type, Reminder.BY_SKYPE)) {
                TelephonyUtil.skypeChat(mReminder!!.target, this)
            }
        } else if (isAppType) {
            openApplication()
        } else if (Reminder.isSame(mReminder!!.type, Reminder.BY_DATE_EMAIL)) {
            TelephonyUtil.sendMail(this, mReminder!!.target!!,
                    mReminder!!.subject, summary, mReminder!!.attachmentFile)
        } else {
            makeCall()
        }
        if (!Reminder.isKind(mReminder!!.type, Reminder.Kind.SMS)) {
            finish()
        }
    }

    private fun showInstallSkypeDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(R.string.skype_is_not_installed)
        builder.setPositiveButton(R.string.yes) { dialogInterface, i ->
            dialogInterface.dismiss()
            SuperUtil.installSkype(this)
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun makeCall() {
        if (mReminder == null) return
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mReminder!!.target, this)
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
        }
    }

    override fun delay() {
        if (mControl != null) {
            val delay = prefs!!.snoozeTime
            mControl!!.setDelay(delay)
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun cancel() {
        if (mControl != null) {
            mControl!!.stop()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun favourite() {
        if (mControl != null) {
            mControl!!.next()
            removeFlags()
            cancelTasks()
            showFavouriteNotification()
        }
        finish()
    }

    override fun ok() {
        if (mControl != null) {
            mControl!!.next()
            removeFlags()
            cancelTasks()
        }
        finish()
    }

    override fun showSendingError() {
        showReminder()
        binding!!.remText.text = getString(R.string.error_sending)
        binding!!.remText.contentDescription = getString(R.string.error_sending)
        binding!!.buttonCall.setImageResource(R.drawable.ic_refresh)
        binding!!.buttonCall.contentDescription = getString(R.string.acc_button_retry_to_send_message)
        if (binding!!.buttonCall.visibility == View.GONE) {
            binding!!.buttonCall.show()
        }
    }

    private fun showFavouriteNotification() {
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(summary)
        val appName: String
        if (Module.isPro) {
            appName = getString(R.string.app_name_pro)
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
        val isWear = prefs!!.isWearEnabled
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
        notificationIntent.putExtra(Constants.INTENT_ID, uuId)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(summary)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX
        if (prefs!!.isManualRemoveEnabled) {
            builder.setOngoing(false)
        } else {
            builder.setOngoing(true)
        }
        val appName: String
        if (Module.isPro) {
            appName = getString(R.string.app_name_pro)
            if (prefs!!.isLedEnabled) {
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
        if (sound != null && !isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this) || SuperUtil.checkNotificationPermission(this) && prefs!!.isSoundInSilentModeEnabled)) {
            val soundUri = soundUri
            LogUtil.d(TAG, "showReminderNotification: $soundUri")
            sound!!.playAlarm(soundUri, prefs!!.isInfiniteSoundEnabled)
        }
        if (isVibrate) {
            val pattern: LongArray
            if (prefs!!.isInfiniteVibrateEnabled) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        val isWear = prefs!!.isWearEnabled
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

    protected fun showTTSNotification(activityClass: Activity) {
        LogUtil.d(TAG, "showTTSNotification: ")
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(summary)
        val notificationIntent = Intent(this, activityClass.javaClass)
        notificationIntent.putExtra(Constants.INTENT_ID, uuId)
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX
        if (prefs!!.isManualRemoveEnabled) {
            builder.setOngoing(false)
        } else {
            builder.setOngoing(true)
        }
        val appName: String
        if (Module.isPro) {
            appName = getString(R.string.app_name_pro)
            if (prefs!!.isLedEnabled) {
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
        if (!isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this) || SuperUtil.checkNotificationPermission(this) && prefs!!.isSoundInSilentModeEnabled)) {
            playDefaultMelody()
        }
        if (isVibrate) {
            val pattern: LongArray
            if (prefs!!.isInfiniteVibrateEnabled) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder.setVibrate(pattern)
        }
        val isWear = prefs!!.isWearEnabled
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
            sound!!.playAlarm(afd)
        } catch (e: IOException) {
            e.printStackTrace()
            sound!!.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
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

        private val TAG = "ReminderDialogActivity"
        private val CALL_PERM = 612
        private val SMS_PERM = 613

        fun getLaunchIntent(context: Context, id: Int): Intent {
            val resultIntent = Intent(context, ReminderDialogActivity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
