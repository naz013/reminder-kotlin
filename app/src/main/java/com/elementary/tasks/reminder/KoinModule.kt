package com.elementary.tasks.reminder

import com.elementary.tasks.reminder.actions.GetReminderActionsUseCase
import com.elementary.tasks.reminder.build.ApplicationPickerResultHolder
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.build.BuildReminderViewModel
import com.elementary.tasks.reminder.build.adapter.BiErrorForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiTypeForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BiValueForUiAdapter
import com.elementary.tasks.reminder.build.adapter.BuilderErrorToTextAdapter
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiFactoryICal
import com.elementary.tasks.reminder.build.bi.BiFilter
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
import com.elementary.tasks.reminder.build.quickstart.FindGroupUseCase
import com.elementary.tasks.reminder.build.quickstart.QuickStartItemsProvider
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.elementary.tasks.reminder.build.reminder.BiTypeToBiValue
import com.elementary.tasks.reminder.build.reminder.ICalDateTimeCalculator
import com.elementary.tasks.reminder.build.reminder.ReminderToBiDecomposer
import com.elementary.tasks.reminder.build.reminder.compose.CalendarExportCalculator
import com.elementary.tasks.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.elementary.tasks.reminder.build.reminder.compose.ReminderActionCalculator
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
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.dialog.CreateReminderActionScreenStateUseCase
import com.elementary.tasks.reminder.dialog.ReminderActionActivityViewModel
import com.elementary.tasks.reminder.lists.data.UiReminderListAdapter
import com.elementary.tasks.reminder.lists.removed.RemindersArchiveViewModel
import com.elementary.tasks.reminder.preview.FullScreenMapViewModel
import com.elementary.tasks.reminder.preview.PreviewReminderViewModel
import com.elementary.tasks.reminder.scheduling.alarmmanager.v2.EventDateTimeCalculatorV2
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ResumeReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SkipReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.SnoozeReminderUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ToggleReminderStateUseCase
import com.elementary.tasks.reminder.todo.TodoEditNavKey
import com.elementary.tasks.reminder.todo.TodoEditViewModel
import com.elementary.tasks.reminder.todo.TodoSeedHolder
import com.elementary.tasks.reminder.usecase.MoveReminderToArchiveUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reminderModule =
  module {
    factory { MoveReminderToArchiveUseCase(get(), get()) }

    viewModelOf(::RemindersArchiveViewModel)

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
        get(),
      )
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
        get(),
      )
    }
    viewModel { (id: String) -> FullScreenMapViewModel(id, get(), get()) }

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

    single { SelectorDialogDataHolder() }
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

    factory { ExtrasDecomposer(get(), get(), get()) }

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

    factory { ShopItemsFormatter(get()) }

    single { RadiusFormatterFactory(get(), get()) }
    single { PlaceFormatterFactory(get()) }

    factoryOf(::UiReminderListAdapter)

    factoryOf(::ResumeReminderUseCase)

    factoryOf(::SnoozeReminderUseCase)
    factoryOf(::CompleteReminderUseCase)
    factoryOf(::SkipReminderUseCase)

    factoryOf(::ToggleReminderStateUseCase)

    factory { EventDateTimeCalculatorV2(get(), get()) }

    factory { GetReminderActionsUseCase() }

    factory { CreateReminderActionScreenStateUseCase(get(), get(), get(), get()) }

    factoryOf(::FindGroupUseCase)

    factoryOf(::IsSimpleTodoReminderUseCase)
  }
