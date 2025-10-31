package com.elementary.tasks.reminder.build

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.databinding.FragmentReminderBuilderBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.reminder.build.adapter.BuilderAdapter
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPrediction
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialog
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogCallback
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogCommunicator
import com.elementary.tasks.reminder.build.valuedialog.ValueDialog
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogCallback
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogCommunicator
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.singleClick
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BuildReminderFragment :
  BaseToolbarFragment<FragmentReminderBuilderBinding>(),
  SelectorDialogCallback,
  ValueDialogCallback {

  private val viewModel by viewModel<BuildReminderViewModel> { parametersOf(arguments) }
  private val reviewsApi by inject<ReviewsApi>()

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

  override fun getTitle(): String {
    return ""
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentReminderBuilderBinding {
    return FragmentReminderBuilderBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the reminder edit screen for id: ${Logger.data(viewModel.id)}")
    SelectorDialogCommunicator.addCallback(this)
    ValueDialogCommunicator.addCallback(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.builderList.layoutManager = LinearLayoutManager(context)
    binding.builderList.adapter = builderAdapter

    binding.addButton.applyBottomInsets()
    binding.addButton.singleClick {
      SelectorDialog().show(parentFragmentManager, SelectorDialog.TAG)
    }

    binding.saveAsPresetCheck.setOnCheckedChangeListener { _, isChecked ->
      viewModel.saveAsPreset = isChecked
    }
    binding.presetNameInput.onTextChanged {
      viewModel.presetName = it ?: ""
    }

    addMenu(
      menuRes = R.menu.fragment_reminder_builder,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_add -> {
            askNotificationPermissionIfNeeded()
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

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(0)?.isEnabled = viewModel.canSave.value ?: false
        menu.getItem(1)?.isVisible = viewModel.canRemove
      }
    )

    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.builderItems.nonNullObserve(viewLifecycleOwner) {
      invalidateOptionsMenu()
      builderAdapter.submitList(it)
      binding.scrollView.visibleGone(it.isNotEmpty())
      binding.emptyView.visibleGone(it.isEmpty())
    }
    viewModel.askPermissions.observeEvent(viewLifecycleOwner) { list ->
      permissionFlow.askPermissions(list) {
        viewModel.onPermissionsGranted()
      }
    }
    viewModel.askEditPermissions.observeEvent(viewLifecycleOwner) { list ->
      permissionFlow.askPermissions(list) {
        viewModel.onEditPermissionsGranted()
      }
    }
    viewModel.showEditDialog.observeEvent(viewLifecycleOwner) { pair ->
      ValueDialog.newInstance(pair.first)
        .show(parentFragmentManager, ValueDialog.TAG)
    }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      when (commands) {
        Commands.DELETED, Commands.SAVED -> {
          moveBack()
        }

        else -> {
        }
      }
    }
    viewModel.showPrediction.nonNullObserve(viewLifecycleOwner) { showPredictionState(it) }
    viewModel.canSaveAsPreset.nonNullObserve(viewLifecycleOwner) {
      binding.savePresetViewHolder.visibleGone(it)
    }
    viewModel.canSave.nonNullObserve(viewLifecycleOwner) { invalidateOptionsMenu() }
    viewModel.showReviewDialog.observeEvent(viewLifecycleOwner) {
      showReviewDialog()
    }
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

  private fun deleteReminder() {
    if (viewModel.isRemoved) {
      dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
        if (it) {
          viewModel.deleteReminder(true)
        }
      }
    } else {
      dialogues.askConfirmation(requireContext(), getString(R.string.move_to_trash)) {
        if (it) {
          viewModel.moveToTrash()
        }
      }
    }
  }

  private fun askNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
        askCopySaving()
      }
    } else {
      askCopySaving()
    }
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(requireContext())
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

  /**
   * Shows the ReviewDialog to collect user feedback.
   * Determines the app source (FREE or PRO) based on BuildParams.
   */
  private fun showReviewDialog() {
    val appSource = if (BuildParams.isPro) {
      AppSource.PRO
    } else {
      AppSource.FREE
    }

    reviewsApi.showFeedbackForm(
      context = requireContext(),
      title = getString(R.string.share_your_experience),
      appSource = appSource
    )
  }

  companion object {
    private const val TAG = "BuildReminderFragment"
  }
}
