package com.github.naz013.feature.note

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.common.Permissions
import com.github.naz013.common.datapicker.compose.rememberCameraPicker
import com.github.naz013.common.datapicker.compose.rememberGalleryPicker
import com.github.naz013.common.speech.SpeechEngine
import com.github.naz013.common.speech.SpeechEngineCallback
import com.github.naz013.common.speech.SpeechError
import com.github.naz013.common.speech.SpeechText
import com.github.naz013.feature.note.create.EditTab
import com.github.naz013.feature.note.create.NoteEditActions
import com.github.naz013.feature.note.create.NoteEditScreen
import com.github.naz013.feature.note.create.NoteEditViewModel
import com.github.naz013.feature.note.create.UrlImagePickerDialogs
import com.github.naz013.feature.note.create.rememberUrlImagePickerState
import com.github.naz013.feature.note.list.NotesScreen
import com.github.naz013.feature.note.list.NotesScreenState
import com.github.naz013.feature.note.list.NotesViewModel
import com.github.naz013.feature.note.preview.ImagePreviewScreen
import com.github.naz013.feature.note.preview.ImagePreviewState
import com.github.naz013.feature.note.preview.ImagePreviewViewModel
import com.github.naz013.feature.note.preview.PreviewNoteActions
import com.github.naz013.feature.note.preview.PreviewNoteScreen
import com.github.naz013.feature.note.preview.PreviewNoteState
import com.github.naz013.feature.note.preview.PreviewNoteViewModel
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.DialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberColorPickerDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.ToastDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.PermissionRequester
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.note.NoteColorEngine
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.notesEntries(
  backStack: MutableList<NavKey>,
  applicationId: String,
  onOpenNoteSettings: (String) -> Unit,
  onEditReminder: (String) -> Unit,
  adsContent: @Composable () -> Unit = {},
) {
  entry<NotesNavKey.List> { NotesListEntry(backStack, applicationId, onOpenNoteSettings) }
  entry<NotesNavKey.Archive> { NotesArchiveEntry(backStack, applicationId) }
  entry<NotesNavKey.Preview> { key -> NotePreviewEntry(key, backStack, applicationId, onEditReminder, adsContent) }
  entry<NotesNavKey.Edit> { key -> NoteEditEntry(key, backStack, applicationId) }
  entry<NotesNavKey.ImagePreview> { key -> NoteImagePreviewEntry(key, backStack) }
}

/** Bundles [NotesListEntry]/[NotesArchiveEntry]'s shared navigation-event handler dependencies,
 * which are otherwise identical across both call sites - keeps [handleNotesNavigationEvent] to a
 * reasonable parameter count. */
private class NotesNavHandlers(
  val viewModel: NotesViewModel,
  val backStack: MutableList<NavKey>,
  val dialogDispatcher: DialogDispatcher,
  val toastDispatcher: ToastDispatcher,
  val noteIntentSender: NoteIntentSender,
  val permissionRequester: PermissionRequester,
  val onOpenNoteSettings: (String) -> Unit,
)

private fun handleNotesNavigationEvent(
  event: NotesViewModel.NavigationEvent,
  handlers: NotesNavHandlers,
) {
  when (event) {
    is NotesViewModel.NavigationEvent.OpenNotePreview -> {
      handlers.backStack.add(NotesNavKey.Preview(event.id))
    }

    is NotesViewModel.NavigationEvent.OpenCreateNote -> {
      handlers.backStack.add(NotesNavKey.Edit())
    }

    is NotesViewModel.NavigationEvent.OpenEditNote -> {
      handlers.backStack.add(NotesNavKey.Edit(event.id))
    }

    is NotesViewModel.NavigationEvent.OpenArchive -> handlers.backStack.add(NotesNavKey.Archive)

    is NotesViewModel.NavigationEvent.OpenSettings -> {
      handlers.onOpenNoteSettings(event.title)
    }

    is NotesViewModel.NavigationEvent.OpenImagePreview -> {
      handlers.backStack.add(NotesNavKey.ImagePreview(event.imagePosition))
    }

    is NotesViewModel.NavigationEvent.ShareNote -> {
      handlers.noteIntentSender.send(event.summary, event.file)
    }

    is NotesViewModel.NavigationEvent.RequestNotificationPermission -> {
      handlers.permissionRequester.request(
        Permissions.POST_NOTIFICATION,
        onGranted = { handlers.viewModel.showNoteInNotification(event.id) },
      )
    }

    is NotesViewModel.NavigationEvent.ConfirmDelete -> {
      handlers.dialogDispatcher.showDialog(
        titleRes = R.string.delete_note_permanently,
        positiveButtonRes = R.string.yes,
        negativeButtonRes = R.string.cancel,
        onPositive = { handlers.viewModel.deleteNote(event.id) }
      )
    }

    is NotesViewModel.NavigationEvent.ConfirmDeleteSelected -> {
      handlers.dialogDispatcher.showDialog(
        title = event.title,
        positiveButtonRes = R.string.yes,
        negativeButtonRes = R.string.cancel,
        onPositive = { handlers.viewModel.deleteSelectedNotes(event.ids) }
      )
    }

    is NotesViewModel.NavigationEvent.Error -> {
      handlers.toastDispatcher.showToast(message = event.message)
    }
  }
}

