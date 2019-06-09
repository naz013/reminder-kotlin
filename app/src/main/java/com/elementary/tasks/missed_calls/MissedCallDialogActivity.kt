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
import com.elementary.tasks.core.arch.BaseNotificationActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel
import com.elementary.tasks.databinding.ActivityMissedDialogBinding
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.sql.Date

class MissedCallDialogActivity : BaseNotificationActivity<ActivityMissedDialogBinding>(R.layout.activity_missed_dialog) {

    private lateinit var viewModel: MissedCallViewModel

    private val themeUtil: ThemeUtil by inject()

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

    override val groupName: String
        get() = "missed_call"

    private val mMissedCallObserver: Observer<in MissedCall> = Observer { missedCall ->
        if (missedCall != null) {
            showInfo(missedCall)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        super.onCreate(savedInstanceState)

        binding.contactPhoto.borderColor = themeUtil.getNoteLightColor()
        binding.contactPhoto.visibility = View.GONE

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
        binding.buttonOk.setOnClickListener { removeMissed() }
        binding.buttonSms.setOnClickListener { sendSMS() }
        binding.buttonCall.setOnClickListener { makeCall() }
        if (prefs.isTelephonyAllowed) {
            binding.buttonSms.visibility = View.VISIBLE
            binding.buttonCall.visibility = View.VISIBLE
        } else {
            binding.buttonSms.visibility = View.INVISIBLE
            binding.buttonCall.visibility = View.INVISIBLE
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
                Picasso.get().load(photo).into(binding.contactPhoto)
            } else {
                BitmapUtils.imageFromName(name ?: missedCall.number) {
                    binding.contactPhoto.setImageDrawable(it)
                }
            }
        } else {
            binding.contactPhoto.visibility = View.INVISIBLE
        }

        binding.remText.setText(R.string.last_called)
        binding.reminderTime.text = formattedTime

        binding.contactName.text = name
        binding.contactNumber.text = missedCall.number

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
        if (Permissions.checkPermission(this, CALL_PERM, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mMissedCall?.number ?: "", this)
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
            CALL_PERM -> if (Permissions.checkPermission(grantResults)) {
                makeCall()
            }
        }
    }

    private fun showMissedReminder(name: String?) {
        if (isScreenResumed) {
            return
        }
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
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
            sound?.playAlarm(soundUri, prefs.isInfiniteSoundEnabled, prefs.playbackDuration)
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
            builder.setGroup(groupName)
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
