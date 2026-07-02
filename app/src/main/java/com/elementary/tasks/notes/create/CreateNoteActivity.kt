package com.elementary.tasks.notes.create

import android.content.ClipDescription
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.enableEdgeToEdge
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
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class CreateNoteActivity :
  LightThemedActivity(),
  PhotoSelectionUtil.UriCallback {

  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  private val viewModel by viewModel<CreateNoteViewModel> { parametersOf(getId()) }
  private val photoSelectionUtil = PhotoSelectionUtil(this, this)
  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)
  private val speechEngine = SpeechEngine(this)

  private var textFieldValue by mutableStateOf(TextFieldValue())
  private var boldRange by mutableStateOf<IntRange?>(null)
  private var speechUiState by mutableStateOf(SpeechUiState.IDLE)
  private var activeDialog by mutableStateOf<NoteEditDialog?>(null)

  private val speechEngineCallback = object : SpeechEngineCallback() {
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
      textFieldValue = TextFieldValue(
        text = speechText.text,
        selection = TextRange(speechText.text.length)
      )
      boldRange = speechText.newText?.let { it.startIndex..it.endIndex }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(photoSelectionUtil)

    savedInstanceState?.getString(STATE_TEXT)?.let {
      textFieldValue = TextFieldValue(text = it, selection = TextRange(it.length))
    }

    initViewModel()
    loadNote()

    composeView {
      NoteEditContent()
    }
  }

  override fun onStart() {
    super.onStart()
    ViewUtils.registerDragAndDrop(
      this,
      window.decorView,
      true,
      ThemeProvider.getPrimaryColor(this),
      {
        if (it.itemCount > 0) {
          viewModel.parseDrop(it, getText())
        }
      },
      ClipDescription.MIMETYPE_TEXT_PLAIN,
      UriUtil.ANY_MIME
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_TEXT, textFieldValue.text)
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.removeObserver(viewModel)
    hideKeyboard()
    speechEngine.stopListening()
  }

  override fun handleBackPress(): Boolean {
    if (!viewModel.collapseExpandedTab()) {
      finish()
    }
    return true
  }

  @Composable
  private fun NoteEditContent() {
    val state by viewModel.state.collectAsState()
    val colors = remember(state) { viewModel.colorsFor(state) }

    SideEffect {
      window.statusBarColor = colors.statusBarColor
      window.navigationBarColor = colors.statusBarColor
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
      boldRange = boldRange,
      backgroundColor = Color(colors.background),
      barColor = Color(colors.statusBarColor),
      contentColor = Color(colors.content),
      sliderColors = colors.sliderColors,
      activeDialog = activeDialog,
      colorsForPalette = viewModel::sliderColorsForPalette,
      actions = NoteEditActions(
        onBackClick = { finish() },
        onSaveClick = { trySave() },
        onShareClick = { viewModel.shareNote(getText()) },
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
        onPaletteDialogClick = { activeDialog = NoteEditDialog.PALETTE },
        onColorSelected = { viewModel.onColorSelected(it) },
        onOpacityChanged = { viewModel.onOpacityChanged(it) },
        onReminderAttachedChanged = { viewModel.onReminderAttachedChanged(it) },
        onDateClick = { dateDialog() },
        onTimeClick = { timeDialog() },
        onFontSizeChanged = { viewModel.onFontSizeChanged(it) },
        onImageOpen = { openImagePreview(it, state.colorIndex) },
        onImageRemove = { viewModel.removeImage(it) },
        onFontStyleSelected = { viewModel.onFontStyleChanged(it) },
        onPaletteSelected = {
          viewModel.onPaletteChanged(it)
          activeDialog = null
        },
        onDeleteConfirmed = { viewModel.deleteNote() },
        onSameNoteKeep = { viewModel.saveNote(getText(), newId = true) },
        onSameNoteReplace = { viewModel.saveNote(getText()) },
        onDialogDismiss = { activeDialog = null }
      )
    )
  }

  private fun trySave() {
    if (viewModel.shouldConfirmBeforeSaving()) {
      activeDialog = NoteEditDialog.SAME_NOTE
    } else {
      viewModel.saveNote(getText())
    }
  }

  private fun getText(): String = textFieldValue.text.trim()

  private fun getId(): String = intentString(IntentKeys.INTENT_ID)

  private fun setText(text: String?) {
    val value = text ?: ""
    speechEngine.setText(value)
    textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
    boldRange = null
  }

  private fun tryMicClick() {
    permissionFlowDelegate.with {
      askPermission(Permissions.RECORD_AUDIO) { micClick() }
    }
  }

  private fun micClick() {
    if (speechEngine.isStarted()) {
      speechEngine.stopListening()
    } else {
      permissionFlowDelegate.with {
        askPermission(Permissions.RECORD_AUDIO) {
          speechEngine.startListening(speechEngineCallback)
        }
      }
    }
  }

  private fun loadNote() {
    when {
      intent?.action == Intent.ACTION_SEND -> {
        if ("text/plain" == intent.type) {
          handleSendText(intent)
        } else if (intent.type?.startsWith("image/") == true) {
          handleSendImage(intent)
        }
      }

      intent?.action == Intent.ACTION_SEND_MULTIPLE &&
        intent.type?.startsWith("image/") == true -> {
        handleSendMultipleImages(intent)
      }

      else -> {
        if (intent.getBooleanExtra(IntentKeys.INTENT_ITEM, false)) {
          viewModel.onNoteReceivedFromIntent()
        }
      }
    }
  }

  private fun handleSendText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
      Logger.d(TAG, "handleSendText: $it")
      setText(it)
    }
  }

  private fun handleSendImage(intent: Intent) {
    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
      viewModel.addMultiple(listOf(it))
    }
  }

  private fun handleSendMultipleImages(intent: Intent) {
    intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let { list ->
      viewModel.addMultiple(list.filterNotNull().filterIsInstance<Uri>())
    }
  }

  private fun initViewModel() {
    viewModel.textUpdate.observeEvent(this) { update ->
      Logger.d(TAG, "textUpdate: $update")
      speechEngine.setText(update.text)
      textFieldValue = TextFieldValue(text = update.text, selection = TextRange(update.text.length))
      boldRange = update.boldRange
    }
    viewModel.resultEvent.observeEvent(this) { commands ->
      Logger.d(TAG, "resultEvent: $commands")
      when (commands) {
        Commands.DELETED, Commands.SAVED -> {
          appWidgetUpdater.updateNotesWidget()
          appWidgetUpdater.updateAllWidgets()
          finish()
        }

        else -> {
        }
      }
    }
    viewModel.noteToShare.observeEvent(this) { sendNote(it.second, it.first) }
    viewModel.errorEvent.observeEvent(this) { toast(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun sendNote(file: File, name: String) {
    if (isFinishing) return
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendNote(file, this, name)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  private fun openImagePreview(position: Int, colorIndex: Int) {
    imagesSingleton.setCurrent(
      images = viewModel.state.value.images,
      color = colorIndex,
      palette = viewModel.state.value.palette
    )
    startActivity(ImagePreviewActivity::class.java) {
      putExtra(IntentKeys.INTENT_POSITION, position)
    }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(
      fragmentManager = supportFragmentManager,
      date = viewModel.date,
      title = getString(R.string.select_date)
    ) { viewModel.onNewDate(it) }
  }

  private fun timeDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = supportFragmentManager,
      time = viewModel.time,
      title = getString(R.string.select_time)
    ) { viewModel.onNewTime(it) }
  }

  override fun onImageSelected(uris: List<Uri>) {
    viewModel.addMultiple(uris)
  }

  override fun onBitmapReady(bitmap: Bitmap) {
    viewModel.addBitmap(bitmap)
  }

  companion object {
    private const val TAG = "CreateNoteActivity"
    private const val STATE_TEXT = "state_note_text"
  }
}
