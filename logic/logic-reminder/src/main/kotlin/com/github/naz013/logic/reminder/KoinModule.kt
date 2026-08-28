package com.github.naz013.logic.reminder

import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.occurrence.CalculateReminderOccurrencesUseCase
import com.github.naz013.logic.reminder.occurrence.ReminderOccurrenceCalculatorFactoryV2
import com.github.naz013.logic.reminder.query.GetActiveRemindersV2ByGroupIdUseCase
import com.github.naz013.logic.reminder.query.ResolveReminderV2NotificationSettingsUseCase
import com.github.naz013.logic.reminder.scheduling.EventDateTimeCalculatorV2
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteRelatedGoogleTaskUseCase
import com.github.naz013.logic.reminder.usecase.DeactivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteAllReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.reminder.usecase.ResumeReminderUseCase
import com.github.naz013.logic.reminder.usecase.SaveOneTimeReminderUseCaseImpl
import com.github.naz013.logic.reminder.usecase.SaveReminderUseCase
import com.github.naz013.logic.reminder.usecase.SkipReminderUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCaseImpl
import com.github.naz013.logic.reminder.usecase.StartLocationTrackingUseCase
import com.github.naz013.logic.reminder.usecase.StopLocationTrackingUseCase
import com.github.naz013.logic.reminder.usecase.SyncReminderToCloudUseCase
import com.github.naz013.logic.reminder.usecase.TogglePinnedReminderUseCase
import com.github.naz013.logic.reminder.usecase.ToggleReminderStateUseCase
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

  factoryOf(::GetActiveRemindersV2ByGroupIdUseCase)
  factoryOf(::ResolveReminderV2NotificationSettingsUseCase)

  factoryOf(::ActivateReminderUseCase)
  factory { SaveReminderUseCase(get(), get(), get(), lazy { get() }) }
  factoryOf(::CompleteRelatedGoogleTaskUseCase)

  factoryOf(::StopLocationTrackingUseCase)
  factoryOf(::StartLocationTrackingUseCase)

  factoryOf(::UpdatePermanentReminderNotificationUseCase)

  factoryOf(::PauseReminderUseCase)
  factoryOf(::DeactivateReminderUseCase)
  factoryOf(::MoveReminderToArchiveUseCase)

  factoryOf(::DeleteReminderUseCase)
  factoryOf(::DeleteAllReminderUseCase)

  factoryOf(::SaveReminderToGoogleTasksUseCase)
  factoryOf(::SaveReminderToGoogleCalendarUseCase)

  factory { SaveOneTimeReminderUseCaseImpl(get(), get(), get()) as SaveOneTimeReminderUseCase }

  factoryOf(::ResumeReminderUseCase)
  factoryOf(::CompleteReminderUseCase)
  factoryOf(::SkipReminderUseCase)
  factoryOf(::ToggleReminderStateUseCase)
  factoryOf(::TogglePinnedReminderUseCase)
  factoryOf(::SyncReminderToCloudUseCase)
  factory { EventDateTimeCalculatorV2(get(), get()) }

  factory { SnoozeReminderUseCaseImpl(get(), get(), get(), get(), get(), lazy { get() }) as SnoozeReminderUseCase }
}
