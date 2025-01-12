package com.github.naz013.usecase.googletasks

import org.koin.dsl.module

val googleTasksUseCaseModule = module {
  factory { GetAllGoogleTaskListsUseCase(get()) }

  factory { GetAllGoogleTasksUseCase(get()) }
  factory { GetGoogleTaskByIdUseCase(get()) }
}
