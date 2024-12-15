package com.elementary.tasks.groups.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.applyBottomInsets
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.databinding.ActivityCreateGroupBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateGroupActivity : BindingActivity<ActivityCreateGroupBinding>() {

  private val viewModel by viewModel<CreateGroupViewModel> { parametersOf(getId()) }

  override fun inflateBinding() = ActivityCreateGroupBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.scrollView.applyBottomInsets()
    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(this))
    binding.colorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.colorSlider.setSelection(viewModel.sliderPosition)
    binding.colorSlider.setListener { position, _ ->
      viewModel.onPositionChanged(position)
    }

    initViewModel()
    loadGroup()
  }

  override fun requireLogin() = true

  private fun getId(): String = intentString(Constants.INTENT_ID)

  private fun initActionBar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setTitle(R.string.create_group)
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_add -> {
          askCopySaving()
          true
        }

        R.id.action_delete -> {
          dialogues.askConfirmation(this, getString(R.string.delete)) {
            if (it) {
              viewModel.deleteGroup()
            }
          }
          true
        }

        else -> false
      }
    }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      it.getItem(1).isVisible = !viewModel.isFromFile && viewModel.canBeDeleted
    }
  }

  private fun showGroup(group: UiGroupEdit) {
    binding.nameInput.setText(group.title)
    binding.colorSlider.setSelection(group.colorPosition)
    binding.defaultCheck.isEnabled = !group.isDefault
    binding.defaultCheck.isChecked = group.isDefault
    binding.toolbar.setTitle(R.string.change_group)
    updateMenu()
  }

  private fun loadGroup() {
    if (intent.data != null) {
      permissionFlow.askPermission(Permissions.READ_EXTERNAL) { readUri() }
    } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
      runCatching {
        viewModel.loadFromIntent(intentParcelable(Constants.INTENT_ITEM, ReminderGroup::class.java))
      }
    }
  }

  private fun readUri() {
    intent.data?.let { viewModel.loadFromFile(it) }
  }

  private fun initViewModel() {
    viewModel.reminderGroup.nonNullObserve(this) { showGroup(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> finish()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
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
      dialogues.getMaterialDialog(this)
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
    private const val MENU_ITEM_DELETE = 12
  }
}
