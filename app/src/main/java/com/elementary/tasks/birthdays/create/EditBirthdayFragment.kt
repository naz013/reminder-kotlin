package com.elementary.tasks.birthdays.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.showError
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.views.ContactPickerView
import com.elementary.tasks.databinding.FragmentEditBirthdayBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.visibleInvisible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

class EditBirthdayFragment : BaseToolbarFragment<FragmentEditBirthdayBinding>() {

  private val viewModel by viewModel<EditBirthdayViewModel> { parametersOf(idFromIntent()) }
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String {
    return if (viewModel.hasId()) {
      getString(R.string.edit_birthday)
    } else {
      getString(R.string.add_birthday)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentEditBirthdayBinding {
    return FragmentEditBirthdayBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the birthday screen for id: ${idFromIntent()}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.birthDate.setOnClickListener { dateDialog() }
    initContactPicker()

    binding.yearCheck.setOnCheckedChangeListener { _, isChecked ->
      viewModel.onYearCheckChanged(isChecked)
    }

    addMenu(
      menuRes = R.menu.fragment_edit_birthday,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_add -> {
            askCopySaving()
            true
          }

          R.id.action_delete -> {
            dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
              if (it) {
                deleteItem()
              }
            }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(1).isVisible = viewModel.isEdited && !viewModel.isFromFile
      }
    )

    initViewModel()
    checkIntent()
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(requireContext())
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
    Logger.i(TAG, "Saving birthday, id: ${idFromIntent()}, new: $newId")
    val contact = binding.birthName.trimmedText()
    if (contact.isEmpty()) {
      binding.birthNameLayout.showError(R.string.must_be_not_empty)
      return
    }
    val number = binding.pickContactView.number.takeIf { it.isNotEmpty() }
    if (number != null) {
      permissionFlow.askPermission(Permissions.READ_CONTACTS) {
        viewModel.save(contact, number, newId, binding.yearCheck.isChecked)
      }
      return
    }
    viewModel.save(contact, null, newId, binding.yearCheck.isChecked)
  }

  private fun deleteItem() {
    if (viewModel.isEdited && !viewModel.isFromFile) {
      Logger.i(TAG, "Deleting birthday, id: ${idFromIntent()}")
      viewModel.deleteBirthday()
    }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(
      fragmentManager = childFragmentManager,
      date = viewModel.selectedDate,
      title = getString(R.string.select_date)
    ) {
      viewModel.onDateChanged(it)
    }
  }

  private fun initContactPicker() {
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
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.birthday.nonNullObserve(viewLifecycleOwner) { showBirthday(it) }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
      when (it) {
        Commands.SAVED -> moveBack()
        Commands.DELETED -> {
          navigate {
            navigate(
              resId = R.id.actionHome,
              args = null,
              navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .build()
            )
          }
        }

        else -> {
        }
      }
    }
    viewModel.formattedDate.nonNullObserve(viewLifecycleOwner) {
      binding.birthDate.text = it
    }
    viewModel.isContactAttached.nonNullObserve(viewLifecycleOwner) {
      binding.pickContactView.visibleInvisible(it)
    }
    viewModel.load()
  }

  private fun checkIntent() {
    Logger.i(TAG, "Received args: ${arguments?.keySet()?.toList()}")
    val bundle = arguments ?: return
    when {
      bundle.getBoolean(IntentKeys.INTENT_ITEM, false) -> viewModel.onIntent()
      bundle.getBoolean(IntentKeys.INTENT_DEEP_LINK, false) -> viewModel.onDeepLink(bundle)
      idFromIntent().isEmpty() -> viewModel.onDateChanged(LocalDate.now())
    }
  }

  private fun showBirthday(birthday: UiBirthdayEdit) {
    binding.birthName.setText(birthday.name)
    if (birthday.number.isNotEmpty()) {
      binding.pickContactView.number = birthday.number
    }
    binding.yearCheck.isChecked = birthday.isYearIgnored
    invalidateOptionsMenu()
  }

  companion object {
    private const val TAG = "EditBirthdayFragment"
  }
}
