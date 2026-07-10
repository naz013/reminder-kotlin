package com.elementary.tasks.notes

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.PermissionRequester
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.core.os.datapicker.compose.rememberCameraPicker
import com.elementary.tasks.core.os.datapicker.compose.rememberGalleryPicker
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.compose.DateTimePickerDialogs
import com.elementary.tasks.core.utils.ui.compose.rememberDateTimePickerState
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.notes.create.EditTab
import com.elementary.tasks.notes.create.NoteEditActions
import com.elementary.tasks.notes.create.NoteEditScreen
import com.elementary.tasks.notes.create.NoteEditViewModel
import com.elementary.tasks.notes.create.UrlImagePickerDialogs
import com.elementary.tasks.notes.create.rememberUrlImagePickerState
import com.elementary.tasks.notes.list.NotesScreen
import com.elementary.tasks.notes.list.NotesViewModel
import com.elementary.tasks.notes.preview.ImagePreviewScreen
import com.elementary.tasks.notes.preview.ImagePreviewState
import com.elementary.tasks.notes.preview.ImagePreviewViewModel
import com.elementary.tasks.notes.preview.PreviewNoteActions
import com.elementary.tasks.notes.preview.PreviewNoteScreen
import com.elementary.tasks.notes.preview.PreviewNoteState
import com.elementary.tasks.notes.preview.PreviewNoteViewModel
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds the Notes island's [NavDisplay] — the "screens" (Nav3 entries) themselves and the
 * routing between them. None of these entries depend on a Fragment/Activity beyond what Compose
 * itself exposes ([LocalActivity], [LocalView]'s [NavController]) — [NotesFragment] only owns the
 * backstack and forwards its `arguments` bundle once to seed it.
 */
@Composable
internal fun NotesNavGraph(
  backStack: MutableList<NavKey>,
  arguments: Bundle?,
) {
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
        entry<NotesNavKey.List> { NotesListEntry(backStack) }
        entry<NotesNavKey.Archive> { NotesArchiveEntry(backStack) }
        entry<NotesNavKey.Preview> { key -> NotePreviewEntry(key, backStack) }
        entry<NotesNavKey.Edit> { key -> NoteEditEntry(key, backStack, arguments = arguments) }
        entry<NotesNavKey.ImagePreview> { key -> NoteImagePreviewEntry(key, backStack) }
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f

/**
 * Bundles the framework-level dependencies [handleNotesNavigationEvent] needs so [NotesListEntry]
 * and [NotesArchiveEntry] don't have to repeat a long parameter list — resolved once via
 * [rememberNotesHostDependencies], none of it via a Fragment/Activity reference held by the caller.
 */
private class NotesHostDependencies(
  val activity: Activity,
  val navController: NavController,
  val dialogues: Dialogues,
  val permissionRequester: PermissionRequester,
  val settingsTitle: String,
  val colorDialogTitle: String,
  val deleteDialogTitle: String,
)

@Composable
private fun rememberNotesHostDependencies(permissionRequester: PermissionRequester): NotesHostDependencies {
  val activity =
    requireNotNull(LocalActivity.current) { "Notes screens require an Activity-backed composition" }
  val view = LocalView.current
  val navController = remember(view) { view.findNavController() }
  val dialogues = koinInject<Dialogues>()
  val settingsTitle = stringResource(R.string.action_settings)
  val colorDialogTitle = stringResource(R.string.color)
  val deleteDialogTitle = stringResource(R.string.delete)
  return remember(
    activity,
    navController,
    dialogues,
    permissionRequester,
    settingsTitle,
    colorDialogTitle,
    deleteDialogTitle,
  ) {
    NotesHostDependencies(
      activity = activity,
      navController = navController,
      dialogues = dialogues,
      permissionRequester = permissionRequester,
      settingsTitle = settingsTitle,
      colorDialogTitle = colorDialogTitle,
      deleteDialogTitle = deleteDialogTitle,
    )
  }
}

private fun handleNotesNavigationEvent(
  event: NotesViewModel.NavigationEvent,
  viewModel: NotesViewModel,
  backStack: MutableList<NavKey>,
  deps: NotesHostDependencies,
) {
  when (event) {
    is NotesViewModel.NavigationEvent.OpenNotePreview -> {
      backStack.add(NotesNavKey.Preview(event.id))
    }

    is NotesViewModel.NavigationEvent.OpenCreateNote -> {
      backStack.add(NotesNavKey.Edit())
    }

    is NotesViewModel.NavigationEvent.OpenEditNote -> {
      backStack.add(NotesNavKey.Edit(event.id))
    }

    is NotesViewModel.NavigationEvent.OpenArchive -> backStack.add(NotesNavKey.Archive)

    is NotesViewModel.NavigationEvent.OpenSettings -> {
      deps.navController.safeNavigation(
        NotesFragmentDirections.actionActionNotesToNoteSettingsFragment(deps.settingsTitle),
      )
    }

    is NotesViewModel.NavigationEvent.OpenImagePreview -> {
      backStack.add(NotesNavKey.ImagePreview(event.imagePosition))
    }

    is NotesViewModel.NavigationEvent.ShareNote -> {
      TelephonyUtil.sendNote(event.file, deps.activity, event.summary)
    }

    is NotesViewModel.NavigationEvent.RequestNotificationPermission -> {
      deps.permissionRequester.request(
        Permissions.POST_NOTIFICATION,
        onGranted = { viewModel.showNoteInNotification(event.id) },
      )
    }

    is NotesViewModel.NavigationEvent.PickColor -> {
      deps.dialogues.showColorDialog(
        deps.activity,
        event.colorPosition,
        deps.colorDialogTitle,
        event.sliderColors,
      ) { color -> viewModel.saveNoteColor(event.id, color) }
    }

    is NotesViewModel.NavigationEvent.ConfirmDelete -> {
      deps.dialogues.askConfirmation(deps.activity, deps.deleteDialogTitle) { confirmed ->
        if (confirmed) viewModel.deleteNote(event.id)
      }
    }
  }
}

@Composable
private fun NotesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(false) }
  bindLifecycle(viewModel)
  val permissionRequester = rememberPermissionRequester()
  val hostDeps = rememberNotesHostDependencies(permissionRequester)
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, viewModel, backStack, hostDeps) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  viewModel.errorEvent.ObserveEvent { message -> scope.launch { snackbarHostState.showSnackbar(message) } }

  PermissionRationaleDialog(permissionRequester)
  val state by viewModel.notesScreenState.collectAsState()
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
    snackbarHostState = snackbarHostState,
    onBackClick = { hostDeps.navController.popBackStack() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onSortOrderSelected = viewModel::onSortOrderSelected,
    onGridToggleClick = viewModel::onGridToggleClick,
    onArchiveClick = viewModel::onArchiveClick,
    onSettingsClick = viewModel::onSettingsClick,
    onAddClick = viewModel::onAddClick,
    onNoteClick = viewModel::onNoteClick,
    onNoteMenuAction = viewModel::onNoteMenuAction,
    onImageClick = viewModel::onImageClick,
  )
}

