package com.elementary.tasks.missed_calls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel
import com.elementary.tasks.databinding.ActivityMissedDialogBinding
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.sql.Date

class MissedCallDialog29Activity : BindingActivity<ActivityMissedDialogBinding>(R.layout.activity_missed_dialog) {

    private lateinit var viewModel: MissedCallViewModel

    private var mMissedCall: MissedCall? = null
    private var isEventShowed = false

    private val id: Int
        get() = mMissedCall?.uniqueId ?: 2122

    private val mMissedCallObserver: Observer<in MissedCall> = Observer { missedCall ->
        if (missedCall != null) {
            showInfo(missedCall)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.contactPhoto.borderColor = ThemeUtil.getThemeSecondaryColor(this)
        binding.contactPhoto.visibility = View.GONE

        initButtons()
        initViewModel()
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
        viewModel = ViewModelProvider(this, MissedCallViewModel.Factory(number))
                .get(MissedCallViewModel::class.java)
        viewModel.missedCall.observeForever(mMissedCallObserver)
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> finish()
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
        val name: String
        if (missedCall.number.isNotEmpty() && Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
            name = Contacts.getNameFromNumber(missedCall.number, this) ?: missedCall.number
            val conID = Contacts.getIdFromNumber(missedCall.number, this)
            val photo = Contacts.getPhoto(conID)
            if (photo != null) {
                Picasso.get().load(photo).into(binding.contactPhoto)
            } else {
                BitmapUtils.imageFromName(name) {
                    binding.contactPhoto.setImageDrawable(it)
                }
            }
        } else {
            name = missedCall.number
            binding.contactPhoto.visibility = View.INVISIBLE
        }

        binding.remText.setText(R.string.last_called)
        binding.reminderTime.text = formattedTime

        binding.contactName.text = name
        binding.contactNumber.text = missedCall.number
    }

    private fun discardMedia() {
        ContextCompat.startForegroundService(this,
                EventOperationalService.getIntent(this, mMissedCall?.number ?: "",
                        EventOperationalService.TYPE_MISSED,
                        EventOperationalService.ACTION_STOP,
                        id))
    }

    private fun discardNotification(id: Int) {
        Timber.d("discardNotification: $id")
        discardMedia()
        Notifier.getManager(this)?.cancel(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.missedCall.removeObserver(mMissedCallObserver)
        lifecycle.removeObserver(viewModel)
    }

    override fun onBackPressed() {
        discardMedia()
        if (prefs.isFoldingEnabled) {
            finish()
        } else {
            Toast.makeText(this@MissedCallDialog29Activity, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
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
        discardNotification(id)
        isEventShowed = true
        viewModel.missedCall.removeObserver(mMissedCallObserver)
        mMissedCall?.let {
            viewModel.deleteMissedCall(it)
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

    companion object {
        private const val ARG_TEST = "arg_test"
        private const val ARG_TEST_ITEM = "arg_test_item"
        private const val CALL_PERM = 612

        fun mockTest(context: Context, missedCall: MissedCall) {
            val intent = Intent(context, MissedCallDialog29Activity::class.java)
            intent.putExtra(ARG_TEST, true)
            intent.putExtra(ARG_TEST_ITEM, missedCall)
            context.startActivity(intent)
        }

        fun getLaunchIntent(context: Context, id: String): Intent {
            val resultIntent = Intent(context, MissedCallDialog29Activity::class.java)
            resultIntent.putExtra(Constants.INTENT_ID, id)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            return resultIntent
        }
    }
}
