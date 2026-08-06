package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.dayview.GetDayEventItemsUseCase
import com.elementary.tasks.calendar.dayview.WeekViewViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.calendar.history.AddReminderToHistoryUseCase
import com.elementary.tasks.calendar.history.GetHistoryByDayUseCase
import com.elementary.tasks.calendar.monthview.CalendarViewModel
import com.elementary.tasks.calendar.monthview.LoadMonthEventsUseCase
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridFactory
import com.elementary.tasks.calendar.occurrence.CalculateBirthdayOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.CalculateReminderOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesTask
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val calendarModule =
  module {
    factory { WeekFactory(get(), get(), get()) }
    factory { WeekHeaderController(get()) }

    viewModel { (dateMillis: Long) -> WeekViewViewModel(dateMillis, get(), get(), get(), get(), get(), get(), get(), get()) }

    factory { GetDayEventItemsUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }

    factory { MonthGridFactory(get()) }
    factory { LoadMonthEventsUseCase(get(), get(), get(), get(), get()) }
    viewModelOf(::CalendarViewModel)

    factory { CalculateBirthdayOccurrencesUseCase(get(), get(), get(), get()) }
    factory { MigrateExistingEventOccurrencesUseCase(get(), get(), get()) }
    factory { CalculateReminderOccurrencesUseCase(get(), get(), get(), get(), get(), get()) }

    factory<BackgroundTask>(named(CalculateBirthdayOccurrencesTask.TASK_KEY)) { CalculateBirthdayOccurrencesTask(get()) }
    factory<BackgroundTask>(named(CalculateReminderOccurrencesTask.TASK_KEY)) { CalculateReminderOccurrencesTask(get()) }

    factory { GetOccurrencesByDateRangeUseCase(get()) }
    factory { GetOccurrencesByDayUseCase(get()) }

    factory { AddReminderToHistoryUseCase(get(), get(), get()) }
    factory { GetHistoryByDayUseCase(get()) }
  }
