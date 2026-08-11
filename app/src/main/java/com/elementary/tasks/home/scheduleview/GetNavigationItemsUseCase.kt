package com.elementary.tasks.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.home.HeaderNavigationItem
import com.github.naz013.feature.workflow.WorkflowConfig
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.ui.common.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class GetNavigationItemsUseCase(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(
    scope: CoroutineScope,
    day: LocalDateTime,
  ): List<HeaderNavigationItem> = buildList {
    add(getCalendarItem(scope = scope))
    add(getAgendaItem(scope = scope))
    add(getNoteItem(scope = scope))
    add(getGoogleTasksItem(scope = scope))
    add(getGroupItem(scope = scope))
    if (WorkflowConfig.isEnabled) {
      add(getWorkflowItem(scope = scope))
    }
  }

  private suspend fun getCalendarItem(scope: CoroutineScope): HeaderNavigationItem {
    return scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.calendar,
          iconRes = R.drawable.ic_fluent_calendar,
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
          iconRes = R.drawable.ic_fluent_timeline,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenAgenda,
          subtitle = "${reminderV2Repository.getAll(active = true, removed = false).size}",
        )
      }.await()

  private suspend fun getNoteItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.notes,
          iconRes = R.drawable.ic_fluent_note,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenNotes,
          subtitle = "${noteRepository.countAll(isArchived = false)}",
        )
      }.await()

  private suspend fun getGoogleTasksItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.google_tasks,
          iconRes = R.drawable.ic_builder_google_task_list,
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
          iconRes = R.drawable.ic_fluent_arrow_repeat_all,
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
          iconRes = R.drawable.ic_fluent_group,
          color = Color.Green,
          navigationEvent = ScheduleHomeViewModel.ViewModelEvent.OpenGroups,
          subtitle = "${groupV2Repository.countAll()}",
        )
      }.await()
}
