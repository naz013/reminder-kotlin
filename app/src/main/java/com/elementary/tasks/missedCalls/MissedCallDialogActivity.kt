package com.elementary.tasks.missedCalls

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.missedCalls.MissedCallViewModel
import kotlinx.android.synthetic.main.activity_missed_dialog.*
import timber.log.Timber
import java.sql.Date

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

    private lateinit var viewModel: MissedCallViewModel

    private var mMissedCall: MissedCall? = null
    private var isEventShowed = false
    override var isScreenResumed: Boolean = false
        private set

    override val melody: String
        get() = ""

    override val isVibrate: Boolean
        get() = prefs.isVibrateEnabled

    override val summary: String
        get() = mMissedCall?.number ?: ""

    override val uuId: String
        get() = ""

    override val id: Int
        get() = mMissedCall?.uniqueId ?: 0

    override val ledColor: Int
        get() = LED.getLED(prefs.ledColor)

    override val isAwakeDevice: Boolean
        get() = prefs.isDeviceAwakeEnabled

    override val isGlobal: Boolean
        get() = false

    override val isUnlockDevice: Boolean
        get() = prefs.isDeviceUnlockEnabled

    override val maxVolume: Int
        get() = prefs.loudness

    override fun onCreate(savedInstanceState: Bundle?) {
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missed_dialog)

        contactPhoto.borderColor = themeUtil.getColor(themeUtil.colorPrimary())
        contactPhoto.visibility = View.GONE

        initButtons()

        initViewModel()
    }

    private fun initButtons() {
        buttonSms.setOnClickListener { sendSMS() }
        buttonOk.setOnClickListener { removeMissed() }
        buttonCall.setOnClickListener { makeCall() }
    }

    private fun loadTest() {
        val isMocked = intent.getBooleanExtra(ARG_TEST, false)
        if (isMocked) {
            val missedCall = intent.getSerializableExtra(ARG_TEST_ITEM) as MissedCall?
            if (missedCall != null) showInfo(missedCall)
        }
    }

    private fun initViewModel() {
        val number = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        viewModel = ViewModelProviders.of(this, MissedCallViewModel.Factory(application, number))
                .get(MissedCallViewModel::class.java)
        viewModel.missedCall.observe(this, Observer { missedCall ->
            if (missedCall != null) {
                showInfo(missedCall)
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                }
            }
        })
        if (number == "" && BuildConfig.DEBUG) {
            loadTest()
        }
    }

    private fun showInfo(missedCall: MissedCall) {
        if (isEventShowed) return
        this.mMissedCall = missedCall
        var formattedTime = ""
        try {
            formattedTime = TimeUtil.getTime(Date(missedCall.dateTime), prefs.is24HourFormatEnabled)
        } catch (e: NullPointerException) {
            Timber.d("showInfo: ${e.message}")
        }

        val name = Contacts.getNameFromNumber(missedCall.number, this)
        if (missedCall.number != "") {
            val conID = Contacts.getIdFromNumber(missedCall.number, this)
            val photo = Contacts.getPhoto(conID)
            if (photo != null) {
                contactPhoto.setImageURI(photo)
            } else {
                contactPhoto.setImageDrawable(BitmapUtils.imageFromName(name ?: missedCall.number))
            }
        } else {
            contactPhoto.visibility = View.INVISIBLE
        }

        remText.setText(R.string.last_called)
        reminder_time.text = formattedTime

        contactName.text = name
        contactNumber.text = missedCall.number

        showMissedReminder(if (name == null || name.matches("".toRegex())) missedCall.number else name)
        init()
    }

    private fun closeWindow() {
        discardNotification(id)
        removeFlags()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFlags()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs.isFoldingEnabled) {
            closeWindow()
        } else {
            Toast.makeText(this@MissedCallDialogActivity, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mMissedCall!!.number, this@MissedCallDialogActivity)
            removeMissed()
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
        }
    }

    private fun sendSMS() {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.type = "vnd.android-dir/mms-sms"
        sendIntent.putExtra("address", mMissedCall?.number)
        startActivity(Intent.createChooser(sendIntent, "SMS:"))
        removeMissed()
    }

    private fun removeMissed() {
        isEventShowed = true
        val missedCall = mMissedCall
        if (missedCall != null) {
            viewModel.deleteMissedCall(missedCall)
        }
    }

    override fun showSendingError() {
        remText.text = getString(R.string.error_sending)
        buttonSms.text = getString(R.string.retry)
        if (buttonSms.visibility == View.GONE) {
            buttonSms.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
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
            builder.setSmallIcon(R.drawable.ic_twotone_call_white)
        } else {
            builder.setSmallIcon(R.drawable.ic_call_nv_white)
        }
        if (Module.isLollipop) {
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        }
        if (sound != null && !isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this)
                        || SuperUtil.checkNotificationPermission(this)
                        && prefs.isSoundInSilentModeEnabled)) {
            val soundUri = soundUri
            sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled)
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
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    companion object {
        private const val ARG_TEST = "arg_test"
        private const val ARG_TEST_ITEM = "arg_test_item"
        private const val CALL_PERM = 612

        fun mockTest(context: Context, missedCall: MissedCall) {
            val intent = Intent(context, MissedCallDialogActivity::class.java)
            intent.putExtra(ARG_TEST, true)
            intent.putExtra(ARG_TEST_ITEM, missedCall)
            context.startActivity(intent)
        }
    }
}
