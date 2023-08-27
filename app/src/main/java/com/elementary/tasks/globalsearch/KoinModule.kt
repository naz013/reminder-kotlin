package com.elementary.tasks.globalsearch

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
  factory { SearchLiveData(get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { GlobalSearchViewModel(get(), get(), get(), get()) }
}