@Composable
private fun NotesArchiveEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(true) }
  bindLifecycle(viewModel)
  val permissionRequester = rememberPermissionRequester()
  val hostDeps = rememberNotesHostDependencies(permissionRequester)
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, viewModel, backStack, hostDeps) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  viewModel.errorEvent.ObserveEvent { message -> scope.launch { snackbarHostState.showSnackbar(message) } }

  PermissionRationaleDialog(permissionRequester)
  val state by viewModel.notesScreenState.collectAsState()
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
    snackbarHostState = snackbarHostState,
    onBackClick = { backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onSortOrderSelected = viewModel::onSortOrderSelected,
    onGridToggleClick = viewModel::onGridToggleClick,
    onArchiveClick = null,
    onSettingsClick = null,
    onAddClick = null,
    onNoteClick = viewModel::onNoteClick,
    onNoteMenuAction = viewModel::onNoteMenuAction,
    onImageClick = viewModel::onImageClick,
  )
}

@Composable
private fun NotePreviewEntry(
  key: NotesNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewNoteViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  LaunchedEffect(Unit) { viewModel.saveStatusBarColor(key.initialStatusBarColor) }

  val activity =
    requireNotNull(LocalActivity.current) { "NotePreviewEntry requires an Activity-backed composition" }
  val view = LocalView.current
  val navController = remember(view) { view.findNavController() }
  val permissionRequester = rememberPermissionRequester()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val errorSendingMessage = stringResource(R.string.error_sending)

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PreviewNoteViewModel.NavigationEvent.EditNote -> {
        backStack.add(NotesNavKey.Edit(event.id))
      }

      is PreviewNoteViewModel.NavigationEvent.EditReminder -> {
        navController.safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is PreviewNoteViewModel.NavigationEvent.OpenImagePreview -> {
        backStack.add(
          NotesNavKey.ImagePreview(event.position, activity.window.statusBarColor),
        )
      }
    }
  }
  viewModel.resultEvent.ObserveEvent { if (it == Commands.DELETED) backStack.removeLastOrNull() }
  viewModel.errorEvent.ObserveEvent { scope.launch { snackbarHostState.showSnackbar(errorSendingMessage) } }
  viewModel.sharedFile.ObserveNonNull { (note, file) ->
    if (file.exists() && file.canRead()) {
      TelephonyUtil.sendNote(file, activity, note.note?.summary)
    } else {
      scope.launch { snackbarHostState.showSnackbar(errorSendingMessage) }
    }
  }

  val state by viewModel.state.collectAsState(PreviewNoteState())
  val colors = remember(state.backgroundColor, state.opacity) { viewModel.colorsFor(state) }
  SideEffect {
    activity.window.statusBarColor = colors.statusBarColor
    activity.window.navigationBarColor = colors.statusBarColor
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.getStatusBarColor()?.also {
        activity.window.statusBarColor = it
        activity.window.navigationBarColor = it
      }
    }
  }

  PermissionRationaleDialog(permissionRequester)
  PreviewNoteScreen(
    state = state,
    colors = colors,
    snackbarHostState = snackbarHostState,
    actions =
      PreviewNoteActions(
        onBackClick = { backStack.removeLastOrNull() },
        onEditClick = viewModel::onEditClick,
        onStatusClick = {
          permissionRequester.request(Permissions.POST_NOTIFICATION, onGranted = { viewModel.onStatusClick() })
        },
        onShareClick = viewModel::onShareClick,
        onArchiveClick = viewModel::onArchiveClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDialogDismiss = viewModel::onDialogDismiss,
        onImageOpen = viewModel::onImageOpen,
        onReminderEditClick = viewModel::onReminderEditClick,
        onReminderDetachClick = viewModel::onReminderDetachClick,
      ),
    adsBanner =
      if (state.showAdsBanner) {
        { NoteNativeAdBanner(remember { AdsProvider() }) }
      } else {
        null
      },
  )
}

