package com.github.naz013.appfunctions.note

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.repository.NoteRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateSimpleNoteUseCaseTest {

  private val noteRepository = mockk<NoteRepository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = CreateSimpleNoteUseCase(noteRepository, dateTimeManager)

  @Test
  fun `invoke saves a note with the title, content and current date`() = runTest {
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-08-01 00:00:00.000+0000"

    val result = useCase(title = "Wi-Fi password", content = "hunter2")

    assertEquals("Wi-Fi password", result.title)
    assertEquals("hunter2", result.summary)
    assertEquals("2026-08-01 00:00:00.000+0000", result.date)
    coVerify { noteRepository.save(result) }
  }
}
