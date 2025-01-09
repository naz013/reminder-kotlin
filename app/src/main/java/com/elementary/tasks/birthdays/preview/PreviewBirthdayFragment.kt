package com.elementary.tasks.birthdays.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.databinding.FragmentBirthdayPreviewBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PreviewBirthdayFragment : BaseToolbarFragment<FragmentBirthdayPreviewBinding>() {

  private val viewModel by viewModel<BirthdayPreviewViewModel> { parametersOf(idFromIntent()) }
  private val adsProvider = AdsProvider()

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String {
    return getString(R.string.details)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentBirthdayPreviewBinding {
    return FragmentBirthdayPreviewBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the birthday preview screen for id: ${idFromIntent()}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.buttonCall.setOnClickListener { tryToMakeCall() }
    binding.buttonSms.setOnClickListener { tryToSendSms() }
    binding.buttonsView.gone()
    binding.contactPhoto.gone()
    binding.contactNumberBlockView.gone()

    loadAds()

    addMenu(
      menuRes = R.menu.fragment_birthday_preview,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_edit -> {
            editBirthday()
            true
          }

          R.id.action_delete -> {
            dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
              if (it) viewModel.deleteBirthday()
            }
            true
          }

          else -> false
        }
      }
    )

    initViewModel()
  }

  private fun editBirthday() {
    navigate {
      navigate(
        R.id.editBirthdayFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, idFromIntent())
        }
      )
    }
  }

  private fun loadAds() {
    if (!BuildParams.isPro && AdsProvider.hasAds()) {
      adsProvider.showBanner(
        binding.adsHolder,
        AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID
      )
    }
  }

  private fun tryToMakeCall() {
    val number = viewModel.birthday.value?.number ?: return
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      TelephonyUtil.makeCall(number, requireContext())
    }
  }

  private fun tryToSendSms() {
    val number = viewModel.birthday.value?.number ?: return
    TelephonyUtil.sendSms(number, requireContext())
  }

  private fun showTextIfNotNull(
    textView: TextView,
    value: String?,
    visibilityFunc: (Boolean) -> Unit
  ) {
    visibilityFunc(value != null)
    textView.text = value
  }

  private fun showBirthday(birthday: UiBirthdayPreview) {
    binding.nameBlockTextView.text = birthday.name

    showTextIfNotNull(binding.ageBlockTextView, birthday.ageFormatted) {
      binding.ageBlockView.visibleGone(it)
    }
    showTextIfNotNull(binding.dateOfBirthBlockTextView, birthday.dateOfBirth) {
      binding.dateOfBirthBlockView.visibleGone(it)
    }
    showTextIfNotNull(binding.nextBirthdayDateBlockTextView, birthday.nextBirthdayDate) {
      binding.nextBirthdayDateBlockView.visibleGone(it)
    }

    if (birthday.number != null) {
      val displayName = if (birthday.contactName != null) {
        "${birthday.contactName} (${birthday.number})"
      } else {
        birthday.number
      }
      showTextIfNotNull(binding.contactNumberBlockTextView, displayName) {
        binding.contactNumberBlockView.visibleGone(it)
      }

      if (birthday.photo != null) {
        binding.contactPhoto.visible()
        binding.contactPhoto.setImageBitmap(birthday.photo)
      } else {
        binding.contactPhoto.gone()
      }
    } else {
      binding.buttonsView.gone()
      binding.contactPhoto.gone()
      binding.contactNumberBlockView.gone()
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

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.DELETED -> moveBack()
        else -> {
        }
      }
    }
  }

  companion object {
    private const val TAG = "PreviewBirthdayFragment"
  }
}
