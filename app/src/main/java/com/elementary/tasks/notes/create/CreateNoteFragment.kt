package com.elementary.tasks.notes.create

import android.content.ClipDescription
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.ComposeFragment
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.startActivity
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class CreateNoteFragment :
  ComposeFragment(),
  PhotoSelectionUtil.UriCallback,
  BackPressHandler {
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val dialogues by inject<Dialogues>()

  private val viewModel by viewModel<CreateNoteViewModel> { parametersOf(idFromArguments()) }
  private lateinit var photoSelectionUtil: PhotoSelectionUtil
  private lateinit var permissionFlow: PermissionFlow
  private val speechEngine by lazy { SpeechEngine(requireContext()) }

  private var textFieldValue by mutableStateOf(TextFieldValue())
  private var titleFieldValue by mutableStateOf(TextFieldValue())
  private var boldRange by mutableStateOf<IntRange?>(null)
  private var speechUiState by mutableStateOf(SpeechUiState.IDLE)
  private var activeDialog by mutableStateOf<NoteEditDialog?>(null)

  private val speechEngineCallback =
    object : SpeechEngineCallback() {
      override fun onStarted() {
        super.onStarted()
        speechUiState = SpeechUiState.STARTED
      }

      override fun onStopped() {
        super.onStopped()
        speechUiState = SpeechUiState.IDLE
      }

      override fun onSpeechStarted() {
        super.onSpeechStarted()
        speechUiState = SpeechUiState.SPEAKING
      }

      override fun onSpeechEnded() {
        super.onSpeechEnded()
        speechUiState = SpeechUiState.STOPPED
      }

      override fun onSpeechError(error: SpeechError) {
        super.onSpeechError(error)
        speechUiState = SpeechUiState.IDLE
      }

      override fun onSpeechResult(speechText: SpeechText) {
        super.onSpeechResult(speechText)
        textFieldValue =
          TextFieldValue(
            text = speechText.text,
            selection = TextRange(speechText.text.length),
          )
        boldRange = speechText.newText?.let { it.startIndex..it.endIndex }
      }
    }

  private fun idFromArguments(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
    photoSelectionUtil = PhotoSelectionUtil(this, this)

    lifecycle.addObserver(photoSelectionUtil)
    lifecycle.addObserver(viewModel)
    viewModel.saveStatusBarColor(activity?.window?.statusBarColor ?: -1)

    savedInstanceState?.getString(STATE_TEXT)?.let {
      textFieldValue = TextFieldValue(text = it, selection = TextRange(it.length))
    }
    savedInstanceState?.getString(STATE_TITLE)?.let {
      titleFieldValue = TextFieldValue(text = it, selection = TextRange(it.length))
    }
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    initViewModel()
    loadNote()
  }

  override fun onStart() {
    super.onStart()
    ViewUtils.registerDragAndDrop(
      requireActivity(),
      requireActivity().window.decorView,
      true,
      ThemeProvider.getPrimaryColor(requireContext()),
      {
        if (it.itemCount > 0) {
          viewModel.parseDrop(it, getText())
        }
      },
      ClipDescription.MIMETYPE_TEXT_PLAIN,
      UriUtil.ANY_MIME,
    )
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  override fun onPause() {
    super.onPause()
    viewModel.getStatusBarColor()?.also {
      activity?.window?.statusBarColor = it
      activity?.window?.navigationBarColor = it
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_TEXT, textFieldValue.text)
    outState.putString(STATE_TITLE, titleFieldValue.text)
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.removeObserver(viewModel)
    hideKeyboard()
    speechEngine.stopListening()
  }

  override fun canGoBack(): Boolean = !viewModel.collapseExpandedTab()

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsState()
    val colors = remember(state) { viewModel.colorsFor(state) }

    SideEffect {
      activity?.window?.statusBarColor = colors.statusBarColor
      activity?.window?.navigationBarColor = colors.statusBarColor
    }

    NoteEditScreen(
      state = state,
      speechState = speechUiState,
      supportsSpeech = remember { speechEngine.supportsRecognition() },
      hasCamera = remember { photoSelectionUtil.hasCamera() },
      textFieldValue = textFieldValue,
      onTextFieldValueChange = {
        textFieldValue = it
        boldRange = null
      },
      titleFieldValue = titleFieldValue,
      onTitleFieldValueChange = { titleFieldValue = it },
      boldRange = boldRange,
      backgroundColor = Color(colors.background),
      contentColor = Color(colors.content),
      sliderColors = colors.sliderColors,
      activeDialog = activeDialog,
      colorsForPalette = viewModel::sliderColorsForPalette,
      actions =
        NoteEditActions(
          onBackClick = { moveBack() },
          onSaveClick = { trySave() },
          onShareClick = { viewModel.shareNote(getText(), getNoteTitle()) },
          onDeleteClick = { activeDialog = NoteEditDialog.DELETE },
          onMicClick = { tryMicClick() },
          onColorTabClick = { viewModel.onTabClicked(EditTab.COLOR) },
          onImageTabClick = { viewModel.onTabClicked(EditTab.IMAGE) },
          onImagePickFromGallery = {
            photoSelectionUtil.tryToPickFromGallery()
            viewModel.collapseExpandedTab()
          },
          onImagePickFromCamera = {
            photoSelectionUtil.tryToTakePhoto()
            viewModel.collapseExpandedTab()
          },
          onImagePickFromUrl = {
            photoSelectionUtil.checkClipboard()
            viewModel.collapseExpandedTab()
          },
          onReminderTabClick = { viewModel.onTabClicked(EditTab.REMINDER) },
          onFontTabClick = { viewModel.onTabClicked(EditTab.FONT) },
          onColorSelected = { viewModel.onColorSelected(it) },
          onOpacityChanged = { viewModel.onOpacityChanged(it) },
          onReminderAttachedChanged = { viewModel.onReminderAttachedChanged(it) },
          onDateClick = { dateDialog() },
          onTimeClick = { timeDialog() },
          onFontSizeChanged = { viewModel.onFontSizeChanged(it) },
          onFieldFocused = { viewModel.onFieldFocused(it) },
          onImageOpen = { openImagePreview(it, state.colorIndex) },
          onImageRemove = { viewModel.removeImage(it) },
          onFontStyleSelected = { viewModel.onFontStyleChanged(it) },
          onPaletteSelected = { viewModel.onPaletteChanged(it) },
          onDeleteConfirmed = { viewModel.deleteNote() },
          onSameNoteKeep = { viewModel.saveNote(getText(), getNoteTitle(), newId = true) },
          onSameNoteReplace = { viewModel.saveNote(getText(), getNoteTitle()) },
          onDialogDismiss = { activeDialog = null },
        ),
    )
  }

  private fun moveBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
  }

  private fun trySave() {
    if (viewModel.shouldConfirmBeforeSaving()) {
      activeDialog = NoteEditDialog.SAME_NOTE
    } else {
      viewModel.saveNote(getText(), getNoteTitle())
    }
  }

  private fun getText(): String = textFieldValue.text.trim()

  private fun getNoteTitle(): String = titleFieldValue.text.trim()

  private fun setText(text: String?) {
    val value = text ?: ""
    speechEngine.setText(value)
    textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
    boldRange = null
  }

  private fun tryMicClick() {
    permissionFlow.askPermission(Permissions.RECORD_AUDIO) { micClick() }
  }

  private fun micClick() {
    if (speechEngine.isStarted()) {
      speechEngine.stopListening()
    } else {
      permissionFlow.askPermission(Permissions.RECORD_AUDIO) {
        speechEngine.startListening(speechEngineCallback)
      }
    }
  }

  private fun loadNote() {
    val args = arguments ?: return
    when {
      args.containsKey(Intent.EXTRA_TEXT) -> handleSendText(args)
      args.containsKey(Intent.EXTRA_STREAM) -> handleSendImages(args)
      args.getBoolean(IntentKeys.INTENT_ITEM, false) -> viewModel.onNoteReceivedFromIntent()
    }
  }

  private fun handleSendText(args: Bundle) {
    args.getString(Intent.EXTRA_TEXT)?.let {
      Logger.d(TAG, "handleSendText: $it")
      setText(it)
    }
  }

  private fun handleSendImages(args: Bundle) {
    args.getParcelableArrayList<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
      viewModel.addMultiple(list.filterNotNull().filterIsInstance<Uri>())
    }
  }

  private fun initViewModel() {
    viewModel.textUpdate.observeEvent(viewLifecycleOwner) { update ->
      Logger.d(TAG, "textUpdate: $update")
      speechEngine.setText(update.text)
      textFieldValue = TextFieldValue(text = update.text, selection = TextRange(update.text.length))
      boldRange = update.boldRange
    }
    viewModel.titleUpdate.observeEvent(viewLifecycleOwner) { title ->
      Logger.d(TAG, "titleUpdate: $title")
      titleFieldValue = TextFieldValue(text = title, selection = TextRange(title.length))
    }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      Logger.d(TAG, "resultEvent: $commands")
      when (commands) {
        Commands.DELETED, Commands.SAVED -> {
          appWidgetUpdater.updateNotesWidget()
          appWidgetUpdater.updateAllWidgets()
          moveBack()
        }

        else -> {
        }
      }
    }
    viewModel.noteToShare.observeEvent(viewLifecycleOwner) { sendNote(it.second, it.first) }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
  }

  private fun sendNote(
    file: File,
    name: String,
  ) {
    if (isDetached) return
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendNote(file, requireContext(), name)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  private fun openImagePreview(
    position: Int,
    colorIndex: Int,
  ) {
    imagesSingleton.setCurrent(
      images = viewModel.state.value.images,
      color = colorIndex,
      palette = viewModel.state.value.palette,
    )
    startActivity(ImagePreviewActivity::class.java) {
      putExtra(IntentKeys.INTENT_POSITION, position)
    }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(
      fragmentManager = childFragmentManager,
      date = viewModel.date,
      title = getString(R.string.select_date),
    ) { viewModel.onNewDate(it) }
  }

  private fun timeDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.time,
      title = getString(R.string.select_time),
    ) { viewModel.onNewTime(it) }
  }

  override fun onImageSelected(uris: List<Uri>) {
    viewModel.addMultiple(uris)
  }

  override fun onBitmapReady(bitmap: Bitmap) {
    viewModel.addBitmap(bitmap)
  }

  companion object {
    private const val TAG = "CreateNoteFragment"
    private const val STATE_TEXT = "state_note_text"
    private const val STATE_TITLE = "state_note_title"
  }
}
