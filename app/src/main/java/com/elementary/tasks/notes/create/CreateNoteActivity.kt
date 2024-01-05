package com.elementary.tasks.notes.create

import android.content.ClipDescription
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.colorOf
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.core.utils.adjustAlpha
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.font.FontParams
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.ActivityCreateNoteBinding
import com.elementary.tasks.databinding.DialogSelectPaletteBinding
import com.elementary.tasks.notes.create.images.ImagesGridAdapter
import com.elementary.tasks.notes.create.images.KeepLayoutManager
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.pin.PinLoginActivity
import com.google.android.material.slider.Slider
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File
import java.util.Random

class CreateNoteActivity :
  BindingActivity<ActivityCreateNoteBinding>(),
  PhotoSelectionUtil.UriCallback {

  private val themeUtil by inject<ThemeProvider>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private var isBgDark = false

  private val viewModel by viewModel<CreateNoteViewModel> { parametersOf(getId()) }
  private val photoSelectionUtil = PhotoSelectionUtil(this, this)
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
        appendText(res[0].toString())
      }
      Timber.d("onResults: $res")
      releaseSpeech()
    }

    override fun onPartialResults(bundle: Bundle?) {
      val res = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      if (res != null && res.size > 0) {
        appendText(res[0].toString())
      }
      Timber.d("onPartialResults: $res")
    }

    override fun onEvent(i: Int, bundle: Bundle?) {
    }
  }

  private val tabController = TabController(
    object : TabController.Listener {
      override fun onTabSelected(tab: TabController.Tab) {
        binding.colorLayout.gone()
        binding.fontLayout.gone()
        binding.reminderLayout.gone()
        when (tab) {
          TabController.Tab.FONT -> {
            binding.fontLayout.visible()
          }

          TabController.Tab.COLOR -> {
            binding.colorLayout.visible()
          }

          TabController.Tab.REMINDER -> {
            binding.reminderLayout.visible()
          }

          TabController.Tab.NONE -> {
          }
        }
      }

      override fun onShow() {
        binding.expandedLayout.visible()
      }

      override fun onHide() {
        binding.expandedLayout.gone()
      }
    }
  )

  override fun inflateBinding() = ActivityCreateNoteBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycle.addObserver(photoSelectionUtil)

    isBgDark = isDarkMode

    initActionBar()
    initMenu()
    hideRecording()

    binding.taskMessage.textSize = prefs.lastNoteFontSize.toFloat()

    binding.micButton.setOnClickListener { tryMicClick() }

    binding.clickView.setOnClickListener {
      Timber.d("onCreate: on outside touch")
      hideKeyboard(binding.taskMessage.windowToken)
    }
    initImagesList()

    if (getId().isEmpty()) {
      val pair = if (savedInstanceState == null) {
        initDefaults()
      } else {
        newPair()
      }
      initFromState(pair)
    }
    initViewModel()
    loadNote()
  }

  private fun initFromState(pair: Pair<Int, Int>) {
    binding.colorSlider.setSelection(pair.first)
    binding.opacityBar.valueInt = pair.second
    viewModel.colorOpacity.postValue(pair)
  }

  override fun onStart() {
    super.onStart()
    ViewUtils.registerDragAndDrop(
      this,
      binding.clickView,
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

    val fontSize = if (prefs.isNoteFontSizeRememberingEnabled) {
      prefs.lastNoteFontSize
    } else {
      FontParams.DEFAULT_FONT_SIZE
    }
    viewModel.onFontSizeChanged(fontSize)
    binding.fontSizeBar.value = fontSize.toFloat()

    if (prefs.isNoteFontStyleRememberingEnabled) {
      viewModel.onFontStyleChanged(prefs.lastNoteFontStyle)
    } else {
      viewModel.onFontStyleChanged(FontParams.DEFAULT_FONT_STYLE)
    }

    return Pair(color, opacity)
  }

  private fun palette(): Int = viewModel.palette.value ?: 0

  private fun newColor(): Int = if (prefs.isNoteColorRememberingEnabled) {
    prefs.lastNoteColor
  } else {
    Random().nextInt(ThemeProvider.NOTE_COLORS)
  }

  private fun appendText(text: String?) {
    if (text != null) {
      val oldText = binding.taskMessage.trimmedText()
      val newText = if (oldText.isEmpty()) {
        StringUtils.capitalize(text)
      } else {
        "$oldText $text"
      }
      binding.taskMessage.setText(newText)
      binding.taskMessage.setSelection(binding.taskMessage.text.toString().length)
    }
  }

  private fun setText(text: String?) {
    binding.taskMessage.setText(text)
    binding.taskMessage.setSelection(binding.taskMessage.text.toString().length)
  }

  private fun showRecording() {
    binding.voiceProgress.visible()
  }

  private fun hideRecording() {
    binding.voiceProgress.gone()
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
      showRecording()
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
      newColor = binding.colorSlider.selectedItem
    }
    if (opacity == -1) {
      newOpacity = binding.opacityBar.valueInt
    }
    Timber.d("newPair: $newColor, $newOpacity")
    return Pair(newColor, newOpacity)
  }

  private fun initMenu() {
    binding.micButton.visibleGone(Module.hasMicrophone(this))

    binding.colorButton.setOnClickListener {
      tabController.onTabClick(TabController.Tab.COLOR)
    }
    binding.imageButton.setOnClickListener { photoSelectionUtil.selectImage() }

    binding.reminderButton.setOnClickListener {
      tabController.onTabClick(TabController.Tab.REMINDER)
    }
    binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
      viewModel.isReminderAttached.postValue(isChecked)
    }
    binding.remindDate.setOnClickListener { dateDialog() }
    binding.remindTime.setOnClickListener { timeDialog() }

    viewModel.isReminderAttached.postValue(false)

    binding.paletteButton.setOnClickListener { showPaletteDialog() }
    binding.fontButton.setOnClickListener {
      tabController.onTabClick(TabController.Tab.FONT)
    }
    binding.fontStyleView.setOnClickListener { showStyleDialog() }

    binding.colorSlider.setSelectorColorResource(
      if (isDarkMode) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.colorSlider.setListener { position, _ ->
      viewModel.colorOpacity.postValue(newPair(color = position))
      if (prefs.isNoteColorRememberingEnabled) {
        prefs.lastNoteColor = position
      }
    }

    binding.opacityBar.addOnChangeListener { slider, value, _ ->
      prefs.noteColorOpacity = slider.valueInt
      viewModel.colorOpacity.postValue(newPair(opacity = slider.valueInt))
    }
    binding.opacityBar.setLabelFormatter { "${it.toInt()}%" }

    binding.fontSizeBar.addOnChangeListener { _, value, _ ->
      viewModel.onFontSizeChanged(value.toInt())
      prefs.lastNoteFontSize = value.toInt()
    }
    binding.fontSizeBar.setLabelFormatter { "${it.toInt()}" }
  }

  private fun updateDarkness(pair: Pair<Int, Int>, palette: Int = palette()) {
    isBgDark = when {
      pair.second.isAlmostTransparent() -> isDarkMode
      else -> themeUtil.getNoteLightColor(pair.first, pair.second, palette).isColorDark()
    }
  }

  @ColorInt
  private fun getTextColor(): Int {
    return if (isBgDark) {
      colorOf(R.color.pureWhite)
    } else {
      colorOf(R.color.pureBlack)
    }
  }

  private fun updateTextColors() {
    val textColor = getTextColor()
    binding.taskMessage.setTextColor(textColor)
    binding.taskMessage.setHintTextColor(textColor)
    binding.taskMessage.backgroundTintList = ContextCompat.getColorStateList(
      this,
      if (isBgDark) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }
    )
    binding.opacityLabel.setTextColor(textColor)

    val stateList = ColorStateList.valueOf(textColor)
    val inactiveStateList = ColorStateList.valueOf(textColor.adjustAlpha(24))

    binding.opacityBar.thumbTintList = stateList
    binding.opacityBar.trackActiveTintList = stateList
    binding.opacityBar.trackInactiveTintList = inactiveStateList

    binding.fontSizeLabel.setTextColor(textColor)
    binding.fontStyleLabel.setTextColor(textColor)
    binding.fontStyleView.setTextColor(textColor)

    binding.fontSizeBar.thumbTintList = stateList
    binding.fontSizeBar.trackActiveTintList = stateList
    binding.fontSizeBar.trackInactiveTintList = inactiveStateList

    binding.reminderDotView.backgroundTintList = stateList
    binding.reminderSwitch.setTextColor(textColor)
    binding.reminderSwitch.thumbIconTintList = stateList
    binding.reminderSwitch.trackDecorationTintList = stateList

    updateDateTimeViewState(viewModel.isReminderAttached.value ?: false)
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

      intent?.action == Intent.ACTION_SEND_MULTIPLE &&
        intent.type?.startsWith("image/") == true -> {
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

  private fun updateDateTimeViewState(isEnabled: Boolean) {
    if (binding.reminderSwitch.isChecked != isEnabled) {
      binding.reminderSwitch.isChecked = isEnabled
    }
    binding.reminderDotView.visibleGone(isEnabled)
    binding.remindDate.isEnabled = isEnabled
    binding.remindTime.isEnabled = isEnabled
    val textColor = if (isEnabled) {
      getTextColor()
    } else {
      getTextColor().adjustAlpha(50)
    }
    binding.remindDate.setTextColor(textColor)
    binding.remindTime.setTextColor(textColor)
  }

  private fun initViewModel() {
    viewModel.colorOpacity.nonNullObserve(this) {
      Timber.d("observeStates: opacity $it")
      updateDarkness(it)
      updateBackground(it)
      updateTextColors()
      updateIcons()
      updateMenu()
    }
    viewModel.timeFormatted.nonNullObserve(this) {
      binding.remindTime.text = it
    }
    viewModel.dateFormatted.nonNullObserve(this) {
      binding.remindDate.text = it
    }
    viewModel.isReminderAttached.nonNullObserve(this) { updateDateTimeViewState(it) }
    viewModel.fontStyle.nonNullObserve(this) { updateFontStyle(it) }
    viewModel.fontSize.nonNullObserve(this) { updateFontSize(it) }
    viewModel.images.nonNullObserve(this) {
      Timber.d("observeStates: images -> $it")
      imagesGridAdapter.submitList(it)
    }
    viewModel.palette.nonNullObserve(this) {
      Timber.d("observeStates: palette -> $it")
      prefs.notePalette = it
      binding.colorSlider.setColors(themeUtil.noteColorsForSlider(it))
      val pair = newPair(binding.colorSlider.selectedItem, binding.opacityBar.valueInt)
      updateDarkness(pair, it)
      updateBackground(pair, it)
      updateTextColors()
      updateIcons()
      updateMenu()
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
    viewModel.parsedText.nonNullObserve(this) { binding.taskMessage.setText(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_share -> {
          permissionFlow.askPermissions(
            listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
          ) { viewModel.shareNote(getText(), binding.opacityBar.valueInt) }
          true
        }

        R.id.action_delete -> {
          deleteDialog()
          true
        }

        R.id.action_add -> {
          askCopySaving()
          true
        }

        else -> false
      }
    }
    updateIcons()
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also { menu ->
      ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_fluent_checkmark, isBgDark)
      ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_fluent_share_android, isBgDark)
      menu.getItem(2).isVisible = viewModel.isNoteEdited && !viewModel.isFromFile
      ViewUtils.tintMenuIcon(this, menu, 2, R.drawable.ic_fluent_delete, isBgDark)
    }
  }

  private fun updateIcons() {
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
    binding.toolbar.tintOverflowButton(isBgDark)
    binding.micButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_builder_mic_on, isBgDark)
    )
    binding.colorButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_color_background, isBgDark)
    )
    binding.imageButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_image, isBgDark)
    )
    binding.reminderButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_clock_alarm, isBgDark)
    )
    binding.fontButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_text, isBgDark)
    )
    binding.paletteButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_settings, isBgDark)
    )
  }

  private fun loadNoteFromFile() {
    intent.data?.let { viewModel.loadFromFile(it) }
  }

  private fun showNote(uiNoteEdit: UiNoteEdit) {
    Timber.d("editNote: $uiNoteEdit")
    binding.colorSlider.setSelection(uiNoteEdit.colorPosition)
    binding.opacityBar.valueInt = uiNoteEdit.opacity
    binding.fontSizeBar.valueInt = uiNoteEdit.fontSize
    setText(uiNoteEdit.text)
    val pair = newPair(uiNoteEdit.colorPosition, uiNoteEdit.opacity)
    updateDarkness(pair)
    updateBackground(pair)
    updateTextColors()
    updateIcons()
    updateMenu()
  }

  private fun initImagesList() {
    imagesGridAdapter.isEditable = true
    imagesGridAdapter.actionsListener = object : ActionsListener<UiNoteImage> {
      override fun onAction(view: View, position: Int, t: UiNoteImage?, actions: ListActions) {
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
    imagesSingleton.setCurrent(
      images = imagesGridAdapter.currentList,
      color = binding.colorSlider.selectedItem,
      palette = palette()
    )
    startActivity(ImagePreviewActivity::class.java) {
      putExtra(Constants.INTENT_POSITION, position)
    }
  }

  private fun hideProgress() {
    binding.voiceProgress.gone()
  }

  private fun showProgress() {
    binding.voiceProgress.visible()
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
          viewModel.saveNote(getText(), binding.opacityBar.valueInt, true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          viewModel.saveNote(getText(), binding.opacityBar.valueInt)
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      viewModel.saveNote(getText(), binding.opacityBar.valueInt)
    }
  }

  private fun getText(): String {
    return binding.taskMessage.trimmedText()
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

  private fun updateFontStyle(fontStyle: Int) {
    binding.taskMessage.typeface = AssetsUtil.getTypeface(this, fontStyle)
    binding.fontStyleView.typeface = AssetsUtil.getTypeface(this, fontStyle)

    val fontName = runCatching { AssetsUtil.getFontNames()[fontStyle] }.getOrNull()
      ?: getString(R.string.font_style)
    binding.fontStyleView.text = fontName
  }

  private fun updateFontSize(fontSize: Int) {
    binding.taskMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
  }

  private fun updateBackground(pair: Pair<Int, Int>, palette: Int = palette()) {
    Timber.d("updateBackground: $pair, $palette")

    val lightColorSemi = themeUtil.getNoteLightColor(pair.first, pair.second, palette)
    binding.layoutContainer.setBackgroundColor(lightColorSemi)

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
      viewModel.onFontStyleChanged(which)
      prefs.lastNoteFontStyle = which
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
    if (tabController.isTabVisible()) {
      tabController.hide()
    } else {
      finish()
    }
    return true
  }
}

private var Slider.valueInt: Int
  set(v) {
    this.value = v.toFloat()
  }
  get() = value.toInt()
