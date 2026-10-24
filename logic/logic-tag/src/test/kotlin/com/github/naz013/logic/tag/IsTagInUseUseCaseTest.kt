package com.github.naz013.logic.tag

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IsTagInUseUseCaseTest {
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private lateinit var useCase: IsTagInUseUseCase

  @Before
  fun setUp() {
    coEvery { tagAssignmentRepository.getItemIdsForTag(any(), any()) } returns emptyList()
    useCase = IsTagInUseUseCase(tagAssignmentRepository)
  }

  @Test
  fun `is not in use when the tag has no assignments`() =
    runTest {
      assertEquals(false, useCase("1"))
    }

  @Test
  fun `is in use when any item type has an assignment`() =
    runTest {
      coEvery { tagAssignmentRepository.getItemIdsForTag("1", TaggedItemType.GOOGLE_TASK) } returns listOf("t")

      assertEquals(true, useCase("1"))
    }

  @Test
  fun `anyInUse is true when only one of the tags is assigned`() =
    runTest {
      coEvery { tagAssignmentRepository.getItemIdsForTag("2", TaggedItemType.NOTE) } returns listOf("n")

      assertEquals(true, useCase.anyInUse(listOf("1", "2")))
    }

  @Test
  fun `anyInUse is false when none of the tags is assigned`() =
    runTest {
      assertEquals(false, useCase.anyInUse(listOf("1", "2")))
    }
}