@Composable
private fun NotesListEntry(
  backStack: MutableList<NavKey>,
  applicationId: String,
  onOpenNoteSettings: (String) -> Unit,
) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(false) }

  val permissionRequester = rememberPermissionRequesterRationale()
  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val noteIntentSender = rememberNoteIntentSender(applicationId)
  val colorPickerDialogDispatcher = rememberColorPickerDialogDispatcher()
  val noteColorEngine = koinInject<NoteColorEngine>()

  val handlers =
    NotesNavHandlers(
      viewModel = viewModel,
      backStack = backStack,
      dialogDispatcher = dialogDispatcher,
      toastDispatcher = toastDispatcher,
      noteIntentSender = noteIntentSender,
      permissionRequester = permissionRequester,
      onOpenNoteSettings = onOpenNoteSettings,
    )
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, handlers) }

  val state by viewModel.notesScreenState.collectAsState(NotesScreenState())
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onSortOrderSelected = viewModel::onSortOrderSelected,
    onGridToggleClick = viewModel::onGridToggleClick,
    onArchiveClick = viewModel::onArchiveClick,
    onSettingsClick = viewModel::onSettingsClick,
    onAddClick = viewModel::onAddClick,
    onNoteClick = viewModel::onNoteClick,
    onNoteLongClick = viewModel::onNoteLongClick,
    onNoteMenuAction = viewModel::onNoteMenuAction,
    onImageClick = viewModel::onImageClick,
    onTagSelected = viewModel::onTagSelected,
    onSelectionCancel = viewModel::onSelectionCancel,
    onDeleteSelectedClick = viewModel::onDeleteSelectedClick,
    onArchiveSelectedClick = viewModel::onArchiveSelectedClick,
    onChangeColorClick = {
      colorPickerDialogDispatcher.showDialog(
        titleRes = R.string.acc_select_color,
        colors = noteColorEngine.allColors(),
        selectedIndex = 0,
        onColorSelected = viewModel::applySelectedColor,
      )
    },
  )
}

@Composable
private fun NotesArchiveEntry(backStack: MutableList<NavKey>, applicationId: String) {
  val viewModel = koinViewModel<NotesViewModel> { parametersOf(true) }

  val permissionRequester = rememberPermissionRequesterRationale()
  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val noteIntentSender = rememberNoteIntentSender(applicationId)
  val colorPickerDialogDispatcher = rememberColorPickerDialogDispatcher()
  val noteColorEngine = koinInject<NoteColorEngine>()

  val handlers =
    NotesNavHandlers(
      viewModel = viewModel,
      backStack = backStack,
      dialogDispatcher = dialogDispatcher,
      toastDispatcher = toastDispatcher,
      noteIntentSender = noteIntentSender,
      permissionRequester = permissionRequester,
      onOpenNoteSettings = {},
    )
  viewModel.navigationEvent.ObserveEvent { handleNotesNavigationEvent(it, handlers) }

  val state by viewModel.notesScreenState.collectAsState(NotesScreenState())
  NotesScreen(
    modifier = Modifier.fillMaxSize(),
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onSortOrderSelected = viewModel::onSortOrderSelected,
    onGridToggleClick = viewModel::onGridToggleClick,
    onArchiveClick = null,
    onSettingsClick = null,
    onAddClick = null,
    onNoteClick = viewModel::onNoteClick,
    onNoteLongClick = viewModel::onNoteLongClick,
    onNoteMenuAction = viewModel::onNoteMenuAction,
    onImageClick = viewModel::onImageClick,
    onTagSelected = viewModel::onTagSelected,
    onSelectionCancel = viewModel::onSelectionCancel,
    onDeleteSelectedClick = viewModel::onDeleteSelectedClick,
    onArchiveSelectedClick = viewModel::onArchiveSelectedClick,
    onChangeColorClick = {
      colorPickerDialogDispatcher.showDialog(
        titleRes = R.string.acc_select_color,
        colors = noteColorEngine.allColors(),
        selectedIndex = 0,
        onColorSelected = viewModel::applySelectedColor,
      )
    },
  )
}

