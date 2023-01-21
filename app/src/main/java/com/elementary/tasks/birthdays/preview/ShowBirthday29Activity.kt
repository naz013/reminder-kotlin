package com.elementary.tasks.birthdays.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayShow
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.services.EventOperationalService
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ActivityDialogBirthdayBinding
import com.squareup.picasso.Picasso
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class ShowBirthday29Activity : BindingActivity<ActivityDialogBirthdayBinding>() {

  private val viewModel by viewModel<ShowBirthdayViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  override fun inflateBinding() = ActivityDialogBirthdayBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding.buttonOk.setOnClickListener { ok() }
    binding.buttonCall.setOnClickListener { makeCall() }
    binding.buttonSms.setOnClickListener { sendSMS() }

    binding.contactPhoto.borderColor = ThemeProvider.getThemeSecondaryColor(this)
    binding.contactPhoto.visibility = View.GONE

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
      loadTest()
    }
  }

  private fun loadTest() {
    val isMocked = intentBoolean(ARG_TEST, false)
    if (isMocked) {
      viewModel.onTestLoad(intentParcelable(ARG_TEST_ITEM, Birthday::class.java))
    }
  }

  private fun showBirthday(birthday: UiBirthdayShow) {
    if (viewModel.isEventShowed) return

    birthday.photo?.also {
      Picasso.get().load(it).into(binding.contactPhoto)
      binding.contactPhoto.visible()
    } ?: run { binding.contactPhoto.gone() }

    binding.userName.text = birthday.name
    binding.userName.contentDescription = birthday.name

    binding.userYears.text = birthday.ageFormatted
    binding.userYears.contentDescription = birthday.ageFormatted

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

  private fun discardMedia() {
    ContextCompat.startForegroundService(
      this,
      EventOperationalService.getIntent(
        this,
        viewModel.getId(),
        EventOperationalService.TYPE_BIRTHDAY,
        EventOperationalService.ACTION_STOP,
        viewModel.getUniqueId()
      )
    )
  }

  private fun discardNotification(id: Int) {
    Timber.d("discardNotification: $id")
    discardMedia()
    notifier.cancel(id)
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
      viewModel.getNumber()?.also {
        TelephonyUtil.makeCall(it, this)
        updateBirthday()
      } ?: run { ok() }
    }
  }

  private fun sendSMS() {
    viewModel.getNumber()?.also {
      TelephonyUtil.sendSms(it, this)
      updateBirthday()
    } ?: run { ok() }
  }

  private fun ok() {
    updateBirthday()
  }

  private fun updateBirthday() {
    discardNotification(viewModel.getUniqueId())
    viewModel.isEventShowed = true
    viewModel.saveBirthday()
  }

  companion object {

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
