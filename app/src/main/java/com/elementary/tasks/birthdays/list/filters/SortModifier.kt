package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class SortModifier(modifier: Modifier<Birthday>? = null,
                   callback: ((List<Birthday>) -> Unit)? = null) : Modifier<Birthday>(modifier, callback), KoinComponent {

    private val prefs: Prefs by inject()

    override fun apply(data: List<Birthday>): List<Birthday> {
        return sort(super.apply(data))
    }

    private fun sort(data: List<Birthday>): List<Birthday> {
        val birthTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)
        return data.asSequence().sortedBy { it.getFutureTime(birthTime) }.toList()
    }
}