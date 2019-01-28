package com.elementary.tasks.notes.create

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.core.views.GridMarginDecoration
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import com.elementary.tasks.notes.editor.ImageEditActivity
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import kotlinx.android.synthetic.main.activity_create_note.*
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class CreateNoteActivity : ThemedActivity(), PhotoSelectionUtil.UriCallback {

    private var mEditPosition = -1
    private var isBgDark = false

    private lateinit var viewModel: NoteViewModel
    private lateinit var stateViewModel: CreateNoteViewModel
    private lateinit var photoSelectionUtil: PhotoSelectionUtil

    private val imagesGridAdapter = ImagesGridAdapter()

    private var mItem: NoteWithImages? = null
    private var mReminder: Reminder? = null
    private var speech: SpeechRecognizer? = null

    @Inject
    lateinit var backupTool: BackupTool
    @Inject
    lateinit var imagesSingleton: ImagesSingleton

    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {
        }

        override fun onBeginningOfSpeech() {
            Timber.d("onBeginningOfSpeech: ")
            showRecording()
        }

        override fun onRmsChanged(v: Float) {
        }

        override fun onBufferReceived(bytes: ByteArray) {
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

        override fun onResults(bundle: Bundle) {
            val res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (res != null && res.size > 0) {
                setText(StringUtils.capitalize(res[0].toString().toLowerCase()))
            }
            Timber.d("onResults: $res")
            releaseSpeech()
        }

        override fun onPartialResults(bundle: Bundle) {
            val res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (res != null && res.size > 0) {
                setText(res[0].toString().toLowerCase())
            }
            Timber.d("onPartialResults: $res")
        }

        override fun onEvent(i: Int, bundle: Bundle) {
        }
    }

    private var mDateCallBack = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        c.set(Calendar.MONTH, monthOfYear)
        c.set(Calendar.YEAR, year)
        stateViewModel.date.postValue(c.timeInMillis)
    }

    private var mCallBack = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        stateViewModel.time.postValue(c.timeInMillis)
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateViewModel = ViewModelProviders.of(this).get(CreateNoteViewModel::class.java)
        lifecycle.addObserver(stateViewModel)

        isBgDark = isDark

        setContentView(R.layout.activity_create_note)

        initActionBar()
        initMenu()
        hideRecording()
        remindDate.setOnClickListener { dateDialog() }
        remindTime.setOnClickListener { timeDialog() }
        micButton.setOnClickListener { micClick() }
        discardReminder.setOnClickListener { stateViewModel.isReminderAttached.postValue(false) }
        initImagesList()

        if (savedInstanceState == null) {
            initDefaults()
        }
        initFromState()
        loadNote()
    }

    private fun initFromState() {
        val pair = newPair()

        colorSlider.setSelection(pair.first)
        opacityBar.progress = pair.second
    }

    override fun onStart() {
        super.onStart()
        photoSelectionUtil = PhotoSelectionUtil(this, dialogues, true, this)

        ViewUtils.registerDragAndDrop(this, layoutContainer, true, themeUtil.getSecondaryColor(), {
            if (it.itemCount > 0) {
                taskMessage.setText(taskMessage.text.toString().trim() + "\n" + it.getItemAt(0).text.toString())
            }
        }, ClipDescription.MIMETYPE_TEXT_PLAIN)

        observeStates()

        if (prefs.hasPinCode && !stateViewModel.isLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun observeStates() {
        stateViewModel.colorOpacity.observe(this, Observer {
            if (it != null) {
                updateDarkness(it.second)
                updateBackground(it.first, it.second)
                updateTextColors()
                updateIcons()
            }
        })
        stateViewModel.time.observe(this, Observer {
            if (it != null) {
                remindTime.text = TimeUtil.getTime(it, prefs.is24HourFormat, prefs.appLanguage)
            }
        })
        stateViewModel.date.observe(this, Observer {
            if (it != null) {
                remindDate.text = TimeUtil.getDate(it, prefs.appLanguage)
            }
        })
        stateViewModel.isReminderAttached.observe(this, Observer {
            if (it != null) {
                remindContainer.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
        stateViewModel.fontStyle.observe(this, Observer {
            if (it != null) {
                updateFontStyle(it)
            }
        })
        stateViewModel.images.observe(this, Observer {
            if (it != null) {
                imagesGridAdapter.submitList(it)
            }
        })
    }

    private fun initDefaults() {
        stateViewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)

        val color = if (prefs.isNoteColorRememberingEnabled) {
            prefs.lastNoteColor
        } else {
            newColor()
        }
        val opacity = prefs.noteColorOpacity
        stateViewModel.colorOpacity.postValue(newPair(color, opacity))

        colorSlider.setSelection(color)
        opacityBar.progress = opacity

        stateViewModel.fontStyle.postValue(0)
        stateViewModel.time.postValue(System.currentTimeMillis())
        stateViewModel.date.postValue(System.currentTimeMillis())
    }

    private fun newColor(): Int = Random().nextInt(16)

    private fun setText(text: String?) {
        taskMessage.setText(text)
        taskMessage.setSelection(taskMessage.text.toString().length)
    }

    private fun showRecording() {
        recordingView.visibility = View.VISIBLE
    }

    private fun hideRecording() {
        recordingView.visibility = View.GONE
    }

    private fun initRecognizer() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech?.setRecognitionListener(mRecognitionListener)
        speech?.startListening(recognizerIntent)
    }

    private fun releaseSpeech() {
        try {
            if (speech != null) {
                speech?.stopListening()
                speech?.cancel()
                speech?.destroy()
                speech = null
            }
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private fun micClick() {
        if (!Permissions.ensurePermissions(this, AUDIO_CODE, Permissions.RECORD_AUDIO)) {
            return
        }
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
            newColor = stateViewModel.colorOpacity.value?.first ?: newColor()
        }
        if (opacity == -1) {
            newOpacity = stateViewModel.colorOpacity.value?.second ?: 100
        }
        Timber.d("newPair: $newColor, $newOpacity")
        return Pair(newColor, newOpacity)
    }

    private fun initMenu() {
        if (Module.hasMicrophone(this)) {
            micButton.visibility = View.VISIBLE
        } else {
            micButton.visibility = View.GONE
        }

        colorButton.setOnClickListener { toggleColorView() }
        imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
        reminderButton.setOnClickListener { switchReminder() }
        fontButton.setOnClickListener { showStyleDialog() }

        colorSlider.setColors(themeUtil.colorsForSlider())
        colorSlider.setSelectorColorResource(if (themeUtil.isDark) R.color.pureWhite else R.color.pureBlack)
        colorSlider.setListener { position, _ ->
            stateViewModel.colorOpacity.postValue(newPair(color = position))
            if (prefs.isNoteColorRememberingEnabled) {
                prefs.lastNoteColor = position
            }
        }
        opacityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.noteColorOpacity = progress
                stateViewModel.colorOpacity.postValue(newPair(opacity = progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun updateDarkness(opacity: Int) {
        isBgDark = if (themeUtil.isAlmostTransparent(opacity)) {
            isDark
        } else {
            false
        }
    }

    private fun updateTextColors() {
        val textColor = if (isBgDark) {
            ContextCompat.getColor(this, R.color.pureWhite)
        } else {
            ContextCompat.getColor(this, R.color.pureBlack)
        }
        taskMessage.setTextColor(textColor)
        taskMessage.setHintTextColor(textColor)
        taskMessage.backgroundTintList = ContextCompat.getColorStateList(this, if (isBgDark) {
            R.color.pureWhite
        } else {
            R.color.pureBlack
        })
        remindDate.setTextColor(textColor)
        remindTime.setTextColor(textColor)
    }

    private fun toggleColorView() {
        if (isColorPickerHidden()) {
            colorLayout.visibility = View.VISIBLE
        } else {
            colorLayout.visibility = View.GONE
        }
    }

    private fun isColorPickerHidden(): Boolean {
        return colorLayout.visibility == View.GONE
    }

    private fun isReminderAdded(): Boolean = stateViewModel.isReminderAttached.value ?: false

    private fun switchReminder() {
        stateViewModel.isReminderAttached.postValue(!isReminderAdded())
    }

    private fun loadNote() {
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        val data = intent.data
        if (data != null) {
            val filePath = intent.getStringExtra(Constants.FILE_PICKED) ?: ""
            loadNoteFromFile(filePath, data)
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                val note = intent.getSerializableExtra(Constants.INTENT_ITEM) as NoteWithImages?
                showNote(note)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveEditedImage() {
        val image = imagesSingleton.getEditable() ?: return
        stateViewModel.setImage(image, mEditPosition)
    }

    private val mNoteObserver: Observer<in NoteWithImages> = Observer { this.showNote(it) }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(id)).get(NoteViewModel::class.java)
        viewModel.note.observe(this, mNoteObserver)
        viewModel.reminder.observe(this, Observer<Reminder> { this.showReminder(it) })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                Timber.d("initViewModel: $commands")
                when (commands) {
                    Commands.DELETED, Commands.SAVED -> {
                        UpdatesHelper.updateNotesWidget(this)
                        UpdatesHelper.updateWidget(this)
                        finish()
                    }
                    else -> {
                    }
                }
            }
        })
        lifecycle.addObserver(viewModel)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        taskMessage.textSize = (prefs.noteTextSize + 12).toFloat()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        ViewUtils.listenScrollableView(touchView) {
            appBar.isSelected = it > 0
        }

        toolbar.inflateMenu(R.menu.activity_create_note)
    }

    private fun updateIcons() {
        toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
        ViewUtils.tintOverflowButton(toolbar, isBgDark)
        invalidateOptionsMenu()
        discardReminder.setImageDrawable(ViewUtils.tintIcon(this, R.drawable.ic_twotone_cancel_24px, isBgDark))
    }

    private fun loadNoteFromFile(filePath: String, uri: Uri?) {
        try {
            mItem = if (uri != null) {
                val scheme = uri.scheme
                if (ContentResolver.SCHEME_CONTENT != scheme) {
                    backupTool.getNote(uri.path, null)
                } else {
                    null
                }
            } else {
                backupTool.getNote(filePath, null)
            }
            showNote(mItem)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun showNote(noteWithImages: NoteWithImages?) {
        this.mItem = noteWithImages
        Timber.d("showNote: $noteWithImages")
        if (noteWithImages != null && !stateViewModel.isNoteEdited) {
            val note = noteWithImages.note ?: return
            colorSlider.setSelection(note.color)
            opacityBar.progress = note.opacity
            setText(note.summary)
            stateViewModel.fontStyle.postValue(note.style)
            stateViewModel.images.postValue(noteWithImages.images)
            stateViewModel.colorOpacity.postValue(newPair(note.color, note.opacity))
            stateViewModel.isNoteEdited = true
        }
    }

    private fun initImagesList() {
        imagesGridAdapter.isEditable = true
        imagesGridAdapter.actionsListener = object : ActionsListener<ImageFile> {
            override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
                when (actions) {
                    ListActions.EDIT -> editImage(position)
                    ListActions.OPEN -> openImagePreview(position)
                    ListActions.REMOVE -> stateViewModel.removeImage(position)
                    else -> {
                    }
                }
            }
        }
        imagesList.layoutManager = KeepLayoutManager(this, 6, imagesGridAdapter)
        imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        imagesList.adapter = imagesGridAdapter
    }

    private fun openImagePreview(position: Int) {
        imagesSingleton.setCurrent(imagesGridAdapter.data)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun editImage(position: Int) {
        imagesSingleton.setEditable(imagesGridAdapter.get(position))
        this.mEditPosition = position
        startActivityForResult(Intent(this, ImageEditActivity::class.java), EDIT_CODE)
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null && !stateViewModel.isReminderEdited) {
            setDateTime(reminder.eventTime)
            stateViewModel.isReminderAttached.postValue(true)
            stateViewModel.isReminderEdited = true
        }
    }

    private fun hideProgress() {
        recordingView.visibility = View.GONE
    }

    private fun showProgress() {
        recordingView.visibility = View.VISIBLE
    }

    private fun shareNote() {
        if (!Permissions.ensurePermissions(this, SEND_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            return
        }
        val note = createObject() ?: return
        showProgress()
        launchDefault {
            val file = backupTool.createNote(note)
            withUIContext {
                hideProgress()
                if (file != null) {
                    sendNote(file)
                } else {
                    showErrorSending()
                }
            }
        }
    }

    private fun sendNote(file: File) {
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            showErrorSending()
            return
        }
        val noteWithImages = mItem
        if (noteWithImages != null) {
            TelephonyUtil.sendNote(file, this, noteWithImages.note?.summary)
        }
    }

    private fun showErrorSending() {
        Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
    }

    private fun setDateTime(eventTime: String?) {
        val calendar = Calendar.getInstance()
        if (eventTime == null) {
            calendar.timeInMillis = System.currentTimeMillis()
        } else {
            calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(eventTime)
        }
        stateViewModel.date.postValue(calendar.timeInMillis)
        stateViewModel.time.postValue(calendar.timeInMillis)
    }

    private fun createObject(): NoteWithImages? {
        val text = taskMessage.text.toString().trim()
        val images = imagesGridAdapter.data
        if (TextUtils.isEmpty(text) && images.isEmpty()) {
            taskMessage.error = getString(R.string.must_be_not_empty)
            return null
        }

        val pair = stateViewModel.colorOpacity.value ?: Pair(newColor(), opacityBar.progress)

        var noteWithImages = mItem
        var note = noteWithImages?.note
        if (note == null) {
            note = Note()
        }
        note.summary = text
        note.date = TimeUtil.gmtDateTime
        note.color = pair.first
        note.style = stateViewModel.fontStyle.value ?: 0
        note.opacity = pair.second

        if (noteWithImages == null) {
            noteWithImages = NoteWithImages()
        }

        noteWithImages.images = images
        noteWithImages.note = note
        return noteWithImages
    }

    private fun saveNote() {
        val noteWithImages = createObject() ?: return
        val hasReminder = stateViewModel.isReminderAttached.value ?: false
        if (!hasReminder && mItem != null) removeNoteFromReminder()
        var reminder: Reminder? = null
        val note = noteWithImages.note
        if (hasReminder && note != null) {
            reminder = createReminder(note) ?: return
        }
        viewModel.note.removeObserver(mNoteObserver)
        viewModel.saveNote(noteWithImages, reminder)
    }

    private fun dateTime(): Long {
        val result = Calendar.getInstance()
        result.timeInMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stateViewModel.date.value ?: System.currentTimeMillis()

        result.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        result.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        result.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))

        calendar.timeInMillis = stateViewModel.time.value ?: System.currentTimeMillis()

        result.set(Calendar.HOUR, calendar.get(Calendar.HOUR))
        result.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        result.set(Calendar.SECOND, 0)
        result.set(Calendar.MILLISECOND, 0)

        return result.timeInMillis
    }

    private fun createReminder(note: Note): Reminder? {
        var reminder = mReminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.type = Reminder.BY_DATE
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.useGlobal = true
        reminder.noteId = note.key
        reminder.isActive = true
        reminder.isRemoved = false
        reminder.summary = note.summary

        val startTime = dateTime()
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return null
        }
        return reminder
    }

    private fun removeNoteFromReminder() {
        val reminder = mReminder
        if (reminder != null) {
            viewModel.deleteReminder(reminder)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_share -> {
                shareNote()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteDialog()
                return true
            }
            R.id.action_add -> {
                saveNote()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteDialog() {
        val builder = dialogues.getDialog(this)
        builder.setMessage(getString(R.string.delete_this_note))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteNote()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteNote() {
        val note = mItem
        if (note != null) {
            viewModel.deleteNote(note)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_create_note, menu)
        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_done_24px, isBgDark)
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    private fun updateFontStyle(fontStyle: Int) {
        taskMessage.typeface = AssetsUtil.getTypeface(this, fontStyle)
    }

    private fun updateBackground(color: Int, opacity: Int) {
        Timber.d("updateBackground: $color, $opacity")

        val lightColorSemi = themeUtil.getNoteLightColor(color, opacity)
        layoutContainer.setBackgroundColor(lightColorSemi)
        toolbar.setBackgroundColor(lightColorSemi)
        appBar.setBackgroundColor(lightColorSemi)

        val lightColor = themeUtil.getNoteLightColor(color, 100)
        window.statusBarColor = lightColor
        bottomBar.setCardBackgroundColor(lightColor)
        bottomBar.invalidate()
    }

    private fun showStyleDialog() {
        val builder = dialogues.getDialog(this)
        builder.setTitle(getString(R.string.font_style))

        val contacts = AssetsUtil.getFontNames()

        val inflater = LayoutInflater.from(this)
        val adapter = object : ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, contacts) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var cView = convertView
                if (cView == null) {
                    cView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null)
                }
                val textView = cView?.findViewById<TextView>(android.R.id.text1)
                if (textView != null) {
                    textView.typeface = getTypeface(position)
                    textView.text = contacts[position]
                }
                return cView
            }

            private fun getTypeface(position: Int): Typeface? {
                return AssetsUtil.getTypeface(this@CreateNoteActivity, position)
            }
        }
        builder.setSingleChoiceItems(adapter, stateViewModel.fontStyle.value ?: 0) { _, which ->
            stateViewModel.fontStyle.postValue(which)
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun dateDialog() {
        val c = Calendar.getInstance()
        c.timeInMillis = stateViewModel.date.value ?: System.currentTimeMillis()
        TimeUtil.showDatePicker(this, themeUtil.dialogStyle, prefs, c.get(Calendar.YEAR),
                c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), mDateCallBack)
    }

    private fun timeDialog() {
        val c = Calendar.getInstance()
        c.timeInMillis = stateViewModel.time.value ?: System.currentTimeMillis()
        TimeUtil.showTimePicker(this, themeUtil.dialogStyle, prefs.is24HourFormat,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), mCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(stateViewModel)
        lifecycle.removeObserver(viewModel)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(taskMessage.windowToken, 0)
        releaseSpeech()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionUtil.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            } else {
                stateViewModel.isLogged = true
            }
        } else if (requestCode == EDIT_CODE) {
            if (resultCode == RESULT_OK) {
                if (mEditPosition != -1) {
                    saveEditedImage()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        photoSelectionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                AUDIO_CODE -> micClick()
                SEND_CODE -> shareNote()
            }
        }
    }

    override fun onImageSelected(uri: Uri?, clipData: ClipData?) {
        stateViewModel.addMultiple(uri, clipData, this)
    }

    override fun onBitmapReady(bitmap: Bitmap) {
        stateViewModel.addBitmap(bitmap)
    }

    override fun onBackPressed() {
        if (!isColorPickerHidden()) {
            toggleColorView()
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val MENU_ITEM_DELETE = 12
        private const val EDIT_CODE = 11223
        private const val AUDIO_CODE = 255000
        private const val SEND_CODE = 25501
        private const val ARG_LOGGED = "arg_logged"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, CreateNoteActivity::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }
    }
}