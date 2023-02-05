package com.elementary.tasks.reminder.create.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.UriUtil
import com.elementary.tasks.core.utils.bindProperty
import com.elementary.tasks.core.utils.copyExtra
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.AttachmentView
import com.elementary.tasks.core.views.BeforePickerView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.core.views.GroupView
import com.elementary.tasks.core.views.HorizontalSelectorView
import com.elementary.tasks.core.views.LedPickerView
import com.elementary.tasks.core.views.LoudnessPickerView
import com.elementary.tasks.core.views.MelodyView
import com.elementary.tasks.core.views.PriorityPickerView
import com.elementary.tasks.core.views.RepeatLimitView
import com.elementary.tasks.core.views.RepeatView
import com.elementary.tasks.core.views.TuneExtraView
import com.elementary.tasks.core.views.WindowTypeView
import com.github.florent37.expansionpanel.ExpansionLayout
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDateTime
import timber.log.Timber

abstract class TypeFragment<B : ViewBinding> : BindingFragment<B>() {

  protected val dateTimeManager by inject<DateTimeManager>()
  protected val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val contactPicker = ContactPicker(this) { actionView?.number = it.phone }

  lateinit var iFace: ReminderInterface
    private set

  protected val prefs by inject<Prefs>()
  protected val themeUtil by inject<ThemeProvider>()
  protected val googleCalendarUtils by inject<GoogleCalendarUtils>()

  private val calendars: List<GoogleCalendarUtils.CalendarItem> by lazy {
    googleCalendarUtils.getCalendarsList()
  }

  private var melodyView: MelodyView? = null
  private var attachmentView: AttachmentView? = null
  private var groupView: GroupView? = null
  private var actionView: ActionView? = null

  abstract fun prepare(): Reminder?

  override fun onAttach(context: Context) {
    super.onAttach(context)
    iFace = context as ReminderInterface
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    getDynamicViews().forEach { initViews(it) }
  }

  protected abstract fun getDynamicViews(): List<View>

