package com.github.naz013.feature.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.home.HeaderNavigationItem
import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.logic.workflow.WorkflowConfig
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.icon.DrawableCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class GetNavigationItemsUseCase(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val noteRepository: NoteRepository,
  private val birthdayRepository: BirthdayRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val routineRepository: RoutineRepository,
  private val tagRepository: TagRepository,
  private val dateTimeManager: DateTimeManager,
  private val routineConfig: RoutineConfig,
  private val workflowConfig: WorkflowConfig,
  private val homePreferences: HomePreferences,
) {
  suspend operator fun invoke(
    scope: CoroutineScope,
    day: LocalDateTime,
  ): List<HeaderNavigationItem> {
    val disabled = homePreferences.disabledHeaderNavigationSections
    val sections =
      HeaderNavigationSection.pinned +
        homePreferences.headerNavigationOrder.filter { it !in disabled && isAvailable(it) }
    return sections.map { section -> buildItem(section, scope) }
  }

  private fun isAvailable(section: HeaderNavigationSection): Boolean = when (section) {
    HeaderNavigationSection.ROUTINES -> routineConfig.isEnabled
    HeaderNavigationSection.WORKFLOW -> workflowConfig.isEnabled
    else -> true
  }

  private suspend fun buildItem(
    section: HeaderNavigationSection,
    scope: CoroutineScope,
  ): HeaderNavigationItem = when (section) {
    HeaderNavigationSection.CALENDAR -> getCalendarItem(scope = scope)
    HeaderNavigationSection.AGENDA -> getAgendaItem(scope = scope)
    HeaderNavigationSection.NOTES -> getNoteItem(scope = scope)
    HeaderNavigationSection.BIRTHDAYS -> getBirthdayItem(scope = scope)
    HeaderNavigationSection.GOOGLE_TASKS -> getGoogleTasksItem(scope = scope)
    HeaderNavigationSection.GROUPS -> getGroupItem(scope = scope)
    HeaderNavigationSection.TAG -> getTagItem(scope = scope)
    HeaderNavigationSection.ROUTINES -> getRoutineItem(scope = scope)
    HeaderNavigationSection.WORKFLOW -> getWorkflowItem(scope = scope)
  }

  private suspend fun getCalendarItem(scope: CoroutineScope): HeaderNavigationItem {
    return scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.calendar,
          iconRes = DrawableCatalog.Fluent.Calendar,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenCalendar,
          subtitle = dateTimeManager.formatDayMonth(LocalDate.now()),
        )
      }.await()
  }

  private suspend fun getAgendaItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.agenda,
          iconRes = DrawableCatalog.Fluent.CalendarAgenda,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenAgenda,
          subtitle = "${reminderV2Repository.count(active = true, removed = false)}",
        )
      }.await()

  private suspend fun getNoteItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.notes,
          iconRes = DrawableCatalog.Fluent.Note,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
          subtitle = "${noteRepository.countAll(isArchived = false)}",
        )
      }.await()

  private suspend fun getBirthdayItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.birthdays,
          iconRes = DrawableCatalog.Fluent.FoodCake,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenBirthdays,
          subtitle = "${birthdayRepository.countAll()}",
        )
      }.await()

  private suspend fun getGoogleTasksItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.google_tasks,
          iconRes = DrawableCatalog.Builder.GoogleTaskList,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenGoogleTasks,
          subtitle = "${googleTaskRepository.countAll()}",
        )
      }.await()

  private suspend fun getWorkflowItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.workflow_automations,
          iconRes = DrawableCatalog.Fluent.Branch,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenWorkflowGallery,
          subtitle = "${workflowRuleRepository.getEnabled().size}",
        )
      }.await()

  private suspend fun getGroupItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.groups,
          iconRes = DrawableCatalog.Fluent.Group,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenGroups,
          subtitle = "${groupV2Repository.countAll()}",
        )
      }.await()

  private suspend fun getTagItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.tags,
          iconRes = DrawableCatalog.Builder.Tag,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenTags,
          subtitle = "${tagRepository.getAll().size}",
        )
      }.await()

  private suspend fun getRoutineItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.routines,
          iconRes = DrawableCatalog.Builder.Timer,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenRoutines,
          subtitle = "${routineRepository.getAll().size}",
        )
      }.await()
}
