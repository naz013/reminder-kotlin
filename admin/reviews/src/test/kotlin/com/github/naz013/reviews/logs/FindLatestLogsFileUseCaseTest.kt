package com.github.naz013.reviews.logs

import android.content.Context
import com.github.naz013.common.ContextProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FindLatestLogsFileUseCaseTest {

  @get:Rule
  val tempFolder = TemporaryFolder()

  private lateinit var contextProvider: ContextProvider
  private lateinit var context: Context
  private lateinit var useCase: FindLatestLogsFileUseCase

  @Before
  fun setUp() {
    context = mockk(relaxed = true)
    contextProvider = mockk(relaxed = true)
    useCase = FindLatestLogsFileUseCase(contextProvider)
  }

  @Test
  fun `invoke returns null when log directory does not exist`() = runTest {
    // Given
    val dataDir = tempFolder.newFolder("data")
    every { contextProvider.context } returns context
    every { context.filesDir } returns dataDir

    // When
    val result = useCase.invoke()

    // Then
    assertNull(result)
  }

  @Test
  fun `invoke returns null when log directory is empty`() = runTest {
    // Given
    val dataDir = tempFolder.newFolder("data")
    val logDir = File(dataDir, "log")
    logDir.mkdirs()

    every { contextProvider.context } returns context
    every { context.filesDir } returns dataDir

    // When
    val result = useCase.invoke()

    // Then
    assertNull(result)
  }

  @Test
  fun `invoke returns latest log file when multiple files exist`() = runTest {
    // Given
    val dataDir = tempFolder.newFolder("data")
    val logDir = File(dataDir, "log")
    logDir.mkdirs()

    // Create log files with different modification times
    val oldFile = File(logDir, "2024-01-01.0.log").apply {
      createNewFile()
      setLastModified(1000L)
    }

    val newerFile = File(logDir, "2024-01-02.0.log").apply {
      createNewFile()
      setLastModified(2000L)
    }

    val latestFile = File(logDir, "2024-01-03.0.log").apply {
      createNewFile()
      setLastModified(3000L)
    }

    every { contextProvider.context } returns context
    every { context.filesDir } returns dataDir

    // When
    val result = useCase.invoke()

    // Then
    assertEquals(latestFile.absolutePath, result)
  }

  @Test
  fun `invoke ignores non-log files`() = runTest {
    // Given
    val dataDir = tempFolder.newFolder("data")
    val logDir = File(dataDir, "log")
    logDir.mkdirs()

    // Create a log file
    val logFile = File(logDir, "2024-01-01.0.log").apply {
      createNewFile()
      setLastModified(1000L)
    }

    // Create a non-log file with a more recent timestamp
    File(logDir, "readme.txt").apply {
      createNewFile()
      setLastModified(5000L)
    }

    every { contextProvider.context } returns context
    every { context.filesDir } returns dataDir

    // When
    val result = useCase.invoke()

    // Then
    assertEquals(logFile.absolutePath, result)
  }

  @Test
  fun `invoke handles single log file correctly`() = runTest {
    // Given
    val dataDir = tempFolder.newFolder("data")
    val logDir = File(dataDir, "log")
    logDir.mkdirs()

    val singleFile = File(logDir, "2024-01-01.0.log").apply {
      createNewFile()
    }

    every { contextProvider.context } returns context
    every { context.filesDir } returns dataDir

    // When
    val result = useCase.invoke()

    // Then
    assertEquals(singleFile.absolutePath, result)
  }
}

