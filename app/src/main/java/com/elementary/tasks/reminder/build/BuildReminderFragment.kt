package com.elementary.tasks.reminder.build

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.os.datapicker.MultipleUriPicker
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.help.ReminderHelpScreen
import com.elementary.tasks.reminder.build.selectordialog.BuilderSelectorSheet
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.build.valuedialog.editor.MapEditorScreen
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.recur.RecurHelpScreen
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Hosts the reminder builder as a self-contained Navigation 3 "island": Main (the actual builder
 * screen) plus the Configure and Help sub-screens, previously separate Activities/Fragment-nav
 * destinations, are now internal [BuildReminderNavKey] entries sharing this Fragment's classic
 * toolbar. The toolbar is hidden (see [setAppBarVisible]) whenever a sub-screen - which owns its
 * own Material 3 Scaffold/TopAppBar - is on top, so the two don't stack.
 */
class BuildReminderFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<BuildReminderViewModel> { parametersOf(arguments) }
  private val reviewsApi by inject<ReviewsApi>()
  private val featureManager by inject<FeatureManager>()
  private val selectorDialogDataHolder by inject<SelectorDialogDataHolder>()
  private val paramToTextAdapter by inject<ParamToTextAdapter>()
  private val googleCalendarUtils by inject<GoogleCalendarUtils>()
  private val packageManagerWrapper by inject<PackageManagerWrapper>()
  private val attachmentFileAdapter by inject<UriToAttachmentFileAdapter>()
  private val dateTimeManager by inject<DateTimeManager>()

  private val applicationPicker = ApplicationPicker(this) { }
  private val contactPicker = ContactPicker(this) { }
  private val multipleUriPicker = MultipleUriPicker(this)

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack - the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  override fun getTitle(): String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the reminder edit screen for id: ${Logger.data(viewModel.id)}")
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
            currentBackStack?.add(BuildReminderNavKey.Configure)
            true
          }

          R.id.action_report_issue -> {
            Logger.i(TAG, "User wants to report an issue.")
            showReviewDialog(getString(R.string.report_an_issue))
            true
          }

          R.id.action_help -> {
            Logger.i(TAG, "User wants to see help.")
            currentBackStack?.add(BuildReminderNavKey.Help)
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
    val backStack = rememberNavBackStack(BuildReminderNavKey.Main)
    SideEffect { currentBackStack = backStack }
    BuildReminderNavGraph(backStack)
  }

  @Composable
  private fun BuildReminderNavGraph(backStack: MutableList<NavKey>) {
    val topKey = backStack.lastOrNull()
    LaunchedEffect(topKey) {
      setAppBarVisible(topKey == BuildReminderNavKey.Main || topKey == null)
    }

    NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      entryDecorators =
        listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
        ),
      transitionSpec = {
        (
          fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
        ) togetherWith (
          fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
        )
      },
      popTransitionSpec = {
        (
          fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
        ) togetherWith (
          fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
        )
      },
      predictivePopTransitionSpec = {
        (
          fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
        ) togetherWith (
          fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
            scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
        )
      },
      entryProvider =
        entryProvider {
          entry<BuildReminderNavKey.Main> { MainEntry(backStack) }
          entry<BuildReminderNavKey.Configure> { ConfigureEntry(backStack) }
          entry<BuildReminderNavKey.Help> {
            ReminderHelpScreen(onBackClick = { backStack.removeLastOrNull() })
          }
          entry<BuildReminderNavKey.RecurHelp> {
            RecurHelpScreen(onBackClick = { backStack.removeLastOrNull() })
          }
        },
    )
  }

  @Composable
  private fun MainEntry(backStack: MutableList<NavKey>) {
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
    viewModel.showEditDialog.ObserveEvent { pair -> editingItem = pair }
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
      // Arriving/Leaving coordinates host a real Fragment (SimpleMapFragment, Google Maps SDK) via
      // FragmentContainerView. ValueEditorSheet's AppModalBottomSheet renders in a separate Compose
      // Popup/Dialog window, which isn't part of the fragment view tree childFragmentManager
      // searches when resolving that container by id, so it must be shown in-place instead (see
      // MapEditorScreen's kdoc).
      if (item is ArrivingCoordinatesBuilderItem || item is LeavingCoordinatesBuilderItem) {
        @Suppress("UNCHECKED_CAST")
        MapEditorScreen(
          builderItem = item as BuilderItem<Place>,
          parentFragment = this,
          dateTimeManager = dateTimeManager,
          onDismissRequest = { editingItem = null },
          onValueChange = { updated -> viewModel.updateValue(position, updated) },
        )
      } else {
        ValueEditorSheet(
          builderItem = item,
          is24HourFormat = prefs.is24HourFormat,
          paramToTextAdapter = paramToTextAdapter,
          googleCalendarUtils = googleCalendarUtils,
          packageManagerWrapper = packageManagerWrapper,
          attachmentFileAdapter = attachmentFileAdapter,
          dateTimeManager = dateTimeManager,
          onPickApplication = { onResult -> applicationPicker.pickApplication(onResult) },
          onPickContact = { onResult ->
            permissionFlow.askPermission(Permissions.READ_CONTACTS) {
              contactPicker.pickContact { contactData -> onResult(contactData.phone) }
            }
          },
          onPickFiles = { onResult -> multipleUriPicker.pickFiles(onResult) },
          onDismissRequest = { editingItem = null },
          onValueChange = { updated -> viewModel.updateValue(position, updated) },
          onHelpClick = if (item.biGroup == BiGroup.ICAL) {
            { backStack.add(BuildReminderNavKey.RecurHelp) }
          } else {
            null
          },
        )
      }
    }
  }

  @Composable
  private fun ConfigureEntry(backStack: MutableList<NavKey>) {
    val configureViewModel = koinViewModel<BuilderConfigureViewModel>()
    // BuilderConfigureViewModel writes each toggle straight to prefs; BuildReminderViewModel only
    // needs to know the config may have changed once the user leaves this screen (any way: back
    // button, system back gesture, or programmatic pop), matching the old ActivityResult callback.
    DisposableEffect(Unit) {
      onDispose { viewModel.onConfigurationChanged() }
    }

    val state by configureViewModel.state.collectAsState()
    BuilderConfigureScreen(
      state = state,
      onBackClick = { backStack.removeLastOrNull() },
      onSummaryToggle = configureViewModel::onSummaryToggle,
      onBeforeToggle = configureViewModel::onBeforeToggle,
      onRepeatToggle = configureViewModel::onRepeatToggle,
      onRepeatLimitToggle = configureViewModel::onRepeatLimitToggle,
      onPriorityToggle = configureViewModel::onPriorityToggle,
      onAttachmentToggle = configureViewModel::onAttachmentToggle,
      onCalendarToggle = configureViewModel::onCalendarToggle,
      onTasksToggle = configureViewModel::onTasksToggle,
      onExtraToggle = configureViewModel::onExtraToggle,
      onLedToggle = configureViewModel::onLedToggle,
      onICalendarToggle = configureViewModel::onICalendarToggle,
      onMakeCallToggle = configureViewModel::onMakeCallToggle,
      onSendSmsToggle = configureViewModel::onSendSmsToggle,
      onOpenAppToggle = configureViewModel::onOpenAppToggle,
      onOpenLinkToggle = configureViewModel::onOpenLinkToggle,
      onSendEmailToggle = configureViewModel::onSendEmailToggle,
    )
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1

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

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f
