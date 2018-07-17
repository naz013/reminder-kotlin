package com.elementary.tasks.missed_calls

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.missedCalls.MissedCallViewModel
import com.elementary.tasks.databinding.ActivityReminderDialogBinding

import java.sql.Date
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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
class MissedCallDialogActivity : BaseNotificationActivity() {

    private var binding: ActivityReminderDialogBinding? = null
    private var viewModel: MissedCallViewModel? = null

    private var mMissedCall: MissedCall? = null
    protected override var isScreenResumed: Boolean = false
        private set

    protected override val melody: String?
        get() = null

    protected override val isVibrate: Boolean
        get() = prefs!!.isVibrateEnabled

    protected override val summary: String?
        get() = mMissedCall!!.number

    protected override val uuId: String?
        get() = null

    protected override val id: Int
        get() = mMissedCall!!.uniqueId

    protected override val ledColor: Int
        get() = LED.getLED(prefs!!.ledColor)

    protected override val isAwakeDevice: Boolean
        get() = prefs!!.isDeviceAwakeEnabled

    protected override val isGlobal: Boolean
        get() = false

    protected override val isUnlockDevice: Boolean
        get() = prefs!!.isDeviceUnlockEnabled

    protected override val maxVolume: Int
        get() = prefs!!.loudness

    override fun onCreate(savedInstanceState: Bundle?) {
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog)

        binding!!.card.setCardBackgroundColor(themeUtil!!.cardStyle)
        if (Module.isLollipop) binding!!.card.cardElevation = Configs.CARD_ELEVATION
        binding!!.container.visibility = View.GONE
        binding!!.subjectContainer.visibility = View.GONE
        loadImage(binding!!.bgImage)
        colorify(binding!!.buttonOk, binding!!.buttonCancel, binding!!.buttonCall, binding!!.buttonDelay,
                binding!!.buttonDelayFor, binding!!.buttonNotification, binding!!.buttonEdit)
        binding!!.buttonDelay.hide()
        binding!!.buttonDelayFor.hide()
        binding!!.buttonNotification.hide()
        binding!!.buttonEdit.hide()

        binding!!.buttonOk.setImageResource(R.drawable.ic_done_black_24dp)
        binding!!.buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp)
        binding!!.buttonCall.setImageResource(R.drawable.ic_call_black_24dp)

        binding!!.contactPhoto.borderColor = themeUtil!!.getColor(themeUtil!!.colorPrimary())
        binding!!.contactPhoto.visibility = View.GONE

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, MissedCallViewModel.Factory(application,
                intent.getStringExtra(Constants.INTENT_ID))).get(MissedCallViewModel::class.java)
        viewModel!!.missedCall.observe(this, { missedCall ->
            if (missedCall != null) {
                showInfo(missedCall!!)
            } else {
                closeWindow()
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
    }

    private fun showInfo(missedCall: MissedCall) {
        this.mMissedCall = missedCall
        var formattedTime = ""
        try {
            formattedTime = TimeUtil.getTime(Date(missedCall.dateTime), prefs!!.is24HourFormatEnabled)
        } catch (e: NullPointerException) {
            LogUtil.d(TAG, "onCreate: " + e.localizedMessage)
        }

        val name = Contacts.getNameFromNumber(missedCall.number, this)
        val wearMessage = (name ?: "") + "\n" + missedCall.number
        if (missedCall.number != null) {
            val conID = Contacts.getIdFromNumber(missedCall.number, this).toLong()
            val photo = Contacts.getPhoto(conID)
            if (photo != null) {
                binding!!.contactPhoto.setImageURI(photo)
            } else {
                binding!!.contactPhoto.visibility = View.GONE
            }
            binding!!.remText.setText(R.string.missed_call)
            binding!!.contactInfo.text = wearMessage
            binding!!.actionDirect.setText(R.string.from)
            binding!!.someView.setText(R.string.last_called)
            binding!!.messageView.text = formattedTime
            binding!!.container.visibility = View.VISIBLE
        }
        binding!!.buttonCancel.setOnClickListener { v -> sendSMS() }
        binding!!.buttonOk.setOnClickListener { v -> ok() }
        binding!!.buttonCall.setOnClickListener { v -> call() }
        showMissedReminder(if (name == null || name.matches("".toRegex())) missedCall.number else name)
        init()
    }

    private fun closeWindow() {
        removeFlags()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFlags()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs!!.isFoldingEnabled) {
            closeWindow()
        } else {
            Toast.makeText(this@MissedCallDialogActivity, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    override fun call() {
        makeCall()
    }

    private fun makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mMissedCall!!.number, this@MissedCallDialogActivity)
            removeMissed()
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
        }
    }

    override fun delay() {
        closeWindow()
    }

    override fun cancel() {
        sendSMS()
    }

    private fun sendSMS() {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.type = "vnd.android-dir/mms-sms"
        sendIntent.putExtra("address", mMissedCall!!.number)
        startActivity(Intent.createChooser(sendIntent, "SMS:"))
        removeMissed()
    }

    override fun favourite() {
        closeWindow()
    }

    override fun ok() {
        removeMissed()
    }

    private fun removeMissed() {
        if (mMissedCall != null) {
            viewModel!!.deleteMissedCall(mMissedCall!!)
        }
    }

    override fun showSendingError() {
        binding!!.remText.text = getString(R.string.error_sending)
        binding!!.buttonCall.setImageResource(R.drawable.ic_refresh)
        if (binding!!.buttonCall.visibility == View.GONE) {
            binding!!.buttonCall.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            CALL_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall()
            }
        }
    }

    private fun showMissedReminder(name: String?) {
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(name)
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
            builder.setSmallIcon(R.drawable.ic_call_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_call_nv_white)
        }
        if (Module.isLollipop) {
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        }
        if (sound != null && !isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this) || SuperUtil.checkNotificationPermission(this) && prefs!!.isSoundInSilentModeEnabled)) {
            val soundUri = soundUri
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

    companion object {

        private val TAG = "MCDialogActivity"

        private val CALL_PERM = 612
    }
}
