package com.elementary.tasks.missed_calls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel
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

    override val isGlobal: Boolean
        get() = false

    override val isUnlockDevice: Boolean
        get() = prefs.isDeviceUnlockEnabled

    override val maxVolume: Int
        get() = prefs.loudness

    override val priority: Int
        get() = prefs.missedCallPriority

    private val mMissedCallObserver: Observer<in MissedCall> = Observer { missedCall ->
        if (missedCall != null) {
            showInfo(missedCall)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missed_dialog)

        contactPhoto.borderColor = themeUtil.getNoteLightColor()
        contactPhoto.visibility = View.GONE

        initButtons()

        if (savedInstanceState != null) {
            isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
        }

        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_IS_ROTATED, true)
        super.onSaveInstanceState(outState)
    }

    private fun initButtons() {
        buttonOk.setOnClickListener { removeMissed() }
        buttonSms.setOnClickListener { sendSMS() }
        buttonCall.setOnClickListener { makeCall() }
        if (prefs.isTelephonyAllowed) {
            buttonSms.visibility = View.VISIBLE
            buttonCall.visibility = View.VISIBLE
        } else {
            buttonSms.visibility = View.INVISIBLE
            buttonCall.visibility = View.INVISIBLE
        }
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
        viewModel = ViewModelProviders.of(this, MissedCallViewModel.Factory(number))
                .get(MissedCallViewModel::class.java)
        viewModel.missedCall.observeForever(mMissedCallObserver)
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                    else -> {
                    }
                }
            }
        })
        lifecycle.addObserver(viewModel)
        if (number == "" && BuildConfig.DEBUG) {
            loadTest()
        }
    }

    private fun showInfo(missedCall: MissedCall) {
        if (isEventShowed) return
        this.mMissedCall = missedCall
        var formattedTime = ""
        try {
            formattedTime = TimeUtil.getTime(Date(missedCall.dateTime), prefs.is24HourFormat, prefs.appLanguage)
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
        viewModel.missedCall.removeObserver(mMissedCallObserver)
        lifecycle.removeObserver(viewModel)
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
        if (Permissions.ensurePermissions(this, CALL_PERM, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mMissedCall!!.number, this@MissedCallDialogActivity)
            removeMissed()
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
        viewModel.missedCall.removeObserver(mMissedCallObserver)
        val missedCall = mMissedCall
        if (missedCall != null) {
            viewModel.deleteMissedCall(missedCall)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALL_PERM -> if (Permissions.isAllGranted(grantResults)) {
                makeCall()
            }
        }
    }

    private fun showMissedReminder(name: String?) {
        if (isScreenResumed) {
            return
        }
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
        builder.setSmallIcon(R.drawable.ic_twotone_call_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
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

    companion object {
        private const val ARG_TEST = "arg_test"
        private const val ARG_IS_ROTATED = "arg_rotated"
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