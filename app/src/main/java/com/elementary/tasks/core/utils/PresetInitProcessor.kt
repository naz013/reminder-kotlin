package com.elementary.tasks.core.utils

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.build.preset.BuilderPresetsGenerateUseCase
import com.elementary.tasks.reminder.build.preset.DefaultPresetsGenerateUseCase
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.icalendar.TagType
import com.github.naz013.repository.RecurPresetRepository
import org.threeten.bp.LocalDateTime

class PresetInitProcessor(
  private val recurPresetRepository: RecurPresetRepository,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val builderPresetsGenerateUseCase: BuilderPresetsGenerateUseCase,
  private val builderDefaultPresetsGenerateUseCase: DefaultPresetsGenerateUseCase
) {

  suspend fun run() {
    if (prefs.initPresets) {
      prefs.initPresets = false
      setBuilderSettings()
      createRecurPresets().forEach { savePreset(it) }
      builderPresetsGenerateUseCase().forEach { savePreset(it) }
    }
    if (prefs.initDefaultPresets) {
      prefs.initDefaultPresets = false
      builderDefaultPresetsGenerateUseCase().forEach { savePreset(it) }
    }
  }

  private fun setBuilderSettings() {
    val versionCode = packageManagerWrapper.getVersionCode()
    val prefsVersionCode = prefs.lastVersionCode
    if (prefsVersionCode < versionCode) {
      // Updated app
      prefs.reminderCreatorParams.apply {
        setICalendarEnabled(true)
        setPhoneCallEnabled(true)
        setSendSmsEnabled(true)
        setOpenAppEnabled(true)
        setOpenLinkEnabled(true)
        setSendEmailEnabled(true)
      }.also {
        prefs.reminderCreatorParams = it
      }
    }
  }

  private fun createRecurPresets(): List<RecurPreset> {
    return listOf(
      createRecurPreset(
        rule = "RRULE:FREQ=DAILY;COUNT=10",
        name = textProvider.getText(R.string.recur_daily_10_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 1
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=DAILY;INTERVAL=2;COUNT=10",
        name = textProvider.getText(R.string.recur_every_other_day_10_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 2
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=DAILY;INTERVAL=10;COUNT=5",
        name = textProvider.getText(R.string.recur_every_10_days_5_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 3
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;COUNT=20;BYMONTH=5;BYDAY=SU,MO,TU,WE,TH,FR,SA",
        name = textProvider.getText(R.string.recur_every_day_in_may),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 4
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=WEEKLY;COUNT=10",
        name = textProvider.getText(R.string.recur_weekly_for_10_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 5
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYDAY=1FR",
        name = textProvider.getText(R.string.recur_monthly_on_the_first_friday_for_10_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 6
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=2;COUNT=10;BYDAY=1SU,-1SU",
        name = textProvider.getText(
          R.string.recur_every_other_month_on_the_first_and_last_sun_for_10_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 7
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=6;BYDAY=-2MO",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_second_to_last_monday_of_the_month_for_6_months
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 8
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;BYMONTHDAY=-3;COUNT=6",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_third_to_the_last_day_of_the_month
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 9
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=2,15",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_2nd_and_15th_of_the_month_for_10_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 10
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=10;BYMONTHDAY=1,-1",
        name = textProvider.getText(
          R.string.recur_monthly_on_the_first_and_last_day_of_the_month_for_10_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 11
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=18;COUNT=10;BYMONTHDAY=10,11,12,13,14,15",
        name = textProvider.getText(
          R.string.recur_every_18_months_on_the_10th_thru_15th_of_the_month_for_10_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 12
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;INTERVAL=2;BYDAY=TU;COUNT=10",
        name = textProvider.getText(R.string.recur_every_tuesday_every_other_month),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 13
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;COUNT=10;BYMONTH=6,7",
        name = textProvider.getText(R.string.recur_yearly_in_june_and_july_for_10_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 14
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;INTERVAL=2;COUNT=10;BYMONTH=1,2,3",
        name = textProvider.getText(
          R.string.recur_every_other_year_on_january_february_and_march_for_10_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 15
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;BYDAY=20MO;COUNT=10",
        name = textProvider.getText(R.string.recur_every_20th_monday_of_the_year),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 16
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=TH;COUNT=10",
        name = textProvider.getText(R.string.recur_every_thursday_in_march),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 17
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=YEARLY;BYDAY=TH;BYMONTH=6,7,8;COUNT=10",
        name = textProvider.getText(
          R.string.recur_every_thursday_but_only_during_june_july_and_august
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 18
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=FR;BYMONTHDAY=13;COUNT=10",
        name = textProvider.getText(R.string.recur_every_friday_the_13th),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 19
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=SA;BYMONTHDAY=7,8,9,10,11,12,13;COUNT=10",
        name = textProvider.getText(
          R.string.recur_the_first_saturday_that_follows_the_first_sunday_of_the_month
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 20
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MINUTELY;INTERVAL=15;COUNT=6",
        name = textProvider.getText(R.string.recur_every_15_minutes_for_6_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 21
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MINUTELY;INTERVAL=90;COUNT=4",
        name = textProvider.getText(R.string.recur_every_hour_and_a_half_for_4_occurrences),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 22
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;COUNT=3;BYDAY=TU,WE,TH;BYSETPOS=3",
        name = textProvider.getText(
          R.string.recur_the_third_instance_into_the_month_for_the_next_3_months
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 23
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-2",
        name = textProvider.getText(R.string.recur_the_second_to_last_weekday_of_the_month),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 24
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=WEEKLY;COUNT=10;WKST=SU;BYDAY=TU,TH",
        name = textProvider.getText(R.string.recur_weekly_on_tuesday_and_thursday_for_five_weeks),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 25
      ),
      createRecurPreset(
        rule = "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=8;WKST=SU;BYDAY=TU,TH",
        name = textProvider.getText(
          R.string.recur_every_other_week_on_tuesday_and_thursday_for_8_occurrences
        ),
        itemsToAddAtRuntime = createParams(TagType.DTSTART.value),
        index = 26
      )
    )
  }

  private fun createRecurPreset(
    rule: String,
    name: String,
    itemsToAddAtRuntime: String?,
    index: Int
  ): RecurPreset {
    return RecurPreset(
      id = "default_recur_$index",
      name = name,
      recurObject = rule,
      type = PresetType.RECUR,
      createdAt = LocalDateTime.now(),
      useCount = 0,
      description = null,
      isDefault = true,
      recurItemsToAdd = itemsToAddAtRuntime,
      syncState = SyncState.WaitingForUpload,
      version = 1
    )
  }

  private fun createParams(vararg params: String): String {
    return params.joinToString(";")
  }

  private suspend fun savePreset(recurPreset: RecurPreset) {
    recurPresetRepository.save(recurPreset)
  }
}
