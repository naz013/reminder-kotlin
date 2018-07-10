package com.elementary.tasks.birthdays

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.BackupBirthdaysTask
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.databinding.ActivityShowBirthdayBinding

import java.util.Calendar
import java.util.Locale
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

class ShowBirthdayActivity : BaseNotificationActivity() {

    private var binding: ActivityShowBirthdayBinding? = null
    private var viewModel: BirthdayViewModel? = null
    private var mBirthday: Birthday? = null
    protected override var isScreenResumed: Boolean = false
        private set
    protected override var summary: String? = null
        private set

    private val isBirthdaySilentEnabled: Boolean
        get() {
            var `is` = prefs!!.isSoundInSilentModeEnabled
            if (Module.isPro && !isGlobal) {
                `is` = prefs!!.isBirthdaySilentEnabled
            }
            return `is`
        }

    private val isTtsEnabled: Boolean
        get() {
            var `is` = prefs!!.isTtsEnabled
            if (Module.isPro && !isGlobal) {
                `is` = prefs!!.isBirthdayTtsEnabled
            }
            return `is`
        }

    protected override val ttsLocale: Locale?
        get() {
            var locale = Language().getLocale(this, false)
            if (Module.isPro && !isGlobal) {
                locale = Language().getLocale(this, true)
            }
            return locale
        }

    protected override val melody: String?
        get() = if (Module.isPro && !isGlobal) {
            prefs!!.birthdayMelody
        } else {
            prefs!!.melodyFile
        }

    protected override val isBirthdayInfiniteVibration: Boolean
        get() {
            var vibrate = prefs!!.isInfiniteVibrateEnabled
            if (Module.isPro && !isGlobal) {
                vibrate = prefs!!.isBirthdayInfiniteVibrationEnabled
            }
            return vibrate
        }

    protected override val isBirthdayInfiniteSound: Boolean
        get() {
            var isLooping = prefs!!.isInfiniteSoundEnabled
            if (Module.isPro && !isGlobal) {
                isLooping = prefs!!.isBirthdayInfiniteSoundEnabled
            }
            return isLooping
        }

    protected override val isVibrate: Boolean
        get() {
            var vibrate = prefs!!.isVibrateEnabled
            if (Module.isPro && !isGlobal) {
                vibrate = prefs!!.isBirthdayVibrationEnabled
            }
            return vibrate
        }

    protected override val uuId: String?
        get() = if (mBirthday != null) {
            mBirthday!!.uuId
        } else
            ""

    protected override val id: Int
        get() = if (mBirthday != null) {
            mBirthday!!.uniqueId
        } else
            0

    protected override val ledColor: Int
        get() {
            var ledColor = LED.getLED(prefs!!.ledColor)
            if (Module.isPro && !isGlobal) {
                ledColor = LED.getLED(prefs!!.birthdayLedColor)
            }
            return ledColor
        }

    protected override val isAwakeDevice: Boolean
        get() {
            var isWake = prefs!!.isDeviceAwakeEnabled
            if (Module.isPro && !isGlobal) {
                isWake = prefs!!.isBirthdayWakeEnabled
            }
            return isWake
        }

    protected override val maxVolume: Int
        get() = prefs!!.loudness

    protected override val isGlobal: Boolean
        get() = prefs!!.isBirthdayGlobalEnabled