@Composable
private fun NotePreviewEntry(
  key: NotesNavKey.Preview,
  backStack: MutableList<NavKey>,
  applicationId: String,
  onEditReminder: (String) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<PreviewNoteViewModel> { parametersOf(key.id) }

  val permissionRequester = rememberPermissionRequesterRationale()
  val noteIntentSender = rememberNoteIntentSender(applicationId)
  val toastDispatcher = rememberToastDispatcher()
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is PreviewNoteViewModel.ViewModelEvent.EditNote -> {
        backStack.add(NotesNavKey.Edit(event.id))
      }

      is PreviewNoteViewModel.ViewModelEvent.EditReminder -> {
        onEditReminder(event.id)
      }

      is PreviewNoteViewModel.ViewModelEvent.OpenImagePreview -> {
        backStack.add(
          NotesNavKey.ImagePreview(event.position),
        )
      }

      is PreviewNoteViewModel.ViewModelEvent.Message -> {
        toastDispatcher.showToast(message = event.message)
      }

      is PreviewNoteViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is PreviewNoteViewModel.ViewModelEvent.ShareNote -> {
        noteIntentSender.send(event.text, event.file)
      }

      is PreviewNoteViewModel.ViewModelEvent.Delete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_this_note,
          positiveButtonRes = R.string.delete,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.onDeleteConfirmed() }
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewNoteState())
  PreviewNoteScreen(
    state = state,
    actions =
    PreviewNoteActions(
      onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
      onEditClick = viewModel::onEditClick,
      onStatusClick = {
        permissionRequester.request(Permissions.POST_NOTIFICATION, onGranted = { viewModel.onStatusClick() })
      },
      onShareClick = viewModel::onShareClick,
      onArchiveClick = viewModel::onArchiveClick,
      onPinClick = viewModel::onPinClick,
      onDeleteClick = viewModel::onDeleteClick,
      onImageOpen = viewModel::onImageOpen,
      onReminderEditClick = viewModel::onReminderEditClick,
      onReminderDetachClick = viewModel::onReminderDetachClick,
    ),
    adsBanner = adsContent,
  )
}

@Composable
private fun NoteEditEntry(
  key: NotesNavKey.Edit,
  backStack: MutableList<NavKey>,
  applicationId: String,
) {
  val viewModel = koinViewModel<NoteEditViewModel> {
    parametersOf(key.id, key.sharedText, key.sharedImageUris, key.fromIntentData)
  }

  val toastDispatcher = rememberToastDispatcher()
  val noteIntentSender = rememberNoteIntentSender(applicationId)
  val context = LocalContext.current
  val galleryPicker =
    rememberGalleryPicker(chooserTitle = stringResource(R.string.gallery)) { uris ->
      viewModel.addMultiple(uris)
    }
  val cameraPicker =
    rememberCameraPicker(applicationId = applicationId) { uri ->
      viewModel.addMultiple(listOf(uri))
    }
  val permissionRequester = rememberPermissionRequesterRationale()
  val dateTimePicker = rememberDateTimePicker()
  val urlImagePickerState = rememberUrlImagePickerState()
  UrlImagePickerDialogs(urlImagePickerState, onUrlConfirmed = viewModel::downloadImageFromUrl)

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
      is NoteEditViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is NoteEditViewModel.ViewModelEvent.Error -> {
        toastDispatcher.showToast(message = event.message)
      }

      is NoteEditViewModel.ViewModelEvent.OpenImagePreview -> {
        backStack.add(NotesNavKey.ImagePreview(event.position))
      }

      is NoteEditViewModel.ViewModelEvent.ShareNote -> {
        noteIntentSender.send(event.text, event.file)
      }

      is NoteEditViewModel.ViewModelEvent.ShowDatePicker -> {
        dateTimePicker.showDatePicker(event.date, event.title, viewModel::onNewDate)
      }

      is NoteEditViewModel.ViewModelEvent.ShowTimePicker -> {
        dateTimePicker.showTimePicker(
          time = event.time,
          title = event.title,
          is24Hour = viewModel.is24HourFormat,
          onTimeSelected = viewModel::onNewTime
        )
      }

      NoteEditViewModel.ViewModelEvent.OpenManageTags -> {
        backStack.add(TagsNavKey.Manage)
      }
    }
  }

  val state by viewModel.state.collectAsState()
  NoteEditScreen(
    state = state,
    supportsSpeech = remember { speechEngine.supportsRecognition() },
    onTextFieldValueChange = viewModel::onTextFieldValueChange,
    onTitleFieldValueChange = viewModel::onTitleFieldValueChange,
    actions =
    NoteEditActions(
      onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
      onDateClick = viewModel::onDateClicked,
      onTimeClick = viewModel::onTimeClicked,
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
      onTagsTabClick = { viewModel.onTabClicked(EditTab.TAGS) },
      onTagToggle = viewModel::onTagToggle,
      onManageTagsClick = viewModel::onManageTagsClick,
    ),
  )
}

@Composable
private fun NoteImagePreviewEntry(
  key: NotesNavKey.ImagePreview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<ImagePreviewViewModel> { parametersOf(key.position) }
  val state by viewModel.state.collectAsState(ImagePreviewState())

  ImagePreviewScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onPageChanged = viewModel::onPageChanged,
  )
}
