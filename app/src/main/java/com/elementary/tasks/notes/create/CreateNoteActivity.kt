package com.elementary.tasks.notes.create

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
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

    private var mHour = 0
    private var mMinute = 0
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 1
    private var mColor = 0
    private var mFontStyle = 9
    private var mEditPosition = -1
    private var isBgDark = false
    private var mIsLogged = false

    private lateinit var viewModel: NoteViewModel
    private val imagesGridAdapter = ImagesGridAdapter()
    private var mProgress: ProgressDialog? = null

    private var mItem: NoteWithImages? = null
    private var mReminder: Reminder? = null

    private var speech: SpeechRecognizer? = null
    private lateinit var photoSelectionUtil: PhotoSelectionUtil
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

    private val isReminderAttached: Boolean
        get() = remindContainer.visibility == View.VISIBLE

    private var mDateCallBack = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        val dayStr: String = if (mDay < 10) {
            "0$mDay"
        } else {
            mDay.toString()
        }
        val monthStr: String = if (mMonth < 9) {
            "0" + (mMonth + 1)
        } else {
            (mMonth + 1).toString()
        }
        remindDate.text = SuperUtil.appendString(dayStr, "/", monthStr, "/", mYear.toString())
    }

    private var mCallBack = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        remindTime.text = TimeUtil.getTime(c.time, prefs.is24HourFormat, prefs.appLanguage)
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBgDark = isDark
        mIsLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        setContentView(R.layout.activity_create_note)

        photoSelectionUtil = PhotoSelectionUtil(this, dialogues, true, this)

        initActionBar()
        initMenu()

        hideRecording()

        remindDate.setOnClickListener { dateDialog() }
        remindTime.setOnClickListener { timeDialog() }
        micButton.setOnClickListener { micClick() }
        discardReminder.setOnClickListener { remindContainer.visibility = View.GONE }
        remindContainer.visibility = View.GONE
        initImagesList()

        initColor()
        loadNote()

        updateBackground()
        updateDarkness()
        updateTextStyle()
        updateTextColors()
        updateIcons()

        if (prefs.hasPinCode && !mIsLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun initColor() {
        mColor = if (prefs.isNoteColorRememberingEnabled) {
            prefs.lastNoteColor
        } else {
            Random().nextInt(16)
        }
        colorSlider.setSelection(mColor)
    }

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

    private fun initMenu() {
        colorButton.setOnClickListener { toggleColorView() }
        imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
        reminderButton.setOnClickListener { switchReminder() }
        fontButton.setOnClickListener { showStyleDialog() }

        colorSlider.setColors(themeUtil.colorsForSlider())
        colorSlider.setListener { position, _ ->
            mColor = position
            if (prefs.isNoteColorRememberingEnabled) {
                prefs.lastNoteColor = mColor
            }
            updateBackground()
        }
        opacityBar.progress = prefs.noteColorOpacity
        opacityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.noteColorOpacity = progress
                updateBackground()
                updateDarkness()
                updateTextColors()
                updateIcons()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        updateBackground()
    }

    private fun updateDarkness() {
        isBgDark = if (themeUtil.isAlmostTransparent(opacityBar.progress)) {
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
        if (Module.isLollipop) {
            taskMessage.backgroundTintList = ContextCompat.getColorStateList(this, if (isBgDark) {
                R.color.pureWhite
            } else {
                R.color.pureBlack
            })
        }
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

    private fun switchReminder() {
        if (!isReminderAttached) {
            setDateTime(null)
            remindContainer.visibility = View.VISIBLE
        } else {
            remindContainer.visibility = View.GONE
        }
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
        imagesGridAdapter.setImage(image, mEditPosition)
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(application, id)).get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer { this.showNote(it) })
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
        updateIcons()
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
        if (noteWithImages != null) {
            imagesGridAdapter.setImages(noteWithImages.images)
            val note = noteWithImages.note ?: return
            mColor = note.color
            mFontStyle = note.style
            setText(note.summary)
            colorSlider.setSelection(mColor)
            opacityBar.progress = note.opacity
            updateBackground()
        }
    }

    private fun initImagesList() {
        imagesGridAdapter.setEditable(true)
        imagesGridAdapter.actionsListener = object : ActionsListener<ImageFile> {
            override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
                when (actions) {
                    ListActions.EDIT -> editImage(position)
                    ListActions.OPEN -> openImagePreview(position)
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
        imagesSingleton.setEditable(imagesGridAdapter.getItem(position))
        this.mEditPosition = position
        startActivityForResult(Intent(this, ImageEditActivity::class.java), EDIT_CODE)
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null) {
            setDateTime(reminder.eventTime)
            remindContainer.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        if (mProgress != null && (mProgress?.isShowing == true)) {
            mProgress?.dismiss()
        }
    }

    private fun showProgress() {
        mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
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
                if (file != null) sendNote(file)
            }
        }
    }

    private fun sendNote(file: File) {
        hideProgress()
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        val noteWithImages = mItem
        if (noteWithImages != null) {
            TelephonyUtil.sendNote(file, this, noteWithImages.note?.summary)
        }
    }

    private fun setDateTime(eventTime: String?) {
        val calendar = Calendar.getInstance()
        if (eventTime == null) {
            calendar.timeInMillis = System.currentTimeMillis()
        } else {
            calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(eventTime)
        }
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mMonth = calendar.get(Calendar.MONTH)
        mYear = calendar.get(Calendar.YEAR)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        remindDate.text = TimeUtil.getDate(calendar.timeInMillis, prefs.appLanguage)
        remindTime.text = TimeUtil.getTime(calendar.time, prefs.is24HourFormat, prefs.appLanguage)
    }

    private fun createObject(): NoteWithImages? {
        val text = taskMessage.text.toString().trim()
        val images = imagesGridAdapter.data
        if (TextUtils.isEmpty(text) && images.isEmpty()) {
            taskMessage.error = getString(R.string.must_be_not_empty)
            return null
        }
        var noteWithImages = mItem

        var note = noteWithImages?.note
        if (note == null) {
            note = Note()
        }
        note.summary = text
        note.date = TimeUtil.gmtDateTime
        note.color = mColor
        note.style = mFontStyle
        note.opacity = opacityBar.progress

        if (noteWithImages == null) {
            noteWithImages = NoteWithImages()
        }

        noteWithImages.images = images
        noteWithImages.note = note
        return noteWithImages
    }

    private fun saveNote() {
        val noteWithImages = createObject() ?: return
        val hasReminder = isReminderAttached
        if (!hasReminder && mItem != null) removeNoteFromReminder()
        var reminder: Reminder? = null
        val note = noteWithImages.note
        if (hasReminder && note != null) {
            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay, mHour, mMinute)
            reminder = createReminder(note, calendar) ?: return
        }
        if (prefs.isNoteColorRememberingEnabled) {
            prefs.lastNoteColor = mColor
        }
        viewModel.saveNote(noteWithImages, reminder)
    }

    private fun createReminder(note: Note, calendar: Calendar): Reminder? {
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

        val startTime = calendar.timeInMillis
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionUtil.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            }
        } else if (requestCode == EDIT_CODE) {
            if (resultCode == RESULT_OK) {
                if (mEditPosition != -1) {
                    saveEditedImage()
                }
            }
        }
    }

    private fun addImageFromUri(uri: Uri?) {
        if (uri == null) return
        launchDefault {
            var bitmapImage: Bitmap? = null
            try {
                bitmapImage = BitmapUtils.decodeUriToBitmap(this@CreateNoteActivity, uri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (bitmapImage != null) {
                val outputStream = ByteArrayOutputStream()
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val imageFile = ImageFile(outputStream.toByteArray())
                withUIContext {
                    imagesGridAdapter.addImage(imageFile)
                }
            }
        }
    }

    private fun updateTextStyle() {
        taskMessage.typeface = AssetsUtil.getTypeface(this, mFontStyle)
    }

    private fun updateBackground() {
        val lightColorSemi = themeUtil.getNoteLightColor(mColor, opacityBar.progress)
        layoutContainer.setBackgroundColor(lightColorSemi)
        toolbar.setBackgroundColor(lightColorSemi)
        appBar.setBackgroundColor(lightColorSemi)

        val lightColor = themeUtil.getNoteLightColor(mColor, 100)
        if (Module.isLollipop) {
            window.statusBarColor = lightColor
        }
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
        builder.setSingleChoiceItems(adapter, mFontStyle) { _, which ->
            mFontStyle = which
            updateTextStyle()
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun dateDialog() {
        TimeUtil.showDatePicker(this, themeUtil.dialogStyle, prefs, mYear, mMonth, mDay, mDateCallBack)
    }

    private fun timeDialog() {
        TimeUtil.showTimePicker(this, themeUtil.dialogStyle, prefs.is24HourFormat, mHour, mMinute, mCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(taskMessage.windowToken, 0)
        releaseSpeech()
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
        if (uri != null) {
            addImageFromUri(uri)
        } else if (clipData != null) {
            DecodeImages.startDecoding(this, clipData, imagesGridAdapter.itemCount, {
                imagesGridAdapter.addNextImages(it)
            }, { i, imageFile ->
                imagesGridAdapter.setImage(imageFile, i)
            })
        }
    }

    override fun onBitmapReady(bitmap: Bitmap) {
        launchDefault {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageFile = ImageFile(outputStream.toByteArray())
            withUIContext {
                imagesGridAdapter.addImage(imageFile)
            }
        }
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