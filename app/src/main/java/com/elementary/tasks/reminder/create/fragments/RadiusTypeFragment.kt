package com.elementary.tasks.reminder.create.fragments

import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.SuperUtil

abstract class RadiusTypeFragment<B : ViewDataBinding> : TypeFragment<B>() {

    override fun getSummary(): String {
        val reminder = iFace.state.reminder

        var summary = ""

        val groupName = reminder.groupTitle
        if (groupName != "") {
            summary += "$groupName, "
        }

        summary += ReminderUtils.getPriorityTitle(context!!, reminder.priority) + ", "

        return summary
    }

    protected abstract fun recreateMarker()

    override fun prepare(): Reminder? {
        if (!SuperUtil.checkLocationEnable(context!!)) {
            SuperUtil.showLocationAlert(context!!, iFace)
            return null
        }
        return iFace.state.reminder
    }
}
