package com.elementary.tasks.calendar

import com.elementary.tasks.calendar.data.CalendarDataEngine
import com.elementary.tasks.calendar.data.CalendarDataEngineBroadcast
import com.elementary.tasks.calendar.data.DayLiveData
import com.elementary.tasks.calendar.data.MonthLiveData
import com.elementary.tasks.calendar.dayview.WeekViewModel
import com.elementary.tasks.calendar.dayview.day.DayViewModel
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.dayview.weekheader.WeekHeaderController
import com.elementary.tasks.calendar.occurrence.CalculateBirthdayOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.CalculateReminderOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDateRangeUseCase
import com.elementary.tasks.calendar.occurrence.MigrateExistingEventOccurrencesUseCase
import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesWorker
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.threeten.bp.LocalDate

val calendarModule = module {
  factory { WeekFactory(get(), get(), get()) }
  factory { WeekHeaderController(get()) }

  viewModel { (date: LocalDate) -> WeekViewModel(date, get(), get(), get(), get()) }
  viewModel { (date: LocalDate) -> DayViewModel(date, get(), get(), get(), get(), get(), get()) }

  single {
    CalendarDataEngine(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  factory { CalendarDataEngineBroadcast(get()) }

  factory { MonthLiveData(get(), get(), get(), get(), get()) }
  factory { DayLiveData(get(), get(), get(), get()) }

  factory { CalculateBirthdayOccurrencesUseCase(get(), get(), get(), get()) }
  factory { MigrateExistingEventOccurrencesUseCase(get(), get(), get()) }
  factory { CalculateReminderOccurrencesUseCase(get(), get(), get(), get(), get(), get()) }

  worker { CalculateBirthdayOccurrencesWorker(get(), get(), get(), get()) }
  worker { CalculateReminderOccurrencesWorker(get(), get(), get(), get()) }

  factory { GetOccurrencesByDateRangeUseCase(get()) }
}
