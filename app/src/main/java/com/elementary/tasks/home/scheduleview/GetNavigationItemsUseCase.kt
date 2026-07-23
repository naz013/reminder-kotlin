package com.elementary.tasks.home.scheduleview

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.home.HeaderNavigationItem
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel.NavigationEvent
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.ui.common.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.threeten.bp.LocalDateTime

class GetNavigationItemsUseCase(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val noteRepository: NoteRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
) {
  suspend operator fun invoke(
    scope: CoroutineScope,
    day: LocalDateTime,
  ): List<HeaderNavigationItem> =
    listOfNotNull(
      getCalendarItem(scope = scope),
      getEventsItem(scope = scope),
      getNoteItem(scope = scope),
      getGoogleTasksItem(scope = scope),
//      getGroupItem(scope = scope),
      getWorkflowItem(scope = scope),
    )

  private suspend fun getCalendarItem(scope: CoroutineScope): HeaderNavigationItem {
    val remindersCount = reminderRepository.countAllTypesInState(active = true, removed = false)
    val birthdaysCount = birthdayRepository.countAll()
    return scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.calendar,
          iconRes = R.drawable.ic_fluent_calendar,
          color = Color.Green,
          navigationEvent = NavigationEvent.OpenCalendar,
          subtitle = "${remindersCount + birthdaysCount}",
        )
      }.await()
  }

  private suspend fun getEventsItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.events,
          iconRes = R.drawable.ic_fluent_timeline,
          color = Color.Green,
          navigationEvent = NavigationEvent.OpenEvents,
          subtitle = "${reminderRepository.countAllTypesInState(active = true, removed = false)}",
        )
      }.await()

  private suspend fun getNoteItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.notes,
          iconRes = R.drawable.ic_fluent_note,
          color = Color.Green,
          navigationEvent = NavigationEvent.OpenNotes,
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
          navigationEvent = NavigationEvent.OpenGoogleTasks,
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
          navigationEvent = NavigationEvent.OpenWorkflowGallery,
          subtitle = "${workflowRuleRepository.getEnabled().size}",
        )
      }.await()

  private suspend fun getGroupItem(scope: CoroutineScope): HeaderNavigationItem =
    scope
      .async(dispatcherProvider.io()) {
        HeaderNavigationItem(
          titleRes = R.string.groups,
          iconRes = R.drawable.ic_builder_group,
          color = Color.Green,
          navigationEvent = NavigationEvent.OpenGroups,
          subtitle = "${reminderGroupRepository.countAll()}",
        )
      }.await()
}
