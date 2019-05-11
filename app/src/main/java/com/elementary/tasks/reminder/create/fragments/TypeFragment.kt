package com.elementary.tasks.reminder.create.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.widget.NestedScrollView
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.BindingFragment
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.views.*
import com.github.florent37.expansionpanel.ExpansionLayout
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class TypeFragment<B : ViewDataBinding> : BindingFragment<B>() {

    lateinit var iFace: ReminderInterface
        private set

    protected val prefs: Prefs by inject()
    protected val dialogues: Dialogues by inject()
    protected val themeUtil: ThemeUtil by inject()

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
        provideViews()
    }

    abstract fun provideViews()

    abstract fun onNewHeader(newHeader: String)

    protected fun setViews(scrollView: NestedScrollView? = null, expansionLayout: ExpansionLayout? = null,
                           ledPickerView: LedPickerView? = null, calendarCheck: AppCompatCheckBox? = null,
                           tasksCheck: AppCompatCheckBox? = null, extraView: TuneExtraView? = null,
                           melodyView: MelodyView? = null, attachmentView: AttachmentView? = null,
                           groupView: GroupView? = null, beforePickerView: BeforePickerView? = null,
                           summaryView: TextInputEditText? = null, repeatView: RepeatView? = null,
                           dateTimeView: DateTimeView? = null, priorityPickerView: PriorityPickerView? = null,
                           windowTypeView: WindowTypeView? = null, repeatLimitView: RepeatLimitView? = null,
                           loudnessPickerView: LoudnessPickerView? = null, actionView: ActionView? = null) {
        this.attachmentView = attachmentView
        this.melodyView = melodyView
        this.groupView = groupView
        this.actionView = actionView

        actionView?.let {
            if (prefs.isTelephonyAllowed) {
                it.show()
                it.setActivity(activity!!)
                it.setContactClickListener(View.OnClickListener { selectContact() })
                it.bindProperty(iFace.state.reminder.target) { number ->
                    iFace.state.reminder.target = number
                    updateActions()
                }
                if (iFace.state.reminder.target != "") {
                    it.setAction(true)
                    if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.CALL)) {
                        it.type = ActionView.TYPE_CALL
                    } else if (Reminder.isKind(iFace.state.reminder.type, Reminder.Kind.SMS)) {
                        it.type = ActionView.TYPE_MESSAGE
                    }
                }
            } else {
                it.hide()
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
            it.bindProperty(iFace.state.reminder.windowType) { type ->
                iFace.state.reminder.windowType = type
            }
        }
        priorityPickerView?.let {
            it.bindProperty(iFace.state.reminder.priority) { priority ->
                iFace.state.reminder.priority = priority
                updateHeader()
            }
        }
        dateTimeView?.let {
            it.bindProperty(iFace.state.reminder.eventTime) { dateTime ->
                iFace.state.reminder.eventTime = dateTime
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
                updateHeader()
            }
        }
        summaryView?.let {
            it.filters = arrayOf(InputFilter.LengthFilter(Configs.MAX_REMINDER_SUMMARY_LENGTH))
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
            ViewUtils.registerDragAndDrop(activity!!, it, true, ThemeUtil.getSecondaryColor(it.context),
                    { clipData ->
                        if (clipData.itemCount > 0) {
                            it.setUri(clipData.getItemAt(0).uri)
                        }
                    }, *ATTACHMENT_TYPES)
            it.bindProperty(iFace.state.reminder.attachmentFile) { path ->
                iFace.state.reminder.attachmentFile = path
            }
        }
        scrollView?.let { view ->
            ViewUtils.listenScrollableView(view) {
                iFace.updateScroll(it)
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
                it.show()
                it.bindProperty(iFace.state.reminder.color) { color ->
                    iFace.state.reminder.color = color
                }
            } else {
                it.hide()
            }
        }
        calendarCheck?.let {
            if (iFace.canExportToCalendar) {
                it.show()
                it.bindProperty(iFace.state.reminder.exportToCalendar) { isChecked ->
                    iFace.state.reminder.exportToCalendar = isChecked
                }
            } else {
                it.hide()
            }
        }
        tasksCheck?.let {
            if (iFace.canExportToTasks) {
                it.show()
                it.bindProperty(iFace.state.reminder.exportToTasks) { isChecked ->
                    iFace.state.reminder.exportToTasks = isChecked
                }
            } else {
                it.hide()
            }
        }
        extraView?.let {
            it.dialogues = dialogues
            it.bindProperty(iFace.state.reminder) { reminder ->
                iFace.state.reminder.copyExtra(reminder)
            }
        }
        updateHeader()
    }

    protected open fun updateActions() {

    }

    open fun getSummary(): String {
        return ""
    }

    open fun onBackPressed(): Boolean {
        return true
    }

    open fun onVoiceAction(text: String) {

    }

    protected fun isTablet(): Boolean = iFace.isTablet()

    private fun showGroup(groupView: GroupView?, reminder: Reminder) {
        if (TextUtils.isEmpty(reminder.groupTitle) || reminder.groupTitle == "null") {
            groupView?.reminderGroup = iFace.defGroup
        } else {
            groupView?.reminderGroup = ReminderGroup().apply {
                this.groupUuId = reminder.groupUuId
                this.groupColor = reminder.groupColor
                this.groupTitle = reminder.groupTitle ?: ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume: ${iFace.state.reminder.groupTitle}, ${iFace.defGroup}")
        if (iFace.state.reminder.groupUuId.isBlank() || TextUtils.isEmpty(iFace.state.reminder.groupTitle)) {
            iFace.defGroup?.let {
                onGroupUpdate(it)
            }
        }
        iFace.setFragment(this)
        updateHeader()
    }

    fun onGroupUpdate(reminderGroup: ReminderGroup) {
        try {
            iFace.state.reminder.groupUuId = reminderGroup.groupUuId
            iFace.state.reminder.groupColor = reminderGroup.groupColor
            iFace.state.reminder.groupTitle = reminderGroup.groupTitle
        } catch (e: Exception) {
        }
        if (isResumed) {
            groupView?.reminderGroup = reminderGroup
            updateHeader()
        }
    }

    private fun updateHeader() {
        if (isResumed) onNewHeader(getSummary())
    }

    fun onMelodySelect(path: String) {
        iFace.state.reminder.melodyPath = path
        if (isResumed) {
            melodyView?.file = path
        }
    }

    fun onAttachmentSelect(uri: Uri) {
        attachmentView?.setUri(uri)
    }

    private fun selectContact() {
        if (Permissions.ensurePermissions(activity!!, CONTACTS, Permissions.READ_CONTACTS)) {
            SuperUtil.selectContact(activity!!, Constants.REQUEST_CODE_CONTACTS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            actionView?.number = data?.getStringExtra(Constants.SELECTED_CONTACT_NUMBER) ?: ""
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        actionView?.onRequestPermissionsResult(requestCode, grantResults)
        when (requestCode) {
            CONTACTS -> if (Permissions.isAllGranted(grantResults)) selectContact()
        }
    }

    companion object {
        private const val CONTACTS = 112
        val ATTACHMENT_TYPES = arrayOf(UriUtil.URI_MIME, UriUtil.ANY_MIME)
    }
}
