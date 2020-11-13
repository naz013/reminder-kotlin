package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil

class SortModifier(
  modifier: Modifier<Birthday>? = null,
  callback: ((List<Birthday>) -> Unit)? = null,
  private val prefs: Prefs
) : Modifier<Birthday>(modifier, callback) {

  override fun apply(data: List<Birthday>): List<Birthday> {
    return sort(super.apply(data))
  }

  private fun sort(data: List<Birthday>): List<Birthday> {
    val birthTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)
    data.forEach { it.calculateTime(birthTime) }
    return data.sortedBy { it.calculatedTime }.toList()
  }
}