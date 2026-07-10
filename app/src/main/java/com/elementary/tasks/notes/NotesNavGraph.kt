package com.elementary.tasks.notes

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.fragment.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.core.os.datapicker.compose.rememberCameraPicker
import com.elementary.tasks.core.os.datapicker.compose.rememberGalleryPicker
import com.elementary.tasks.core.utils.ui.compose.DateTimePickerDialogs
import com.elementary.tasks.core.utils.ui.compose.rememberDateTimePickerState
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.notes.create.NoteEditViewModel
import com.elementary.tasks.notes.create.EditTab
import com.elementary.tasks.notes.create.NoteEditActions
import com.elementary.tasks.notes.create.NoteEditScreen
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
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.ui.common.fragment.toast
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.io.File

/**
 * Builds the Notes island's [NavDisplay] — the "screens" (Nav3 entries) themselves and the
 * routing between them. [NotesFragment] only owns the backstack and the Android-framework glue
 * (permissions, photo picking, dialogs, date/time pickers) that these entries react to.
 */
@Composable
internal fun NotesFragment.NotesNavGraph(backStack: MutableList<NavKey>) {
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
        entry<NotesNavKey.Edit> { key -> NoteEditEntry(key, backStack) }
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

private fun NotesFragment.handleNotesNavigationEvent(
  event: NotesViewModel.NavigationEvent,
  viewModel: NotesViewModel,
  backStack: MutableList<NavKey>,
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
      safeNavigation(
        NotesFragmentDirections.actionActionNotesToNoteSettingsFragment(
          getString(R.string.action_settings),
        ),
      )
    }

    is NotesViewModel.NavigationEvent.OpenImagePreview -> {
      backStack.add(NotesNavKey.ImagePreview(event.imagePosition))
    }

    is NotesViewModel.NavigationEvent.ShareNote -> {
      TelephonyUtil.sendNote(event.file, requireContext(), event.summary)
    }

    is NotesViewModel.NavigationEvent.RequestNotificationPermission -> {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
        viewModel.showNoteInNotification(event.id)
      }
    }

    is NotesViewModel.NavigationEvent.PickColor -> {
      dialogues.showColorDialog(
        requireActivity(),
        event.colorPosition,
        getString(R.string.color),
        event.sliderColors,
      ) { color -> viewModel.saveNoteColor(event.id, color) }
    }

    is NotesViewModel.NavigationEvent.ConfirmDelete -> {
      dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
        if (confirmed) viewModel.deleteNote(event.id)
      }
    }
  }
}

@Composable
private fun NotesFragment.NotesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(false) }
  bindLifecycle(viewModel)
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, viewModel, backStack) }
  viewModel.errorEvent.ObserveEvent { toast(it) }

  val state by viewModel.notesScreenState.collectAsState()
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
    onBackClick = { findNavController().popBackStack() },
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
private fun NotesFragment.NotesArchiveEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(true) }
  bindLifecycle(viewModel)
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, viewModel, backStack) }
  viewModel.errorEvent.ObserveEvent { toast(it) }

  val state by viewModel.notesScreenState.collectAsState()
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
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
private fun NotesFragment.NotePreviewEntry(
  key: NotesNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewNoteViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  LaunchedEffect(Unit) { viewModel.saveStatusBarColor(key.initialStatusBarColor) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is PreviewNoteViewModel.NavigationEvent.EditNote -> {
        backStack.add(NotesNavKey.Edit(event.id, requireActivity().window.statusBarColor))
      }

      is PreviewNoteViewModel.NavigationEvent.EditReminder -> {
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is PreviewNoteViewModel.NavigationEvent.OpenImagePreview -> {
        backStack.add(
          NotesNavKey.ImagePreview(event.position, requireActivity().window.statusBarColor),
        )
      }
    }
  }
  viewModel.resultEvent.ObserveEvent { if (it == Commands.DELETED) backStack.removeLastOrNull() }
  viewModel.errorEvent.ObserveEvent { showErrorSending() }
  viewModel.sharedFile.ObserveNonNull { sendNoteWithImages(it.first, it.second) }

  val state by viewModel.state.collectAsState(PreviewNoteState())
  val colors = remember(state.backgroundColor, state.opacity) { viewModel.colorsFor(state) }
  SideEffect {
    requireActivity().window.statusBarColor = colors.statusBarColor
    requireActivity().window.navigationBarColor = colors.statusBarColor
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.getStatusBarColor()?.also {
        requireActivity().window.statusBarColor = it
        requireActivity().window.navigationBarColor = it
      }
    }
  }

  PreviewNoteScreen(
    state = state,
    colors = colors,
    actions =
      PreviewNoteActions(
        onBackClick = { backStack.removeLastOrNull() },
        onEditClick = viewModel::onEditClick,
        onStatusClick = {
          permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { viewModel.onStatusClick() }
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
        { NoteNativeAdBanner(adsProvider) }
      } else {
        null
      },
  )
}

