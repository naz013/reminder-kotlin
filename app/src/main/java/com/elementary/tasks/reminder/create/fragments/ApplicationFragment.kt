package com.elementary.tasks.reminder.create.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.ApplicationActivity
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.fragment_reminder_application.*
import timber.log.Timber

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
class ApplicationFragment : RepeatableTypeFragment() {

    private var selectedPackage: String? = null
    private val type: Int
        get() = if (application.isChecked) {
            Reminder.BY_DATE_APP
        } else {
            Reminder.BY_DATE_LINK
        }
    private val appName: String
        get() {
            val packageManager = context!!.packageManager
            var applicationInfo: ApplicationInfo? = null
            try {
                applicationInfo = packageManager.getApplicationInfo(selectedPackage, 0)
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
            return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "???") as String
        }

    override fun prepare(): Reminder? {
        val type = type
        var number: String
        val reminder = reminderInterface.reminder
        if (Reminder.isSame(type, Reminder.BY_DATE_APP)) {
            number = selectedPackage ?: ""
            if (TextUtils.isEmpty(number)) {
                reminderInterface.showSnackbar(getString(R.string.you_dont_select_application))
                return null
            }
        } else {
            number = urlField.text.toString().trim()
            if (TextUtils.isEmpty(number) || number.matches(".*https?://".toRegex())) {
                reminderInterface.showSnackbar(getString(R.string.you_dont_insert_link))
                return null
            }
            if (!number.startsWith("http://") && !number.startsWith("https://"))
                number = "http://$number"
        }
        val startTime = dateView.dateTime
        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            reminderInterface.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }
        reminder.target = number
        reminder.type = type
        reminder.startTime = reminder.eventTime
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            reminderInterface.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reminder_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            reminderInterface.updateScroll(it)
        }
        moreLayout?.isNestedScrollingEnabled = false

        if (Module.isPro) {
            ledView.visibility = View.VISIBLE
        } else {
            ledView.visibility = View.GONE
        }

        tuneExtraView.dialogues = dialogues
        tuneExtraView.hasAutoExtra = true
        tuneExtraView.hint = getString(R.string.enable_launching_application_automatically)

        melodyView.onFileSelectListener = {
            reminderInterface.selectMelody()
        }
        attachmentView.onFileSelectListener = {
            reminderInterface.attachFile()
        }

        ViewUtils.registerDragAndDrop(activity!!, attachmentView, true, themeUtil.getSecondaryColor(),
                { clipData ->
                    if (clipData.itemCount > 0) {
                        attachmentView.setUri(clipData.getItemAt(0).uri)
                    }
                }, UriUtil.URI_MIME)
        groupView.onGroupSelectListener = {
            reminderInterface.selectGroup()
        }

        pickApplication.setOnClickListener {
            activity?.startActivityForResult(Intent(activity, ApplicationActivity::class.java), Constants.REQUEST_CODE_APPLICATION)
        }
        initScreenState()
        urlLayout.visibility = View.GONE
        application.setOnCheckedChangeListener { _, b ->
            if (!b) {
                applicationLayout.visibility = View.GONE
                urlLayout.visibility = View.VISIBLE
            } else {
                urlLayout.visibility = View.GONE
                applicationLayout.visibility = View.VISIBLE
            }
        }
        initPropertyFields()
        editReminder()
    }

    private fun initPropertyFields() {
        taskSummary.bindProperty(reminderInterface.reminder.summary) {
            reminderInterface.reminder.summary = it.trim()
        }
        beforeView.bindProperty(reminderInterface.reminder.remindBefore) {
            reminderInterface.reminder.remindBefore = it
            updateHeader()
        }
        repeatView.bindProperty(reminderInterface.reminder.repeatInterval) {
            reminderInterface.reminder.repeatInterval = it
        }
        exportToCalendar.bindProperty(reminderInterface.reminder.exportToCalendar) {
            reminderInterface.reminder.exportToCalendar = it
        }
        exportToTasks.bindProperty(reminderInterface.reminder.exportToTasks) {
            reminderInterface.reminder.exportToTasks = it
        }
        dateView.bindProperty(reminderInterface.reminder.eventTime) {
            reminderInterface.reminder.eventTime = it
        }
        priorityView.bindProperty(reminderInterface.reminder.priority) {
            reminderInterface.reminder.priority = it
            updateHeader()
        }
        melodyView.bindProperty(reminderInterface.reminder.melodyPath) {
            reminderInterface.reminder.melodyPath = it
        }
        attachmentView.bindProperty(reminderInterface.reminder.attachmentFile) {
            reminderInterface.reminder.attachmentFile = it
        }
        loudnessView.bindProperty(reminderInterface.reminder.volume) {
            reminderInterface.reminder.volume = it
        }
        repeatLimitView.bindProperty(reminderInterface.reminder.repeatLimit) {
            reminderInterface.reminder.repeatLimit = it
        }
        windowTypeView.bindProperty(reminderInterface.reminder.windowType) {
            reminderInterface.reminder.windowType = it
        }
        tuneExtraView.bindProperty(reminderInterface.reminder) {
            reminderInterface.reminder.copyExtra(it)
        }
        if (Module.isPro) {
            ledView.bindProperty(reminderInterface.reminder.color) {
                reminderInterface.reminder.color = it
            }
        }
    }

    private fun updateHeader() {
        cardSummary?.text = getSummary()
    }

    private fun initScreenState() {
        if (reminderInterface.canExportToCalendar) {
            exportToCalendar.visibility = View.VISIBLE
        } else {
            exportToCalendar.visibility = View.GONE
        }
        if (reminderInterface.canExportToTasks) {
            exportToTasks.visibility = View.VISIBLE
        } else {
            exportToTasks.visibility = View.GONE
        }
    }

    private fun editReminder() {
        val reminder = reminderInterface.reminder
        showGroup(groupView, reminder)
        if (reminder.target != "") {
            if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
                application.isChecked = true
                selectedPackage = reminder.target
                applicationName.text = appName
            } else {
                browser.isChecked = true
                urlField.setText(reminder.target)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_APPLICATION && resultCode == Activity.RESULT_OK) {
            selectedPackage = data?.getStringExtra(Constants.SELECTED_APPLICATION)
            applicationName.text = appName
        }
    }

    override fun onGroupUpdate(reminderGroup: ReminderGroup) {
        super.onGroupUpdate(reminderGroup)
        groupView?.reminderGroup = reminderGroup
        updateHeader()
    }

    override fun onMelodySelect(path: String) {
        super.onMelodySelect(path)
        melodyView.file = path
    }

    override fun onAttachmentSelect(uri: Uri) {
        super.onAttachmentSelect(uri)
        attachmentView.setUri(uri)
    }
}
