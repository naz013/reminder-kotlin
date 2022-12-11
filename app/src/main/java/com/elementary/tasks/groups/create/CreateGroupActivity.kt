package com.elementary.tasks.groups.create

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.groups.GroupViewModel
import com.elementary.tasks.databinding.ActivityCreateGroupBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

class CreateGroupActivity : BindingActivity<ActivityCreateGroupBinding>() {

  private val viewModel by viewModel<GroupViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)
  private var mItem: ReminderGroup? = null

  override fun inflateBinding() = ActivityCreateGroupBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.colorSlider.setColors(ThemeProvider.colorsForSliderThemed(this))
    binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)

    if (savedInstanceState != null) {
      binding.colorSlider.setSelection(savedInstanceState.getInt(ARG_COLOR, 0))
    }

    loadGroup()
  }

  override fun requireLogin() = true

  private fun getId(): String = intentString(Constants.INTENT_ID)

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putInt(ARG_COLOR, binding.colorSlider.selectedItem)
    super.onSaveInstanceState(outState)
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.setTitle(R.string.create_group)
  }

  private fun showGroup(reminderGroup: ReminderGroup, fromFile: Boolean = false) {
    this.mItem = reminderGroup
    if (!viewModel.isEdited) {
      binding.nameInput.setText(reminderGroup.groupTitle)
      binding.colorSlider.setSelection(reminderGroup.groupColor)
      binding.defaultCheck.isEnabled = !reminderGroup.isDefaultGroup
      binding.defaultCheck.isChecked = reminderGroup.isDefaultGroup
      viewModel.isEdited = true
      viewModel.isFromFile = fromFile
      if (fromFile) {
        viewModel.findSame(reminderGroup.groupUuId)
      }
    }
    binding.toolbar.setTitle(R.string.change_group)
    invalidateOptionsMenu()
  }

  private fun loadGroup() {
    initViewModel()
    if (intent.data != null) {
      permissionFlow.askPermission(Permissions.READ_EXTERNAL) { readUri() }
    } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
      runCatching {
        intentParcelable(Constants.INTENT_ITEM, ReminderGroup::class.java)?.let {
          showGroup(it, true)
        }
      }
    }
  }

  private fun readUri() {
    intent.data?.let {
      runCatching {
        (if (ContentResolver.SCHEME_CONTENT != it.scheme) {
          val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_GROUP)
          if (any != null && any is ReminderGroup) {
            any
          } else {
            null
          }
        } else null)?.let { item -> showGroup(item, true) }
      }
    }
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
    viewModel.allGroups.nonNullObserve(this) { invalidateOptionsMenu() }
  }

  private fun saveGroup(newId: Boolean = false) {
    val text = binding.nameInput.text.toString().trim()
    if (text.isEmpty()) {
      binding.nameLayout.error = getString(R.string.must_be_not_empty)
      binding.nameLayout.isErrorEnabled = true
      return
    }
    val wasDefault = mItem?.isDefaultGroup ?: false
    val item = (mItem ?: ReminderGroup()).apply {
      this.groupColor = binding.colorSlider.selectedItem
      this.groupDateTime = TimeUtil.gmtDateTime
      this.groupTitle = text
      this.isDefaultGroup = binding.defaultCheck.isChecked
    }
    if (newId) {
      item.groupUuId = UUID.randomUUID().toString()
    }
    analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GROUP))
    viewModel.saveGroup(item, wasDefault)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_simple_save_action, menu)
    mItem?.let {
      if (!it.isDefaultGroup && !viewModel.isFromFile) {
        viewModel.allGroups.value?.let { groups ->
          if (groups.size > 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
          }
        }
      }
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
          if (it) deleteItem()
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

  private fun deleteItem() {
    mItem?.let {
      viewModel.deleteGroup(it)
    }
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
    private const val ARG_COLOR = "arg_color"
    private const val ARG_LOGGED = "arg_logged"

    fun openLogged(context: Context, intent: Intent? = null) {
      if (intent == null) {
        context.startActivity(
          Intent(context, CreateGroupActivity::class.java)
            .putExtra(ARG_LOGGED, true)
        )
      } else {
        intent.putExtra(ARG_LOGGED, true)
        context.startActivity(intent)
      }
    }
  }
}
