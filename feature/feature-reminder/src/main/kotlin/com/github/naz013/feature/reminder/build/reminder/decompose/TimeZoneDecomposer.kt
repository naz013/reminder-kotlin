package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.TimeZoneBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.feature.reminder.build.timezone.supportsTimeZone

internal class TimeZoneDecomposer(
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    if (!reminder.recurrence.supportsTimeZone()) return emptyList()
    val zoneId = DateTimeManager.parseFixedZone(reminder.timeZoneId)?.id ?: return emptyList()
    return listOfNotNull(biFactory.createWithValue(BiType.TIME_ZONE, zoneId, TimeZoneBuilderItem::class.java))
  }
}
