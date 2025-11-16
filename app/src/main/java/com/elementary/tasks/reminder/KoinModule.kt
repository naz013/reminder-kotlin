package com.elementary.tasks.reminder

import android.os.Bundle
import com.elementary.tasks.reminder.actions.GetReminderActionsUseCase
import com.elementary.tasks.reminder.build.BuildReminderViewModel
import com.elementary.tasks.reminder.build.adapter.BiErrorForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiTypeForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiValueForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BuilderErrorToTextAdapter
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFactoryICal
import com.elementary.tasks.reminder.build.bi.BiFilter
import com.elementary.tasks.reminder.build.bi.CreatorConfigFilter
import com.elementary.tasks.reminder.build.bi.LocationFilter
import com.elementary.tasks.reminder.build.formatter.factory.PlaceFormatterFactory
import com.elementary.tasks.reminder.build.formatter.factory.RadiusFormatterFactory
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.elementary.tasks.reminder.build.logic.BuilderItemBlockedByConstraintCalculator
import com.elementary.tasks.reminder.build.logic.BuilderItemMandatoryIfConstraintCalculator
import com.elementary.tasks.reminder.build.logic.BuilderItemPermissionConstraintCalculator
import com.elementary.tasks.reminder.build.logic.BuilderItemRequiresAllConstraintCalculator
import com.elementary.tasks.reminder.build.logic.BuilderItemRequiresAnyConstraintCalculator
import com.elementary.tasks.reminder.build.logic.BuilderItemsHolder
import com.elementary.tasks.reminder.build.logic.BuilderItemsLogic
import com.elementary.tasks.reminder.build.logic.UiBuilderItemsAdapter
import com.elementary.tasks.reminder.build.logic.UiSelectorItemsAdapter
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderErrorFinder
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderStateCalculator
import com.elementary.tasks.reminder.build.logic.builderstate.ReminderPredictionCalculator
import com.elementary.tasks.reminder.build.preset.BiValueToBuilderSchemeValue
import com.elementary.tasks.reminder.build.preset.BuilderItemsToBuilderPresetAdapter
import com.elementary.tasks.reminder.build.preset.BuilderPresetToBiAdapter
import com.elementary.tasks.reminder.build.preset.BuilderPresetsGenerateUseCase
import com.elementary.tasks.reminder.build.preset.DefaultPresetsGenerateUseCase
import com.elementary.tasks.reminder.build.preset.ManagePresetsViewModel
import com.elementary.tasks.reminder.build.preset.RecurParamsToBiAdapter
import com.elementary.tasks.reminder.build.preset.primitive.PrimitiveProtocol
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.elementary.tasks.reminder.build.reminder.BiTypeToBiValue
import com.elementary.tasks.reminder.build.reminder.ICalDateTimeCalculator
import com.elementary.tasks.reminder.build.reminder.ReminderToBiDecomposer
import com.elementary.tasks.reminder.build.reminder.compose.ActionCalculator
import com.elementary.tasks.reminder.build.reminder.compose.DateTimeInjector
import com.elementary.tasks.reminder.build.reminder.compose.EditedReminderDataCleaner
import com.elementary.tasks.reminder.build.reminder.compose.ICalDateTimeInjector
import com.elementary.tasks.reminder.build.reminder.compose.ReminderDateTimeCleaner
import com.elementary.tasks.reminder.build.reminder.compose.TypeCalculator
import com.elementary.tasks.reminder.build.reminder.decompose.ActionDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByDateDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByDayOfMonthDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByDayOfYearDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByLocationDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByTimerDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ByWeekdaysDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ExtrasDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.GroupDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.ICalDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.NoteDecomposer
import com.elementary.tasks.reminder.build.reminder.decompose.TypeDecomposer
import com.elementary.tasks.reminder.build.reminder.validation.EventTimeValidator
import com.elementary.tasks.reminder.build.reminder.validation.PermissionValidator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.elementary.tasks.reminder.build.reminder.validation.SubTasksValidator
import com.elementary.tasks.reminder.build.reminder.validation.TargetValidator
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogViewModel
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueControllerFactory
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.dialog.CreateReminderActionScreenStateUseCase
import com.elementary.tasks.reminder.dialog.ReminderActionActivityViewModel
import com.elementary.tasks.reminder.lists.active.ActiveGpsRemindersViewModel
import com.elementary.tasks.reminder.lists.active.ActiveRemindersViewModel
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListsAdapter
import com.elementary.tasks.reminder.lists.filter.ReminderFilterDialogViewModel
import com.elementary.tasks.reminder.lists.removed.RemindersArchiveFragmentViewModel
import com.elementary.tasks.reminder.lists.todo.ActiveTodoRemindersViewModel
import com.elementary.tasks.reminder.preview.AttachmentToUiReminderPreviewAttachment
import com.elementary.tasks.reminder.preview.EventToUiReminderPreview
import com.elementary.tasks.reminder.preview.FullScreenMapViewModel
import com.elementary.tasks.reminder.preview.GoogleTaskToUiReminderPreviewGoogleTask
import com.elementary.tasks.reminder.preview.NoteToUiReminderPreviewNote
import com.elementary.tasks.reminder.preview.PreviewReminderViewModel
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDataAdapter
import com.elementary.tasks.reminder.scheduling.alarmmanager.EventDateTimeCalculator
import com.elementary.tasks.reminder.scheduling.behavior.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.occurrence.ReminderOccurrenceCalculatorFactory
import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.DeactivateReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.PauseReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ResumeReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.scheduling.usecase.google.CompleteRelatedGoogleTaskUseCase
import com.elementary.tasks.reminder.scheduling.usecase.google.SaveReminderToGoogleCalendarUseCase
import com.elementary.tasks.reminder.scheduling.usecase.google.SaveReminderToGoogleTasksUseCase
import com.elementary.tasks.reminder.scheduling.usecase.legacy.MigrateRecurringParamsUseCase
import com.elementary.tasks.reminder.scheduling.usecase.location.StartLocationTrackingUseCase
import com.elementary.tasks.reminder.scheduling.usecase.location.StopLocationTrackingUseCase
import com.elementary.tasks.reminder.scheduling.usecase.notification.UpdatePermanentReminderNotificationUseCase
import com.elementary.tasks.reminder.usecase.DeleteAllReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import com.elementary.tasks.reminder.usecase.SaveReminderUseCase
import com.elementary.tasks.reminder.usecase.ScheduleReminderUploadUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reminderModule = module {
  factory { DeleteReminderUseCase(get(), get(), get(), get()) }
  factory { DeleteAllReminderUseCase(get(), get(), get(), get()) }
  factory { MoveReminderToArchiveUseCase(get(), get()) }
  factory { SaveReminderUseCase(get(), get(), get()) }
  factory { ScheduleReminderUploadUseCase(get()) }

  viewModel { ActiveGpsRemindersViewModel(get(), get(), get()) }
  viewModel {
    ActiveRemindersViewModel(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  viewModel { ActiveTodoRemindersViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { RemindersArchiveFragmentViewModel(get(), get(), get(), get(), get(), get(), get()) }

  viewModel { ManagePresetsViewModel(get(), get(), get(), get()) }
  viewModel { SelectorDialogViewModel(get(), get()) }
  viewModel { (arguments: Bundle?) ->
    BuildReminderViewModel(
      arguments,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  viewModel { (arguments: Bundle?) ->
    PreviewReminderViewModel(
      arguments,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  viewModel { (id: String, isTest: Boolean) ->
    ReminderActionActivityViewModel(
      id,
      isTest,
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    )
  }
  viewModel { (arguments: Bundle?) -> FullScreenMapViewModel(arguments, get(), get()) }

  factory { UriToAttachmentFileAdapter(get()) }

  factory { ParamToTextAdapter(get()) }

  factory { BuilderItemsHolder() }
  factory { BuilderItemsLogic(get()) }
  factory {
    BiFactory(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  factory { BiFactoryICal(get(), get(), get(), get()) }

  single { SelectorDialogDataHolder() }
  single { ValueDialogDataHolder() }

  factory { UiSelectorItemsAdapter(get(), get(), get()) }

  factory { UiBuilderItemsAdapter(get(), get(), get(), get(), get()) }
  factory { BiValueForUiAdapter(get()) }
  factory { BiErrorForUiAdapter(get(), get()) }
  factory { BiTypeForUiAdapter(get(), get()) }

  factory { BuilderItemRequiresAllConstraintCalculator() }
  factory { BuilderItemBlockedByConstraintCalculator() }
  factory { BuilderItemMandatoryIfConstraintCalculator() }
  factory { BuilderItemPermissionConstraintCalculator(get()) }
  factory { BuilderItemRequiresAnyConstraintCalculator() }

  factory {
    ValueControllerFactory(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }

  factory { BuilderStateCalculator() }
  factory { TypeCalculator(get()) }
  factory { ActionCalculator() }

  factory { BiToReminderAdapter(get(), get(), get(), get(), get(), get()) }

  factory { BuilderErrorFinder(get(), get(), get(), get(), get()) }
  factory { BuilderErrorToTextAdapter(get(), get()) }

  factory { RecurParamsToBiAdapter(get()) }

  factory { ReminderValidator(get(), get(), get()) }
  factory { TargetValidator() }
  factory { EventTimeValidator() }
  factory { SubTasksValidator() }

  factory { PermissionValidator(get()) }

  factory { ICalDateTimeCalculator(get(), get()) }

  factory { DateTimeInjector(get(), get(), get()) }
  factory { ICalDateTimeInjector(get(), get()) }
  factory { ReminderDateTimeCleaner() }
  factory { EditedReminderDataCleaner() }

  factory { ReminderToBiDecomposer(get(), get(), get(), get(), get(), get(), get()) }

  factory { TypeDecomposer(get(), get(), get(), get(), get(), get(), get()) }
  factory { ByDateDecomposer(get(), get()) }
  factory { ByTimerDecomposer(get()) }
  factory { ByWeekdaysDecomposer(get(), get()) }
  factory { ByDayOfMonthDecomposer(get(), get()) }
  factory { ByDayOfYearDecomposer(get(), get()) }
  factory { ByLocationDecomposer(get(), get()) }
  factory { ICalDecomposer(get(), get(), get()) }

  factory { ActionDecomposer(get()) }

  factory { ExtrasDecomposer(get(), get(), get()) }

  factory { GroupDecomposer(get(), get(), get()) }

  factory { NoteDecomposer(get(), get(), get()) }

  factory { LocationFilter(get()) }
  factory { CreatorConfigFilter(get()) }
  factory { BiFilter(get(), get()) }

  factory { PrimitiveProtocol() }
  factory { BiTypeToBiValue() }
  factory { BuilderPresetToBiAdapter(get(), get()) }
  factory { BuilderItemsToBuilderPresetAdapter(get()) }
  factory { BiValueToBuilderSchemeValue(get()) }

  factory { ReminderPredictionCalculator(get(), get(), get()) }

  factory { BuilderPresetsGenerateUseCase(get(), get(), get(), get()) }

  factory { DefaultPresetsGenerateUseCase(get(), get(), get(), get()) }

  factory { ShopItemsFormatter(get()) }

  single { RadiusFormatterFactory(get(), get()) }
  single { PlaceFormatterFactory(get()) }

  factory { GoogleTaskToUiReminderPreviewGoogleTask(get(), get(), get(), get()) }
  factory { NoteToUiReminderPreviewNote(get(), get(), get(), get()) }
  factory { EventToUiReminderPreview(get(), get(), get(), get()) }
  factory { AttachmentToUiReminderPreviewAttachment(get(), get(), get(), get()) }

  factory { UiReminderPreviewDataAdapter(get(), get(), get(), get(), get(), get(), get()) }

  factory { UiReminderListsAdapter(get(), get(), get(), get(), get()) }
  factory { UiReminderListAdapter(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

  viewModel { ReminderFilterDialogViewModel(get()) }

  factory { BehaviorStrategyResolver(get(), get()) }

  factory {
    ActivateReminderUseCase(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get()
    )
  }
  factory { DeactivateReminderUseCase(get(), get(), get(), get(), get()) }

  factory { PauseReminderUseCase(get(), get(), get(), get()) }
  factory { ResumeReminderUseCase(get(), get(), get()) }

  factory { SnoozeReminderUseCase(get(), get(), get(), get(), get()) }
  factory { CompleteReminderUseCase(get(), get(), get(), get()) }
  factory { SkipReminderUseCase(get(), get(), get()) }

  factory { ToggleReminderStateUseCase(get(), get()) }

  factory { UpdatePermanentReminderNotificationUseCase(get(), get()) }

  factory { StopLocationTrackingUseCase(get(), get(), get()) }
  factory { StartLocationTrackingUseCase(get(), get()) }

  factory { CompleteRelatedGoogleTaskUseCase(get(), get()) }
  factory { SaveReminderToGoogleTasksUseCase(get(), get(), get()) }
  factory { SaveReminderToGoogleCalendarUseCase(get(), get()) }

  factory { ReminderOccurrenceCalculatorFactory(get(), get()) }

  factory { EventDateTimeCalculator(get(), get()) }

  factory { RecurrenceCalculator() }

  factory { MigrateRecurringParamsUseCase(get(), get()) }

  factory { GetReminderActionsUseCase() }

  factory { CreateReminderActionScreenStateUseCase(get(), get(), get(), get()) }
}
