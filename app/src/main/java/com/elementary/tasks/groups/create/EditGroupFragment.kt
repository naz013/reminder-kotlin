package com.elementary.tasks.groups.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.elementary.tasks.databinding.FragmentEditGroupBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditGroupFragment : BaseToolbarFragment<FragmentEditGroupBinding>() {

  private val viewModel by viewModel<EditGroupViewModel> { parametersOf(idFromIntent()) }

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String {
    return if (viewModel.hasId()) {
      getString(R.string.change_group)
    } else {
      getString(R.string.create_group)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentEditGroupBinding {
    return FragmentEditGroupBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the group screen for id: ${idFromIntent()}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(requireContext()))
    binding.colorSlider.setSelectorColorResource(
      if (isDark) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.colorSlider.setSelection(viewModel.sliderPosition)
    binding.colorSlider.setListener { position, _ ->
      viewModel.onPositionChanged(position)
    }

    addMenu(
      menuRes = R.menu.fragment_edit_group,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_add -> {
            askCopySaving()
            true
          }

          R.id.action_delete -> {
            dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
              if (it) {
                viewModel.deleteGroup()
              }
            }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(1).isVisible = !viewModel.isFromFile && viewModel.canBeDeleted
      }
    )

    initViewModel()
    loadGroup()
  }

  private fun loadGroup() {
    if (arguments?.getBoolean(IntentKeys.INTENT_ITEM, false) == true) {
      viewModel.loadFromIntent()
    }
  }

  private fun initViewModel() {
    viewModel.reminderGroup.nonNullObserve(this) { showGroup(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> moveBack()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
  }

  private fun showGroup(group: UiGroupEdit) {
    binding.nameInput.setText(group.title)
    binding.colorSlider.setSelection(group.colorPosition)
    binding.defaultCheck.isEnabled = !group.isDefault
    binding.defaultCheck.isChecked = group.isDefault
    invalidateOptionsMenu()
  }

  private fun saveGroup(newId: Boolean = false) {
    val text = binding.nameInput.text.toString().trim()
    if (text.isEmpty()) {
      binding.nameLayout.error = getString(R.string.must_be_not_empty)
      binding.nameLayout.isErrorEnabled = true
      return
    }
    viewModel.saveGroup(
      text,
      binding.colorSlider.selectedItem,
      binding.defaultCheck.isChecked,
      newId
    )
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(requireContext())
        .setMessage(R.string.same_group_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveGroup(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveGroup()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      saveGroup()
    }
  }

  companion object {
    private const val TAG = "EditGroupFragment"
  }
}
