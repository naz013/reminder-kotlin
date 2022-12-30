package com.elementary.tasks.notes.create

import android.content.ClipDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.core.utils.colorOf
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.databinding.ActivityCreateNoteBinding
import com.elementary.tasks.databinding.DialogSelectPaletteBinding
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.pin.PinLoginActivity
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File
import java.util.Random

class CreateNoteActivity : BindingActivity<ActivityCreateNoteBinding>(),
  PhotoSelectionUtil.UriCallback {

  private val themeUtil by inject<ThemeProvider>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private var isBgDark = false

  private val permissionFlow = PermissionFlow(this, dialogues)
  private val viewModel by viewModel<CreateNoteViewModel> { parametersOf(getId()) }
  private val photoSelectionUtil = PhotoSelectionUtil(this, dialogues, this)
  private val loginLauncher = LoginLauncher(this) {
    if (!it) {
      finish()
    } else {
      viewModel.isLogged = true
    }
  }

  private val imagesGridAdapter = ImagesGridAdapter()
  private var speech: SpeechRecognizer? = null

  private val mRecognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(bundle: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
      Timber.d("onBeginningOfSpeech: ")
      showRecording()
    }

    override fun onRmsChanged(v: Float) {
    }

    override fun onBufferReceived(bytes: ByteArray?) {
    }

    override fun onEndOfSpeech() {
      hideRecording()
      Timber.d("onEndOfSpeech: ")
    }

    override fun onError(i: Int) {
      Timber.d("onError: $i")
      releaseSpeech()
      hideRecording()
    }

    override fun onResults(bundle: Bundle?) {
      val res = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (res != null && res.size > 0) {
        setText(StringUtils.capitalize(res[0].toString().lowercase()))
      }
      Timber.d("onResults: $res")
      releaseSpeech()
    }

    override fun onPartialResults(bundle: Bundle?) {
      val res = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (res != null && res.size > 0) {
        setText(res[0].toString().lowercase())
      }
      Timber.d("onPartialResults: $res")
    }

    override fun onEvent(i: Int, bundle: Bundle?) {
    }
  }

  override fun inflateBinding() = ActivityCreateNoteBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    isBgDark = isDarkMode

    initActionBar()
    initMenu()
    hideRecording()
    binding.remindDate.setOnClickListener { dateDialog() }
    binding.remindTime.setOnClickListener { timeDialog() }
    binding.micButton.setOnClickListener { tryMicClick() }
    binding.discardReminder.setOnClickListener { viewModel.isReminderAttached.postValue(false) }
    binding.clickView.setOnClickListener {
      Timber.d("onCreate: on outside touch")
      hideKeyboard(binding.taskMessage.windowToken)
    }
    initImagesList()

    val pair = if (savedInstanceState == null) {
      initDefaults()
    } else {
      newPair()
    }
    initFromState(pair)
    initViewModel()
    loadNote()
  }

  private fun initFromState(pair: Pair<Int, Int>) {
    binding.colorSlider.setSelection(pair.first)
    binding.opacityBar.progress = pair.second
    viewModel.colorOpacity.postValue(pair)
  }

  override fun onStart() {
    super.onStart()
    ViewUtils.registerDragAndDrop(
      this,
      binding.clickView,
      true,
      ThemeProvider.getSecondaryColor(this),
      {
        if (it.itemCount > 0) {
          viewModel.parseDrop(it, getText())
        }
      },
      ClipDescription.MIMETYPE_TEXT_PLAIN,
      UriUtil.ANY_MIME
    )

    if (prefs.hasPinCode && !viewModel.isLogged) {
      loginLauncher.askLogin()
    }
  }

  private fun initDefaults(): Pair<Int, Int> {
    viewModel.isLogged = intentBoolean(PinLoginActivity.ARG_LOGGED)

    val color = if (prefs.isNoteColorRememberingEnabled) {
      prefs.lastNoteColor
    } else {
      newColor()
    }
    val opacity = prefs.noteColorOpacity
    viewModel.colorOpacity.postValue(newPair(color, opacity))
    viewModel.palette.postValue(prefs.notePalette)

    viewModel.fontStyle.postValue(0)

    return Pair(color, opacity)
  }

  private fun palette(): Int = viewModel.palette.value ?: 0

  private fun newColor(): Int = if (prefs.isNoteColorRememberingEnabled) {
    prefs.lastNoteColor
  } else {
    Random().nextInt(ThemeProvider.NOTE_COLORS)
  }

  private fun setText(text: String?) {
    binding.taskMessage.setText(text)
    binding.taskMessage.setSelection(binding.taskMessage.text.toString().length)
  }

  private fun showRecording() {
    binding.recordingView.visible()
  }

  private fun hideRecording() {
    binding.recordingView.gone()
  }

  private fun initRecognizer() {
    try {
      val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      recognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
      )
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
      speech = SpeechRecognizer.createSpeechRecognizer(this)
      speech?.setRecognitionListener(mRecognitionListener)
      speech?.startListening(recognizerIntent)
    } catch (e: Throwable) {
      speech = null
      toast(R.string.failed_to_start_voice_recognition)
    }
  }

  private fun releaseSpeech() {
    runCatching {
      speech?.stopListening()
      speech?.cancel()
      speech?.destroy()
      speech = null
    }
  }

  private fun tryMicClick() {
    permissionFlow.askPermission(Permissions.RECORD_AUDIO) { micClick() }
  }

  private fun micClick() {
    if (speech != null) {
      hideRecording()
      releaseSpeech()
      return
    }
    initRecognizer()
  }

  private fun newPair(color: Int = -1, opacity: Int = -1): Pair<Int, Int> {
    var newColor = color
    var newOpacity = opacity
    if (color == -1) {
      newColor = viewModel.colorOpacity.value?.first ?: newColor()
    }
    if (opacity == -1) {
      newOpacity = viewModel.colorOpacity.value?.second ?: prefs.noteColorOpacity
    }
    Timber.d("newPair: $newColor, $newOpacity")
    return Pair(newColor, newOpacity)
  }

  private fun initMenu() {
    binding.micButton.visibleGone(Module.hasMicrophone(this))

    binding.colorButton.setOnClickListener { toggleColorView() }
    binding.imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
    binding.reminderButton.setOnClickListener { switchReminder() }
    binding.fontButton.setOnClickListener { showStyleDialog() }
    binding.paletteButton.setOnClickListener { showPaletteDialog() }

    binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
    binding.colorSlider.setListener { position, _ ->
      viewModel.colorOpacity.postValue(newPair(color = position))
      if (prefs.isNoteColorRememberingEnabled) {
        prefs.lastNoteColor = position
      }
    }
    binding.opacityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        prefs.noteColorOpacity = progress
        viewModel.colorOpacity.postValue(newPair(opacity = progress))
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
      }

      override fun onStopTrackingTouch(seekBar: SeekBar?) {
      }
    })
  }

  private fun updateDarkness(pair: Pair<Int, Int>, palette: Int = palette()) {
    isBgDark = when {
      pair.second.isAlmostTransparent() -> isDarkMode
      else -> themeUtil.getNoteLightColor(pair.first, pair.second, palette).isColorDark()
    }
  }

  private fun updateTextColors() {
    val textColor = if (isBgDark) {
      colorOf(R.color.pureWhite)
    } else {
      colorOf(R.color.pureBlack)
    }
    binding.taskMessage.setTextColor(textColor)
    binding.taskMessage.setHintTextColor(textColor)
    binding.taskMessage.backgroundTintList = ContextCompat.getColorStateList(
      this, if (isBgDark) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.remindDate.setTextColor(textColor)
    binding.remindTime.setTextColor(textColor)
  }

  private fun toggleColorView() {
    binding.colorLayout.visibleGone(isColorPickerHidden())
  }

  private fun isColorPickerHidden(): Boolean {
    return !binding.colorLayout.isVisible()
  }

  private fun isReminderAdded(): Boolean = viewModel.isReminderAttached.value ?: false

  private fun switchReminder() {
    viewModel.isReminderAttached.postValue(!isReminderAdded())
  }

  private fun getId(): String = intentString(Constants.INTENT_ID)

  private fun loadNote() {

    when {
      intent?.action == Intent.ACTION_SEND -> {
        if ("text/plain" == intent.type) {
          handleSendText(intent)
        } else if (intent.type?.startsWith("image/") == true) {
          handleSendImage(intent)
        }
      }

      intent?.action == Intent.ACTION_SEND_MULTIPLE
        && intent.type?.startsWith("image/") == true -> {
        handleSendMultipleImages(intent)
      }

      else -> {
        if (intent.data != null) {
          permissionFlow.askPermission(Permissions.READ_EXTERNAL) { loadNoteFromFile() }
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
          runCatching {
            viewModel.onNoteReceivedFromIntent(
              intentParcelable(Constants.INTENT_ITEM, NoteWithImages::class.java)
            )
          }
        }
      }
    }
  }

  private fun saveEditedImage() {
    val image = imagesSingleton.getEditable() ?: return
    viewModel.setImage(image, viewModel.editPosition)
  }

  private fun initViewModel() {
    viewModel.colorOpacity.nonNullObserve(this) {
      Timber.d("observeStates: $it")
      updateDarkness(it)
      updateBackground(it)
      updateTextColors()
      updateIcons()
    }
    viewModel.timeFormatted.nonNullObserve(this) {
      binding.remindTime.text = it
    }
    viewModel.dateFormatted.nonNullObserve(this) {
      binding.remindDate.text = it
    }
    viewModel.isReminderAttached.nonNullObserve(this) {
      binding.remindContainer.visibleGone(it)
    }
    viewModel.fontStyle.nonNullObserve(this) { updateFontStyle(it) }
    viewModel.images.nonNullObserve(this) {
      Timber.d("observeStates: images -> $it")
      imagesGridAdapter.submitList(it)
    }
    viewModel.palette.nonNullObserve(this) {
      prefs.notePalette = it
      binding.colorSlider.setColors(themeUtil.noteColorsForSlider(it))
      val pair = newPair()
      updateDarkness(pair)
      updateBackground(pair, it)
      updateTextColors()
      updateIcons()
    }
    viewModel.note.nonNullObserve(this) { showNote(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      Timber.d("initViewModel: $commands")
      when (commands) {
        Commands.DELETED, Commands.SAVED -> {
          updatesHelper.updateNotesWidget()
          updatesHelper.updateWidgets()
          finish()
        }

        else -> {
        }
      }
    }
    viewModel.noteToShare.nonNullObserve(this) { sendNote(it.second, it.first) }
    viewModel.isInProgress.nonNullObserve(this) {
      if (it) {
        showProgress()
      } else {
        hideProgress()
      }
    }
    viewModel.error.nonNullObserve(this) { toast(it) }
    viewModel.parsedText.nonNullObserve(this) {  binding.taskMessage.setText(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    binding.taskMessage.textSize = (prefs.noteTextSize + 12).toFloat()
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)

    ViewUtils.listenScrollableView(binding.touchView) {
      binding.appBar.isSelected = it > 0
    }

    binding.toolbar.inflateMenu(R.menu.activity_create_note)
  }

  private fun updateIcons() {
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
    binding.toolbar.tintOverflowButton(isBgDark)
    invalidateOptionsMenu()
    binding.discardReminder.setImageDrawable(
      ViewUtils.tintIcon(
        this,
        R.drawable.ic_twotone_cancel_24px,
        isBgDark
      )
    )
  }

  private fun loadNoteFromFile() {
    intent.data?.let { viewModel.loadFromFile(it) }
  }

  private fun showNote(noteWithImages: NoteWithImages) {
    Timber.d("showNote: $noteWithImages")
    if (!viewModel.isNoteEdited) {
      val note = noteWithImages.note ?: return
      binding.colorSlider.setSelection(note.color)
      binding.opacityBar.progress = note.opacity
      setText(note.summary)
    }
  }

  private fun initImagesList() {
    imagesGridAdapter.isEditable = true
    imagesGridAdapter.actionsListener = object : ActionsListener<ImageFile> {
      override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
        when (actions) {
          ListActions.OPEN -> openImagePreview(position)
          ListActions.REMOVE -> viewModel.removeImage(position)
          else -> {
          }
        }
      }
    }
    binding.imagesList.layoutManager = KeepLayoutManager(this, 6, imagesGridAdapter)
    binding.imagesList.adapter = imagesGridAdapter
  }

  private fun openImagePreview(position: Int) {
    imagesSingleton.setCurrent(imagesGridAdapter.data)
    startActivity(
      Intent(this, ImagePreviewActivity::class.java)
        .putExtra(Constants.INTENT_POSITION, position)
    )
  }

  private fun hideProgress() {
    binding.recordingView.gone()
  }

  private fun showProgress() {
    binding.recordingView.visible()
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

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_note_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          viewModel.saveNote(getText(), binding.opacityBar.progress, true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          viewModel.saveNote(getText(), binding.opacityBar.progress)
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      viewModel.saveNote(getText(), binding.opacityBar.progress)
    }
  }

  private fun getText(): String {
    return binding.taskMessage.trimmedText()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        finish()
        return true
      }

      R.id.action_share -> {
        permissionFlow.askPermissions(
          listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        ) { viewModel.shareNote(getText(), binding.opacityBar.progress) }
        return true
      }

      MENU_ITEM_DELETE -> {
        deleteDialog()
        return true
      }

      R.id.action_add -> {
        askCopySaving()
        return true
      }

      else -> return super.onOptionsItemSelected(item)
    }
  }

  private fun deleteDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setMessage(getString(R.string.delete_this_note))
    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
      dialog.dismiss()
      viewModel.deleteNote()
    }
    builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_create_note, menu)
    ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_done_24px, isBgDark)
    if (viewModel.isNoteEdited && !viewModel.isFromFile) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  private fun updateFontStyle(fontStyle: Int) {
    binding.taskMessage.typeface = AssetsUtil.getTypeface(this, fontStyle)
  }

  private fun updateBackground(pair: Pair<Int, Int>, palette: Int = palette()) {
    Timber.d("updateBackground: $pair, $palette")

    val lightColorSemi = themeUtil.getNoteLightColor(pair.first, pair.second, palette)
    binding.layoutContainer.setBackgroundColor(lightColorSemi)
    binding.toolbar.setBackgroundColor(lightColorSemi)
    binding.appBar.setBackgroundColor(lightColorSemi)

    val lightColor = themeUtil.getNoteLightColor(pair.first, 100, palette)
    window.statusBarColor = lightColor
    window.navigationBarColor = lightColor
    binding.bottomBar.setCardBackgroundColor(lightColor)
    binding.bottomBar.invalidate()
  }

  private fun showStyleDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(getString(R.string.font_style))

    val names = AssetsUtil.getFontNames()

    val inflater = LayoutInflater.from(this)
    val adapter = object : ArrayAdapter<String>(
      this,
      android.R.layout.simple_list_item_single_choice,
      names
    ) {
      override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView = convertView
        if (cView == null) {
          cView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null)
        }
        val textView = cView?.findViewById<TextView>(android.R.id.text1)
        if (textView != null) {
          textView.typeface = getTypeface(position)
          textView.text = names[position]
        }
        return cView!!
      }

      private fun getTypeface(position: Int): Typeface? {
        return AssetsUtil.getTypeface(this@CreateNoteActivity, position)
      }
    }
    builder.setSingleChoiceItems(adapter, viewModel.fontStyle.value ?: 0) { _, which ->
      viewModel.fontStyle.postValue(which)
    }
    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  private fun showPaletteDialog() {
    val builder = dialogues.getMaterialDialog(this)
    val bind = DialogSelectPaletteBinding.inflate(layoutInflater)

    when (palette()) {
      1 -> bind.paletteTwo.isChecked = true
      2 -> bind.paletteThree.isChecked = true
      else -> bind.paletteOne.isChecked = true
    }

    bind.colorSliderOne.setColors(themeUtil.noteColorsForSlider(0))
    bind.colorSliderTwo.setColors(themeUtil.noteColorsForSlider(1))
    bind.colorSliderThree.setColors(themeUtil.noteColorsForSlider(2))

    bind.colorSliderOne.isEnabled = false
    bind.colorSliderTwo.isEnabled = false
    bind.colorSliderThree.isEnabled = false

    val buttons = arrayOf(bind.paletteOne, bind.paletteTwo, bind.paletteThree)

    bind.paletteOne.setOnCheckedChangeListener { buttonView, isChecked ->
      updateCheck(
        buttonView,
        isChecked,
        *buttons
      )
    }
    bind.paletteTwo.setOnCheckedChangeListener { buttonView, isChecked ->
      updateCheck(
        buttonView,
        isChecked,
        *buttons
      )
    }
    bind.paletteThree.setOnCheckedChangeListener { buttonView, isChecked ->
      updateCheck(
        buttonView,
        isChecked,
        *buttons
      )
    }

    builder.setView(bind.root)
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      val selected = when {
        bind.paletteTwo.isChecked -> 1
        bind.paletteThree.isChecked -> 2
        else -> 0
      }
      dialog.dismiss()
      viewModel.palette.postValue(selected)
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun updateCheck(view: View, isChecked: Boolean, vararg radioButtons: RadioButton) {
    if (!isChecked) return
    radioButtons.forEach {
      if (view.id != it.id) {
        it.isChecked = false
      }
    }
  }

  private fun dateDialog() {
    dateTimePickerProvider.showDatePicker(this, viewModel.date) { viewModel.onNewDate(it) }
  }

  private fun timeDialog() {
    dateTimePickerProvider.showTimePicker(this, viewModel.time) { viewModel.onNewTime(it) }
  }

  override fun onDestroy() {
    super.onDestroy()
    imagesGridAdapter.actionsListener = null
    lifecycle.removeObserver(viewModel)
    hideKeyboard(binding.taskMessage.windowToken)
    releaseSpeech()
  }

  private fun handleSendText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
      Timber.d("handleSendText: $it")
      binding.taskMessage.setText(it)
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

  override fun onImageSelected(uris: List<Uri>) {
    viewModel.addMultiple(uris)
  }

  override fun onBitmapReady(bitmap: Bitmap) {
    viewModel.addBitmap(bitmap)
  }

  override fun handleBackPress(): Boolean {
    if (!isColorPickerHidden()) {
      toggleColorView()
    } else {
      finish()
    }
    return true
  }

  companion object {
    const val MENU_ITEM_DELETE = 12
  }
}
