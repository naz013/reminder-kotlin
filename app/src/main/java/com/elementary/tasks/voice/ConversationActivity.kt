package com.elementary.tasks.voice

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.backdoor.engine.Model
import com.backdoor.engine.misc.Action
import com.backdoor.engine.misc.ActionType
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VolumeDialog
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.TtsLauncher
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.transparent
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ActivityConversationBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.settings.other.SendFeedbackActivity
import org.apache.commons.lang3.StringUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Locale

class ConversationActivity : BindingActivity<ActivityConversationBinding>() {

  private val permissionFlow = PermissionFlow(this, dialogues)
  private var speech: SpeechRecognizer? = null
  private val conversationAdapter = ConversationAdapter(currentStateHolder)
  private val viewModel by viewModel<ConversationViewModel>()
  private var tts: TextToSpeech? = null
  private var isTtsReady = false
  private var isListening = false
  private var isRotated = false
  private var mAskAction: AskAction? = null
  private val handler = Handler(Looper.getMainLooper())
  private var mItemSelected: Int = 0
  private val ttsLauncher = TtsLauncher(this) {
    if (it) {
      tts = TextToSpeech(this, mTextToSpeechListener)
    } else {
      showInstallTtsDialog()
    }
  }

  private val mTextToSpeechListener = TextToSpeech.OnInitListener { status ->
    if (status == TextToSpeech.SUCCESS && tts != null) {
      val result = tts?.setLanguage(Locale(language.getLanguage(prefs.voiceLocale)))
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Timber.d("This Language is not supported")
      } else {
        isTtsReady = true
        if (!isRotated || conversationAdapter.itemCount == 0) {
          addResponse(getLocalized(R.string.voice_hi_how_can_i_help_you))
          delayMicClickIfNeeded()
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
      if (!list.isNullOrEmpty()) {
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

  override fun inflateBinding() = ActivityConversationBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isRotated = savedInstanceState != null
    binding.micButton.setOnClickListener { tryToActivateMic() }
    binding.recordingView.setOnClickListener {
      stopView()
      viewModel.removePartial()
    }
    binding.settingsButton.setOnClickListener { showSettingsPopup() }
    initList()
    ttsLauncher.checkTts()
    initViewModel()
  }

  override fun onResume() {
    super.onResume()
    if (!Module.hasMicrophone(this)) {
      finish()
    }
  }

  private fun initViewModel() {
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.TRASH_CLEARED -> {
          stopView()
          addResponse(getLocalized(R.string.voice_trash_was_cleared))
        }

        Commands.DELETED -> {
          stopView()
          addResponse(getLocalized(R.string.voice_all_reminders_were_disabled))
        }

        else -> {
        }
      }
    }
    viewModel.shoppingLists.nonNullObserve(this) { showShoppingLists(it) }
    viewModel.notes.nonNullObserve(this) { showNotes(it) }
    viewModel.activeReminders.nonNullObserve(this) { showActiveReminders(it) }
    viewModel.enabledReminders.nonNullObserve(this) { showEnabledReminders(it) }
    viewModel.birthdays.observe(this) { showBirthdays(it) }
    viewModel.replies.nonNullObserve(this) {
      conversationAdapter.submitList(it)
      binding.conversationList.scrollToPosition(0)
      Timber.d("initViewModel: $it")
    }
  }

  private fun delayAction(action: () -> Unit, time: Long = 1500) {
    handler.postDelayed({ action.invoke() }, time)
  }

  private fun showSilentMessage() {
    stopView()
    playTts(getLocalized(R.string.voice_did_you_say_something))
  }

  private fun getLocalized(id: Int): String {
    return language.getConversationLocalizedText(this, id)
  }

  private fun parseResults(list: List<String>?) {
    Timber.d("parseResults: $list")
    if (list.isNullOrEmpty()) {
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
      addResponse(getLocalized(R.string.voice_can_not_recognize_your_command))
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
    when (model.type) {
      ActionType.REMINDER -> reminderAction(model)
      ActionType.NOTE -> noteAction(model)
      ActionType.ACTION -> {
        when (model.action) {
          Action.BIRTHDAY -> {
            stopView()
            PinLoginActivity.openLogged(this, AddBirthdayActivity::class.java)
          }

          Action.REMINDER -> {
            stopView()
            PinLoginActivity.openLogged(this, CreateReminderActivity::class.java)
          }

          Action.VOLUME -> {
            stopView()
            startActivity(
              Intent(this, VolumeDialog::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            )
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
        Timber.d("performResult: gmt ${model.dateTime}")
        when (model.action) {
          Action.REMINDERS -> viewModel.getReminders(model.dateTime)
          Action.NOTES -> viewModel.getNotes()
          Action.GROUPS -> showGroups()
          Action.ACTIVE_REMINDERS -> viewModel.getEnabledReminders(model.dateTime)
          Action.BIRTHDAYS -> viewModel.getBirthdays(model.dateTime)
          Action.SHOP_LISTS -> viewModel.getShoppingReminders()
          else -> showUnsupportedMessage()
        }
      }

      else -> showUnsupportedMessage()
    }
  }

  private fun showUnsupportedMessage() {
    stopView()
    addResponse(getLocalized(R.string.voice_this_command_not_supported_on_that_screen))
  }

  private fun showShoppingLists(reminders: List<UiReminderList>?) {
    val items = Container(reminders)
    if (items.isEmpty) {
      addResponse(getLocalized(R.string.voice_no_shopping_lists_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_shopping_list))
      } else {
        addResponse(
          getLocalized(R.string.voice_found) + " " + items.list.size + " " + getLocalized(
            R.string.voice_shopping_lists
          )
        )
      }
      addReminderObject(items.list.removeAt(0))
      if (!items.isEmpty) {
        addMoreAction()
        addObjectResponse(Reply(Reply.SHOW_MORE, items))
      }
    }
  }

  private fun addMoreAction() {
    conversationAdapter.showMore = {
      viewModel.addMoreItemsToList(it)
    }
  }

  private fun showBirthdays(birthdays: List<UiBirthdayList>?) {
    val items = Container(birthdays)
    if (items.isEmpty) {
      addResponse(getLocalized(R.string.voice_no_birthdays_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_birthday))
      } else {
        addResponse(
          StringUtils.capitalize(
            StringUtils.lowerCase(
              getLocalized(R.string.voice_found) +
                " " + items.list.size + " " + getLocalized(R.string.voice_birthdays)
            )
          )
        )
      }
      addObjectResponse(Reply(Reply.BIRTHDAY, items.list.removeAt(0)))
      if (!items.isEmpty) {
        addMoreAction()
        addObjectResponse(Reply(Reply.SHOW_MORE, items))
      }
    }
  }

  private fun showEnabledReminders(list: List<UiReminderList>?) {
    val items = Container(list)
    if (items.isEmpty) {
      addResponse(getLocalized(R.string.voice_no_reminders_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_reminder))
      } else {
        addResponse(
          getLocalized(R.string.voice_found) + " " + items.list.size + " " +
            getLocalized(R.string.voice_reminders)
        )
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
      addResponse(getLocalized(R.string.voice_no_groups_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_group))
      } else {
        addResponse(
          StringUtils.capitalize(
            StringUtils.lowerCase(
              getLocalized(R.string.voice_found) +
                " " + items.list.size + " " + getLocalized(R.string.voice_groups)
            )
          )
        )
      }
      addObjectResponse(Reply(Reply.GROUP, items.list.removeAt(0)))
      if (!items.isEmpty) {
        addMoreAction()
        addObjectResponse(Reply(Reply.SHOW_MORE, items))
      }
    }
  }

  private fun showNotes(notes: List<UiNoteList>) {
    val items = Container(notes)
    if (items.isEmpty) {
      addResponse(getLocalized(R.string.voice_no_notes_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_note))
      } else {
        addResponse(
          StringUtils.capitalize(
            StringUtils.lowerCase(
              getLocalized(R.string.voice_found) +
                " " + items.list.size + " " + getLocalized(R.string.voice_notes)
            )
          )
        )
      }
      addObjectResponse(Reply(Reply.NOTE, items.list.removeAt(0)))
      if (!items.isEmpty) {
        addMoreAction()
        addObjectResponse(Reply(Reply.SHOW_MORE, items))
      }
    }
  }

  private fun showActiveReminders(list: List<UiReminderList>?) {
    val items = Container(list)
    if (items.isEmpty) {
      addResponse(getLocalized(R.string.voice_no_reminders_found))
    } else {
      if (items.list.size == 1) {
        addResponse(getLocalized(R.string.voice_found_one_reminder))
      } else {
        addResponse(
          getLocalized(R.string.voice_found) + " " + items.list.size + " " +
            getLocalized(R.string.voice_reminders)
        )
      }
      addReminderObject(items.list.removeAt(0))
      if (!items.isEmpty) {
        addMoreAction()
        addObjectResponse(Reply(Reply.SHOW_MORE, items))
      }
    }
  }

  private fun addReminderObject(reminder: UiReminderList) {
    when (reminder) {
      is UiReminderListActiveShop, is UiReminderListRemovedShop -> {
        addObjectResponse(Reply(Reply.SHOPPING, reminder))
      }

      else -> {
        addObjectResponse(Reply(Reply.REMINDER, reminder))
      }
    }
  }

  private fun groupAction(model: Model) {
    stopView()
    addResponse(getLocalized(R.string.voice_group_created))
    val item = viewModel.createGroup(model)
    addObjectResponse(Reply(Reply.GROUP, item))
    delayAction({ askGroupAction(item) }, 1000)
  }

  private fun noteAction(model: Model) {
    stopView()
    addResponse(getLocalized(R.string.voice_note_created))
    val item = viewModel.createNote(model.summary)
    addObjectResponse(Reply(Reply.NOTE, item))
    delayAction({ askNoteAction(item) }, 1000)
  }

  private fun reminderAction(model: Model) {
    stopView()
    val reminder = viewModel.createReminder(model) ?: return
    addObjectResponse(Reply(Reply.REMINDER, viewModel.toReminderListItem(reminder)))
    if (viewModel.tellAboutEvent) {
      addResponse(
        getLocalized(R.string.voice_reminder_created_on) + " " +
          viewModel.getDateTimeText(reminder.eventTime) +
          ". " + getLocalized(R.string.voice_would_you_like_to_save_it)
      )
      delayAction({ askReminderAction(reminder, false) }, 7000)
    } else {
      addResponse(getLocalized(R.string.voice_reminder_created))
      delayAction({ askReminderAction(reminder, true) }, 1000)
    }
  }

  private fun askGroupAction(reminderGroup: ReminderGroup) {
    addResponse(getLocalized(R.string.voice_would_you_like_to_save_it))
    mAskAction = object : AskAction {
      override fun onYes() {
        viewModel.saveGroup(reminderGroup, false)
        addResponse(getLocalized(R.string.voice_group_saved))
        mAskAction = null
      }

      override fun onNo() {
        addResponse(getLocalized(R.string.voice_group_canceled))
        mAskAction = null
      }
    }
    addAskReply()
    delayMicClickIfNeeded(1000)
  }

  private fun askReminderAction(reminder: Reminder, ask: Boolean) {
    if (ask) addResponse(getLocalized(R.string.voice_would_you_like_to_save_it))
    mAskAction = object : AskAction {
      override fun onYes() {
        viewModel.saveAndStartReminder(reminder)
        addResponse(getLocalized(R.string.voice_reminder_saved))
        mAskAction = null
      }

      override fun onNo() {
        addResponse(getLocalized(R.string.voice_reminder_canceled))
        mAskAction = null
      }
    }
    addAskReply()
    delayMicClickIfNeeded(1000)
  }

  private fun askNoteAction(note: Note) {
    addResponse(getLocalized(R.string.voice_would_you_like_to_save_it))
    mAskAction = object : AskAction {
      override fun onYes() {
        viewModel.saveNote(note, showToast = false, addQuickNote = false)
        addResponse(getLocalized(R.string.voice_note_saved))
        if (prefs.isNoteReminderEnabled) {
          delayAction({ askQuickReminder(note) }, 1000)
        } else {
          mAskAction = null
        }
      }

      override fun onNo() {
        addResponse(getLocalized(R.string.voice_note_canceled))
        mAskAction = null
      }
    }
    addAskReply()
    delayMicClickIfNeeded(1000)
  }

  private fun askQuickReminder(note: Note) {
    addResponse(getLocalized(R.string.voice_would_you_like_to_add_reminder))
    mAskAction = object : AskAction {
      override fun onYes() {
        val model = viewModel.findSuggestion(note.summary)
        addResponse(getLocalized(R.string.voice_reminder_saved))
        if (model != null && model.type == ActionType.REMINDER) {
          val reminder = viewModel.createReminder(model) ?: return
          viewModel.saveAndStartReminder(reminder)
          addObjectResponse(Reply(Reply.REMINDER, viewModel.toReminderListItem(reminder)))
        } else {
          val reminder = viewModel.saveQuickReminder(note.key, note.summary)
          addObjectResponse(Reply(Reply.REMINDER, viewModel.toReminderListItem(reminder)))
        }
        mAskAction = null
      }

      override fun onNo() {
        addResponse(getLocalized(R.string.voice_note_saved_without_reminder))
        mAskAction = null
      }
    }
    addAskReply()
    delayMicClickIfNeeded(1000)
  }

  private fun addAskReply() {
    mAskAction?.also { viewModel.addReply(Reply(Reply.ASK, createAsk(it))) }
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
    popupMenu.menu.getItem(0).title = getLocalized(R.string.voice_language)
    popupMenu.menu.getItem(1).title = getLocalized(R.string.voice_tell_about_event)
    popupMenu.menu.getItem(2).title = getLocalized(R.string.voice_auto_mic)
    popupMenu.menu.getItem(3).title = getLocalized(R.string.voice_feedback)
    popupMenu.menu.getItem(4).title = getLocalized(R.string.voice_help)
    popupMenu.menu.getItem(1).isChecked = viewModel.tellAboutEvent
    popupMenu.menu.getItem(2).isChecked = viewModel.autoMicClick
    popupMenu.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.action_locale -> {
          showLanguageDialog()
          return@setOnMenuItemClickListener true
        }

        R.id.action_tell -> {
          viewModel.toggleTellAboutEvent()
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

        R.id.action_auto_mic -> {
          viewModel.toggleAutoMic()
          return@setOnMenuItemClickListener true
        }
      }
      false
    }
    popupMenu.show()
  }

