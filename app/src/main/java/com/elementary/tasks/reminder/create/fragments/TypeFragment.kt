package com.elementary.tasks.reminder.create.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
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
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.AttachmentView
import com.elementary.tasks.core.views.BeforePickerView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.core.views.ExportToCalendarView
import com.elementary.tasks.core.views.ExportToGoogleTasksView
import com.elementary.tasks.core.views.GroupView
import com.elementary.tasks.core.views.LedPickerView
import com.elementary.tasks.core.views.PriorityPickerView
import com.elementary.tasks.core.views.RepeatLimitView
import com.elementary.tasks.core.views.RepeatView
import com.elementary.tasks.core.views.TuneExtraView
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.github.naz013.logging.Logger
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDateTime

@Deprecated("Replaced by new Builder")
abstract class TypeFragment<B : ViewBinding> : BindingFragment<B>() {

  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  protected val dateTimeManager by inject<DateTimeManager>()
  protected val dateTimePickerProvider by inject<DateTimePickerProvider>()
  private val contactPicker = ContactPicker(this) { actionView?.number = it.phone }

  lateinit var iFace: ReminderInterface
    private set

  protected val prefs by inject<Prefs>()
  protected val themeUtil by inject<ThemeProvider>()
  protected val googleCalendarUtils by inject<GoogleCalendarUtils>()
  protected val explanationVisibility by inject<ReminderExplanationVisibility>()

  private val calendars: List<GoogleCalendarUtils.CalendarItem> by lazy {
    googleCalendarUtils.getCalendarsList()
  }

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
    getExplanationView().visibleGone(
      explanationVisibility.shouldShowExplanation(getExplanationVisibilityType())
    )
    getLegacyMessageView().run {
      visibleGone(prefs.showLegacyBuilderWarning)
      onTryClicked = {
        reminderBuilderLauncher.toggleBuilder(requireActivity())
      }
      onCloseClicked = {
        prefs.showLegacyBuilderWarning = false
        gone()
      }
    }
    setCloseListenerToExplanationView(getExplanationVisibilityHideClickListener())
  }

  protected abstract fun getLegacyMessageView(): ClosableLegacyBuilderWarningView

  protected abstract fun getDynamicViews(): List<View>

  protected abstract fun getExplanationView(): View

  protected abstract fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type

  protected abstract fun setCloseListenerToExplanationView(listener: View.OnClickListener)

  private fun getExplanationVisibilityHideClickListener(): View.OnClickListener {
    return View.OnClickListener {
      explanationVisibility.explanationShowed(getExplanationVisibilityType())
      getExplanationView().visibleGone(
        explanationVisibility.shouldShowExplanation(getExplanationVisibilityType())
      )
    }
  }

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
        ViewUtils.registerDragAndDrop(
          requireActivity(),
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
        view.addOnDateChangeListener(
          object : DateTimeView.OnDateChangeListener {
            override fun onChanged(dateTime: LocalDateTime) {
              iFace.state.reminder.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
            }
          }
        )
      }

      is ExportToCalendarView -> {
        view.visibleGone(
          iFace.canExportToCalendar && prefs.reminderCreatorParams.isCalendarPickerEnabled()
        )
        view.bindProperty(
          iFace.state.reminder.exportToCalendar,
          iFace.state.reminder.calendarId
        ) { isChecked, calendarId ->
          iFace.state.reminder.exportToCalendar = isChecked
          iFace.state.reminder.calendarId = calendarId
        }
      }

      is ExportToGoogleTasksView -> {
        view.visibleGone(
          iFace.canExportToTasks &&
            prefs.reminderCreatorParams.isGoogleTasksPickerEnabled()
        )
        view.bindProperty(
          iFace.state.reminder.exportToTasks,
          iFace.state.reminder.taskListId
        ) { isChecked, listId ->
          iFace.state.reminder.exportToTasks = isChecked
          iFace.state.reminder.taskListId = listId
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

      is ExportToCalendarView -> {
        view.visibleGone(
          iFace.canExportToCalendar &&
            prefs.reminderCreatorParams.isCalendarPickerEnabled()
        )
      }

      is ExportToGoogleTasksView -> {
        view.visibleGone(
          iFace.canExportToTasks &&
            prefs.reminderCreatorParams.isGoogleTasksPickerEnabled()
        )
      }

      is LedPickerView -> {
        if (Module.isPro) {
          view.visibleGone(prefs.reminderCreatorParams.isLedPickerEnabled())
        } else {
          view.gone()
        }
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
    Logger.d("onResume: ${iFace.state.reminder.groupTitle}, ${iFace.defGroup}")
    if (
      iFace.state.reminder.groupUuId.isBlank() ||
      TextUtils.isEmpty(iFace.state.reminder.groupTitle)
    ) {
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
  }

  fun onAttachmentSelect(uri: Uri) {
    attachmentView?.setUri(uri)
  }

  companion object {
    val ATTACHMENT_TYPES = arrayOf(UriUtil.URI_MIME, UriUtil.ANY_MIME)
  }
}
