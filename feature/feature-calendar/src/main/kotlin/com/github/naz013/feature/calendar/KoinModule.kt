package com.github.naz013.feature.calendar

import com.github.naz013.feature.calendar.dayview.GetDayEventItemsUseCase
import com.github.naz013.feature.calendar.dayview.GetDayHolidayUseCase
import com.github.naz013.feature.calendar.dayview.WeekViewViewModel
import com.github.naz013.feature.calendar.dayview.weekheader.WeekFactory
import com.github.naz013.feature.calendar.dayview.weekheader.WeekHeaderController
import com.github.naz013.logic.reminder.usecase.AddReminderToHistoryUseCase
import com.github.naz013.feature.calendar.history.GetHistoryByDayUseCase
import com.github.naz013.feature.calendar.monthview.CalendarViewModel
import com.github.naz013.feature.calendar.monthview.LoadMonthEventsUseCase
import com.github.naz013.feature.calendar.monthview.LoadMonthHolidaysUseCase
import com.github.naz013.feature.calendar.monthview.monthgrid.MonthGridFactory
import com.github.naz013.feature.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.github.naz013.feature.calendar.occurrence.GetOccurrencesByDayUseCase
import com.github.naz013.feature.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.github.naz013.logic.reminder.work.CalculateReminderOccurrencesTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureCalendarModule =
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
