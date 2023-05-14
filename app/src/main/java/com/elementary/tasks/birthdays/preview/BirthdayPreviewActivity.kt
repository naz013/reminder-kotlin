package com.elementary.tasks.birthdays.preview

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.updatePadding
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ActivityBirthdayPreviewBinding
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BirthdayPreviewActivity : BindingActivity<ActivityBirthdayPreviewBinding>() {

  private val viewModel by viewModel<BirthdayPreviewViewModel> { parametersOf(idFromIntent()) }

  private val permissionFlow = PermissionFlow(this, dialogues)
  private val adsProvider = AdsProvider()
  private var initPaddingTop: Int? = null

  override fun inflateBinding() = ActivityBirthdayPreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (initPaddingTop == null) {
      initPaddingTop = binding.rootView.paddingTop
    }

    drawBehindSystemBars(binding.rootView) { insets ->
      binding.rootView.updatePadding(
        top = (initPaddingTop ?: 0) + insets.top
      )
    }

    initTopAppBar()

    binding.buttonCall.setOnClickListener { tryToMakeCall() }
    binding.buttonSms.setOnClickListener { tryToSendSms() }
    binding.buttonsView.gone()
    binding.contactPhoto.gone()
    binding.contactNameNumberView.gone()

    loadAds()

    initViewModel()
  }

  private fun loadAds() {
    if (!Module.isPro && AdsProvider.hasAds()) {
      adsProvider.showBanner(
        binding.adsHolder,
        AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID
      )
    }
  }

  private fun tryToMakeCall() {
    val number = viewModel.birthday.value?.number ?: return
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      TelephonyUtil.makeCall(number, this)
    }
  }

  private fun tryToSendSms() {
    val number = viewModel.birthday.value?.number ?: return
    TelephonyUtil.sendSms(number, this)
  }

  private fun initTopAppBar() {
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener when (menuItem.itemId) {
        R.id.action_edit -> {
          editBirthday()
          true
        }

        R.id.action_delete -> {
          dialogues.askConfirmation(this, getString(R.string.delete)) {
            if (it) viewModel.deleteBirthday()
          }
          true
        }

        else -> false
      }
    }
    binding.toolbar.setNavigationOnClickListener { finish() }
  }

  private fun showTextIfNotNull(textView: TextView, value: String?) {
    textView.visibleGone(value != null)
    textView.text = value
  }

  private fun showBirthday(birthday: UiBirthdayPreview) {
    binding.nameView.text = birthday.name

    showTextIfNotNull(binding.ageView, birthday.ageFormatted)
    showTextIfNotNull(binding.dateOfBirthView, birthday.dateOfBirth)
    showTextIfNotNull(binding.nextBirthdayDateView, birthday.nextBirthdayDate)

    if (birthday.number != null) {
      val displayName = if (birthday.contactName != null) {
        "${birthday.contactName} (${birthday.number})"
      } else {
        birthday.number
      }
      showTextIfNotNull(binding.contactNameNumberView, displayName)

      if (birthday.photo != null) {
        binding.contactPhoto.visible()
        binding.contactPhoto.setImageBitmap(birthday.photo)
      } else {
        binding.contactPhoto.gone()
      }
    } else {
      binding.buttonsView.gone()
      binding.contactPhoto.gone()
      binding.contactNameNumberView.gone()
    }

    if (birthday.hasBirthdayToday) {
      if (birthday.number != null) {
        binding.buttonsView.visible()
      }

      if (viewModel.canShowAnimation) {
        binding.animationView.visible()
        binding.animationView.postDelayed({ binding.animationView.playAnimation() }, 1000L)
        binding.animationView.postDelayed({ binding.animationView.gone() }, 3500L)
        viewModel.canShowAnimation = false
      }
    } else {
      binding.animationView.gone()
    }
  }

  private fun idFromIntent(): String = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.DELETED -> finishAfterTransition()
        else -> {
        }
      }
    }
  }

  private fun editBirthday() {
    PinLoginActivity.openLogged(
      this, Intent(this, AddBirthdayActivity::class.java)
        .putExtra(Constants.INTENT_ID, idFromIntent())
    )
  }

  override fun requireLogin() = true
}
