package com.github.naz013.feature.home

import com.github.naz013.feature.home.scheduleview.GetActiveEventsForTheDayUseCase
import com.github.naz013.feature.home.scheduleview.GetGreetingTextUseCase
import com.github.naz013.feature.home.scheduleview.GetNavigationItemsUseCase
import com.github.naz013.feature.home.scheduleview.GetTimeSectionsUseCase
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.feature.home.scheduleview.WhatsNewManager
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
  factoryOf(::GetActiveEventsForTheDayUseCase)
  factoryOf(::GetTimeSectionsUseCase)
  factoryOf(::GetGreetingTextUseCase)
  factoryOf(::GetNavigationItemsUseCase)
  singleOf(::WhatsNewManager)

  viewModelOf(::ScheduleHomeViewModel)
}
