package com.elementary.tasks.missed_calls

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.contacts.Contacts
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BitmapUtils
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.missed_calls.MissedCallViewModel
import com.elementary.tasks.databinding.ActivityMissedDialogBinding
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class MissedCallDialog29Activity : BindingActivity<ActivityMissedDialogBinding>() {

  private val viewModel by viewModel<MissedCallViewModel> { parametersOf(getNumber()) }
  private val permissionFlow = PermissionFlow(this, dialogues)
  private val dateTimeManager by inject<DateTimeManager>()
  private val contactsReader by inject<ContactsReader>()

  private var mMissedCall: MissedCall? = null
  private var isEventShowed = false
  private val id: Int
    get() = mMissedCall?.uniqueId ?: 2122
  private val mMissedCallObserver: Observer<in MissedCall> = Observer { missedCall ->
    if (missedCall != null) {
      showInfo(missedCall)
    }
  }

  override fun inflateBinding() = ActivityMissedDialogBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
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
    val isMocked = intentBoolean(ARG_TEST)
    if (isMocked) {
      val missedCall = intentParcelable(ARG_TEST_ITEM, MissedCall::class.java)
      if (missedCall != null) showInfo(missedCall)
    }
  }

  private fun getNumber() = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    viewModel.missedCall.observeForever(mMissedCallObserver)
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> finish()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
    if (getNumber() == "" && BuildConfig.DEBUG) {
      loadTest()
    }
  }

  private fun showInfo(missedCall: MissedCall) {
    if (isEventShowed) return
    this.mMissedCall = missedCall
    val formattedTime = dateTimeManager.getTime(
      dateTimeManager.fromMillis(missedCall.dateTime).toLocalTime()
    )
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
    notifier.cancel(id)
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.missedCall.removeObserver(mMissedCallObserver)
    lifecycle.removeObserver(viewModel)
  }

  override fun handleBackPress(): Boolean {
    discardMedia()
    if (prefs.isFoldingEnabled) {
      finish()
    } else {
      toast(R.string.select_one_of_item)
    }
    return true
  }

  private fun makeCall() {
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
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

  companion object {
    private const val ARG_TEST = "arg_test"
    private const val ARG_TEST_ITEM = "arg_test_item"

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
