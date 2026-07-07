package com.github.naz013.work

import com.github.naz013.workapi.WorkScheduler
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workModule =
  module {
    single<WorkScheduler> { WorkSchedulerImpl(get()) }

    worker { GenericTaskWorker(get(), get(), get()) }
  }
