package com.elementary.tasks.birthdays.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.listenScrollableView
import com.elementary.tasks.core.utils.ui.showError
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleInvisible
import com.elementary.tasks.core.views.ContactPickerView
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import timber.log.Timber

class AddBirthdayActivity : BindingActivity<ActivityAddBirthdayBinding>() {

  private val viewModel by viewModel<AddBirthdayViewModel> { parametersOf(idFromIntent()) }
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  override fun inflateBinding() = ActivityAddBirthdayBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initTopAppBar()
    initContactView()
    binding.scrollView.listenScrollableView { binding.appBar.isSelected = it > 0 }
    binding.birthDate.setOnClickListener { dateDialog() }

    binding.yearCheck.setOnCheckedChangeListener { _, isChecked ->
      viewModel.onYearCheckChanged(isChecked)
    }

    binding.pickContactView.contactPicker = ContactPicker(this) { }
    binding.pickContactView.listener = object : ContactPickerView.OnNumberChangeListener {
      override fun onChanged(phoneNumber: String, contactInfo: ContactPickerView.ContactInfo?) {
        contactInfo?.also {
          if (binding.birthName.text.toString().trim() == "") {
            binding.birthName.setText(it.name)
          }
        }
      }
    }

    loadBirthday()
    setInitState().takeIf { savedInstanceState == null }
  }

  private fun initContactView() {
    if (prefs.isTelephonyAllowed) {
      binding.contactCheck.visible()
      binding.contactCheck.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked && !prefs.isTelephonyAllowed) return@setOnCheckedChangeListener
        permissionFlow.askPermission(Permissions.READ_CONTACTS) {
          viewModel.onContactAttached(isChecked)
        }
      }
    } else {
      binding.contactCheck.gone()
    }
  }

  private fun setInitState() {
    viewModel.onContactAttached(false)
  }

  private fun initTopAppBar() {
    binding.toolbar.setTitle(R.string.add_birthday)
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener when (menuItem.itemId) {
        R.id.action_add -> {
          askCopySaving()
          true
        }

        R.id.action_delete -> {
          dialogues.askConfirmation(this, getString(R.string.delete)) {
            if (it) deleteItem()
          }
          true
        }

        else -> false
      }
    }
    binding.toolbar.setNavigationOnClickListener { finish() }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      it.getItem(1).isVisible = viewModel.isEdited && !viewModel.isFromFile
    }
  }

  private fun showBirthday(birthday: UiBirthdayEdit) {
    binding.toolbar.setTitle(R.string.edit_birthday)
    binding.birthName.setText(birthday.name)
    if (!TextUtils.isEmpty(birthday.number)) {
      binding.pickContactView.number = birthday.number
      binding.contactCheck.isChecked = true
    }
    binding.yearCheck.isChecked = birthday.isYearIgnored
    updateMenu()
  }

  private fun loadBirthday() {
    initViewModel()
    viewModel.load()
    when {
      intent.data != null -> {
        permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
          intent.data?.let { viewModel.onFile(it) }
        }
      }

      intent.hasExtra(Constants.INTENT_ITEM) -> viewModel.onIntent()
      intent.getBooleanExtra(Constants.INTENT_DEEP_LINK, false) -> {
        runCatching {
          val parser = DeepLinkDataParser()
          when (val deepLinkData = parser.readDeepLinkData(intent)) {
            is BirthdayDateDeepLinkData -> {
              viewModel.onDateChanged(deepLinkData.date)
            }
            else -> {
              viewModel.onDateChanged(LocalDate.now())
            }
          }
        }
      }
      idFromIntent().isEmpty() -> viewModel.onDateChanged(LocalDate.now())
    }
  }

  private fun idFromIntent(): String = intentString(Constants.INTENT_ID)

  private fun initViewModel() {
    viewModel.birthday.nonNullObserve(this) { showBirthday(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> closeScreen()
        else -> {
        }
      }
    }
    viewModel.formattedDate.nonNullObserve(this) {
      Timber.d("onDateChanged: $it")
      binding.birthDate.text = it
    }
    viewModel.isContactAttached.nonNullObserve(this) {
      binding.pickContactView.visibleInvisible(it)
    }
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_birthday_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveBirthday(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveBirthday()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      saveBirthday()
    }
  }

  private fun saveBirthday(newId: Boolean = false) {
    val contact = binding.birthName.trimmedText()
    if (contact.isEmpty()) {
      binding.birthNameLayout.showError(R.string.must_be_not_empty)
      return
    }
    val number = binding.pickContactView.number.takeIf { binding.contactCheck.isChecked }
    if (binding.contactCheck.isChecked) {
      if (number.isNullOrEmpty()) {
        binding.pickContactView.showError(R.string.you_dont_insert_number)
        return
      }
      permissionFlow.askPermission(Permissions.READ_CONTACTS) {
        viewModel.save(contact, number, newId, binding.yearCheck.isChecked)
      }
      return
    }
    viewModel.save(contact, number, newId, binding.yearCheck.isChecked)
  }

  private fun closeScreen() {
    sendBroadcast(
      Intent(this, PermanentBirthdayReceiver::class.java)
        .setAction(PermanentBirthdayReceiver.ACTION_SHOW)
    )
    setResult(Activity.RESULT_OK)
    finish()
  }

  private fun deleteItem() {
    if (viewModel.isEdited && !viewModel.isFromFile) {
      viewModel.deleteBirthday()
    }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(
      context = this,
      date = viewModel.selectedDate,
      showYear = !binding.yearCheck.isChecked
    ) {
      viewModel.onDateChanged(it)
    }
  }

  override fun requireLogin() = true
}
