package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class SortModifier(modifier: Modifier<Birthday>? = null,
                   callback: ((List<Birthday>) -> Unit)? = null) : Modifier<Birthday>(modifier, callback), KoinComponent {

    private val prefs: Prefs by inject()

    override fun apply(data: List<Birthday>): List<Birthday> {
        return sort(super.apply(data))
    }

    private fun sort(data: List<Birthday>): List<Birthday> {
        val birthTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)
        data.sortedWith(Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                when {
                    o2 != null -> return@Comparator 1
                    o1 != null -> return@Comparator -1
                    else -> return@Comparator 0
                }
            } else {
                val t1 = TimeUtil.getFutureBirthdayDate(birthTime, o1.date)
                val t2 = TimeUtil.getFutureBirthdayDate(birthTime, o2.date)
                if (t1 == null || t2 == null) {
                    when {
                        t2 != null -> return@Comparator 1
                        t1 != null -> return@Comparator -1
                        else -> return@Comparator 0
                    }
                } else {
                    val diff1 = TimeCount.getDiffDays(t1.millis)
                    val diff2 = TimeCount.getDiffDays(t2.millis)
                    Timber.d("sortBirthdays: $diff1, $diff2")
                    when {
                        diff1 > diff2 -> return@Comparator 1
                        diff2 > diff1 -> return@Comparator -1
                        else -> return@Comparator 0
                    }
                }
            }
        })
        return data
    }
}