package com.elementary.tasks.core.utils

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.repository.RecurPresetRepository
import com.elementary.tasks.core.utils.params.Prefs

class PresetInitProcessor(
  private val recurPresetRepository: RecurPresetRepository,
  private val prefs: Prefs,
  private val textProvider: TextProvider
) {

  fun run() {
    if (prefs.initPresets) {
      prefs.initPresets = false
      createPresets().forEach { savePreset(it) }
    }
  }

  private fun createPresets(): List<RecurPreset> {
    return listOf(
      createPreset(
        rule = "RRULE:FREQ=DAILY;COUNT=10",
        name = textProvider.getText(R.string.recur_daily_10_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=15",
        name = textProvider.getText(R.string.recur_every_other_day_10_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
        name = textProvider.getText(R.string.recur_every_10_days_5_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;COUNT=20;BYMONTH=5;BYDAY=SU,MO,TU,WE,TH,FR,SA",
        name = textProvider.getText(R.string.recur_every_day_in_may)
      ),
      createPreset(
        rule = "RRULE:FREQ=WEEKLY;COUNT=10",
        name = textProvider.getText(R.string.recur_weekly_for_10_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYDAY=1FR",
        name = textProvider.getText(R.string.recur_monthly_on_the_first_friday_for_10_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=2;COUNT=10;BYDAY=1SU,-1SU",
        name = textProvider.getText(
          R.string.recur_every_other_month_on_the_first_and_last_sun_for_10_occurrences
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=6;BYDAY=-2MO",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_second_to_last_monday_of_the_month_for_6_months
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;BYMONTHDAY=-3;COUNT=6",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_third_to_the_last_day_of_the_month
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=2,15",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_2nd_and_15th_of_the_month_for_10_occurrences
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=1,-1",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_first_and_last_day_of_the_month_for_10_occurrences
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=18;COUNT=10;BYMONTHDAY=10,11,12,13,14,15",
        name = textProvider.getText(
          R.string.recur_every_18_months_on_the_10th_thru_15th_of_the_month_for_10_occurrences
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TU;COUNT=10",
        name = textProvider.getText(R.string.recur_every_tuesday_every_other_month)
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;COUNT=10;BYMONTH=6,7",
        name = textProvider.getText(R.string.recur_yearly_in_june_and_july_for_10_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;INTERVAL=2;COUNT=10;BYMONTH=1,2,3",
        name = textProvider.getText(
          R.string.recur_every_other_year_on_january_february_and_march_for_10_occurrences
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;BYDAY=20MO;COUNT=10",
        name = textProvider.getText(R.string.recur_every_20th_monday_of_the_year)
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=TH;COUNT=10",
        name = textProvider.getText(R.string.recur_every_thursday_in_march)
      ),
      createPreset(
        rule = "RRULE:FREQ=YEARLY;BYDAY=TH;BYMONTH=6,7,8;COUNT=10",
        name = textProvider.getText(
          R.string.recur_every_thursday_but_only_during_june_july_and_august
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13;COUNT=10",
        name = textProvider.getText(R.string.recur_every_friday_the_13th)
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13;COUNT=10",
        name = textProvider.getText(
          R.string.recur_the_first_saturday_that_follows_the_first_sunday_of_the_month
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MINUTELY;INTERVAL=15;COUNT=6",
        name = textProvider.getText(R.string.recur_every_15_minutes_for_6_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=MINUTELY;INTERVAL=90;COUNT=4",
        name = textProvider.getText(R.string.recur_every_hour_and_a_half_for_4_occurrences)
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=3;BYDAY=TU,WE,TH;BYSETPOS=3",
        name = textProvider.getText(
          R.string.recur_the_third_instance_into_the_month_for_the_next_3_months
        )
      ),
      createPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-2",
        name = textProvider.getText(R.string.recur_the_second_to_last_weekday_of_the_month)
      ),
      createPreset(
        rule = "RRULE:FREQ=WEEKLY;COUNT=10;WKST=SU;BYDAY=TU,TH",
        name = textProvider.getText(R.string.recur_weekly_on_tuesday_and_thursday_for_five_weeks)
      ),
      createPreset(
        rule = "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=8;WKST=SU;BYDAY=TU,TH",
        name = textProvider.getText(
          R.string.recur_every_other_week_on_tuesday_and_thursday_for_8_occurrences
        )
      )
    )
  }

  private fun createPreset(rule: String, name: String): RecurPreset {
    return RecurPreset(
      name = name,
      recurObject = rule
    )
  }

  private fun savePreset(recurPreset: RecurPreset) {
    recurPresetRepository.save(recurPreset)
  }
}
