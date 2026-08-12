package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.dayview.GetDayEventItemsUseCase
import com.elementary.tasks.calendar.dayview.GetDayHolidayUseCase
import com.elementary.tasks.calendar.dayview.WeekViewViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.github.naz013.logic.reminder.usecase.AddReminderToHistoryUseCase
import com.elementary.tasks.calendar.history.GetHistoryByDayUseCase
import com.elementary.tasks.calendar.monthview.CalendarViewModel
import com.elementary.tasks.calendar.monthview.LoadMonthEventsUseCase
import com.elementary.tasks.calendar.monthview.LoadMonthHolidaysUseCase
import com.elementary.tasks.calendar.monthview.monthgrid.MonthGridFactory
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.github.naz013.logic.reminder.work.CalculateReminderOccurrencesTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val calendarModule =
  module {
    factory { WeekFactory(get(), get(), get()) }
    factory { WeekHeaderController(get()) }

    viewModel { (dateMillis: Long) ->
      WeekViewViewModel(dateMillis, get(), get(), get(), get(), get(), get(), get(), get(), get())
    }

    factory { GetDayEventItemsUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { GetDayHolidayUseCase(get(), get()) }

    factory { MonthGridFactory(get()) }
    factory { LoadMonthEventsUseCase(get(), get(), get(), get(), get()) }
    factory { LoadMonthHolidaysUseCase(get(), get()) }
    viewModelOf(::CalendarViewModel)

    factory { MigrateExistingEventOccurrencesUseCase(get(), get(), get()) }

    factory<BackgroundTask>(named(CalculateReminderOccurrencesTask.TASK_KEY)) { CalculateReminderOccurrencesTask(get()) }

    factory { GetOccurrencesByDateRangeUseCase(get()) }
    factory { GetOccurrencesByDayUseCase(get()) }

    factory { AddReminderToHistoryUseCase(get(), get(), get()) }
    factory { GetHistoryByDayUseCase(get()) }
  }
