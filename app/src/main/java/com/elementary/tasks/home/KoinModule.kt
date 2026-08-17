package com.elementary.tasks.home

import com.elementary.tasks.home.scheduleview.GetActiveEventsForTheDayUseCase
import com.elementary.tasks.home.scheduleview.GetGreetingTextUseCase
import com.elementary.tasks.home.scheduleview.GetNavigationItemsUseCase
import com.elementary.tasks.home.scheduleview.GetTimeSectionsUseCase
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
  factoryOf(::GetActiveEventsForTheDayUseCase)
  factoryOf(::GetTimeSectionsUseCase)
  factoryOf(::GetGreetingTextUseCase)
  factoryOf(::GetNavigationItemsUseCase)

  viewModelOf(::ScheduleHomeViewModel)
}
