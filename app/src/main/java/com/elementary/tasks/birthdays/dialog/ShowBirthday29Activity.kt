package com.elementary.tasks.birthdays.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.setTextOrHide
import com.elementary.tasks.databinding.ActivityDialogBirthdayBinding
import com.elementary.tasks.tests.TestObjects
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ShowBirthday29Activity : BindingActivity<ActivityDialogBirthdayBinding>() {

  private val prefs by inject<Prefs>()
  private val notifier by inject<Notifier>()
  private val viewModel by viewModel<ShowBirthdayViewModel> { parametersOf(getId()) }
  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)

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

  private fun getId() = intentString(IntentKeys.INTENT_ID)

  private fun initViewModel() {
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.resultEvent.observeEvent(this) { commands ->
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
    permissionFlowDelegate.permissionFlow.askPermission(Permissions.CALL_PHONE) {
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
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
    }
  }
}