  private fun showLanguageDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(getLocalized(R.string.voice_language))
    val locales = language.getLanguages(language.getConversationLocalizedContext())
    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, locales)
    mItemSelected = prefs.voiceLocale
    builder.setSingleChoiceItems(adapter, mItemSelected) { _, which ->
      mItemSelected = which
    }
    builder.setPositiveButton(getLocalized(R.string.voice_ok)) { dialog, _ ->
      prefs.voiceLocale = mItemSelected
      dialog.dismiss()
      viewModel.clearConversation()
      recreate()
    }
    builder.setNegativeButton(getLocalized(R.string.voice_cancel)) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun playTts(text: String) {
    if (!isTtsReady) return
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
  }

  private fun initList() {
    val layoutManager = LinearLayoutManager(this)
    layoutManager.reverseLayout = true
    binding.conversationList.layoutManager = layoutManager
    binding.conversationList.adapter = conversationAdapter
  }

  private fun initRecognizer() {
    try {
      val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
      recognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        language.getLanguage(prefs.voiceLocale)
      )
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
      recognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
      )
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      speech = SpeechRecognizer.createSpeechRecognizer(this)
      speech?.setRecognitionListener(mRecognitionListener)
      speech?.startListening(recognizerIntent)
      isListening = true
    } catch (e: Throwable) {
      speech = null
      isListening = false
      stopView()
    }
  }

  private fun isSpeaking(): Boolean {
    return tts?.isSpeaking ?: false
  }

  private fun delayMicClickIfNeeded(delay: Long = 2000) {
    if (viewModel.autoMicClick && Module.hasMicrophone(this)) {
      delayAction(::tryToActivateMic, delay)
    }
  }

  private fun tryToActivateMic() {
    if (isSpeaking()) {
      delayAction(::tryToActivateMic, 1000)
    } else {
      permissionFlow.askPermission(Permissions.RECORD_AUDIO) {
        micClick()
      }
    }
  }

  private fun micClick() {
    if (!Permissions.checkPermission(this, Permissions.RECORD_AUDIO)) {
      return
    }
    if (isListening) {
      speech?.stopListening()
      stopView()
      return
    }
    binding.recordingView.visible()
    binding.micButton.transparent()
    initRecognizer()
  }

  private fun releaseTts() {
    tts?.stop()
    tts?.shutdown()
    tts = null
  }

  override fun onDestroy() {
    super.onDestroy()
    releaseSpeech()
    releaseTts()
  }

  private fun releaseSpeech() {
    runCatching {
      speech?.let {
        it.stopListening()
        it.cancel()
        it.destroy()
      }
    }
    speech = null
    isListening = false
  }

  private fun showInstallTtsDialog() {
    val builder = dialogues.getMaterialDialog(this)
    builder.setMessage(R.string.would_you_like_to_install_tts)
    builder.setPositiveButton(getLocalized(R.string.voice_install)) { dialogInterface, _ ->
      dialogInterface.dismiss()
      installTts()
    }
    builder.setNegativeButton(getLocalized(R.string.voice_cancel)) {
        dialogInterface, _ -> dialogInterface.dismiss()
    }
    builder.create().show()
  }

  private fun installTts() {
    val installTTSIntent = Intent()
    installTTSIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
    runCatching { startActivity(installTTSIntent) }
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
}
