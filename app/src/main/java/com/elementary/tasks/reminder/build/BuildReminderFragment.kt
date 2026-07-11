package com.elementary.tasks.reminder.build

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.selectordialog.BuilderSelectorSheet
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.ValueDialog
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogCallback
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogCommunicator
import com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet
import com.elementary.tasks.reminder.build.valuedialog.isSupportedByComposeEditor
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.recur.RecurHelpActivity
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.context.startActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class BuildReminderFragment :
  BaseComposeToolbarFragment(),
  ValueDialogCallback {
  private val viewModel by viewModel<BuildReminderViewModel> { parametersOf(arguments) }
  private val reviewsApi by inject<ReviewsApi>()
  private val featureManager by inject<FeatureManager>()
  private val selectorDialogDataHolder by inject<SelectorDialogDataHolder>()
  private val paramToTextAdapter by inject<ParamToTextAdapter>()
  private val googleCalendarUtils by inject<GoogleCalendarUtils>()
  private val packageManagerWrapper by inject<PackageManagerWrapper>()

  private val builderConfigureLauncher =
    BuilderConfigureActivity.BuilderConfigureLauncher(this) {
      viewModel.onConfigurationChanged()
    }
  private val applicationPicker = ApplicationPicker(this) { }
  private val contactPicker = ContactPicker(this) { }

  override fun getTitle(): String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the reminder edit screen for id: ${Logger.data(viewModel.id)}")
    ValueDialogCommunicator.addCallback(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(
      menuRes = R.menu.fragment_reminder_builder,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_add -> {
            Logger.i(TAG, "User wants to add reminder.")
            askNotificationPermissionIfNeeded()
            true
          }

          R.id.action_delete -> {
            Logger.i(TAG, "User wants to delete reminder.")
            deleteReminder()
            true
          }

          R.id.action_configure -> {
            Logger.i(TAG, "User wants to configure reminder.")
            builderConfigureLauncher.configure()
            true
          }

          R.id.action_report_issue -> {
            Logger.i(TAG, "User wants to report an issue.")
            showReviewDialog(getString(R.string.report_an_issue))
            true
          }

          R.id.action_help -> {
            Logger.i(TAG, "User wants to see help.")
            navigate {
              navigate(
                R.id.reminderHelpFragment,
                null,
                NavigationAnimations.inDepthNavOptions(),
              )
            }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(0)?.isEnabled = viewModel.canSave.value ?: false
        menu.getItem(1)?.isVisible = viewModel.canRemove
      },
    )
  }

  @Composable
  override fun Content() {
    LaunchedEffect(viewModel) { lifecycle.addObserver(viewModel) }

    val builderItems by viewModel.builderItems.observeAsState(emptyList())
    val prediction by viewModel.showPrediction.observeAsState()
    val canSaveAsPreset by viewModel.canSaveAsPreset.observeAsState(false)
    val canSave by viewModel.canSave.observeAsState(false)

    var saveAsPresetChecked by remember { mutableStateOf(false) }
    var presetNameState by remember { mutableStateOf("") }
    var showSelector by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Pair<Int, BuilderItem<*>>?>(null) }

    LaunchedEffect(builderItems, canSave) { invalidateOptionsMenu() }

    viewModel.askPermissions.ObserveEvent { list ->
      permissionFlow.askPermissions(list) { viewModel.onPermissionsGranted() }
    }
    viewModel.askEditPermissions.ObserveEvent { list ->
      permissionFlow.askPermissions(list) { viewModel.onEditPermissionsGranted() }
    }
    viewModel.showEditDialog.ObserveEvent { pair ->
      if (isSupportedByComposeEditor(pair.second)) {
        editingItem = pair
      } else {
        ValueDialog.newInstance(pair.first).show(parentFragmentManager, ValueDialog.TAG)
      }
    }
    viewModel.resultEvent.ObserveEvent { commands ->
      when (commands) {
        Commands.DELETED, Commands.SAVED -> moveBack()
        else -> {}
      }
    }
    viewModel.showReviewDialog.ObserveEvent {
      showReviewDialog(getString(R.string.share_your_experience))
    }

    BuildReminderScreen(
      builderItems = builderItems,
      prediction = prediction,
      canSaveAsPreset = canSaveAsPreset,
      saveAsPresetChecked = saveAsPresetChecked,
      presetName = presetNameState,
      onSaveAsPresetChange = {
        saveAsPresetChecked = it
        viewModel.saveAsPreset = it
      },
      onPresetNameChange = {
        presetNameState = it
        viewModel.presetName = it
      },
      onItemClick = { position, item -> viewModel.onItemEditedClicked(position, item) },
      onItemRemove = { position, item -> viewModel.removeItem(position, item) },
      onAddClick = { showSelector = true },
    )

    if (showSelector) {
      BuilderSelectorSheet(
        tabs = selectorDialogDataHolder.getTabs(),
        builderItems = selectorDialogDataHolder.selectorBuilderItems,
        presets = selectorDialogDataHolder.presets,
        recurPresets = selectorDialogDataHolder.recurPresets,
        onDismissRequest = { showSelector = false },
        onBuilderItemSelected = { builderItem ->
          showSelector = false
          viewModel.addItem(builderItem)
        },
        onPresetSelected = { preset ->
          showSelector = false
          viewModel.onPresetSelected(preset)
        },
      )
    }

    editingItem?.let { (position, item) ->
      ValueEditorSheet(
        builderItem = item,
        is24HourFormat = prefs.is24HourFormat,
        paramToTextAdapter = paramToTextAdapter,
        googleCalendarUtils = googleCalendarUtils,
        packageManagerWrapper = packageManagerWrapper,
        onPickApplication = { onResult -> applicationPicker.pickApplication(onResult) },
        onPickContact = { onResult ->
          permissionFlow.askPermission(Permissions.READ_CONTACTS) {
            contactPicker.pickContact { contactData -> onResult(contactData.phone) }
          }
        },
        onDismissRequest = { editingItem = null },
        onValueChange = { updated -> viewModel.updateValue(position, updated) },
        onHelpClick = if (item.biGroup == BiGroup.ICAL) {
          { requireContext().startActivity(RecurHelpActivity::class.java) }
        } else {
          null
        },
      )
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
      dialogues.askConfirmation(requireContext(), getString(R.string.move_to_the_archive)) {
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
      dialogues
        .getMaterialDialog(requireContext())
        .setMessage(R.string.same_reminder_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save(true)
        }.setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save()
        }.setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }.create()
        .show()
    } else {
      save()
    }
  }

  private fun save(newId: Boolean = false) {
    viewModel.saveReminder(newId)
  }

  override fun onValueChanged(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    viewModel.updateValue(position, builderItem)
  }

  /**
   * Shows the ReviewDialog to collect user feedback.
   * Determines the app source (FREE or PRO) based on BuildParams.
   */
  private fun showReviewDialog(title: String) {
    val appSource =
      if (BuildParams.isPro) {
        AppSource.PRO
      } else {
        AppSource.FREE
      }

    reviewsApi.showFeedbackForm(
      context = requireContext(),
      title = title,
      appSource = appSource,
      allowLogsAttachment = featureManager.isFeatureEnabled(FeatureManager.Feature.LOGS_IN_REVIEWS),
    )
  }

  companion object {
    private const val TAG = "BuildReminderFragment"
  }
}
