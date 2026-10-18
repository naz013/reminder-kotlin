package com.github.naz013.feature.habit

import com.github.naz013.feature.habit.list.HabitListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val habitModule = module {
  viewModelOf(::HabitListViewModel)
}
