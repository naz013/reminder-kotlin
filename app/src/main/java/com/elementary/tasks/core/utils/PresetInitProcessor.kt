package com.elementary.tasks.core.utils

import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.repository.RecurPresetRepository
import com.elementary.tasks.core.utils.params.Prefs

class PresetInitProcessor(
  private val recurPresetRepository: RecurPresetRepository,
  private val prefs: Prefs
) {

  fun run() {
    if (prefs.initPresets) {
      prefs.initPresets = false
      createPresets().forEach { savePreset(it) }
    }
  }

  private fun createPresets(): List<RecurPreset> {
    return listOf(
      createPreset("RRULE:FREQ=DAILY;COUNT=10", "Daily, 10 occurrences"),
      createPreset("RRULE:FREQ=DAILY;INTERVAL=2;COUNT=15", "Every other day, 10 occurrences"),
      createPreset("RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5", "Every 10 days, 5 occurrences"),
      createPreset("RRULE:FREQ=YEARLY;COUNT=20;BYMONTH=5;BYDAY=SU,MO,TU,WE,TH,FR,SA", "Every day in May"),
      createPreset("RRULE:FREQ=WEEKLY;COUNT=10", "Weekly for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;COUNT=10;BYDAY=1FR", "Monthly on the first Friday for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;INTERVAL=2;COUNT=10;BYDAY=1SU,-1SU", "Every other month on the first and last Sunday of the month for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;COUNT=6;BYDAY=-2MO", "Monthly on the second-to-last Monday of the month for 6 months"),
      createPreset("RRULE:FREQ=MONTHLY;BYMONTHDAY=-3;COUNT=6", "Monthly on the third-to-the-last day of the month"),
      createPreset("RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=2,15", "Monthly on the 2nd and 15th of the month for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=1,-1", "Monthly on the first and last day of the month for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;INTERVAL=18;COUNT=10;BYMONTHDAY=10,11,12,13,14,15", "Every 18 months on the 10th thru 15th of the month for 10 occurrences"),
      createPreset("RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TU;COUNT=10", "Every Tuesday, every other month"),
      createPreset("RRULE:FREQ=YEARLY;COUNT=10;BYMONTH=6,7", "Yearly in June and July for 10 occurrences"),
      createPreset("RRULE:FREQ=YEARLY;INTERVAL=2;COUNT=10;BYMONTH=1,2,3", "Every other year on January, February, and March for 10 occurrences"),
      createPreset("RRULE:FREQ=YEARLY;BYDAY=20MO;COUNT=10", "Every 20th Monday of the year"),
      createPreset("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=TH;COUNT=10", "Every Thursday in March"),
      createPreset("RRULE:FREQ=YEARLY;BYDAY=TH;BYMONTH=6,7,8;COUNT=10", "Every Thursday, but only during June, July, and August"),
      createPreset("RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13;COUNT=10", "Every Friday the 13th"),
      createPreset("RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13;COUNT=10", "The first Saturday that follows the first Sunday of the month"),
      createPreset("RRULE:FREQ=MINUTELY;INTERVAL=15;COUNT=6", "Every 15 minutes for 6 occurrences"),
      createPreset("RRULE:FREQ=MINUTELY;INTERVAL=90;COUNT=4", "Every hour and a half for 4 occurrences"),
      createPreset("RRULE:FREQ=MINUTELY;INTERVAL=90;COUNT=4", "Every hour and a half for 4 occurrences"),
    )
  }

  private fun createPreset(rule: String, name: String):RecurPreset {
    return RecurPreset(
      name = name,
      recurObject = rule
    )
  }

  private fun savePreset(recurPreset: RecurPreset) {
    recurPresetRepository.save(recurPreset)
  }
}
