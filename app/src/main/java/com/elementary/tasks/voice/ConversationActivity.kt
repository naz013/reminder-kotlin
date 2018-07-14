package com.elementary.tasks.voice

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.PopupMenu

import com.backdoor.engine.Action
import com.backdoor.engine.ActionType
import com.backdoor.engine.Model
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.conversation.ConversationViewModel
import com.elementary.tasks.databinding.ActivityConversationBinding
import com.elementary.tasks.reminder.create_edit.AddReminderActivity

import org.apache.commons.lang3.StringUtils
import java.util.Locale
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Copyright 2017 Nazar Suhovich
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
class ConversationActivity : ThemedActivity() {

    private var speech: SpeechRecognizer? = null
    private var binding: ActivityConversationBinding? = null

    private val mAdapter = ConversationAdapter()
    private var viewModel: ConversationViewModel? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady: Boolean = false
    private var mAskAction: AskAction? = null

    private val mTextToSpeechListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts!!.setLanguage(Locale(Language.getLanguage(prefs!!.voiceLocale)))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                LogUtil.d(TAG, "This Language is not supported")
            } else {
                isTtsReady = true
                addResponse(getLocalized(R.string.hi_how_can_i_help_you))
                Handler().postDelayed({ micClick() }, 1500)
            }
        } else {
            LogUtil.d(TAG, "Initialization Failed!")
        }
    }
    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {
            LogUtil.d(TAG, "onReadyForSpeech: ")
        }

        override fun onBeginningOfSpeech() {
            LogUtil.d(TAG, "onBeginningOfSpeech: ")
        }

        override fun onRmsChanged(v: Float) {
            var v = v
            v = v * 2000
            var db = 0.0
            if (v > 1) {
                db = 20 * Math.log10(v.toDouble())
            }
            binding!!.recordingView.setVolume(db.toFloat())
        }

        override fun onBufferReceived(bytes: ByteArray) {
            LogUtil.d(TAG, "onBufferReceived: ")
        }

        override fun onEndOfSpeech() {
            LogUtil.d(TAG, "onEndOfSpeech: ")

        }

        override fun onError(i: Int) {
            LogUtil.d(TAG, "onError: $i")
            showSilentMessage()
        }

        override fun onResults(bundle: Bundle?) {
            binding!!.recordingView.loading()
            if (bundle == null) {
                showSilentMessage()
                return
            }
            parseResults(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
        }

        override fun onPartialResults(bundle: Bundle) {
            LogUtil.d(TAG, "onPartialResults: ")
        }

        override fun onEvent(i: Int, bundle: Bundle) {
            LogUtil.d(TAG, "onEvent: ")
        }
    }
    private val mInsertCallback = ConversationAdapter.InsertCallback { binding!!.conversationList.scrollToPosition(0) }

    private fun showSilentMessage() {
        stopView()
        playTts(getLocalized(R.string.did_you_say_something))
    }

    private fun getLocalized(id: Int): String {
        return Language.getLocalized(this, id)
    }

    private fun parseResults(list: List<String>?) {
        LogUtil.d(TAG, "parseResults: " + list!!)
        if (list == null || list.isEmpty()) {
            showSilentMessage()
            return
        }
        var model: Model? = null
        var suggestion: String? = null
        for (s in list) {
            suggestion = s
            model = viewModel!!.findSuggestion(s)
            if (model != null) {
                break
            }
        }
        if (model != null) {
            performResult(model, suggestion!!)
        } else {
            stopView()
            mAdapter.addReply(Reply(Reply.REPLY, list[0]))
            addResponse(getLocalized(R.string.can_not_recognize_your_command))
        }
    }

    private fun performAnswer(answer: Model) {
        stopView()
        if (mAskAction != null) {
            mAdapter.removeAsk()
            if (answer.action == Action.YES) {
                mAskAction!!.onYes()
            } else if (answer.action == Action.NO) {
                mAskAction!!.onNo()
            }
        }
    }

    private fun stopView() {
        releaseSpeech()
        binding!!.recordingView.stop()
    }

    private fun addObjectResponse(reply: Reply) {
        stopView()
        mAdapter.addReply(reply)
    }

    private fun performResult(model: Model, s: String) {
        if (mAskAction != null) {
            mAdapter.removeAsk()
        }
        mAdapter.addReply(Reply(Reply.REPLY, s.toLowerCase()))
        LogUtil.d(TAG, "performResult: $model")
        val actionType = model.type
        if (actionType == ActionType.REMINDER) {
            reminderAction(model)
        } else if (actionType == ActionType.NOTE) {
            noteAction(model)
        } else if (actionType == ActionType.ACTION) {
            val action = model.action
            if (action == Action.BIRTHDAY) {
                stopView()
                startActivity(Intent(this, AddBirthdayActivity::class.java))
            } else if (action == Action.REMINDER) {
                stopView()
                startActivity(Intent(this, AddReminderActivity::class.java))
            } else if (action == Action.VOLUME) {
                stopView()
                startActivity(Intent(this, VolumeDialog::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
            } else if (action == Action.TRASH) {
                clearTrash()
            } else if (action == Action.DISABLE) {
                disableReminders()
            } else {
                showUnsupportedMessage()
            }
        } else if (actionType == ActionType.GROUP) {
            groupAction(model)
        } else if (actionType == ActionType.ANSWER) {
            performAnswer(model)
        } else if (actionType == ActionType.SHOW) {
            stopView()
            LogUtil.d(TAG, "performResult: " + TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(model.dateTime), true, true))
            val action = model.action
            if (action == Action.REMINDERS) {
                viewModel!!.getReminders(TimeUtil.getDateTimeFromGmt(model.dateTime))
            } else if (action == Action.NOTES) {
                viewModel!!.getNotes()
            } else if (action == Action.GROUPS) {
                showGroups()
            } else if (action == Action.ACTIVE_REMINDERS) {
                viewModel!!.getEnabledReminders(TimeUtil.getDateTimeFromGmt(model.dateTime))
            } else if (action == Action.BIRTHDAYS) {
                viewModel!!.getBirthdays(
                        TimeUtil.getDateTimeFromGmt(model.dateTime),
                        TimeUtil.getBirthdayTime(prefs!!.birthdayTime))
            } else if (action == Action.SHOP_LISTS) {
                viewModel!!.getShoppingReminders()
            } else {
                showUnsupportedMessage()
            }
        } else {
            showUnsupportedMessage()
        }
    }

    private fun showUnsupportedMessage() {
        stopView()
        addResponse(getLocalized(R.string.this_command_not_supported_on_that_screen))
    }

    private fun showShoppingLists(reminders: List<Reminder>?) {
        val items = Container(reminders)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_shopping_lists_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_shopping_list))
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.list.size + " " + getLocalized(R.string.shopping_lists))
            }
            addReminderObject(items.list.removeAt(0))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun showBirthdays(birthdays: List<Birthday>?) {
        val items = Container(birthdays)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_birthdays_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_birthday))
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.list.size + " " + getLocalized(R.string.birthdays))))
            }
            addObjectResponse(Reply(Reply.BIRTHDAY, items.list.removeAt(0)))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun showEnabledReminders(list: List<Reminder>?) {
        val items = Container(list)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_reminders_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_reminder))
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.list.size + " " +
                        getLocalized(R.string.reminders))
            }
            addReminderObject(items.list.removeAt(0))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun showGroups() {
        val items = Container(viewModel!!.allGroups.value)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_groups_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_group))
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.list.size + " " + getLocalized(R.string.groups))))
            }
            addObjectResponse(Reply(Reply.GROUP, items.list.removeAt(0)))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun showNotes(notes: List<Note>?) {
        val items = Container(notes)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_notes_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_note))
            } else {
                addResponse(StringUtils.capitalize(StringUtils.lowerCase(getLocalized(R.string.found) +
                        " " + items.list.size + " " + getLocalized(R.string.notes))))
            }
            addObjectResponse(Reply(Reply.NOTE, items.list.removeAt(0)))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun showActiveReminders(list: List<Reminder>?) {
        val items = Container(list)
        if (items.isEmpty) {
            addResponse(getLocalized(R.string.no_reminders_found))
        } else {
            if (items.list.size == 1) {
                addResponse(getLocalized(R.string.found_one_reminder))
            } else {
                addResponse(getLocalized(R.string.found) + " " + items.list.size + " " +
                        getLocalized(R.string.reminders))
            }
            addReminderObject(items.list.removeAt(0))
            if (!items.isEmpty) addObjectResponse(Reply(Reply.SHOW_MORE, items))
        }
    }

    private fun addReminderObject(reminder: Reminder) {
        if (reminder.viewType == Reminder.REMINDER) {
            addObjectResponse(Reply(Reply.REMINDER, reminder))
        } else {
            addObjectResponse(Reply(Reply.SHOPPING, reminder))
        }
    }

    private fun groupAction(model: Model) {
        stopView()
        addResponse(getLocalized(R.string.group_created))
        val item = viewModel!!.createGroup(model)
        addObjectResponse(Reply(Reply.GROUP, item))
        Handler().postDelayed({ askGroupAction(item) }, 1000)
    }

    private fun noteAction(model: Model) {
        stopView()
        addResponse(getLocalized(R.string.note_created))
        val item = viewModel!!.createNote(model.summary)
        addObjectResponse(Reply(Reply.NOTE, item))
        Handler().postDelayed({ askNoteAction(item) }, 1000)
    }

    private fun reminderAction(model: Model) {
        stopView()
        val reminder = viewModel!!.createReminder(model)
        addObjectResponse(Reply(Reply.REMINDER, reminder))
        if (prefs!!.isTellAboutEvent) {
            addResponse(getLocalized(R.string.reminder_created_on) + " " +
                    TimeUtil.getVoiceDateTime(reminder.eventTime, prefs!!.is24HourFormatEnabled, prefs!!.voiceLocale) +
                    ". " + getLocalized(R.string.would_you_like_to_save_it))
            Handler().postDelayed({ askReminderAction(reminder, false) }, 8000)
        } else {
            addResponse(getLocalized(R.string.reminder_created))
            Handler().postDelayed({ askReminderAction(reminder, true) }, 1000)
        }
    }

    private fun askGroupAction(group: Group) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel!!.saveGroup(group, false)
                addResponse(getLocalized(R.string.group_saved))
                mAskAction = null
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.group_canceled))
                mAskAction = null
            }
        }
        addAskReply()
        Handler().postDelayed({ this.micClick() }, 1500)
    }

    private fun askReminderAction(reminder: Reminder, ask: Boolean) {
        if (ask) addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel!!.saveAndStartReminder(reminder)
                addResponse(getLocalized(R.string.reminder_saved))
                mAskAction = null
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.reminder_canceled))
                mAskAction = null
            }
        }
        addAskReply()
        Handler().postDelayed({ this.micClick() }, 1500)
    }

    private fun askNoteAction(note: Note) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel!!.saveNote(note, false, false)
                addResponse(getLocalized(R.string.note_saved))
                if (prefs!!.isNoteReminderEnabled) {
                    Handler().postDelayed({ askQuickReminder(note) }, 1500)
                } else {
                    mAskAction = null
                }
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.note_canceled))
                mAskAction = null
            }
        }
        addAskReply()
        Handler().postDelayed({ this.micClick() }, 1500)
    }

    private fun askQuickReminder(note: Note) {
        addResponse(getLocalized(R.string.would_you_like_to_add_reminder))
        mAskAction = object : AskAction {
            override fun onYes() {
                val model = viewModel!!.findSuggestion(note.summary)
                addResponse(getLocalized(R.string.reminder_saved))
                if (model != null && model.type == ActionType.REMINDER) {
                    val reminder = viewModel!!.createReminder(model)
                    viewModel!!.saveAndStartReminder(reminder)
                    addObjectResponse(Reply(Reply.REMINDER, reminder))
                } else {
                    val reminder = viewModel!!.saveQuickReminder(note.key, note.summary)
                    addObjectResponse(Reply(Reply.REMINDER, reminder))
                }
                mAskAction = null
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.note_saved_without_reminder))
                mAskAction = null
            }
        }
        addAskReply()
        Handler().postDelayed({ this.micClick() }, 1500)
    }

    private fun addAskReply() {
        if (mAdapter != null && mAskAction != null)
            mAdapter.addReply(Reply(Reply.ASK, createAsk(mAskAction!!)))
    }

    private fun addResponse(message: String) {
        mAdapter?.addReply(Reply(Reply.RESPONSE, message))
        playTts(message)
    }

    private fun disableReminders() {
        viewModel!!.disableAllReminders(false)
    }

    private fun clearTrash() {
        viewModel!!.emptyTrash(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation)

        binding!!.recordingView.setOnClickListener { view -> micClick() }
        binding!!.settingsButton.setOnClickListener { v -> showSettingsPopup() }
        binding!!.backButton.setOnClickListener { view -> onBackPressed() }

        initList()

        checkTts()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.TRASH_CLEARED -> {
                        stopView()
                        addResponse(getLocalized(R.string.trash_was_cleared))
                    }
                    Commands.DELETED -> {
                        stopView()
                        addResponse(getLocalized(R.string.all_reminders_were_disabled))
                    }
                }
            }
        })
        viewModel!!.shoppingLists.observe(this, { reminders -> if (reminders != null) showShoppingLists(reminders) })
        viewModel!!.notes.observe(this, { list -> if (list != null) showNotes(list) })
        viewModel!!.activeReminders.observe(this, { list -> if (list != null) showActiveReminders(list) })
        viewModel!!.enabledReminders.observe(this, { list -> if (list != null) showEnabledReminders(list) })
        viewModel!!.birthdays.observe(this, Observer { birthdays -> if (birthdays != null) showBirthdays(birthdays) })
    }

    private fun showSettingsPopup() {
        val popupMenu = PopupMenu(this, binding!!.settingsButton)
        popupMenu.inflate(R.menu.activity_conversation)
        popupMenu.menu.getItem(1).isChecked = prefs!!.isTellAboutEvent
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_locale -> {
                    showLanguageDialog()
                    return@popupMenu.setOnMenuItemClickListener true
                }
                R.id.action_tell -> {
                    prefs!!.isTellAboutEvent = !prefs!!.isTellAboutEvent
                    return@popupMenu.setOnMenuItemClickListener true
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showLanguageDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.language))
        val locales = Language.getLanguages(this)
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_single_choice, locales)
        val language = prefs!!.voiceLocale
        builder.setSingleChoiceItems(adapter, language) { dialog, which ->
            if (which != -1) {
                prefs!!.voiceLocale = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            dialog.dismiss()
            recreate()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun playTts(text: String) {
        if (!isTtsReady || tts == null) return
        if (Module.isLollipop) {
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private fun checkTts() {
        val checkTTSIntent = Intent()
        checkTTSIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(checkTTSIntent, CHECK_CODE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun initList() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        binding!!.conversationList.layoutManager = layoutManager
        mAdapter.setInsertListener(mInsertCallback)
        binding!!.conversationList.adapter = mAdapter
    }

    private fun initRecognizer() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Language.getLanguage(prefs!!.voiceLocale))
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech!!.setRecognitionListener(mRecognitionListener)
        speech!!.startListening(recognizerIntent)
    }

    private fun micClick() {
        if (binding!!.recordingView.isWorking) {
            if (speech != null) speech!!.stopListening()
            stopView()
            return
        }
        if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
            Permissions.requestPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO)
            return
        }
        binding!!.recordingView.start()
        initRecognizer()
    }

    private fun releaseTts() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
            tts = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseSpeech()
        releaseTts()
    }

    private fun releaseSpeech() {
        try {
            if (speech != null) {
                speech!!.stopListening()
                speech!!.cancel()
                speech!!.destroy()
                speech = null
            }
        } catch (ignored: IllegalArgumentException) {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = TextToSpeech(this, mTextToSpeechListener)
            } else {
                showInstallTtsDialog()
            }
        }
    }

    private fun showInstallTtsDialog() {
        val builder = Dialogues.getDialog(this)
        builder.setMessage(R.string.would_you_like_to_install_tts)
        builder.setPositiveButton(R.string.install) { dialogInterface, i ->
            dialogInterface.dismiss()
            installTts()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        builder.create().show()
    }

    private fun installTts() {
        val installTTSIntent = Intent()
        installTTSIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
        try {
            startActivity(installTTSIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            AUDIO_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                micClick()
            }
        }
    }

    private fun createAsk(askAction: AskAction): AskAction {
        return object : AskAction {
            override fun onYes() {
                stopView()
                askAction.onYes()
            }

            override fun onNo() {
                stopView()
                askAction.onNo()
            }
        }
    }

    companion object {

        private val TAG = "ConversationActivity"
        private val AUDIO_CODE = 255000
        private val CHECK_CODE = 1651
    }
}