    protected override val isUnlockDevice: Boolean
        get() = prefs!!.isDeviceUnlockEnabled

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        val key = intent.getIntExtra(Constants.INTENT_ID, 0)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_birthday)
        binding!!.card.setCardBackgroundColor(themeUtil!!.cardStyle)
        if (Module.isLollipop) {
            binding!!.card.cardElevation = Configs.CARD_ELEVATION
        }
        loadImage(binding!!.bgImage)
        colorify(binding!!.buttonOk, binding!!.buttonCall, binding!!.buttonSend)

        binding!!.buttonOk.setOnClickListener { view -> ok() }
        binding!!.buttonCall.setOnClickListener { view -> call() }
        binding!!.buttonSend.setOnClickListener { view -> sendSMS() }

        binding!!.buttonOk.setImageResource(R.drawable.ic_done_black_24dp)
        binding!!.buttonCall.setImageResource(R.drawable.ic_call_black_24dp)
        binding!!.buttonSend.setImageResource(R.drawable.ic_send_black_24dp)

        binding!!.contactPhoto.borderColor = themeUtil!!.getColor(themeUtil!!.colorPrimary())
        binding!!.contactPhoto.visibility = View.GONE

        initViewModel(key)
    }

    private fun initViewModel(id: Int) {
        viewModel = ViewModelProviders.of(this, BirthdayViewModel.Factory(application, id)).get(BirthdayViewModel::class.java)
        viewModel!!.birthday.observe(this, { birthday ->
            if (birthday != null) {
                showBirthday(birthday!!)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> close()
                }
            }
        })
    }

    private fun showBirthday(birthday: Birthday) {
        this.mBirthday = birthday

        if (!TextUtils.isEmpty(birthday.number) && checkContactPermission()) {
            birthday.number = Contacts.getNumber(birthday.name, this)
        }
        if (birthday.contactId == 0 && !TextUtils.isEmpty(birthday.number) && checkContactPermission()) {
            birthday.contactId = Contacts.getIdFromNumber(birthday.number, this)
        }
        val photo = Contacts.getPhoto(birthday.contactId.toLong())
        if (photo != null) {
            binding!!.contactPhoto.setImageURI(photo)
        } else {
            binding!!.contactPhoto.visibility = View.GONE
        }
        val years = TimeUtil.getAgeFormatted(this, birthday.date)
        binding!!.userName.text = birthday.name
        binding!!.userName.contentDescription = birthday.name
        binding!!.userYears.text = years
        binding!!.userYears.contentDescription = years
        summary = birthday.name + "\n" + years
        if (TextUtils.isEmpty(birthday.number)) {
            binding!!.buttonCall.hide()
            binding!!.buttonSend.hide()
            binding!!.userNumber.visibility = View.GONE
        } else {
            binding!!.userNumber.text = birthday.number
            binding!!.userNumber.contentDescription = birthday.number
        }
        showNotification(TimeUtil.getAge(birthday.date), birthday.name)
        if (isTtsEnabled) {
            startTts()
        }
    }

    private fun checkContactPermission(): Boolean {
        return Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)
    }

    fun showNotification(years: Int, name: String?) {
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        builder.setContentTitle(name)
        builder.setContentText(TimeUtil.getAgeFormatted(this, years))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_cake_white_24dp)
            builder.color = ViewUtils.getColor(this, R.color.bluePrimary)
        } else {
            builder.setSmallIcon(R.drawable.ic_cake_nv_white)
        }
        if (!isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this) || SuperUtil.checkNotificationPermission(this) && isBirthdaySilentEnabled)) {
            val sound = sound
            sound?.playAlarm(soundUri, isBirthdayInfiniteSound)
        }
        if (isVibrate) {
            var pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            if (isBirthdayInfiniteVibration) {
                pattern = longArrayOf(150, 86400000)
            }
            builder.setVibrate(pattern)
        }
        if (Module.isPro) {
            builder.setLights(ledColor, 500, 1000)
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
            showWearNotification(name)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFlags()
        if (prefs!!.isAutoBackupEnabled) {
            BackupTask(this).execute()
        }
        BackupBirthdaysTask(this).execute()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs!!.isFoldingEnabled) {
            removeFlags()
            finish()
        } else {
            Toast.makeText(this@ShowBirthdayActivity, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    override fun call() {
        makeCall()
    }

    private fun makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE) && mBirthday != null) {
            TelephonyUtil.makeCall(mBirthday!!.number, this)
            updateBirthday(mBirthday)
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE)
        }
    }

    override fun delay() {
        close()
    }

    override fun cancel() {
        sendSMS()
    }

    private fun sendSMS() {
        if (Permissions.checkPermission(this@ShowBirthdayActivity, Permissions.SEND_SMS) && mBirthday != null) {
            TelephonyUtil.sendSms(mBirthday!!.number, this@ShowBirthdayActivity)
            updateBirthday(mBirthday)
        } else {
            Permissions.requestPermission(this@ShowBirthdayActivity, SMS_PERM, Permissions.SEND_SMS)
        }
    }

    override fun favourite() {
        close()
    }

    override fun ok() {
        updateBirthday(mBirthday)
    }

    private fun updateBirthday(birthday: Birthday?) {
        if (birthday != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val year = calendar.get(Calendar.YEAR)
            birthday.showedYear = year
            viewModel!!.saveBirthday(birthday)
        }
    }

    private fun close() {
        removeFlags()
        discardNotification(id)
        finish()
    }

    override fun showSendingError() {
        binding!!.buttonCall.setImageResource(R.drawable.ic_refresh)
        binding!!.buttonCall.contentDescription = getString(R.string.acc_button_retry_to_send_message)
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
            SMS_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMS()
            }
        }
    }

    companion object {

        private val CALL_PERM = 612
        private val SMS_PERM = 613

        fun getLaunchIntent(context: Context, id: Int): Intent {
            val resultIntent = Intent(context, ShowBirthdayActivity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