private fun NotesFragment.sendNoteWithImages(
  note: NoteWithImages,
  file: File,
) {
  if (isDetached) return
  if (!file.exists() || !file.canRead()) {
    showErrorSending()
    return
  }
  TelephonyUtil.sendNote(file, requireContext(), note.note?.summary)
}

private fun NotesFragment.showErrorSending() {
  toast(R.string.error_sending)
}

@Composable
private fun NoteEditEntry(
  key: NotesNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<NoteEditViewModel> { parametersOf(key.id, null /**arguments**/) }

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
  viewModel.event.ObserveEvent { event ->
    when (event) {
      is NoteEditViewModel.Action.Finish -> {
        backStack.removeLastOrNull()
      }
      is NoteEditViewModel.Action.Error -> {

      }
      is NoteEditViewModel.Action.OpenImagePreview -> {

      }
      is NoteEditViewModel.Action.ShareNote -> {
        if (event.file.exists() && event.file.canRead()) {
          TelephonyUtil.sendNote(event.file, context, event.text)
        } else {
          Toast.makeText(context, R.string.error_sending, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

//  val decorView = requireActivity().window.decorView
//  val primaryColor = remember { ThemeProvider.getPrimaryColor(requireContext()) }
//  DisposableEffect(viewModel) {
//    ViewUtils.registerDragAndDrop(
//      requireActivity(),
//      decorView,
//      true,
//      primaryColor,
//      { clipData -> if (clipData.itemCount > 0) viewModel.parseDrop(clipData) },
//      ClipDescription.MIMETYPE_TEXT_PLAIN,
//      UriUtil.ANY_MIME,
//    )
//    onDispose { decorView.setOnDragListener(null) }
//  }

//  LaunchedEffect(Unit) {
//    val args = arguments
//    when {
//      args?.containsKey(Intent.EXTRA_TEXT) == true -> {
//        args.getString(Intent.EXTRA_TEXT)?.let { viewModel.onSharedTextReceived(it) }
//      }
//
//      args?.containsKey(Intent.EXTRA_STREAM) == true -> {
//        val uris = args.getParcelableArrayList<Parcelable>(Intent.EXTRA_STREAM)
//        uris?.let { list -> viewModel.addMultiple(list.filterNotNull().filterIsInstance<Uri>()) }
//      }
//
//      args?.getBoolean(IntentKeys.INTENT_ITEM, false) == true -> viewModel.onNoteReceivedFromIntent()
//    }
//  }

//  viewModel.errorEvent.ObserveEvent { toast(it) }

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
      ),
  )
}

@Composable
private fun NotesFragment.NoteImagePreviewEntry(
  key: NotesNavKey.ImagePreview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<ImagePreviewViewModel> { parametersOf(key.position) }
  LaunchedEffect(Unit) { viewModel.saveStatusBarColor(key.initialStatusBarColor) }

  val state by viewModel.state.collectAsState(ImagePreviewState())
  val colors = viewModel.colorsFor(state)
  SideEffect {
    colors.statusBarColor?.let {
      requireActivity().window.statusBarColor = it
      requireActivity().window.navigationBarColor = it
    }
  }
  DisposableEffect(viewModel) {
    onDispose {
      viewModel.getStatusBarColor()?.also {
        requireActivity().window.statusBarColor = it
        requireActivity().window.navigationBarColor = it
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
