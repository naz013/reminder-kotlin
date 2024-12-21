package com.elementary.tasks.birthdays.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.os.Permissions
import com.github.naz013.feature.common.android.buildIntent
import com.github.naz013.feature.common.android.startActivity
import com.github.naz013.feature.common.android.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.android.gone
import com.elementary.tasks.core.utils.ui.setTextOrHide
import com.github.naz013.feature.common.android.transparent
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.ActivityDialogBirthdayBinding
import com.elementary.tasks.tests.TestObjects
import com.github.naz013.logging.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ShowBirthday29Activity : BindingActivity<ActivityDialogBirthdayBinding>() {

  private val viewModel by viewModel<ShowBirthdayViewModel> { parametersOf(getId()) }

  override fun inflateBinding() = ActivityDialogBirthdayBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i("Showing the birthday full screen for id: ${getId()}")

    drawBehindSystemBars(binding.rootView)

    binding.buttonOk.setOnClickListener { ok() }
    binding.buttonCall.setOnClickListener { makeCall() }
    binding.buttonSms.setOnClickListener { sendSMS() }

    binding.contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
    binding.contactPhoto.gone()

    initViewModel()
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.SAVED -> finish()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
    if (getId().isEmpty() && BuildConfig.DEBUG) {
      Logger.d("Showing the birthday full screen for test id")
      loadTest()
    }
  }

  private fun loadTest() {
    val isMocked = intentBoolean(TestObjects.ARG_TEST, false)
    if (isMocked) {
      val item = if (intentBoolean(TestObjects.ARG_TEST_HAS_NUMBER, false)) {
        TestObjects.getBirthday(number = "123456789")
      } else {
        TestObjects.getBirthday()
      }
      viewModel.onTestLoad(item)
    }
  }

  private fun showBirthday(birthday: UiBirthdayShow) {
    if (viewModel.isEventShowed) return

    birthday.photo?.also {
      binding.contactPhoto.setImageBitmap(it)
      binding.contactPhoto.visible()
    } ?: run { binding.contactPhoto.gone() }

    binding.userName.text = birthday.name
    binding.userYears.setTextOrHide(birthday.ageFormatted)

    if (birthday.number.isEmpty()) {
      binding.buttonCall.transparent()
      binding.buttonSms.transparent()
      binding.userNumber.gone()
    } else {
      binding.userNumber.text = birthday.number
      binding.userNumber.contentDescription = birthday.number
      binding.userNumber.visible()
      if (prefs.isTelephonyAllowed) {
        binding.buttonCall.visible()
        binding.buttonSms.visible()
      } else {
        binding.buttonCall.transparent()
        binding.buttonSms.transparent()
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.removeObserver(viewModel)
  }

  private fun discardNotification(id: Int) {
    notifier.cancel(id)
  }

  override fun handleBackPress(): Boolean {
    toast(R.string.select_one_of_item)
    return true
  }

  private fun makeCall() {
    Logger.i("Making a call for id: ${getId()}")
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      viewModel.getNumber()?.also {
        TelephonyUtil.makeCall(it, this)
        updateBirthday()
      } ?: run { ok() }
    }
  }

  private fun sendSMS() {
    Logger.i("Sending an SMS for id: ${getId()}")
    viewModel.getNumber()?.also {
      TelephonyUtil.sendSms(it, this)
      updateBirthday()
    } ?: run { ok() }
  }

  private fun ok() {
    Logger.i("Ok button clicked for id: ${getId()}")
    updateBirthday()
  }

  private fun updateBirthday() {
    discardNotification(viewModel.getUniqueId())
    viewModel.isEventShowed = true
    viewModel.saveBirthday()
  }

  companion object {

    fun mockTest(context: Context, hasNumber: Boolean = false) {
      context.startActivity(ShowBirthday29Activity::class.java) {
        putExtra(TestObjects.ARG_TEST, true)
        putExtra(TestObjects.ARG_TEST_HAS_NUMBER, hasNumber)
      }
    }

    fun getLaunchIntent(context: Context, id: String): Intent {
      return context.buildIntent(ShowBirthday29Activity::class.java) {
        putExtra(Constants.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
    }
  }
}
