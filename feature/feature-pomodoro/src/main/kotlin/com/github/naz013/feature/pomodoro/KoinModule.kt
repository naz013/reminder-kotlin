package com.github.naz013.feature.pomodoro

import com.github.naz013.feature.pomodoro.compose.PomodoroTimerViewModel
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngineImpl
import com.github.naz013.feature.pomodoro.usecase.RecordPomodoroSessionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featurePomodoroModule = module {
  single<PomodoroTimerEngine> { PomodoroTimerEngineImpl(get(), get(), get(), get()) }
  factoryOf(::RecordPomodoroSessionUseCase)
  viewModel { (linkedReminderId: String?) ->
    PomodoroTimerViewModel(
      initialLinkedReminderId = linkedReminderId,
      pomodoroTimerEngine = get(),
      reminderV2Repository = get(),
      dispatcherProvider = get(),
    )
  }
}
