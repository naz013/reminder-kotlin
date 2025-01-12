package com.elementary.tasks.reminder

import android.os.Bundle
import com.elementary.tasks.reminder.build.BuildReminderViewModel
import com.elementary.tasks.reminder.build.adapter.BiErrorForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiTypeForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiValueForUiAdapter
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
import com.elementary.tasks.reminder.build.reminder.compose.ICalDateTimeInjector
import com.elementary.tasks.reminder.build.reminder.compose.ReminderCleaner
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
import com.elementary.tasks.reminder.dialog.ReminderViewModel
import com.elementary.tasks.reminder.lists.active.ActiveGpsRemindersViewModel
import com.elementary.tasks.reminder.lists.active.ActiveRemindersViewModel
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderListsAdapter
import com.elementary.tasks.reminder.lists.removed.ArchiveRemindersViewModel
import com.elementary.tasks.reminder.lists.todo.ActiveTodoRemindersViewModel
import com.elementary.tasks.reminder.preview.AttachmentToUiReminderPreviewAttachment
import com.elementary.tasks.reminder.preview.EventToUiReminderPreview
import com.elementary.tasks.reminder.preview.FullScreenMapViewModel
import com.elementary.tasks.reminder.preview.GoogleTaskToUiReminderPreviewGoogleTask
import com.elementary.tasks.reminder.preview.NoteToUiReminderPreviewNote
import com.elementary.tasks.reminder.preview.PreviewReminderViewModel
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDataAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reminderModule = module {
  viewModel { ActiveGpsRemindersViewModel(get(), get(), get()) }
  viewModel { ActiveRemindersViewModel(get(), get(), get(), get(), get()) }
  viewModel { ActiveTodoRemindersViewModel(get(), get(), get(), get(), get()) }
  viewModel { ArchiveRemindersViewModel(get(), get(), get(), get(), get(), get()) }

  viewModel { ManagePresetsViewModel(get(), get(), get()) }
  viewModel { SelectorDialogViewModel(get(), get()) }
  viewModel {
    BuildReminderViewModel(
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
      get()
    )
  }

  viewModel { (id: String) -> ReminderViewModel(id, get(), get(), get(), get(), get()) }
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

  factory { BiToReminderAdapter(get(), get(), get(), get(), get()) }

  factory { RecurParamsToBiAdapter(get()) }

  factory { ReminderValidator(get(), get(), get()) }
  factory { TargetValidator() }
  factory { EventTimeValidator() }
  factory { SubTasksValidator() }

  factory { PermissionValidator(get()) }

  factory { ICalDateTimeCalculator(get(), get()) }

  factory { DateTimeInjector(get(), get(), get()) }
  factory { ICalDateTimeInjector(get(), get()) }
  factory { ReminderCleaner() }

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

  factory { ReminderPredictionCalculator(get(), get()) }

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
}
