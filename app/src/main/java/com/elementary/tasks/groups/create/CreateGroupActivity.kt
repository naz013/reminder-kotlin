package com.elementary.tasks.groups.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.databinding.ActivityCreateGroupBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreateGroupActivity : BindingActivity<ActivityCreateGroupBinding>() {

  private val viewModel by viewModel<CreateGroupViewModel> { parametersOf(getId()) }

  private val permissionFlow = PermissionFlow(this, dialogues)

  override fun inflateBinding() = ActivityCreateGroupBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(this))
    binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
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
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.setTitle(R.string.create_group)
  }

  private fun showGroup(group: UiGroupEdit) {
    binding.nameInput.setText(group.title)
    binding.colorSlider.setSelection(group.colorPosition)
    binding.defaultCheck.isEnabled = !group.isDefault
    binding.defaultCheck.isChecked = group.isDefault
    binding.toolbar.setTitle(R.string.change_group)
    invalidateOptionsMenu()
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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_simple_save_action, menu)
    if (!viewModel.isFromFile && viewModel.canBeDeleted) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_add -> {
        askCopySaving()
        true
      }

      android.R.id.home -> {
        finish()
        true
      }

      MENU_ITEM_DELETE -> {
        dialogues.askConfirmation(this, getString(R.string.delete)) {
          if (it) {
            viewModel.deleteGroup()
          }
        }
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
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
