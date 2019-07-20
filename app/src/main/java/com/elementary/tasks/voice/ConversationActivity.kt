package com.elementary.tasks.voice

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.backdoor.engine.Model
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.databinding.ActivityConversationBinding
import com.elementary.tasks.navigation.settings.other.SendFeedbackActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.*

class ConversationActivity : BindingActivity<ActivityConversationBinding>(R.layout.activity_conversation) {

    private var speech: SpeechRecognizer? = null

    private val mAdapter = ConversationAdapter()
    private lateinit var viewModel: ConversationViewModel
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isListening = false
    private var isRotated = false
    private var mAskAction: AskAction? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mItemSelected: Int = 0

    private val mTextToSpeechListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts?.setLanguage(Locale(language.getLanguage(prefs.voiceLocale)))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.d("This Language is not supported")
            } else {
                isTtsReady = true
                if (!isRotated || mAdapter.itemCount == 0) {
                    addResponse(getLocalized(R.string.hi_how_can_i_help_you))
                    if (Module.hasMicrophone(this)) {
                        postMicClick({ micClick() }, 2000)
                    }
                }
            }
        } else {
            Timber.d("Initialization Failed!")
        }
    }
    private val mRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle) {
            Timber.d("onReadyForSpeech: ")
        }

        override fun onBeginningOfSpeech() {
            Timber.d("onBeginningOfSpeech: ")
        }

        override fun onRmsChanged(f: Float) {
        }

        override fun onBufferReceived(bytes: ByteArray) {
            Timber.d("onBufferReceived: ")
        }

        override fun onEndOfSpeech() {
            Timber.d("onEndOfSpeech: ")
            isListening = false
        }

        override fun onError(i: Int) {
            Timber.d("onError: $i")
            isListening = false
            showSilentMessage()
        }

        override fun onResults(bundle: Bundle?) {
            isListening = false
            if (bundle == null) {
                showSilentMessage()
                return
            }
            parseResults(bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION))
        }

        override fun onPartialResults(bundle: Bundle) {
            val list = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (list != null && list.isNotEmpty()) {
                val text = list[0]
                if (text.isNotBlank()) {
                    viewModel.addReply(Reply(Reply.REPLY, text), true)
                }
            }
        }

        override fun onEvent(i: Int, bundle: Bundle) {
            Timber.d("onEvent: ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRotated = savedInstanceState != null
        binding.micButton.setOnClickListener { micClick() }
        binding.recordingView.setOnClickListener {
            stopView()
            viewModel.removePartial()
        }
        binding.settingsButton.setOnClickListener { showSettingsPopup() }
        initList()
        checkTts()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (!Module.hasMicrophone(this)) {
            finish()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ConversationViewModel::class.java)
        viewModel.result.observe(this, Observer { commands ->
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
                    else -> {
                    }
                }
            }
        })
        viewModel.shoppingLists.observe(this, Observer { reminders -> if (reminders != null) showShoppingLists(reminders) })
        viewModel.notes.observe(this, Observer { list -> if (list != null) showNotes(list) })
        viewModel.activeReminders.observe(this, Observer { list -> if (list != null) showActiveReminders(list) })
        viewModel.enabledReminders.observe(this, Observer { list -> if (list != null) showEnabledReminders(list) })
        viewModel.birthdays.observe(this, Observer { birthdays -> if (birthdays != null) showBirthdays(birthdays) })
        viewModel.replies.observe(this, Observer {
            if (it != null) {
                mAdapter.submitList(it)
                binding.conversationList.scrollToPosition(0)
                Timber.d("initViewModel: $it")
            }
        })
    }

    private fun postMicClick(action: () -> Unit, time: Long = 1500) {
        handler.postDelayed({ action.invoke() }, time)
    }

    private fun showSilentMessage() {
        stopView()
        playTts(getLocalized(R.string.did_you_say_something))
    }

    private fun getLocalized(id: Int): String {
        return language.getLocalized(this, id)
    }

    private fun parseResults(list: List<String>?) {
        Timber.d("parseResults: $list")
        if (list == null || list.isEmpty()) {
            showSilentMessage()
            return
        }
        var model: Model? = null
        for (s in list) {
            model = viewModel.findSuggestion(s)
            if (model != null) {
                break
            }
        }
        if (model != null) {
            performResult(model)
        } else {
            stopView()
            addResponse(getLocalized(R.string.can_not_recognize_your_command))
        }
    }

    private fun performAnswer(answer: Model) {
        stopView()
        if (mAskAction != null) {
            viewModel.removeAsk()
            if (answer.action == Action.YES) {
                mAskAction?.onYes()
            } else if (answer.action == Action.NO) {
                mAskAction?.onNo()
            }
        }
    }

    private fun stopView() {
        releaseSpeech()
        binding.recordingView.visibility = View.GONE
        binding.micButton.visibility = View.VISIBLE
    }

    private fun addObjectResponse(reply: Reply) {
        stopView()
        viewModel.addReply(reply)
    }

    private fun performResult(model: Model) {
        if (mAskAction != null) {
            viewModel.removeAsk()
        }
        Timber.d("performResult: $model")
        val actionType = model.type
        when (actionType) {
            ActionType.REMINDER -> reminderAction(model)
            ActionType.NOTE -> noteAction(model)
            ActionType.ACTION -> {
                val action = model.action
                when (action) {
                    Action.BIRTHDAY -> {
                        stopView()
                        AddBirthdayActivity.openLogged(this)
                    }
                    Action.REMINDER -> {
                        stopView()
                        CreateReminderActivity.openLogged(this)
                    }
                    Action.VOLUME -> {
                        stopView()
                        startActivity(Intent(this, VolumeDialog::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
                    }
                    Action.TRASH -> clearTrash()
                    Action.DISABLE -> disableReminders()
                    else -> showUnsupportedMessage()
                }
            }
            ActionType.GROUP -> groupAction(model)
            ActionType.ANSWER -> performAnswer(model)
            ActionType.SHOW -> {
                stopView()
                Timber.d("performResult: ${TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(model.dateTime), true)}")
                val action = model.action
                when (action) {
                    Action.REMINDERS -> viewModel.getReminders(TimeUtil.getDateTimeFromGmt(model.dateTime))
                    Action.NOTES -> viewModel.getNotes()
                    Action.GROUPS -> showGroups()
                    Action.ACTIVE_REMINDERS -> viewModel.getEnabledReminders(TimeUtil.getDateTimeFromGmt(model.dateTime))
                    Action.BIRTHDAYS -> viewModel.getBirthdays(
                            TimeUtil.getDateTimeFromGmt(model.dateTime),
                            TimeUtil.getBirthdayTime(prefs.birthdayTime))
                    Action.SHOP_LISTS -> viewModel.getShoppingReminders()
                    else -> showUnsupportedMessage()
                }
            }
            else -> showUnsupportedMessage()
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
        }
    }

    private fun addMoreAction() {
        mAdapter.showMore = {
            viewModel.addMoreItemsToList(it)
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
        }
    }

    private fun showGroups() {
        val items = Container(viewModel.groups)
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
        }
    }

    private fun showNotes(notes: List<NoteWithImages>?) {
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
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
            if (!items.isEmpty) {
                addMoreAction()
                addObjectResponse(Reply(Reply.SHOW_MORE, items))
            }
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
        val item = viewModel.createGroup(model)
        addObjectResponse(Reply(Reply.GROUP, item))
        postMicClick({ askGroupAction(item) }, 1000)
    }

    private fun noteAction(model: Model) {
        stopView()
        addResponse(getLocalized(R.string.note_created))
        val item = viewModel.createNote(model.summary)
        addObjectResponse(Reply(Reply.NOTE, item))
        postMicClick({ askNoteAction(item) }, 1000)
    }

    private fun reminderAction(model: Model) {
        stopView()
        val reminder = viewModel.createReminder(model)
        addObjectResponse(Reply(Reply.REMINDER, reminder))
        if (prefs.isTellAboutEvent) {
            addResponse(getLocalized(R.string.reminder_created_on) + " " +
                    TimeUtil.getVoiceDateTime(reminder.eventTime, prefs.is24HourFormat, prefs.voiceLocale, language) +
                    ". " + getLocalized(R.string.would_you_like_to_save_it))
            postMicClick({ askReminderAction(reminder, false) }, 7000)
        } else {
            addResponse(getLocalized(R.string.reminder_created))
            postMicClick({ askReminderAction(reminder, true) }, 1000)
        }
    }

    private fun askGroupAction(reminderGroup: ReminderGroup) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel.saveGroup(reminderGroup, false)
                addResponse(getLocalized(R.string.group_saved))
                mAskAction = null
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.group_canceled))
                mAskAction = null
            }
        }
        addAskReply()
        postMicClick({ this.micClick() }, 1000)
    }

    private fun askReminderAction(reminder: Reminder, ask: Boolean) {
        if (ask) addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel.saveAndStartReminder(reminder)
                addResponse(getLocalized(R.string.reminder_saved))
                mAskAction = null
            }

            override fun onNo() {
                addResponse(getLocalized(R.string.reminder_canceled))
                mAskAction = null
            }
        }
        addAskReply()
        postMicClick({ this.micClick() }, 1000)
    }

    private fun askNoteAction(note: Note) {
        addResponse(getLocalized(R.string.would_you_like_to_save_it))
        mAskAction = object : AskAction {
            override fun onYes() {
                viewModel.saveNote(note, showToast = false, addQuickNote = false)
                addResponse(getLocalized(R.string.note_saved))
                if (prefs.isNoteReminderEnabled) {
                    postMicClick({ askQuickReminder(note) }, 1000)
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
        postMicClick({ this.micClick() }, 1000)
    }

    private fun askQuickReminder(note: Note) {
        addResponse(getLocalized(R.string.would_you_like_to_add_reminder))
        mAskAction = object : AskAction {
            override fun onYes() {
                val model = viewModel.findSuggestion(note.summary)
                addResponse(getLocalized(R.string.reminder_saved))
                if (model != null && model.type == ActionType.REMINDER) {
                    val reminder = viewModel.createReminder(model)
                    viewModel.saveAndStartReminder(reminder)
                    addObjectResponse(Reply(Reply.REMINDER, reminder))
                } else {
                    val reminder = viewModel.saveQuickReminder(note.key, note.summary)
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
        postMicClick({ this.micClick() }, 1000)
    }

    private fun addAskReply() {
        val askAction = mAskAction
        if (askAction != null) {
            viewModel.addReply(Reply(Reply.ASK, createAsk(askAction)))
        }
    }

    private fun addResponse(message: String) {
        playTts(message)
        viewModel.addReply(Reply(Reply.RESPONSE, message))
    }

    private fun disableReminders() {
        viewModel.disableAllReminders(false)
    }

    private fun clearTrash() {
        viewModel.emptyTrash(false)
    }

    private fun showSettingsPopup() {
        val popupMenu = PopupMenu(this, binding.settingsButton)
        popupMenu.inflate(R.menu.activity_conversation)
        popupMenu.menu.getItem(0).title = getLocalized(R.string.language)
        popupMenu.menu.getItem(1).title = getLocalized(R.string.tell_about_event)
        popupMenu.menu.getItem(2).title = getLocalized(R.string.feedback)
        popupMenu.menu.getItem(3).title = getLocalized(R.string.help)
        popupMenu.menu.getItem(1).isChecked = prefs.isTellAboutEvent
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_locale -> {
                    showLanguageDialog()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_tell -> {
                    prefs.isTellAboutEvent = !prefs.isTellAboutEvent
                    return@setOnMenuItemClickListener true
                }
                R.id.action_report -> {
                    startActivity(Intent(this@ConversationActivity, SendFeedbackActivity::class.java))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_help -> {
                    startActivity(Intent(this@ConversationActivity, VoiceHelpActivity::class.java))
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun showLanguageDialog() {
        val builder = dialogues.getMaterialDialog(this)
        builder.setTitle(getString(R.string.language))
        val locales = language.getLanguages(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, locales)
        mItemSelected = prefs.voiceLocale
        builder.setSingleChoiceItems(adapter, mItemSelected) { _, which ->
            mItemSelected = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.voiceLocale = mItemSelected
            dialog.dismiss()
            viewModel.clearConversation()
            recreate()
        }
        builder.setNegativeButton(getLocalized(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun playTts(text: String) {
        if (!isTtsReady || tts == null) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
        binding.conversationList.layoutManager = layoutManager
        binding.conversationList.adapter = mAdapter
    }

    private fun initRecognizer() {
        try {
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.getLanguage(prefs.voiceLocale))
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(mRecognitionListener)
            speech?.startListening(recognizerIntent)
            isListening = true
        } catch (e: SecurityException) {
            speech = null
            isListening = false
        }
    }

    private fun micClick() {
        if (!Permissions.checkPermission(this, AUDIO_CODE, Permissions.RECORD_AUDIO)) {
            return
        }
        if (isListening) {
            speech?.stopListening()
            stopView()
            return
        }
        binding.recordingView.visibility = View.VISIBLE
        binding.micButton.visibility = View.INVISIBLE
        initRecognizer()
    }

    private fun releaseTts() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
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
            speech?.let {
                it.stopListening()
                it.cancel()
                it.destroy()
            }
            speech = null
        } catch (ignored: IllegalArgumentException) {
        }
        isListening = false
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
        val builder = dialogues.getMaterialDialog(this)
        builder.setMessage(R.string.would_you_like_to_install_tts)
        builder.setPositiveButton(R.string.install) { dialogInterface, _ ->
            dialogInterface.dismiss()
            installTts()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_CODE -> if (Permissions.checkPermission(grantResults)) {
                micClick()
            }
        }
    }

    private fun createAsk(askAction: AskAction): AskAction {
        return object : AskAction {
            override fun onYes() {
                stopView()
                viewModel.removeFirst()
                askAction.onYes()
            }

            override fun onNo() {
                stopView()
                viewModel.removeFirst()
                askAction.onNo()
            }
        }
    }

    companion object {
        private const val AUDIO_CODE = 255000
        private const val CHECK_CODE = 1651
    }
}
