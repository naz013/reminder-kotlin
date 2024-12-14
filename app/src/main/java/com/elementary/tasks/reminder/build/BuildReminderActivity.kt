package com.elementary.tasks.reminder.build

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.VoiceRecognitionLauncher
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.core.utils.ui.singleClick
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.ActivityReminderBuilderBinding
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.build.adapter.BuilderAdapter
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialog
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogCallback
import com.elementary.tasks.reminder.build.valuedialog.ValueDialog
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogCallback
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BuildReminderActivity :
  BindingActivity<ActivityReminderBuilderBinding>(),
  SelectorDialogCallback,
  ValueDialogCallback {

  private val viewModel by viewModel<BuildReminderViewModel>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  private val voiceRecognitionLauncher = VoiceRecognitionLauncher(this) {
    viewModel.processVoiceResult(it)
  }
  private val builderAdapter = BuilderAdapter(
    onItemClickListener = { position, item ->
      viewModel.onItemEditedClicked(position, item.builderItem)
    },
    onItemRemove = { position, item ->
      viewModel.removeItem(position, item.builderItem)
    }
  )
  private val builderConfigureLauncher = BuilderConfigureActivity.BuilderConfigureLauncher(this) {
    viewModel.onConfigurationChanged()
  }

  override fun inflateBinding() = ActivityReminderBuilderBinding.inflate(layoutInflater)

  override fun requireLogin() = true

  private var paddingApplied = false

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    initToolbar()

    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
      val insets = windowInsets.getInsets(
        WindowInsetsCompat.Type.systemBars() or
          WindowInsetsCompat.Type.displayCutout()
      )
      if (!paddingApplied) {
        paddingApplied = true
        binding.appBar.updatePadding(
          top = binding.appBar.paddingTop + insets.top
        )
        binding.builderList.updatePadding(
          bottom = binding.builderList.paddingBottom + insets.bottom
        )
        binding.addButton.updateLayoutParams<MarginLayoutParams> {
          bottomMargin += insets.bottom
          rightMargin += insets.right
        }
        binding.emptyArrowIconView.updateLayoutParams<MarginLayoutParams> {
          bottomMargin += insets.bottom
          rightMargin += insets.right
        }
      }
      WindowInsetsCompat.CONSUMED
    }

    binding.builderList.layoutManager = LinearLayoutManager(this)
    binding.builderList.adapter = builderAdapter

    binding.addButton.singleClick {
      SelectorDialog().show(supportFragmentManager, SelectorDialog.TAG)
    }

    binding.saveAsPresetCheck.setOnCheckedChangeListener { _, isChecked ->
      viewModel.saveAsPreset = isChecked
    }
    binding.presetNameInput.onTextChanged {
      viewModel.presetName = it ?: ""
    }

    initViewModel()
  }

  private fun initToolbar() {
    binding.toolbar.setOnMenuItemClickListener { item ->
      return@setOnMenuItemClickListener when (item.itemId) {
        R.id.action_add -> {
          askNotificationPermissionIfNeeded()
          true
        }

        R.id.action_voice -> {
          openRecognizer()
          true
        }

        R.id.action_delete -> {
          deleteReminder()
          true
        }

        R.id.action_configure -> {
          builderConfigureLauncher.configure()
          true
        }

        R.id.action_legacy_builder -> {
          reminderBuilderLauncher.toggleBuilder(this)
          true
        }

        else -> false
      }
    }
    binding.toolbar.setNavigationOnClickListener {
      closeScreen()
    }
  }

  private fun updateMenu() {
    val menu = binding.toolbar.menu
    menu[0].isVisible = Module.hasMicrophone(this)
    menu[2].isVisible = viewModel.canRemove
    menu[4].isVisible = prefs.canChangeBuilder
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.builderItems.nonNullObserve(this) {
      updateMenu()
      builderAdapter.submitList(it)
      binding.scrollView.visibleGone(it.isNotEmpty())
      binding.emptyView.visibleGone(it.isEmpty())
    }
    viewModel.askPermissions.nonNullObserve(this) {
      it.getContentIfNotHandled()?.also { list ->
        permissionFlow.askPermissions(list) {
          viewModel.onPermissionsGranted()
        }
      }
    }
    viewModel.askEditPermissions.nonNullObserve(this) {
      it.getContentIfNotHandled()?.also { list ->
        permissionFlow.askPermissions(list) {
          viewModel.onEditPermissionsGranted()
        }
      }
    }
    viewModel.showEditDialog.nonNullObserve(this) {
      it.getContentIfNotHandled()?.also { pair ->
        ValueDialog.newInstance(pair.first)
          .show(supportFragmentManager, ValueDialog.TAG)
      }
    }
    viewModel.result.observe(this) { commands ->
      if (commands != null) {
        when (commands) {
          Commands.DELETED, Commands.SAVED -> {
            setResult(Activity.RESULT_OK)
            finish()
          }

          else -> {
          }
        }
      }
    }
    viewModel.showPrediction.nonNullObserve(this) { showPredictionState(it) }
    viewModel.canSaveAsPreset.nonNullObserve(this) {
      binding.savePresetViewHolder.visibleGone(it)
    }
    viewModel.canSave.nonNullObserve(this) {
      binding.toolbar.menu[1].isEnabled = it
    }
    viewModel.handleDeepLink(intent)
  }

  private fun showPredictionState(prediction: ReminderPrediction) {
    when (prediction) {
      is ReminderPrediction.SuccessPrediction -> {
        binding.forecastViewHolder.visible()
        binding.forecastTextView.text = prediction.message
        binding.forecastIconView.setImageResource(prediction.icon)
      }
      is ReminderPrediction.FailedPrediction -> {
        binding.forecastViewHolder.visible()
        binding.forecastTextView.text = prediction.message
        binding.forecastIconView.setImageResource(prediction.icon)
      }
    }
  }

  private fun openRecognizer() {
    voiceRecognitionLauncher.recognize(true)
  }

  private fun closeScreen() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  private fun deleteReminder() {
    if (viewModel.isRemoved) {
      dialogues.askConfirmation(this, getString(R.string.delete)) {
        if (it) {
          viewModel.deleteReminder(true)
        }
      }
    } else {
      dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
        if (it) {
          viewModel.moveToTrash()
        }
      }
    }
  }

  private fun askNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { askCopySaving() }
    } else {
      askCopySaving()
    }
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_reminder_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      save()
    }
  }

  private fun save(newId: Boolean = false) {
    viewModel.saveReminder(newId)
  }

  override fun onBuilderItemAdd(builderItem: BuilderItem<*>) {
    viewModel.addItem(builderItem)
  }

  override fun onPresetSelected(uiPresetList: UiPresetList) {
    viewModel.onPresetSelected(uiPresetList)
  }

  override fun onValueChanged(position: Int, builderItem: BuilderItem<*>) {
    viewModel.updateValue(position, builderItem)
  }
}
