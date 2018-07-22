package com.elementary.tasks.notes.create

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.notes.NoteViewModel
import com.elementary.tasks.core.viewModels.reminders.ReminderViewModel
import com.elementary.tasks.core.views.ColorPickerView
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration
import com.elementary.tasks.notes.editor.ImageEditActivity
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity.Companion.PREVIEW_IMAGES
import kotlinx.android.synthetic.main.activity_create_note.*
import kotlinx.android.synthetic.main.dialog_color_picker_layout.view.*
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

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
    private var mLastX = -1f

    private lateinit var viewModel: NoteViewModel
    private lateinit var reminderViewModel: ReminderViewModel
    private val mAdapter = ImagesGridAdapter()
    private var mProgress: ProgressDialog? = null

    private var mItem: Note? = null
    private var mReminder: Reminder? = null

    private var speech: SpeechRecognizer? = null
    private lateinit var photoSelectionUtil: PhotoSelectionUtil

    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {
            LogUtil.d(TAG, "onReadyForSpeech: ")
        }

        override fun onBeginningOfSpeech() {
            LogUtil.d(TAG, "onBeginningOfSpeech: ")
            showRecording()
        }

        override fun onRmsChanged(v: Float) {
            var v = v
            v *= 2000
            var db = 0.0
            if (v > 1) {
                db = 20 * Math.log10(v.toDouble())
            }
            recordingView.setVolume(db.toFloat())
        }

        override fun onBufferReceived(bytes: ByteArray) {
            LogUtil.d(TAG, "onBufferReceived: " + Arrays.toString(bytes))
        }

        override fun onEndOfSpeech() {
            hideRecording()
            LogUtil.d(TAG, "onEndOfSpeech: ")
        }

        override fun onError(i: Int) {
            LogUtil.d(TAG, "onError: $i")
            releaseSpeech()
            hideRecording()
        }

        override fun onResults(bundle: Bundle) {
            val res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (res != null && res.size > 0) {
                setText(StringUtils.capitalize(res[0].toString().toLowerCase()))
            }
            LogUtil.d(TAG, "onResults: $res")
            releaseSpeech()
        }

        override fun onPartialResults(bundle: Bundle) {
            val res = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (res != null && res.size > 0) {
                setText(res[0].toString().toLowerCase())
            }
            LogUtil.d(TAG, "onPartialResults: $res")
        }

        override fun onEvent(i: Int, bundle: Bundle) {
            LogUtil.d(TAG, "onEvent: ")
        }
    }

    private val isReminderAttached: Boolean
        get() = remindContainer.visibility == View.VISIBLE

    private var myDateCallBack = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
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

    private var myCallBack = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        mHour = hourOfDay
        mMinute = minute
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        remindTime.text = TimeUtil.getTime(c.time, prefs.is24HourFormatEnabled)
    }

    private fun setText(text: String?) {
        task_message.setText(text)
        task_message.setSelection(task_message.text.toString().length)
    }

    private fun showRecording() {
        recordingView.start()
        recordingView.visibility = View.VISIBLE
    }

    private fun hideRecording() {
        recordingView.stop()
        recordingView.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        photoSelectionUtil = PhotoSelectionUtil(this, this)

        initActionBar()
        initMenu()
        initBgContainer()
        ViewUtils.fadeInAnimation(layoutContainer)
        remindDate.setOnClickListener { dateDialog() }
        remindTime.setOnClickListener { timeDialog() }
        micButton.setOnClickListener { micClick() }
        discardReminder.setOnClickListener { ViewUtils.collapse(remindContainer) }
        initImagesList()

        loadNote()

        updateBackground()
        updateTextStyle()
        showSaturationAlert()
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
        if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
            Permissions.requestPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO)
            return
        }
        if (speech != null) {
            hideRecording()
            releaseSpeech()
            return
        }
        initRecognizer()
    }

    private fun showSaturationAlert() {
        if (prefs.isNoteHintShowed) {
            return
        }
        prefs.isNoteHintShowed = true
        // TODO: 31.05.2018 Add banner about note color opacity
        //        mAlerter = Alerter.create(this)
        //                .setTitle(R.string.swipe_left_or_right_to_adjust_saturation)
        //                .setText(R.string.click_to_hide)
        //                .enableInfiniteDuration(true)
        //                .setBackgroundColorRes(getThemeUtil().colorPrimaryDark(mColor))
        //                .setOnClickListener(v -> {
        //                    if (mAlerter != null) mAlerter.hide();
        //                })
        //                .show();
    }

    private fun initBgContainer() {
        touch_view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener true
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                val x = event.x
                if (mLastX != -1f) {
                    var currentOpacity = prefs.noteColorOpacity
                    if (x - mLastX > 0) {
                        if (currentOpacity < 100) {
                            currentOpacity += 1
                        }
                    } else {
                        if (currentOpacity > 0) {
                            currentOpacity -= 1
                        }
                    }
                    prefs.noteColorOpacity = currentOpacity
                    updateBackground()
                }
                mLastX = x
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun initMenu() {
        bottomBarView.setBackgroundColor(themeUtil.backgroundStyle)
        colorButton.setOnClickListener { showColorDialog() }
        imageButton.setOnClickListener { photoSelectionUtil.selectImage() }
        reminderButton.setOnClickListener { switchReminder() }
        fontButton.setOnClickListener { showStyleDialog() }
    }

    private fun switchReminder() {
        if (!isReminderAttached) {
            setDateTime(null)
            ViewUtils.expand(remindContainer)
        } else {
            ViewUtils.collapse(remindContainer)
        }
    }

    private fun loadNote() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID)
        initViewModel(id)
        if (intent.data != null) {
            val filePath = intent.getStringExtra(Constants.FILE_PICKED)
            val name = intent.data
            loadNoteFromFile(filePath, name)
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, NoteViewModel.Factory(application, id)).get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer { this.showNote(it) })
        viewModel.editedPicture.observe(this, Observer { note ->
            if (note != null && !note.images.isEmpty()) {
                mAdapter.setImage(note.images[0], mEditPosition)
            }
        })
        viewModel.reminder.observe(this, Observer<Reminder> { this.showReminder(it) })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> finish()
                }
            }
        })

        reminderViewModel = ViewModelProviders.of(this, ReminderViewModel.Factory(application, 0)).get(ReminderViewModel::class.java)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        task_message.textSize = (prefs.noteTextSize + 12).toFloat()
        if (supportActionBar != null) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.elevation = 0f
        }
        appBar.visibility = View.VISIBLE
    }

    private fun loadNoteFromFile(filePath: String, name: Uri?) {
        try {
            mItem = if (name != null) {
                val scheme = name.scheme
                if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    BackupTool.getInstance().getNote(cr, name)
                } else {
                    BackupTool.getInstance().getNote(name.path, null)
                }
            } else {
                BackupTool.getInstance().getNote(filePath, null)
            }
            showNote(mItem)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun showNote(note: Note?) {
        this.mItem = note
        if (note != null) {
            mColor = note.color
            mFontStyle = note.style
            setText(note.summary)
            mAdapter.setImages(note.images)
        } else {
            mColor = Random().nextInt(16)
            if (prefs.isNoteColorRememberingEnabled) {
                mColor = prefs.lastNoteColor
            }
        }
    }

    private fun initImagesList() {
        mAdapter.setEditable(true)
        mAdapter.actionsListener = object: ActionsListener<NoteImage> {
            override fun onAction(view: View, position: Int, t: NoteImage?, actions: ListActions) {
                when (actions) {
                    ListActions.EDIT -> editImage(position)
                    ListActions.OPEN -> openImagePreview(position)
                }
            }
        }
        imagesList.layoutManager = KeepLayoutManager(this, 6, mAdapter)
        imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        imagesList.adapter = mAdapter
    }

    private fun openImagePreview(position: Int) {
        val note = Note()
        note.key = PREVIEW_IMAGES
        note.images = mAdapter.data
        viewModel.saveNote(note)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, note.key)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun editImage(position: Int) {
        val note = Note()
        note.key = PREVIEW_IMAGES
        note.images = listOf(mAdapter.getItem(position))
        viewModel.saveNote(note)
        this.mEditPosition = position
        startActivityForResult(Intent(this, ImageEditActivity::class.java), EDIT_CODE)
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null) {
            setDateTime(reminder.eventTime)
            ViewUtils.expand(remindContainer)
        }
    }

    private fun hideProgress() {
        if (mProgress != null && mProgress!!.isShowing) {
            mProgress?.dismiss()
        }
    }

    private fun showProgress() {
        mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false)
    }

    private fun shareNote() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, SEND_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
        createObject()
        showProgress()
        val callback = object : BackupTool.CreateCallback {
            override fun onReady(file: File?) {
                if (file != null) sendNote(file)
            }
        }
        Thread { BackupTool.getInstance().createNote(mItem, callback) }.start()
    }

    private fun sendNote(file: File) {
        hideProgress()
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
            return
        }
        if (mItem != null) {
            TelephonyUtil.sendNote(file, this, mItem!!.summary)
        }
    }

    private fun setDateTime(eventTime: String?) {
        val calendar = Calendar.getInstance()
        if (eventTime == null)
            calendar.timeInMillis = System.currentTimeMillis()
        else
            calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(eventTime)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mMonth = calendar.get(Calendar.MONTH)
        mYear = calendar.get(Calendar.YEAR)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        remindDate.text = TimeUtil.getDate(calendar.timeInMillis)
        remindTime.text = TimeUtil.getTime(calendar.time, prefs.is24HourFormatEnabled)
    }

    private fun createObject(): Boolean {
        val note = task_message.text.toString().trim { it <= ' ' }
        val images = mAdapter.data
        if (TextUtils.isEmpty(note) && images.isEmpty()) {
            task_message.error = getString(R.string.must_be_not_empty)
            return false
        }
        if (mItem == null) {
            mItem = Note()
        }
        mItem?.summary = note
        mItem?.date = TimeUtil.gmtDateTime
        mItem?.images = images
        mItem?.color = mColor
        mItem?.style = mFontStyle
        return true
    }

    private fun saveNote() {
        if (!createObject()) {
            return
        }
        val hasReminder = isReminderAttached
        if (!hasReminder && mItem != null) removeNoteFromReminder()
        val note = mItem
        if (note != null) {
            viewModel.saveNote(note)
            if (hasReminder) {
                val calendar = Calendar.getInstance()
                calendar.set(mYear, mMonth, mDay, mHour, mMinute)
                createReminder(note.key, calendar)
            }
            UpdatesHelper.getInstance(this).updateNotesWidget()
            finish()
        }
    }

    private fun createReminder(key: String, calendar: Calendar) {
        var reminder = mReminder
        if (reminder == null) {
            reminder = Reminder()
        }
        reminder.type = Reminder.BY_DATE
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.useGlobal = true
        reminder.noteId = key
        reminder.isActive = true
        reminder.isRemoved = false
        if (mItem != null)
            reminder.summary = mItem!!.summary
        else
            reminder.summary = ""
        val def = reminderViewModel.defaultGroup.value
        if (def != null) {
            reminder.groupUuId = def.uuId
        }
        val startTime = calendar.timeInMillis
        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            Toast.makeText(this, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
            return
        }
        reminderViewModel.saveAndStartReminder(reminder)
    }

    private fun removeNoteFromReminder() {
        if (mReminder != null) {
            reminderViewModel.deleteReminder(mReminder!!, false)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs.isAutoSaveEnabled) {
            saveNote()
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

    private fun showColorDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(getString(R.string.change_color))
        val binding = layoutInflater.inflate(R.layout.dialog_color_picker_layout, null)
        val view = binding.pickerView
        view.setSelectedColor(mColor)
        builder.setView(binding)
        val dialog = builder.create()
        view.setListener(object : ColorPickerView.OnColorListener {
            override fun onColorSelect(code: Int) {
                mColor = code
                if (prefs.isNoteColorRememberingEnabled) {
                    prefs.lastNoteColor = mColor
                }
                updateBackground()
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    private fun deleteDialog() {
        val builder = Dialogues.getDialog(this)
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
        if (mItem != null) {
            viewModel.deleteNote(mItem!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_note, menu)
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionUtil.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EDIT_CODE -> if (mEditPosition != -1) updateImage()
            }
        }
    }

    private fun updateImage() {
        viewModel.loadEditedPicture()
    }

    private fun addImageFromUri(uri: Uri?) {
        if (uri == null) return
        var bitmapImage: Bitmap? = null
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(this, uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (bitmapImage != null) {
            val outputStream = ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            mAdapter.addImage(NoteImage(outputStream.toByteArray()))
        }
    }

    private fun updateTextStyle() {
        task_message.typeface = AssetsUtil.getTypeface(this, mFontStyle)
    }

    private fun updateBackground() {
        layoutContainer.setBackgroundColor(themeUtil.getNoteLightColor(mColor))
        appBar.setBackgroundColor(themeUtil.getNoteLightColor(mColor))
        if (Module.isLollipop) {
            window.statusBarColor = themeUtil.getNoteDarkColor(mColor)
        }
    }

    private fun showStyleDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setTitle(getString(R.string.font_style))
        val contacts = ArrayList<String>()
        contacts.clear()
        contacts.add("Black")
        contacts.add("Black Italic")
        contacts.add("Bold")
        contacts.add("Bold Italic")
        contacts.add("Italic")
        contacts.add("Light")
        contacts.add("Light Italic")
        contacts.add("Medium")
        contacts.add("Medium Italic")
        contacts.add("Regular")
        contacts.add("Thin")
        contacts.add("Thin Italic")
        val inflater = LayoutInflater.from(this)
        val adapter = object : ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, contacts) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var cView = convertView
                if (cView == null) {
                    cView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null)
                }
                val textView = cView!!.findViewById<TextView>(android.R.id.text1)
                textView.typeface = getTypeface(position)
                textView.text = contacts[position]
                return cView
            }

            private fun getTypeface(position: Int): Typeface {
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
        TimeUtil.showDatePicker(this, myDateCallBack, mYear, mMonth, mDay)
    }

    private fun timeDialog() {
        TimeUtil.showTimePicker(this, myCallBack, mHour, mMinute)
    }

    override fun onDestroy() {
        super.onDestroy()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(task_message.windowToken, 0)
        releaseSpeech()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        photoSelectionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                micClick()
            }
            SEND_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareNote()
            }
        }
    }

    override fun onImageSelected(uri: Uri?, clipData: ClipData?) {
        if (uri != null) {
            addImageFromUri(uri)
        } else if (clipData != null) {
            DecodeImagesAsync(this, {
                if (!it.isEmpty()) {
                    mAdapter.addNextImages(it)
                }
            }, clipData.itemCount).execute(clipData)
        }
    }

    companion object {

        private const val TAG = "CreateNoteActivity"
        const val MENU_ITEM_DELETE = 12
        private const val EDIT_CODE = 11223
        private const val AUDIO_CODE = 255000
        private const val SEND_CODE = 25501
    }
}