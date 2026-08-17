package com.github.naz013.feature.reminder

import com.github.naz013.feature.reminder.actions.GetReminderActionsUseCase
import com.github.naz013.feature.reminder.analytics.ReminderAnalyticsTracker
import com.github.naz013.feature.reminder.apps.SelectApplicationViewModel
import com.github.naz013.feature.reminder.build.ApplicationPickerResultHolder
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.build.BuildReminderViewModel
import com.github.naz013.feature.reminder.build.adapter.BiErrorForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.BiTypeForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.BiValueForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.BuilderErrorToTextAdapter
import com.github.naz013.feature.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.feature.reminder.build.bi.BiFactoryICal
import com.github.naz013.feature.reminder.build.bi.BiFilter
import com.github.naz013.feature.reminder.build.bi.LocationFilter
import com.github.naz013.feature.reminder.build.formatter.factory.PlaceFormatterFactory
import com.github.naz013.feature.reminder.build.formatter.factory.RadiusFormatterFactory
import com.github.naz013.feature.reminder.build.logic.BuilderItemBlockedByConstraintCalculator
import com.github.naz013.feature.reminder.build.logic.BuilderItemMandatoryIfConstraintCalculator
import com.github.naz013.feature.reminder.build.logic.BuilderItemPermissionConstraintCalculator
import com.github.naz013.feature.reminder.build.logic.BuilderItemRequiresAllConstraintCalculator
import com.github.naz013.feature.reminder.build.logic.BuilderItemRequiresAnyConstraintCalculator
import com.github.naz013.feature.reminder.build.logic.BuilderItemsHolder
import com.github.naz013.feature.reminder.build.logic.BuilderItemsLogic
import com.github.naz013.feature.reminder.build.logic.UiBuilderItemsAdapter
import com.github.naz013.feature.reminder.build.logic.UiSelectorItemsAdapter
import com.github.naz013.feature.reminder.build.logic.builderstate.BuilderErrorFinder
import com.github.naz013.feature.reminder.build.logic.builderstate.BuilderStateCalculator
import com.github.naz013.feature.reminder.build.logic.builderstate.ReminderPredictionCalculator
import com.github.naz013.feature.reminder.build.preset.BiValueToBuilderSchemeValue
import com.github.naz013.feature.reminder.build.preset.BuilderItemsToBuilderPresetAdapter
import com.github.naz013.feature.reminder.build.preset.BuilderPresetToBiAdapter
import com.github.naz013.feature.reminder.build.preset.BuilderPresetsGenerateUseCase
import com.github.naz013.feature.reminder.build.preset.DefaultPresetsGenerateUseCase
import com.github.naz013.feature.reminder.build.preset.ManagePresetsViewModel
import com.github.naz013.feature.reminder.settings.RemindersSettingsViewModel
import com.github.naz013.feature.reminder.build.preset.PresetInitProcessor
import com.github.naz013.feature.reminder.build.preset.RecurParamsToBiAdapter
import com.github.naz013.feature.reminder.build.preset.primitive.PrimitiveProtocol
import com.github.naz013.feature.reminder.build.quickstart.FindGroupUseCase
import com.github.naz013.feature.reminder.build.quickstart.QuickStartItemsProvider
import com.github.naz013.feature.reminder.build.reminder.BiToReminderAdapter
import com.github.naz013.feature.reminder.build.reminder.BiTypeToBiValue
import com.github.naz013.feature.reminder.build.reminder.ICalDateTimeCalculator
import com.github.naz013.feature.reminder.build.reminder.ReminderToBiDecomposer
import com.github.naz013.feature.reminder.build.reminder.compose.CalendarExportCalculator
import com.github.naz013.feature.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.github.naz013.feature.reminder.build.reminder.compose.ReminderActionCalculator
import com.github.naz013.feature.reminder.build.reminder.decompose.ActionDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByDateDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByDayOfMonthDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByDayOfYearDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByLocationDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByTimerDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ByWeekdaysDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ExtrasDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.GroupDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.ICalDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.NoteDecomposer
import com.github.naz013.feature.reminder.build.reminder.decompose.TypeDecomposer
import com.github.naz013.feature.reminder.build.reminder.validation.EventTimeValidator
import com.github.naz013.feature.reminder.build.reminder.validation.PermissionValidator
import com.github.naz013.feature.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.feature.reminder.build.reminder.validation.SubTasksValidator
import com.github.naz013.feature.reminder.build.reminder.validation.TargetValidator
import com.github.naz013.feature.reminder.build.selectordialog.SelectorDialogDataHolder
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.github.naz013.feature.reminder.dialog.CreateReminderActionScreenStateUseCase
import com.github.naz013.feature.reminder.dialog.ReminderActionActivityViewModel
import com.github.naz013.feature.reminder.lists.UiReminderListAdapterImpl
import com.github.naz013.feature.reminder.lists.removed.RemindersArchiveViewModel
import com.github.naz013.feature.reminder.note.UiNoteListAdapter
import com.github.naz013.feature.reminder.preset.UiPresetListAdapter
import com.github.naz013.feature.reminder.preview.FullScreenMapViewModel
import com.github.naz013.feature.reminder.preview.PreviewReminderViewModel
import com.github.naz013.feature.reminder.todo.TodoEditNavKey
import com.github.naz013.feature.reminder.todo.TodoEditViewModel
import com.github.naz013.feature.reminder.todo.TodoSeedHolder
import com.github.naz013.feature.reminder.util.BackupTool
import com.github.naz013.feature.reminder.util.UriHelper
import com.github.naz013.ui.reminder.UiReminderListAdapter
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureReminderModule = module {
  factory { GetReminderActionsUseCase() }
  factory { CreateReminderActionScreenStateUseCase(get(), get(), get(), get()) }

  viewModelOf(::RemindersArchiveViewModel)
  viewModelOf(::RemindersSettingsViewModel)

  viewModel { (id: String) ->
    PreviewReminderViewModel(
      id,
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
    )
  }

  viewModel { (id: String) -> FullScreenMapViewModel(id, get(), get()) }

  factory {
    UiReminderListAdapterImpl(
      get(), get(), get(), get(), get(), get(), get(), get(), get()
    ) as UiReminderListAdapter
  }

  viewModel { (id: String) ->
    ReminderActionActivityViewModel(
      id,
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

  viewModelOf(::SelectApplicationViewModel)

  factory { UiNoteListAdapter(get(), get(), get(), get(), get()) }
  factoryOf(::UiPresetListAdapter)

  factory { ReminderAnalyticsTracker(get()) }

  viewModel { ManagePresetsViewModel(get(), get(), get(), get(), get()) }
  // Resolved from the single Main key object, not loose positional values: Koin's
  // ParametersHolder matches by KClass, so two same-typed values in one parametersOf() list
  // (id: String, deepLinkText: String?) resolve ambiguously - id's value was leaking into
  // deepLinkText.
  viewModel { (key: BuildReminderNavKey.Main) ->
    BuildReminderViewModel(
      key,
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
      get(),
    )
  }
  factory { BiFactoryICal(get(), get(), get(), get()) }

  single { SelectorDialogDataHolder(get()) }
  single { ApplicationPickerResultHolder() }
  single { TodoSeedHolder() }

  viewModel { (key: TodoEditNavKey.Main) ->
    TodoEditViewModel(
      key,
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
    )
  }

  factory { UiSelectorItemsAdapter(get(), get(), get(), get()) }

  factory { UiBuilderItemsAdapter(get(), get(), get(), get(), get()) }
  factory { BiValueForUiAdapter(get()) }
  factory { BiErrorForUiAdapter(get(), get()) }
  factory { BiTypeForUiAdapter(get(), get()) }

  factory { BuilderItemRequiresAllConstraintCalculator() }
  factory { BuilderItemBlockedByConstraintCalculator() }
  factory { BuilderItemMandatoryIfConstraintCalculator() }
  factory { BuilderItemPermissionConstraintCalculator(get()) }
  factory { BuilderItemRequiresAnyConstraintCalculator() }

  factory { BuilderStateCalculator() }
  factory { RecurrenceRuleCalculator(get(), get(), get()) }
  factory { ReminderActionCalculator() }
  factory { CalendarExportCalculator() }

  factory { BiToReminderAdapter(get(), get(), get(), get(), get()) }

  factory { BuilderErrorFinder(get(), get(), get(), get()) }
  factory { BuilderErrorToTextAdapter(get(), get()) }

  factory { RecurParamsToBiAdapter(get()) }

  factory { ReminderValidator(get(), get(), get()) }
  factory { TargetValidator() }
  factory { EventTimeValidator() }
  factory { SubTasksValidator() }

  factory { PermissionValidator(get()) }

  factory { ICalDateTimeCalculator(get(), get()) }

  factory { ReminderToBiDecomposer(get(), get(), get(), get(), get(), get(), get()) }

  factory { TypeDecomposer(get(), get(), get(), get(), get(), get(), get()) }
  factory { ByDateDecomposer(get(), get()) }
  factory { ByTimerDecomposer(get()) }
  factory { ByWeekdaysDecomposer(get(), get()) }
  factory { ByDayOfMonthDecomposer(get(), get()) }
  factory { ByDayOfYearDecomposer(get(), get()) }
  factory { ByLocationDecomposer(get(), get()) }
  factory { ICalDecomposer(get(), get(), get()) }

  factory { ActionDecomposer(get(), get()) }

  factory { ExtrasDecomposer(get(), get(), get(), get()) }

  factory { GroupDecomposer(get(), get(), get()) }

  factory { NoteDecomposer(get(), get(), get()) }

  factoryOf(::LocationFilter)
  factoryOf(::BiFilter)

  factory { PrimitiveProtocol() }
  factory { BiTypeToBiValue() }
  factory { BuilderPresetToBiAdapter(get(), get()) }
  factory { BuilderItemsToBuilderPresetAdapter(get()) }
  factory { BiValueToBuilderSchemeValue(get()) }

  factory { ReminderPredictionCalculator(get(), get(), get()) }

  factory { BuilderPresetsGenerateUseCase(get(), get(), get(), get()) }

  factory { DefaultPresetsGenerateUseCase(get(), get(), get(), get()) }

  factoryOf(::QuickStartItemsProvider)

  single { RadiusFormatterFactory(get(), get()) }
  single { PlaceFormatterFactory(get()) }

  factoryOf(::FindGroupUseCase)

  factoryOf(::IsSimpleTodoReminderUseCase)

  factory { UiReminderPlaceAdapter() }

  factory { UiReminderCommonAdapter(get(), get(), get(), get(), get(), get(), get()) }

  factoryOf(::UriHelper)
  singleOf(::BackupTool)
  factoryOf(::PresetInitProcessor)
}
