package com.elementary.tasks.birthdays.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel
import com.elementary.tasks.databinding.ActivityShowBirthdayBinding
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.util.*

class ShowBirthday29Activity : BindingActivity<ActivityShowBirthdayBinding>(R.layout.activity_show_birthday) {

  private lateinit var viewModel: BirthdayViewModel

  private var mBirthday: Birthday? = null
  private var isEventShowed = false

  private val uuId: String
    get() = mBirthday?.uuId ?: ""

  private val id: Int
    get() = mBirthday?.uniqueId ?: 2123

  private val mBirthdayObserver: Observer<in Birthday> = Observer { birthday ->
    if (birthday != null) {
      showBirthday(birthday)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val key = intent.getStringExtra(Constants.INTENT_ID) ?: ""

    binding.buttonOk.setOnClickListener { ok() }
    binding.buttonCall.setOnClickListener { makeCall() }
    binding.buttonSms.setOnClickListener { sendSMS() }

    binding.contactPhoto.borderColor = ThemeUtil.getThemeSecondaryColor(this)
    binding.contactPhoto.visibility = View.GONE

    initViewModel(key)
  }

  private fun initViewModel(id: String) {
    viewModel = ViewModelProvider(this, BirthdayViewModel.Factory(id)).get(BirthdayViewModel::class.java)
    viewModel.birthday.observeForever(mBirthdayObserver)
    viewModel.result.observe(this, { commands ->
      if (commands != null) {
        when (commands) {
          Commands.SAVED -> finish()
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

    if (!TextUtils.isEmpty(birthday.number) && Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
      birthday.number = Contacts.getNumber(birthday.name, this)
    }
    if (birthday.contactId == 0L && !TextUtils.isEmpty(birthday.number) && Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
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
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.birthday.removeObserver(mBirthdayObserver)
    lifecycle.removeObserver(viewModel)
  }

  private fun discardMedia() {
    ContextCompat.startForegroundService(this,
      EventOperationalService.getIntent(this, uuId,
        EventOperationalService.TYPE_BIRTHDAY,
        EventOperationalService.ACTION_STOP,
        id))
  }

  private fun discardNotification(id: Int) {
    Timber.d("discardNotification: $id")
    discardMedia()
    Notifier.getManager(this)?.cancel(id)
  }

  override fun onBackPressed() {
    discardMedia()
    if (prefs.isFoldingEnabled) {
      finish()
    } else {
      Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
    }
  }

  private fun makeCall() {
    if (Permissions.checkPermission(this, CALL_PERM, Permissions.CALL_PHONE) && mBirthday != null) {
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
    discardNotification(id)
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

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        CALL_PERM -> makeCall()
      }
    }
  }

  companion object {

    private const val CALL_PERM = 612
    private const val ARG_TEST = "arg_test"
    private const val ARG_TEST_ITEM = "arg_test_item"

    fun mockTest(context: Context, birthday: Birthday) {
      val intent = Intent(context, ShowBirthday29Activity::class.java)
      intent.putExtra(ARG_TEST, true)
      intent.putExtra(ARG_TEST_ITEM, birthday)
      context.startActivity(intent)
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      val resultIntent = Intent(context, ShowBirthday29Activity::class.java)
      resultIntent.putExtra(Constants.INTENT_ID, id)
      resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      return resultIntent
    }
  }
}
