package com.github.naz013.sync.local

import com.github.naz013.files.DataType
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.RecurPresetRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataTypeRepositoryCallerFactoryTest {

  private lateinit var factory: DataTypeRepositoryCallerFactory

  @Before
  fun setUp() {
    factory = DataTypeRepositoryCallerFactory(
      noteRepository = mockk<NoteRepository>(),
      birthdayRepository = mockk<BirthdayRepository>(),
      placeRepository = mockk<PlaceRepository>(),
      recurPresetRepository = mockk<RecurPresetRepository>(),
      reminderV2Repository = mockk<ReminderV2Repository>(),
      groupV2Repository = mockk<GroupV2Repository>(),
      tagRepository = mockk<TagRepository>(),
      routineRepository = mockk<RoutineRepository>(),
      workflowRuleRepository = mockk<WorkflowRuleRepository>(),
      workflowTemplateRepository = mockk<WorkflowTemplateRepository>()
    )
  }

  @Test
  fun `getCaller for legacy Groups returns a real caller so migrated groups are actually persisted`() {
    // Legacy .gr2 files are converted to GroupV2 by the data converter (ReminderGroup.toGroupV2()),
    // so the caller for DataType.Groups must be able to persist a GroupV2 - a NoopRepositoryCaller
    // here would silently discard every migrated group after its cloud backup is deleted.
    val caller = factory.getCaller(DataType.Groups)

    assertTrue(caller is GroupV2RepositoryCaller)
  }

  @Test
  fun `getCaller for GroupsV2 returns the same caller type as legacy Groups`() {
    val caller = factory.getCaller(DataType.GroupsV2)

    assertTrue(caller is GroupV2RepositoryCaller)
  }

  @Test
  fun `getCaller for NotesV2 legacy type returns a real caller`() {
    val caller = factory.getCaller(DataType.NotesV2)

    assertTrue(caller is NoteRepositoryCaller)
  }

  @Test
  fun `getCaller for Settings and TagAssignments returns a noop caller`() {
    assertTrue(factory.getCaller(DataType.Settings) is NoopRepositoryCaller)
    assertTrue(factory.getCaller(DataType.TagAssignments) is NoopRepositoryCaller)
  }
}
