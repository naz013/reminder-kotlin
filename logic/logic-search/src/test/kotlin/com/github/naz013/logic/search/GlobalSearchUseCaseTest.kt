package com.github.naz013.logic.search

import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.HabitRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GlobalSearchUseCaseTest {

  private val reminderRepository = mockk<ReminderV2Repository>()
  private val noteRepository = mockk<NoteRepository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val groupRepository = mockk<GroupV2Repository>()
  private val placeRepository = mockk<PlaceRepository>()
  private val routineRepository = mockk<RoutineRepository>()
  private val habitRepository = mockk<HabitRepository>()
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val featureFlags = mockk<FeatureFlags>()
  private val routineConfig = mockk<RoutineConfig>()
  private val habitConfig = mockk<HabitConfig>()

  private val now = LocalDateTime.of(2026, 1, 1, 10, 0)

  @Before
  fun setUp() {
    coEvery { reminderRepository.search(any()) } returns emptyList()
    coEvery { noteRepository.search(any()) } returns emptyList()
    coEvery { birthdayRepository.searchByName(any()) } returns emptyList()
    coEvery { googleTaskRepository.search(any()) } returns emptyList()
    coEvery { groupRepository.search(any()) } returns emptyList()
    coEvery { placeRepository.searchByName(any()) } returns emptyList()
    coEvery { routineRepository.search(any()) } returns emptyList()
    coEvery { habitRepository.search(any()) } returns emptyList()
    coEvery { tagRepository.getAll() } returns emptyList()
    every { featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) } returns true
    every { routineConfig.isEnabled } returns true
    every { habitConfig.isEnabled } returns true
  }

  private fun useCase() = GlobalSearchUseCase(
    reminderRepository,
    noteRepository,
    birthdayRepository,
    googleTaskRepository,
    groupRepository,
    placeRepository,
    routineRepository,
    habitRepository,
    tagRepository,
    tagAssignmentRepository,
    featureFlags,
    routineConfig,
    habitConfig,
    mockDispatcherProvider()
  )

  private fun routine(id: String, title: String, steps: List<RoutineStep> = emptyList()) = Routine(
    id = id,
    title = title,
    steps = steps,
    createdAt = now,
    updatedAt = now
  )

  private fun reminder(id: String, summary: String) = ReminderV2(
    uuId = id,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = now)
  )

  @Test
  fun `returns empty list when query is too short`() = runTest {
    val result = useCase()("a")

    assertTrue(result.isEmpty())
    coVerify(exactly = 0) { reminderRepository.search(any()) }
  }

  @Test
  fun `maps results from every repository into common results`() = runTest {
    coEvery { reminderRepository.search("milk") } returns listOf(reminder("r1", "Buy milk"))
    coEvery { habitRepository.search("milk") } returns listOf(
      Habit(id = "h1", name = "Drink milk", createdAt = now, updatedAt = now)
    )
    coEvery { googleTaskRepository.search("milk") } returns listOf(GoogleTask(taskId = "g1", title = "Milk task"))

    val result = useCase()("Milk")

    assertEquals(
      setOf(
        SearchResultType.REMINDER to "r1",
        SearchResultType.HABIT to "h1",
        SearchResultType.GOOGLE_TASK to "g1"
      ),
      result.map { it.type to it.id }.toSet()
    )
  }

  @Test
  fun `caps results per type`() = runTest {
    coEvery { reminderRepository.search("task") } returns (1..10).map { reminder("r$it", "task") }

    val result = useCase()("task", limitPerType = 3)

    assertEquals(3, result.count { it.type == SearchResultType.REMINDER })
  }

  @Test
  fun `skips routines and habits when their features are disabled`() = runTest {
    every { routineConfig.isEnabled } returns false
    every { habitConfig.isEnabled } returns false

    useCase()("walk")

    coVerify(exactly = 0) { routineRepository.search(any()) }
    coVerify(exactly = 0) { habitRepository.search(any()) }
  }

  @Test
  fun `skips google tasks when the feature flag is off`() = runTest {
    every { featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS) } returns false

    useCase()("walk")

    coVerify(exactly = 0) { googleTaskRepository.search(any()) }
  }

  @Test
  fun `includes items carrying a tag whose name matches`() = runTest {
    val tag = Tag(id = "t1", name = "Work", color = 0)
    coEvery { tagRepository.getAll() } returns listOf(tag)
    coEvery { tagAssignmentRepository.getItemIdsForTag(any(), any()) } returns emptyList()
    coEvery { tagAssignmentRepository.getItemIdsForTag("t1", TaggedItemType.REMINDER) } returns listOf("r9")
    coEvery { reminderRepository.getById("r9") } returns reminder("r9", "Quarterly report")

    val result = useCase()("work")

    val item = result.single()
    assertEquals("r9", item.id)
    assertEquals("Work", item.matchedTag)
  }

  @Test
  fun `does not duplicate an item matched by both text and tag`() = runTest {
    val tag = Tag(id = "t1", name = "Work", color = 0)
    val reminder = reminder("r1", "Work lunch")
    coEvery { tagRepository.getAll() } returns listOf(tag)
    coEvery { tagAssignmentRepository.getItemIdsForTag(any(), any()) } returns emptyList()
    coEvery { tagAssignmentRepository.getItemIdsForTag("t1", TaggedItemType.REMINDER) } returns listOf("r1")
    coEvery { reminderRepository.getById("r1") } returns reminder
    coEvery { reminderRepository.search("work") } returns listOf(reminder)

    val result = useCase()("work")

    assertEquals(1, result.size)
    assertEquals(null, result.single().matchedTag)
  }

  @Test
  fun `shows the matching step title as routine subtitle`() = runTest {
    val step = RoutineStep(title = "Stretch legs")
    coEvery { routineRepository.search("stretch") } returns listOf(routine("ro1", "Morning", listOf(step)))

    val result = useCase()("stretch")

    assertEquals("Stretch legs", result.single().subtitle)
  }

  @Test
  fun `keeps other results when one repository fails`() = runTest {
    coEvery { noteRepository.search(any()) } throws IllegalStateException("boom")
    coEvery { reminderRepository.search("milk") } returns listOf(reminder("r1", "milk"))

    val result = useCase()("milk")

    assertEquals(listOf("r1"), result.map { it.id })
  }
}
