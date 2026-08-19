package com.github.naz013.feature.calendar

import com.github.naz013.feature.calendar.agenda.CalendarAgendaItemFactory
import com.github.naz013.logic.reminder.usecase.AddReminderToHistoryUseCase
import com.github.naz013.feature.calendar.history.GetHistoryByDateRangeUseCase
import com.github.naz013.feature.calendar.monthview.CalendarViewModel
import com.github.naz013.feature.calendar.monthview.LoadMonthEventsUseCase
import com.github.naz013.feature.calendar.monthview.LoadMonthHolidaysUseCase
import com.github.naz013.feature.calendar.monthview.monthgrid.MonthGridFactory
import com.github.naz013.feature.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.github.naz013.feature.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.github.naz013.feature.calendar.timeline.GetRangeEventItemsUseCase
import com.github.naz013.feature.calendar.timeline.GetRangeHolidaysUseCase
import com.github.naz013.feature.calendar.timeline.TimelineViewModel
import com.github.naz013.logic.reminder.work.CalculateReminderOccurrencesTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureCalendarModule =
  module {
    viewModel { (startDateMillis: Long, daySpan: Int) ->
      TimelineViewModel(startDateMillis, daySpan, get(), get(), get(), get(), get())
    }

    factory { CalendarAgendaItemFactory(get(), get(), get()) }

    factory { GetHistoryByDateRangeUseCase(get()) }
    factory { GetRangeEventItemsUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetRangeHolidaysUseCase(get(), get()) }

    factory { MonthGridFactory(get()) }
    factory { LoadMonthEventsUseCase(get(), get(), get(), get(), get()) }
    factory { LoadMonthHolidaysUseCase(get(), get()) }
    viewModelOf(::CalendarViewModel)

    viewModel { (initialDateMillis: Long, forcedMode: CalendarViewMode?) ->
      CalendarHostViewModel(initialDateMillis, forcedMode, get(), get())
    }

    factory { MigrateExistingEventOccurrencesUseCase(get(), get(), get()) }

    factory<BackgroundTask>(named(CalculateReminderOccurrencesTask.TASK_KEY)) { CalculateReminderOccurrencesTask(get()) }

    factory { GetOccurrencesByDateRangeUseCase(get()) }

    factory { AddReminderToHistoryUseCase(get(), get(), get()) }
  }
