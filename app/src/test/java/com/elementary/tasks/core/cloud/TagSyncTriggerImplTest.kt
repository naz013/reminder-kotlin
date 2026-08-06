package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.cloud.worker.WorkType
import com.github.naz013.files.DataType
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TagSyncTriggerImplTest {
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)
  private lateinit var trigger: TagSyncTriggerImpl

  @Before
  fun setUp() {
    trigger = TagSyncTriggerImpl(scheduleBackgroundWorkUseCase)
  }

  @Test
  fun `onTagSaved schedules an upload for DataType Tags`() {
    trigger.onTagSaved("tag-1")

    verify {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Upload,
        dataType = DataType.Tags,
        id = "tag-1",
      )
    }
  }

  @Test
  fun `onTagDeleted schedules a delete for DataType Tags`() {
    trigger.onTagDeleted("tag-1")

    verify {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Tags,
        id = "tag-1",
      )
    }
  }
}
