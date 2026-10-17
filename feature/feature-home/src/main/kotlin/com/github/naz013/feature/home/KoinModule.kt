package com.github.naz013.feature.home

import com.github.naz013.feature.home.scheduleview.GetActiveEventsForTheDayUseCase
import com.github.naz013.feature.home.scheduleview.GetGreetingTextUseCase
import com.github.naz013.feature.home.scheduleview.GetNavigationItemsUseCase
import com.github.naz013.feature.home.scheduleview.GetTimeSectionsUseCase
import com.github.naz013.feature.home.scheduleview.HasAnyUserContentUseCase
import com.github.naz013.feature.home.scheduleview.ObserveTodayScheduleUseCase
import com.github.naz013.feature.home.scheduleview.ScheduleHomeViewModel
import com.github.naz013.feature.home.scheduleview.TodayItemHomeEventMapper
import com.github.naz013.feature.home.scheduleview.WhatsNewManager
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
  factoryOf(::GetActiveEventsForTheDayUseCase)
  factoryOf(::ObserveTodayScheduleUseCase)
  factoryOf(::TodayItemHomeEventMapper)
  factoryOf(::GetTimeSectionsUseCase)
  factoryOf(::GetGreetingTextUseCase)
  factoryOf(::GetNavigationItemsUseCase)
  factoryOf(::HasAnyUserContentUseCase)
  singleOf(::WhatsNewManager)

  viewModelOf(::ScheduleHomeViewModel)
}