@Composable
private fun NoteEditEntry(
  key: NotesNavKey.Edit,
  backStack: MutableList<NavKey>,
  arguments: Bundle?,
) {
  val viewModel = koinViewModel<NoteEditViewModel> { parametersOf(key.id, arguments) }

  val context = LocalContext.current
  val speechEngine = remember(viewModel) { SpeechEngine(context) }
  val speechCallback =
    remember(viewModel) {
      object : SpeechEngineCallback() {
        override fun onStarted() {
          super.onStarted()
          viewModel.onSpeechStarted()
        }

        override fun onStopped() {
          super.onStopped()
          viewModel.onSpeechStopped()
        }

        override fun onSpeechStarted() {
          super.onSpeechStarted()
          viewModel.onSpeechSpeaking()
        }

        override fun onSpeechEnded() {
          super.onSpeechEnded()
          viewModel.onSpeechStopped()
        }

        override fun onSpeechError(error: SpeechError) {
          super.onSpeechError(error)
          viewModel.onSpeechError()
        }

        override fun onSpeechResult(speechText: SpeechText) {
          super.onSpeechResult(speechText)
          viewModel.onSpeechResult(
            text = speechText.text,
            boldRange = speechText.newText?.let { it.startIndex..it.endIndex },
          )
        }
      }
    }
  DisposableEffect(speechEngine) {
    onDispose { speechEngine.stopListening() }
  }
  viewModel.textUpdate.ObserveEvent { update -> speechEngine.setText(update.text) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val errorSendingMessage = stringResource(R.string.error_sending)
  viewModel.event.ObserveEvent { event ->
    when (event) {
      is NoteEditViewModel.Action.Finish -> {
        backStack.removeLastOrNull()
      }
      is NoteEditViewModel.Action.Error -> {
        scope.launch { snackbarHostState.showSnackbar(event.message) }
      }
      is NoteEditViewModel.Action.OpenImagePreview -> {
        backStack.add(NotesNavKey.ImagePreview(event.position))
      }
      is NoteEditViewModel.Action.ShareNote -> {
        if (event.file.exists() && event.file.canRead()) {
          TelephonyUtil.sendNote(event.file, context, event.text)
        } else {
          scope.launch { snackbarHostState.showSnackbar(errorSendingMessage) }
        }
      }
    }
  }

  val state by viewModel.state.collectAsState()

  val galleryPicker = rememberGalleryPicker { uris -> viewModel.addMultiple(uris) }
  val cameraPicker = rememberCameraPicker { uri -> viewModel.addMultiple(listOf(uri)) }
  val permissionRequester = rememberPermissionRequester()
  val dateTimePickerState = rememberDateTimePickerState(is24Hour = viewModel.is24HourFormat)
  val urlImagePickerState = rememberUrlImagePickerState()
  val selectDateTitle = stringResource(R.string.select_date)
  val selectTimeTitle = stringResource(R.string.select_time)

  PermissionRationaleDialog(permissionRequester)
  DateTimePickerDialogs(dateTimePickerState)
  UrlImagePickerDialogs(urlImagePickerState, onUrlConfirmed = viewModel::downloadImageFromUrl)

  NoteEditScreen(
    state = state,
    supportsSpeech = remember { speechEngine.supportsRecognition() },
    onTextFieldValueChange = viewModel::onTextFieldValueChange,
    onTitleFieldValueChange = viewModel::onTitleFieldValueChange,
    colorsForPalette = viewModel::sliderColorsForPalette,
    snackbarHostState = snackbarHostState,
    actions =
      NoteEditActions(
        onBackClick = { backStack.removeLastOrNull() },
        onSaveClick = viewModel::onSaveClicked,
        onShareClick = viewModel::onShareClick,
        onDeleteClick = viewModel::onDeleteRequested,
        onMicClick = {
          permissionRequester.request(Permissions.RECORD_AUDIO, onGranted = {
            if (speechEngine.isStarted()) {
              speechEngine.stopListening()
            } else {
              speechEngine.startListening(speechCallback)
            }
          })
        },
        onColorTabClick = { viewModel.onTabClicked(EditTab.COLOR) },
        onImageTabClick = { viewModel.onTabClicked(EditTab.IMAGE) },
        onImagePickFromGallery = {
          permissionRequester.request(Permissions.READ_EXTERNAL, onGranted = galleryPicker)
          viewModel.collapseExpandedTab()
        },
        onImagePickFromCamera = {
          permissionRequester.request(
            listOf(Permissions.CAMERA, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL),
            onGranted = { cameraPicker() },
          )
          viewModel.collapseExpandedTab()
        },
        onImagePickFromUrl = {
          urlImagePickerState.start(context)
          viewModel.collapseExpandedTab()
        },
        onReminderTabClick = { viewModel.onTabClicked(EditTab.REMINDER) },
        onFontTabClick = { viewModel.onTabClicked(EditTab.FONT) },
        onColorSelected = viewModel::onColorSelected,
        onOpacityChanged = viewModel::onOpacityChanged,
        onReminderAttachedChanged = viewModel::onReminderAttachedChanged,
        onDateClick = {
          dateTimePickerState.showDatePicker(state.date, selectDateTitle, viewModel::onNewDate)
        },
        onTimeClick = {
          dateTimePickerState.showTimePicker(state.time, selectTimeTitle, viewModel::onNewTime)
        },
        onFontSizeChanged = viewModel::onFontSizeChanged,
        onFieldFocused = viewModel::onFieldFocused,
        onImageOpen = { position -> viewModel.onImageOpen(position) },
        onImageRemove = viewModel::removeImage,
        onFontStyleSelected = viewModel::onFontStyleChanged,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onSameNoteKeep = { viewModel.saveNote(newId = true) },
        onSameNoteReplace = { viewModel.saveNote() },
        onDialogDismiss = viewModel::onDialogDismissed,
        onDrop = { clipData -> if (clipData.itemCount > 0) viewModel.parseDrop(clipData) },
      ),
  )
}

@Composable
private fun NoteImagePreviewEntry(
  key: NotesNavKey.ImagePreview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<ImagePreviewViewModel> { parametersOf(key.position) }
  LaunchedEffect(Unit) { viewModel.saveStatusBarColor(key.initialStatusBarColor) }

  val activity =
    requireNotNull(LocalActivity.current) { "NoteImagePreviewEntry requires an Activity-backed composition" }
  val state by viewModel.state.collectAsState(ImagePreviewState())
  val colors = viewModel.colorsFor(state)
  SideEffect {
    colors.statusBarColor?.let {
      activity.window.statusBarColor = it
      activity.window.navigationBarColor = it
    }
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.getStatusBarColor()?.also {
        activity.window.statusBarColor = it
        activity.window.navigationBarColor = it
      }
    }
  }

  ImagePreviewScreen(
    state = state,
    colors = colors,
    onBackClick = { backStack.removeLastOrNull() },
    onPageChanged = viewModel::onPageChanged,
  )
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

@Composable
private fun NoteNativeAdBanner(adsProvider: AdsProvider) {
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup ->
      adsProvider.showNativeBanner(
        viewGroup,
        AdsProvider.NOTE_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor,
      )
    },
  )
}
