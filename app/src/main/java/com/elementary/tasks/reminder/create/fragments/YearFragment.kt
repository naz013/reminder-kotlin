package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.databinding.FragmentReminderYearBinding
import timber.log.Timber
import java.util.*

class YearFragment : RepeatableTypeFragment<FragmentReminderYearBinding>() {

    private val time: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, iFace.state.hour)
            calendar.set(Calendar.MINUTE, iFace.state.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    override fun prepare(): Reminder? {
        val reminder = iFace.state.reminder
        var type = Reminder.BY_DAY_OF_YEAR
        val isAction = binding.actionView.hasAction()
        if (TextUtils.isEmpty(reminder.summary) && !isAction) {
            binding.taskLayout.error = getString(R.string.task_summary_is_empty)
            binding.taskLayout.isErrorEnabled = true
            return null
        }
        var number = ""
        if (isAction) {
            number = binding.actionView.number
            if (TextUtils.isEmpty(number)) {
                iFace.showSnackbar(getString(R.string.you_dont_insert_number))
                return null
            }
            type = if (binding.actionView.type == ActionView.TYPE_CALL) {
                Reminder.BY_DAY_OF_YEAR_CALL
            } else {
                Reminder.BY_DAY_OF_YEAR_SMS
            }
        }
        reminder.weekdays = listOf()
        reminder.target = number
        reminder.type = type
        reminder.dayOfMonth = iFace.state.day
        reminder.monthOfYear = iFace.state.month
        reminder.after = 0L
        reminder.delay = 0
        reminder.eventCount = 0
        reminder.repeatInterval = 0

        reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
        val startTime = TimeCount.getNextYearDayTime(reminder)

        if (reminder.remindBefore > 0 && startTime - reminder.remindBefore < System.currentTimeMillis()) {
            iFace.showSnackbar(getString(R.string.invalid_remind_before_parameter))
            return null
        }

        reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
        reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
        Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
        if (!TimeCount.isCurrent(reminder.eventTime)) {
            iFace.showSnackbar(getString(R.string.reminder_is_outdated))
            return null
        }
        return reminder
    }

    override fun layoutRes(): Int = R.layout.fragment_reminder_year

    override fun provideViews() {
        setViews(
                scrollView = binding.scrollView,
                expansionLayout = binding.moreLayout,
                ledPickerView = binding.ledView,
                calendarCheck = binding.exportToCalendar,
                tasksCheck = binding.exportToTasks,
                extraView = binding.tuneExtraView,
                melodyView = binding.melodyView,
                attachmentView = binding.attachmentView,
                groupView = binding.groupView,
                summaryView = binding.taskSummary,
                beforePickerView = binding.beforeView,
                dateTimeView = binding.dateView,
                loudnessPickerView = binding.loudnessView,
                priorityPickerView = binding.priorityView,
                repeatLimitView = binding.repeatLimitView,
                windowTypeView = binding.windowTypeView,
                actionView = binding.actionView
        )
    }

    override fun onNewHeader(newHeader: String) {
        binding.cardSummary.text = newHeader
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tuneExtraView.hasAutoExtra = false

        binding.dateView.setDateFormat(TimeUtil.simpleDate(prefs.appLanguage))
        binding.dateView.setEventListener(object : DateTimeView.OnSelectListener {
            override fun onDateSelect(mills: Long, day: Int, month: Int, year: Int) {
                iFace.state.day = day
                iFace.state.month = month
                iFace.state.year = year
            }

            override fun onTimeSelect(mills: Long, hour: Int, minute: Int) {
                iFace.state.hour = hour
                iFace.state.minute = minute
            }
        })

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_MONTH, iFace.state.day)
        calendar.set(Calendar.MONTH, iFace.state.month)
        calendar.set(Calendar.YEAR, iFace.state.year)
        calendar.set(Calendar.HOUR_OF_DAY, iFace.state.hour)
        calendar.set(Calendar.MINUTE, iFace.state.minute)
        binding.dateView.dateTime = calendar.timeInMillis
        editReminder()
    }

    override fun updateActions() {
        if (binding.actionView.hasAction()) {
            if (binding.actionView.type == ActionView.TYPE_MESSAGE) {
                binding.tuneExtraView.hasAutoExtra = false
            } else {
                binding.tuneExtraView.hasAutoExtra = true
                binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
            }
        } else {
            binding.tuneExtraView.hasAutoExtra = false
        }
    }

    private fun updateDateTime(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        iFace.state.hour = calendar.get(Calendar.HOUR_OF_DAY)
        iFace.state.minute = calendar.get(Calendar.MINUTE)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
        calendar.set(Calendar.MONTH, reminder.monthOfYear)
        calendar.set(Calendar.HOUR_OF_DAY, iFace.state.hour)
        calendar.set(Calendar.MINUTE, iFace.state.minute)
        binding.dateView.dateTime = calendar.timeInMillis
        iFace.state.day = reminder.dayOfMonth
        iFace.state.month = reminder.monthOfYear
    }

    private fun editReminder() {
        updateDateTime(iFace.state.reminder)
    }
}
