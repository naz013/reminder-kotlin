package com.github.naz013.logic.schedule

import com.github.naz013.logic.schedule.impl.DeleteTask
import com.github.naz013.logic.schedule.impl.ForceUploadTask
import com.github.naz013.logic.schedule.impl.GetWorkerTagUseCase
import com.github.naz013.logic.schedule.impl.ScheduleBackgroundWorkUseCaseImpl
import com.github.naz013.logic.schedule.impl.SyncTask
import com.github.naz013.logic.schedule.impl.UploadTask
import com.github.naz013.workapi.BackgroundTask
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val logicScheduleModule = module {
  factoryOf(::GetWorkerTagUseCase)
  factory { ScheduleBackgroundWorkUseCaseImpl(get(), get(), get(), get()) as ScheduleBackgroundWorkUseCase }

  factory<BackgroundTask>(named(DeleteTask.TASK_KEY)) { DeleteTask(get()) }
  factory<BackgroundTask>(named(ForceUploadTask.TASK_KEY)) { ForceUploadTask(get()) }
  factory<BackgroundTask>(named(SyncTask.TASK_KEY)) { SyncTask(get()) }
  factory<BackgroundTask>(named(UploadTask.TASK_KEY)) { UploadTask(get()) }
}
