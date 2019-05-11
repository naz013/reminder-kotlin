package com.elementary.tasks.birthdays.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.databinding.ActivityShowBirthdayBinding
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class ShowBirthdayActivity : BaseNotificationActivity<ActivityShowBirthdayBinding>(R.layout.activity_show_birthday) {

    private lateinit var viewModel: BirthdayViewModel

    private val themeUtil: ThemeUtil by inject()

    private var mBirthday: Birthday? = null
    private var isEventShowed = false
    override var isScreenResumed: Boolean = false
        private set
    override var summary: String = ""
        private set
    override val groupName: String
        get() = "birthdays"

    private val isBirthdaySilentEnabled: Boolean
        get() {
            var isEnabled = prefs.isSoundInSilentModeEnabled
            if (Module.isPro && !isGlobal) {
                isEnabled = prefs.isBirthdaySilentEnabled
            }
            return isEnabled
        }

    private val isTtsEnabled: Boolean
        get() {
            var isEnabled = prefs.isTtsEnabled
            if (Module.isPro && !isGlobal) {
                isEnabled = prefs.isBirthdayTtsEnabled
            }
            return isEnabled
        }

    override val ttsLocale: Locale?
        get() {
            var locale = language.getLocale(false)
            if (Module.isPro && !isGlobal) {
                locale = language.getLocale(true)
            }
            return locale
        }

    override val melody: String
        get() = if (Module.isPro && !isGlobal) {
            prefs.birthdayMelody
        } else {
            prefs.melodyFile
        }

    override val isBirthdayInfiniteVibration: Boolean
        get() {
            var vibrate = prefs.isInfiniteVibrateEnabled
            if (Module.isPro && !isGlobal) {
                vibrate = prefs.isBirthdayInfiniteVibrationEnabled
            }
            return vibrate
        }

    override val isBirthdayInfiniteSound: Boolean
        get() {
            var isLooping = prefs.isInfiniteSoundEnabled
            if (Module.isPro && !isGlobal) {
                isLooping = prefs.isBirthdayInfiniteSoundEnabled
            }
            return isLooping
        }

    override val isVibrate: Boolean
        get() {
            var vibrate = prefs.isVibrateEnabled
            if (Module.isPro && !isGlobal) {
                vibrate = prefs.isBirthdayVibrationEnabled
            }
            return vibrate
        }

    override val uuId: String
        get() = if (mBirthday != null) {
            mBirthday?.uuId ?: ""
        } else
            ""

    override val id: Int
        get() = if (mBirthday != null) {
            mBirthday?.uniqueId ?: 112
        } else
            0

    override val ledColor: Int
        get() {
            var ledColor = LED.getLED(prefs.ledColor)
            if (Module.isPro && !isGlobal) {
                ledColor = LED.getLED(prefs.birthdayLedColor)
            }
            return ledColor
        }

    override val maxVolume: Int
        get() = prefs.loudness

    override val isGlobal: Boolean
        get() = prefs.isBirthdayGlobalEnabled

    override val isUnlockDevice: Boolean
        get() {
            var isWake = prefs.isDeviceUnlockEnabled
            if (Module.isPro && !isGlobal) {
                isWake = prefs.isBirthdayWakeEnabled
            }
            return isWake
        }

    override val priority: Int
        get() = prefs.birthdayPriority

    private val mBirthdayObserver: Observer<in Birthday> = Observer { birthday ->
        if (birthday != null) {
            showBirthday(birthday)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isScreenResumed = intent.getBooleanExtra(Constants.INTENT_NOTIFICATION, false)
        val key = intent.getStringExtra(Constants.INTENT_ID) ?: ""

        binding.buttonOk.setOnClickListener { ok() }
        binding.buttonCall.setOnClickListener { makeCall() }
        binding.buttonSms.setOnClickListener { sendSMS() }

        binding.contactPhoto.borderColor = themeUtil.getNoteLightColor()
        binding.contactPhoto.visibility = View.GONE

        if (savedInstanceState != null) {
            isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
        }

        initViewModel(key)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_IS_ROTATED, true)
        super.onSaveInstanceState(outState)
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, BirthdayViewModel.Factory(id)).get(BirthdayViewModel::class.java)
        viewModel.birthday.observeForever(mBirthdayObserver)
        viewModel.result.observe(this, Observer<Commands>{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED -> close()
                    else -> {
                    }
                }
            }
        })
        lifecycle.addObserver(viewModel)
        if (id == "" && BuildConfig.DEBUG) {
            loadTest()
        }
    }

    private fun loadTest() {
        val isMocked = intent.getBooleanExtra(ARG_TEST, false)
        if (isMocked) {
            val birthday = intent.getSerializableExtra(ARG_TEST_ITEM) as Birthday?
            if (birthday != null) showBirthday(birthday)
        }
    }

    private fun showBirthday(birthday: Birthday) {
        if (isEventShowed) return

        this.mBirthday = birthday

        if (!TextUtils.isEmpty(birthday.number) && checkContactPermission()) {
            birthday.number = Contacts.getNumber(birthday.name, this)
        }
        if (birthday.contactId == 0L && !TextUtils.isEmpty(birthday.number) && checkContactPermission()) {
            birthday.contactId = Contacts.getIdFromNumber(birthday.number, this)
        }
        val photo = Contacts.getPhoto(birthday.contactId)
        if (photo != null) {
            Picasso.get().load(photo).into(binding.contactPhoto)
            binding.contactPhoto.visibility = View.VISIBLE
        } else {
            binding.contactPhoto.visibility = View.GONE
        }
        val years = TimeUtil.getAgeFormatted(this, birthday.date, prefs.appLanguage)
        binding.userName.text = birthday.name
        binding.userName.contentDescription = birthday.name
        binding.userYears.text = years
        binding.userYears.contentDescription = years
        summary = birthday.name + "\n" + years
        if (TextUtils.isEmpty(birthday.number)) {
            binding.buttonCall.visibility = View.INVISIBLE
            binding.buttonSms.visibility = View.INVISIBLE
            binding.userNumber.visibility = View.GONE
        } else {
            binding.userNumber.text = birthday.number
            binding.userNumber.contentDescription = birthday.number
            binding.userNumber.visibility = View.VISIBLE
            if (prefs.isTelephonyAllowed) {
                binding.buttonCall.visibility = View.VISIBLE
                binding.buttonSms.visibility = View.VISIBLE
            } else {
                binding.buttonCall.visibility = View.INVISIBLE
                binding.buttonSms.visibility = View.INVISIBLE
            }
        }
        init()

        if (isTtsEnabled) {
            showTTSNotification(TimeUtil.getAge(birthday.date), birthday.name)
            startTts()
        } else {
            showNotification(TimeUtil.getAge(birthday.date), birthday.name)
        }
    }

    private fun checkContactPermission(): Boolean {
        return Permissions.checkPermission(this, Permissions.READ_CONTACTS)
    }

    private fun showNotification(years: Int, name: String) {
        if (isScreenResumed) {
            return
        }
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
        builder.setContentTitle(name)
        builder.setContentText(TimeUtil.getAgeFormatted(this, years, System.currentTimeMillis(), prefs.appLanguage))
        builder.setSmallIcon(R.drawable.ic_twotone_cake_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        if (!isScreenResumed && (!SuperUtil.isDoNotDisturbEnabled(this)
                        || SuperUtil.checkNotificationPermission(this) && isBirthdaySilentEnabled)) {
            val sound = sound
            sound?.playAlarm(soundUri, isBirthdayInfiniteSound, prefs.birthdayPlaybackDuration)
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
        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup(groupName)
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(name)
        }
    }

    private fun showTTSNotification(years: Int, name: String) {
        if (isScreenResumed) {
            return
        }
        Timber.d("showTTSNotification: ")
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SILENT)
        builder.setContentTitle(name)
        builder.setContentText(TimeUtil.getAgeFormatted(this, years, System.currentTimeMillis(), prefs.appLanguage))
        builder.setSmallIcon(R.drawable.ic_twotone_cake_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        if (isScreenResumed) {
            builder.priority = NotificationCompat.PRIORITY_LOW
        } else {
            builder.priority = priority
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.isSoundInSilentModeEnabled))) {
                playDefaultMelody()
            }
            if (isVibrate) {
                var pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
                if (isBirthdayInfiniteVibration) {
                    pattern = longArrayOf(150, 86400000)
                }
                builder.setVibrate(pattern)
            }
        }
        if (Module.isPro) {
            builder.setLights(ledColor, 500, 1000)
        }
        val isWear = prefs.isWearEnabled
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup(groupName)
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(name)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.birthday.removeObserver(mBirthdayObserver)
        lifecycle.removeObserver(viewModel)
        removeFlags()
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs.isFoldingEnabled) {
            removeFlags()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeCall() {
        if (Permissions.ensurePermissions(this, CALL_PERM, Permissions.CALL_PHONE) && mBirthday != null) {
            TelephonyUtil.makeCall(mBirthday?.number ?: "", this)
            updateBirthday(mBirthday)
        }
    }

    private fun sendSMS() {
        if (mBirthday != null) {
            TelephonyUtil.sendSms(mBirthday?.number ?: "", this)
            updateBirthday(mBirthday)
        }
    }

    private fun ok() {
        updateBirthday(mBirthday)
    }

    private fun updateBirthday(birthday: Birthday?) {
        isEventShowed = true
        viewModel.birthday.removeObserver(mBirthdayObserver)
        if (birthday != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val year = calendar.get(Calendar.YEAR)
            birthday.showedYear = year
            viewModel.saveBirthday(birthday)
        }
    }

    private fun close() {
        removeFlags()
        discardNotification(id)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                CALL_PERM -> makeCall()
            }
        }
    }

    companion object {

        private const val CALL_PERM = 612
        private const val ARG_TEST = "arg_test"
        private const val ARG_TEST_ITEM = "arg_test_item"
        private const val ARG_IS_ROTATED = "arg_rotated"

        fun mockTest(context: Context, birthday: Birthday) {
            val intent = Intent(context, ShowBirthdayActivity::class.java)
            intent.putExtra(ARG_TEST, true)
            intent.putExtra(ARG_TEST_ITEM, birthday)
            context.startActivity(intent)
        }

        fun getLaunchIntent(context: Context, id: String): Intent {
            val resultIntent = Intent(context, ShowBirthdayActivity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
