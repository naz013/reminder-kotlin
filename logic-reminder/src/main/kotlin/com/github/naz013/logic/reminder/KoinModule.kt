package com.github.naz013.logic.reminder

import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.occurrence.CalculateReminderOccurrencesUseCase
import com.github.naz013.logic.reminder.occurrence.ReminderOccurrenceCalculatorFactoryV2
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteRelatedGoogleTaskUseCase
import com.github.naz013.logic.reminder.usecase.DeactivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteAllReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveOneTimeReminderUseCaseImpl
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.logic.reminder.usecase.StartLocationTrackingUseCase
import com.github.naz013.logic.reminder.usecase.StopLocationTrackingUseCase
import com.github.naz013.logic.reminder.usecase.UpdatePermanentReminderNotificationUseCase
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleCalendarUseCase
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleTasksUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val logicReminderModule = module {
  factoryOf(::ScheduleReminderUploadUseCase)
  factoryOf(::BehaviorStrategyResolverV2)
  factoryOf(::ReminderOccurrenceCalculatorFactoryV2)
  factoryOf(::CalculateReminderOccurrencesUseCase)

  factoryOf(::ActivateReminderUseCase)
  factoryOf(::SaveReminderUseCase)
  factoryOf(::CompleteRelatedGoogleTaskUseCase)

  factoryOf(::StopLocationTrackingUseCase)
  factoryOf(::StartLocationTrackingUseCase)

  factoryOf(::UpdatePermanentReminderNotificationUseCase)

  factoryOf(::PauseReminderUseCase)
  factoryOf(::DeactivateReminderUseCase)

  factoryOf(::DeleteReminderUseCase)
  factoryOf(::DeleteAllReminderUseCase)

  factoryOf(::SaveReminderToGoogleTasksUseCase)
  factoryOf(::SaveReminderToGoogleCalendarUseCase)

  factory { SaveOneTimeReminderUseCaseImpl(get(), get(), get()) as SaveOneTimeReminderUseCase }
}