  private fun initViews(view: View) {
    when (view) {
      is ActionView -> {
        this.actionView = view
        if (prefs.isTelephonyAllowed) {
          view.visible()
          view.setPermissionHandle(permissionFlow)
          view.setContactClickListener {
            permissionFlow.askPermission(Permissions.READ_CONTACTS) { contactPicker.pickContact() }
          }
          view.bindProperty(iFace.state.reminder.target) { number ->
            iFace.state.reminder.target = number
            updateActions()
          }
          if (iFace.state.reminder.target != "") {
            if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.CALL)) {
              view.actionState = ActionView.ActionState.CALL
            } else if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.SMS)) {
              view.actionState = ActionView.ActionState.SMS
            } else {
              view.actionState = ActionView.ActionState.NO_ACTION
            }
          }
        } else {
          view.gone()
        }
      }
      is AttachmentView -> {
        this.attachmentView = view
        view.onFileSelectListener = { iFace.attachFile() }
        ViewUtils.registerDragAndDrop(requireActivity(),
          view,
          true,
          ThemeProvider.getPrimaryColor(view.context),
          { clipData ->
            if (clipData.itemCount > 0) {
              view.setUri(clipData.getItemAt(0).uri)
            }
          },
          *ATTACHMENT_TYPES
        )
        view.bindProperty(iFace.state.reminder.attachmentFile) { path ->
          iFace.state.reminder.attachmentFile = path
        }
        view.visibleGone(prefs.reminderCreatorParams.isAttachmentPickerEnabled())
      }
      is BeforePickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isBeforePickerEnabled())
        view.bindProperty(iFace.state.reminder.remindBefore) { millis ->
          iFace.state.reminder.remindBefore = millis
        }
      }
      is DateTimeView -> {
        view.setDateTime(iFace.state.reminder.eventTime)
        view.onDateChangeListener = object : DateTimeView.OnDateChangeListener {
          override fun onChanged(dateTime: LocalDateTime) {
            iFace.state.reminder.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
          }
        }
      }
      is ExpansionLayout -> {
        view.isNestedScrollingEnabled = false
        if (iFace.state.isExpanded) {
          view.expand(false)
        } else {
          view.collapse(false)
        }
        view.addListener { _, expanded ->
          iFace.state.isExpanded = expanded
        }
      }
      is GroupView -> {
        this.groupView = view
        view.onGroupSelectListener = { iFace.selectGroup() }
        showGroup(view, iFace.state.reminder)
      }
      is LedPickerView -> {
        if (Module.isPro) {
          view.visibleGone(prefs.reminderCreatorParams.isLedPickerEnabled())
          view.bindProperty(iFace.state.reminder.color) { color ->
            iFace.state.reminder.color = color
          }
        } else {
          view.gone()
        }
      }
      is LoudnessPickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isLoudnessPickerEnabled())
        view.bindProperty(iFace.state.reminder.volume) { loudness ->
          iFace.state.reminder.volume = loudness
        }
      }
      is MelodyView -> {
        view.visibleGone(prefs.reminderCreatorParams.isMelodyPickerEnabled())
        this.melodyView = view
        view.onFileSelectListener = { iFace.selectMelody() }
        view.bindProperty(iFace.state.reminder.melodyPath) { melody ->
          iFace.state.reminder.melodyPath = melody
        }
      }
      is PriorityPickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isPriorityPickerEnabled())
        view.bindProperty(iFace.state.reminder.priority) { priority ->
          iFace.state.reminder.priority = priority
        }
      }
      is RepeatLimitView -> {
        view.visibleGone(prefs.reminderCreatorParams.isRepeatLimitPickerEnabled())
        view.bindProperty(iFace.state.reminder.repeatLimit) { limit ->
          iFace.state.reminder.repeatLimit = limit
        }
      }
      is RepeatView -> {
        view.visibleGone(prefs.reminderCreatorParams.isRepeatPickerEnabled())
        view.bindProperty(iFace.state.reminder.repeatInterval) { millis ->
          iFace.state.reminder.repeatInterval = millis
        }
      }
      is TextInputEditText -> {
        view.filters = arrayOf(InputFilter.LengthFilter(Configs.MAX_REMINDER_SUMMARY_LENGTH))
        view.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
          InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        view.bindProperty(iFace.state.reminder.summary) { summary ->
          iFace.state.reminder.summary = summary.trim()
        }
      }
      is TuneExtraView -> {
        view.visibleGone(prefs.reminderCreatorParams.isTuneExtraPickerEnabled())
        view.dialogues = dialogues
        view.bindProperty(iFace.state.reminder) { reminder ->
          iFace.state.reminder.copyExtra(reminder)
        }
      }
      is WindowTypeView -> {
        view.visibleGone(!Module.is10 && prefs.reminderCreatorParams.isWindowTypePickerEnabled())
        view.bindProperty(iFace.state.reminder.windowType) { type ->
          iFace.state.reminder.windowType = type
        }
      }
    }
  }

  private fun updateVisibility(view: View) {
    when (view) {
      is AttachmentView -> {
        view.visibleGone(prefs.reminderCreatorParams.isAttachmentPickerEnabled())
      }
      is BeforePickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isBeforePickerEnabled())
      }
      is LedPickerView -> {
        if (Module.isPro) {
          view.visibleGone(prefs.reminderCreatorParams.isLedPickerEnabled())
        } else {
          view.gone()
        }
      }
      is LoudnessPickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isLoudnessPickerEnabled())
      }
      is MelodyView -> {
        view.visibleGone(prefs.reminderCreatorParams.isMelodyPickerEnabled())
      }
      is PriorityPickerView -> {
        view.visibleGone(prefs.reminderCreatorParams.isPriorityPickerEnabled())
      }
      is RepeatLimitView -> {
        view.visibleGone(prefs.reminderCreatorParams.isRepeatLimitPickerEnabled())
      }
      is RepeatView -> {
        view.visibleGone(prefs.reminderCreatorParams.isRepeatPickerEnabled())
      }
      is TuneExtraView -> {
        view.visibleGone(prefs.reminderCreatorParams.isTuneExtraPickerEnabled())
      }
      is WindowTypeView -> {
        view.visibleGone(!Module.is10 && prefs.reminderCreatorParams.isWindowTypePickerEnabled())
      }
    }
  }

  protected fun setViews(
    expansionLayout: ExpansionLayout? = null,
    ledPickerView: LedPickerView? = null,
    calendarCheck: AppCompatCheckBox? = null,
    tasksCheck: AppCompatCheckBox? = null,
    extraView: TuneExtraView? = null,
    melodyView: MelodyView? = null,
    attachmentView: AttachmentView? = null,
    groupView: GroupView? = null,
    beforePickerView: BeforePickerView? = null,
    summaryView: TextInputEditText? = null,
    repeatView: RepeatView? = null,
    dateTimeView: DateTimeView? = null,
    priorityPickerView: PriorityPickerView? = null,
    windowTypeView: WindowTypeView? = null,
    repeatLimitView: RepeatLimitView? = null,
    loudnessPickerView: LoudnessPickerView? = null,
    actionView: ActionView? = null,
    calendarPicker: HorizontalSelectorView? = null
  ) {
    this.attachmentView = attachmentView
    this.melodyView = melodyView
    this.groupView = groupView
    this.actionView = actionView

    actionView?.let {
      if (prefs.isTelephonyAllowed) {
        it.visible()
        it.setPermissionHandle(permissionFlow)
        it.setContactClickListener {
          permissionFlow.askPermission(Permissions.READ_CONTACTS) { contactPicker.pickContact() }
        }
        it.bindProperty(iFace.state.reminder.target) { number ->
          iFace.state.reminder.target = number
          updateActions()
        }
        if (iFace.state.reminder.target != "") {
          if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.CALL)) {
            it.actionState = ActionView.ActionState.CALL
          } else if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.SMS)) {
            it.actionState = ActionView.ActionState.SMS
          } else {
            it.actionState = ActionView.ActionState.NO_ACTION
          }
        }
      } else {
        it.gone()
      }
    }
    loudnessPickerView?.let {
      it.bindProperty(iFace.state.reminder.volume) { loudness ->
        iFace.state.reminder.volume = loudness
      }
    }
    repeatLimitView?.let {
      it.bindProperty(iFace.state.reminder.repeatLimit) { limit ->
        iFace.state.reminder.repeatLimit = limit
      }
    }
    windowTypeView?.let {
      if (Module.is10) {
        it.gone()
      } else {
        it.visible()
        it.bindProperty(iFace.state.reminder.windowType) { type ->
          iFace.state.reminder.windowType = type
        }
      }
    }
    priorityPickerView?.let {
      it.bindProperty(iFace.state.reminder.priority) { priority ->
        iFace.state.reminder.priority = priority
      }
    }
    dateTimeView?.let {
      it.setDateTime(iFace.state.reminder.eventTime)
      it.onDateChangeListener = object : DateTimeView.OnDateChangeListener {
        override fun onChanged(dateTime: LocalDateTime) {
          iFace.state.reminder.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
        }
      }
    }
    repeatView?.let {
      it.bindProperty(iFace.state.reminder.repeatInterval) { millis ->
        iFace.state.reminder.repeatInterval = millis
      }
    }
    beforePickerView?.let {
      it.bindProperty(iFace.state.reminder.remindBefore) { millis ->
        iFace.state.reminder.remindBefore = millis
      }
    }
    summaryView?.let {
      it.filters = arrayOf(InputFilter.LengthFilter(Configs.MAX_REMINDER_SUMMARY_LENGTH))
      it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
        InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
      it.bindProperty(iFace.state.reminder.summary) { summary ->
        iFace.state.reminder.summary = summary.trim()
      }
    }
    groupView?.let {
      it.onGroupSelectListener = {
        iFace.selectGroup()
      }
      showGroup(it, iFace.state.reminder)
    }
    melodyView?.let {
      it.onFileSelectListener = {
        iFace.selectMelody()
      }
      it.bindProperty(iFace.state.reminder.melodyPath) { melody ->
        iFace.state.reminder.melodyPath = melody
      }
    }
    attachmentView?.let {
      it.onFileSelectListener = {
        iFace.attachFile()
      }
      ViewUtils.registerDragAndDrop(requireActivity(),
        it,
        true,
        ThemeProvider.getPrimaryColor(it.context),
        { clipData ->
          if (clipData.itemCount > 0) {
            it.setUri(clipData.getItemAt(0).uri)
          }
        },
        *ATTACHMENT_TYPES
      )
      it.bindProperty(iFace.state.reminder.attachmentFile) { path ->
        iFace.state.reminder.attachmentFile = path
      }
    }
    expansionLayout?.let {
      it.isNestedScrollingEnabled = false
      if (iFace.state.isExpanded) {
        it.expand(false)
      } else {
        it.collapse(false)
      }
      it.addListener { _, expanded ->
        iFace.state.isExpanded = expanded
      }
    }
    ledPickerView?.let {
      if (Module.isPro) {
        it.visible()
        it.bindProperty(iFace.state.reminder.color) { color ->
          iFace.state.reminder.color = color
        }
      } else {
        it.gone()
      }
    }
    calendarCheck?.let {
      if (iFace.canExportToCalendar) {
        it.visible()
        it.bindProperty(iFace.state.reminder.exportToCalendar) { isChecked ->
          iFace.state.reminder.exportToCalendar = isChecked
          if (isChecked) {
            calendarPicker?.visible()
          } else {
            calendarPicker?.gone()
          }
        }
      } else {
        it.gone()
        calendarPicker?.gone()
      }
    }
    tasksCheck?.let {
      if (iFace.canExportToTasks) {
        it.visible()
        it.bindProperty(iFace.state.reminder.exportToTasks) { isChecked ->
          iFace.state.reminder.exportToTasks = isChecked
        }
      } else {
        it.gone()
      }
    }
    extraView?.let {
      it.dialogues = dialogues
      it.bindProperty(iFace.state.reminder) { reminder ->
        iFace.state.reminder.copyExtra(reminder)
      }
    }
    if (iFace.canExportToCalendar) {
      calendarPicker?.let {
        it.pickerProvider = {
          calendars.map { calendarItem -> calendarItem.name }
        }
        it.titleProvider = { pointer -> calendars[pointer].name }
        it.dataSize = calendars.size
        it.selectListener = { pointer, _ ->
          iFace.state.reminder.calendarId = calendars[pointer].id
        }
        var index = 0
        for (c in calendars) {
          if (c.id == iFace.state.reminder.calendarId) {
            index = calendars.indexOf(c)
            break
          }
        }
        it.selectItem(index)
      }
      if (calendarCheck?.isChecked == true) {
        calendarPicker?.visible()
      } else {
        calendarPicker?.gone()
      }
    } else {
      calendarPicker?.gone()
    }
  }

  protected open fun updateActions() {

  }

  open fun onBackPressed(): Boolean {
    return true
  }

  open fun onVoiceAction(text: String) {
  }

  protected fun isTablet() = iFace.isTablet()

  private fun showGroup(groupView: GroupView?, reminder: Reminder) {
    if (TextUtils.isEmpty(reminder.groupTitle) || reminder.groupTitle == "null") {
      groupView?.reminderGroup = iFace.defGroup
    } else {
      groupView?.reminderGroup = ReminderGroup(
        groupDateTime = dateTimeManager.getNowGmtDateTime(),
        groupUuId = reminder.groupUuId,
        groupColor = reminder.groupColor,
        groupTitle = reminder.groupTitle ?: "",
        isDefaultGroup = true
      )
    }
  }

  override fun onResume() {
    super.onResume()
    getDynamicViews().forEach { updateVisibility(it) }
    Timber.d("onResume: ${iFace.state.reminder.groupTitle}, ${iFace.defGroup}")
    if (iFace.state.reminder.groupUuId.isBlank() || TextUtils.isEmpty(iFace.state.reminder.groupTitle)) {
      iFace.defGroup?.let {
        onGroupUpdate(it)
      }
    }
    iFace.setFragment(this)
  }

  fun onGroupUpdate(reminderGroup: ReminderGroup) {
    runCatching {
      iFace.state.reminder.groupUuId = reminderGroup.groupUuId
      iFace.state.reminder.groupColor = reminderGroup.groupColor
      iFace.state.reminder.groupTitle = reminderGroup.groupTitle
    }
    if (isResumed) {
      groupView?.reminderGroup = reminderGroup
    }
  }

  fun onMelodySelect(path: String) {
    iFace.state.reminder.melodyPath = path
    melodyView?.file = path
  }

  fun onAttachmentSelect(uri: Uri) {
    attachmentView?.setUri(uri)
  }

  companion object {
    val ATTACHMENT_TYPES = arrayOf(UriUtil.URI_MIME, UriUtil.ANY_MIME)
  }
}
