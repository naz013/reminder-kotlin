package com.elementary.tasks.notes.create

import android.content.ClipDescription
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.speech.SpeechEngine
import com.elementary.tasks.core.speech.SpeechEngineCallback
import com.elementary.tasks.core.speech.SpeechError
import com.elementary.tasks.core.speech.SpeechText
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.readText
import com.elementary.tasks.core.utils.ui.tintOverflowButton
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.ActivityCreateNoteBinding
import com.elementary.tasks.databinding.DialogSelectPaletteBinding
import com.elementary.tasks.notes.create.images.ImagesGridAdapter
import com.elementary.tasks.notes.create.images.KeepLayoutManager
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.domain.font.FontParams
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.context.colorOf
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.singleClick
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import com.google.android.material.slider.Slider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.Random

class CreateNoteActivity :
  BindingActivity<ActivityCreateNoteBinding>(),
  PhotoSelectionUtil.UriCallback {

  private val prefs by inject<Prefs>()
  private val themeProvider by inject<ThemeProvider>()
  private val dialogues by inject<Dialogues>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private var isBgDark = false

  private val viewModel by viewModel<CreateNoteViewModel> { parametersOf(getId()) }
  private val photoSelectionUtil = PhotoSelectionUtil(this, this)
  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)

  private val imagesGridAdapter = ImagesGridAdapter()

  private val speechEngine = SpeechEngine(this)
  private val speechEngineCallback = object : SpeechEngineCallback() {
    override fun onStarted() {
      super.onStarted()
      updateSpeechState(SpeechState.STARTED)
    }

    override fun onStopped() {
      super.onStopped()
      updateSpeechState(SpeechState.IDLE)
    }

    override fun onSpeechStarted() {
      super.onSpeechStarted()
      updateSpeechState(SpeechState.SPEAKING)
    }

    override fun onSpeechEnded() {
      super.onSpeechEnded()
      updateSpeechState(SpeechState.STOPPED)
    }

    override fun onSpeechError(error: SpeechError) {
      super.onSpeechError(error)
      updateSpeechState(SpeechState.IDLE)
    }

    override fun onSpeechResult(speechText: SpeechText) {
      super.onSpeechResult(speechText)
      binding.taskMessage.clearSections()
      binding.taskMessage.setText(speechText.text)
      speechText.newText?.also { newText ->
        binding.taskMessage.addBoldSection(
          startIndex = newText.startIndex,
          endIndex = newText.endIndex + 1
        )
      }
      binding.taskMessage.setSelection(binding.taskMessage.readText().length)
    }
  }

  private lateinit var tabController: TabController

  override fun inflateBinding() = ActivityCreateNoteBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    binding.appBar.applyTopInsets()
    binding.bottomBarInnerSpace.applyBottomInsetsMargin()

    tabController = TabController(
      tabs = listOf(
        TabController.TabView(
          TabController.Tab.COLOR,
          binding.colorSelectorView
        ),
        TabController.TabView(
          TabController.Tab.REMINDER,
          binding.reminderSelectorView
        ),
        TabController.TabView(
          TabController.Tab.FONT,
          binding.fontSelectorView
        )
      ),
      listener = object : TabController.Listener {
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

    lifecycle.addObserver(photoSelectionUtil)

    isBgDark = isDarkMode

    initActionBar()
    initMenu()

    binding.taskMessage.textSize = prefs.lastNoteFontSize.toFloat()

    binding.voiceInputFrame.visibleGone(speechEngine.supportsRecognition())
    binding.voiceInputFrame.singleClick { tryMicClick() }
    updateSpeechState(SpeechState.IDLE)

    binding.clickView.setOnClickListener {
      Logger.d("onCreate: on outside touch")
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
  }

  private fun initDefaults(): Pair<Int, Int> {
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

  private fun setText(text: String?) {
    speechEngine.setText(text ?: "")
    binding.taskMessage.setText(text)
    binding.taskMessage.setSelection(binding.taskMessage.text.toString().length)
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

  private fun newPair(color: Int = -1, opacity: Int = -1): Pair<Int, Int> {
    var newColor = color
    var newOpacity = opacity
    if (color == -1) {
      newColor = binding.colorSlider.selectedItem
    }
    if (opacity == -1) {
      newOpacity = binding.opacityBar.valueInt
    }
    Logger.d("newPair: $newColor, $newOpacity")
    return Pair(newColor, newOpacity)
  }

  private fun initMenu() {
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
      else -> themeProvider.getNoteLightColor(pair.first, pair.second, palette).isColorDark()
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

  private fun getId(): String = intentString(IntentKeys.INTENT_ID)

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
      Logger.d("observeStates: opacity $it")
      updateDarkness(it)
      updateBackground(it)
      updateTextColors()
      updateBottomBarIcons(it.first)
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
      Logger.d("observeStates: images -> $it")
      imagesGridAdapter.submitList(it)
    }
    viewModel.palette.nonNullObserve(this) {
      Logger.d("observeStates: palette -> $it")
      prefs.notePalette = it
      binding.colorSlider.setColors(themeProvider.noteColorsForSlider(it))
      val pair = newPair(binding.colorSlider.selectedItem, binding.opacityBar.valueInt)
      updateDarkness(pair, it)
      updateBackground(pair, it)
      updateTextColors()
      updateBottomBarIcons(
        colorIndex = binding.colorSlider.selectedItem,
        palette = it
      )
      updateMenu()
    }
    viewModel.note.nonNullObserve(this) { showNote(it) }
    viewModel.resultEvent.observeEvent(this) { commands ->
      Logger.d("initViewModel: $commands")
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
    viewModel.noteToShare.nonNullObserve(this) { sendNote(it.second, it.first) }
    viewModel.errorEvent.observeEvent(this) { toast(it) }
    viewModel.parsedText.nonNullObserve(this) { setText(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_share -> {
          viewModel.shareNote(getText(), binding.opacityBar.valueInt)
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
    updateBottomBarIcons(binding.colorSlider.selectedItem)
    updateMenu()
  }

  private fun updateMenu() {
    Logger.i(TAG, "Update toolbar colors, isBgDark: $isBgDark")
    binding.toolbar.menu.also { menu ->
      ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_fluent_checkmark, isBgDark)
      ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_fluent_share_android, isBgDark)
      menu.getItem(2).isVisible = viewModel.isNoteEdited && !viewModel.isFromFile
      ViewUtils.tintMenuIcon(this, menu, 2, R.drawable.ic_fluent_delete, isBgDark)
    }
    binding.toolbar.setNavigationIconTint(ViewUtils.tintIconColor(this, isBgDark))
    binding.toolbar.tintOverflowButton(isBgDark)
  }

  private fun updateBottomBarIcons(colorIndex: Int, palette: Int = palette()) {
    val isNoteColorDark = themeProvider.getNoteLightColor(
      code = colorIndex,
      opacity = 100,
      palette = palette
    ).isColorDark()
    binding.voiceInputMic.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_builder_mic_on, isNoteColorDark)
    )
    binding.voiceInputStop.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_recording_stop, isNoteColorDark)
    )
    binding.voiceSpeakAnimation.imageTintList = ColorStateList.valueOf(
      ViewUtils.tintIconColor(this, isNoteColorDark)
    )
    binding.colorButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_color_background, isNoteColorDark)
    )
    binding.imageButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_image, isNoteColorDark)
    )
    binding.reminderButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_alert, isNoteColorDark)
    )
    binding.fontButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_text, isNoteColorDark)
    )
    binding.paletteButton.setImageDrawable(
      ViewUtils.tintIcon(this, R.drawable.ic_fluent_settings, isNoteColorDark)
    )

    val selectorTint = ColorStateList.valueOf(
      ViewUtils.tintIconColor(this, isNoteColorDark)
    )
    TextViewCompat.setCompoundDrawableTintList(binding.fontStyleView, selectorTint)
    binding.colorSelectorView.backgroundTintList = selectorTint
    binding.reminderSelectorView.backgroundTintList = selectorTint
    binding.fontSelectorView.backgroundTintList = selectorTint
  }

  private fun showNote(uiNoteEdit: UiNoteEdit) {
    Logger.d("editNote: $uiNoteEdit")
    binding.colorSlider.setSelection(uiNoteEdit.colorPosition)
    binding.opacityBar.valueInt = uiNoteEdit.opacity
    binding.fontSizeBar.valueInt = uiNoteEdit.fontSize
    setText(uiNoteEdit.text)
    val pair = newPair(uiNoteEdit.colorPosition, uiNoteEdit.opacity)
    updateDarkness(pair)
    updateBackground(pair)
    updateTextColors()
    updateBottomBarIcons(uiNoteEdit.colorPosition, uiNoteEdit.colorPalette)
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
      putExtra(IntentKeys.INTENT_POSITION, position)
    }
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
    Logger.d("updateBackground: $pair, $palette")

    val lightColorSemi = themeProvider.getNoteLightColor(pair.first, pair.second, palette)
    binding.layoutContainer.setBackgroundColor(lightColorSemi)

    val lightColor = themeProvider.getNoteLightColor(pair.first, 100, palette)
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

    bind.colorSliderOne.setColors(themeProvider.noteColorsForSlider(0))
    bind.colorSliderTwo.setColors(themeProvider.noteColorsForSlider(1))
    bind.colorSliderThree.setColors(themeProvider.noteColorsForSlider(2))

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

  override fun onDestroy() {
    super.onDestroy()
    imagesGridAdapter.actionsListener = null
    lifecycle.removeObserver(viewModel)
    hideKeyboard(binding.taskMessage.windowToken)
    speechEngine.stopListening()
  }

  private fun handleSendText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
      Logger.d("handleSendText: $it")
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

  private fun updateSpeechState(state: SpeechState) {
    when (state) {
      SpeechState.IDLE -> {
        binding.voiceInputMic.visible()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.gone()
      }

      SpeechState.STARTED -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.visible()
      }

      SpeechState.SPEAKING -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.visible()
        binding.voiceInputStop.gone()
      }

      SpeechState.STOPPED -> {
        binding.voiceInputMic.gone()
        binding.voiceSpeakAnimation.gone()
        binding.voiceInputStop.visible()
      }
    }
  }

  private enum class SpeechState {
    IDLE,
    STARTED,
    SPEAKING,
    STOPPED
  }

  companion object {
    private const val TAG = "CreateNoteActivity"
  }
}

private var Slider.valueInt: Int
  set(v) {
    this.value = v.toFloat()
  }
  get() = value.toInt()
