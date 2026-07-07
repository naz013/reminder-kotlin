package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.data.DayLiveData
import com.elementary.tasks.calendar.data.MonthLiveData
import com.elementary.tasks.calendar.dayview.WeekViewModel
import com.elementary.tasks.calendar.dayview.day.DayViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.calendar.history.AddReminderToHistoryUseCase
import com.elementary.tasks.calendar.history.GetHistoryByDayUseCase
import com.elementary.tasks.calendar.occurrence.CalculateBirthdayOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.CalculateReminderOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesTask
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.threeten.bp.LocalDate

val calendarModule =
  module {
    factory { WeekFactory(get(), get(), get()) }
    factory { WeekHeaderController(get()) }

    viewModel { (date: LocalDate) -> WeekViewModel(date, get(), get(), get()) }
    viewModel { (date: LocalDate) -> DayViewModel(date, get(), get(), get(), get(), get(), get()) }

    factory { MonthLiveData(get(), get(), get(), get(), get(), get()) }
    factory { DayLiveData(get(), get(), get(), get(), get(), get(), get(), get()) }

    factory { CalculateBirthdayOccurrencesUseCase(get(), get(), get(), get()) }
    factory { MigrateExistingEventOccurrencesUseCase(get(), get(), get(), get()) }
    factory { CalculateReminderOccurrencesUseCase(get(), get(), get(), get(), get(), get()) }

    factory<BackgroundTask>(named(CalculateBirthdayOccurrencesTask.TASK_KEY)) { CalculateBirthdayOccurrencesTask(get()) }
    factory<BackgroundTask>(named(CalculateReminderOccurrencesTask.TASK_KEY)) { CalculateReminderOccurrencesTask(get()) }

    factory { GetOccurrencesByDateRangeUseCase(get()) }
    factory { GetOccurrencesByDayUseCase(get()) }

    factory { AddReminderToHistoryUseCase(get(), get(), get()) }
    factory { GetHistoryByDayUseCase(get()) }
  }
