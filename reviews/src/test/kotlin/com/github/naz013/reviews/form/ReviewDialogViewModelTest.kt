package com.github.naz013.reviews.form

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.db.ReviewRepositoryImpl
import com.github.naz013.reviews.fileupload.LogFileUploader
import com.github.naz013.reviews.logs.FindLatestLogsFileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewDialogViewModelTest {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var reviewRepository: ReviewRepositoryImpl
  private lateinit var logFileUploader: LogFileUploader
  private lateinit var findLatestLogsFile: FindLatestLogsFileUseCase
  private lateinit var packageManagerWrapper: PackageManagerWrapper
  private lateinit var dispatcherProvider: DispatcherProvider
  private lateinit var viewModel: ReviewDialogViewModel

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    reviewRepository = mockk(relaxed = true)
    logFileUploader = mockk(relaxed = true)
    findLatestLogsFile = mockk(relaxed = true)
    packageManagerWrapper = mockk(relaxed = true)
    dispatcherProvider = mockk(relaxed = true)

    every { dispatcherProvider.default() } returns testDispatcher
    every { dispatcherProvider.main() } returns testDispatcher
    every { dispatcherProvider.io() } returns testDispatcher
    every { packageManagerWrapper.getVersionName() } returns "1.0.0"

    viewModel = ReviewDialogViewModel(
      reviewRepository,
      logFileUploader,
      findLatestLogsFile,
      packageManagerWrapper,
      dispatcherProvider
    )
  }

  @Test
  fun `submitReview with valid data succeeds without logs`() = runTest {
    // Given
    val rating = 4f
    val comment = "Great app!"
    val attachLog = false
    val email = "test@example.com"

    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify { reviewRepository.saveReview(any()) }
    coVerify(exactly = 0) { findLatestLogsFile() }
    assertNotNull(viewModel.submitSuccess.value)
    assertFalse(viewModel.isLoading.value ?: true)
  }

  @Test
  fun `submitReview with valid data and logs succeeds`() = runTest {
    // Given
    val rating = 5f
    val comment = "Excellent app with detailed logs!"
    val attachLog = true
    val email = null
    val logPath = "/path/to/log.log"
    val logUrl = "https://storage.firebase.com/log.zip"

    coEvery { findLatestLogsFile() } returns logPath
    coEvery { logFileUploader.upload(logPath) } returns Result.success(logUrl)
    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify { findLatestLogsFile() }
    coVerify { logFileUploader.upload(logPath) }
    coVerify { reviewRepository.saveReview(match { it.logFileUrl == logUrl }) }
    assertNotNull(viewModel.submitSuccess.value)
  }

  @Test
  fun `submitReview with invalid rating shows error`() = runTest {
    // Given
    val rating = 0f // Invalid
    val comment = "Test comment"
    val attachLog = false
    val email = null

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { reviewRepository.saveReview(any()) }
    assertNotNull(viewModel.submitError.value)
    assertTrue(
      viewModel.submitError.value?.peekContent()?.contains("Invalid rating") ?: false
    )
  }

  @Test
  fun `submitReview with blank comment shows error`() = runTest {
    // Given
    val rating = 3f
    val comment = "   " // Blank
    val attachLog = false
    val email = null

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { reviewRepository.saveReview(any()) }
    assertNotNull(viewModel.submitError.value)
    assertTrue(
      viewModel.submitError.value?.peekContent()?.contains("comment") ?: false
    )
  }

  @Test
  fun `submitReview handles repository failure`() = runTest {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = false
    val email = null
    val errorMessage = "Failed to save to Firestore"

    coEvery {
      reviewRepository.saveReview(any())
    } returns Result.failure(Exception(errorMessage))

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify { reviewRepository.saveReview(any()) }
    assertNotNull(viewModel.submitError.value)
    assertEquals(errorMessage, viewModel.submitError.value?.peekContent())
    assertFalse(viewModel.isLoading.value ?: true)
  }

  @Test
  fun `submitReview continues if log upload fails`() = runTest {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = true
    val email = null
    val logPath = "/path/to/log.log"

    coEvery { findLatestLogsFile() } returns logPath
    coEvery { logFileUploader.upload(logPath) } returns Result.failure(Exception("Upload failed"))
    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify { findLatestLogsFile() }
    coVerify { logFileUploader.upload(logPath) }
    coVerify { reviewRepository.saveReview(match { it.logFileUrl == null }) }
    assertNotNull(viewModel.submitSuccess.value)
  }

  @Test
  fun `submitReview continues if no log file found`() = runTest {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = true
    val email = null

    coEvery { findLatestLogsFile() } returns null
    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify { findLatestLogsFile() }
    coVerify(exactly = 0) { logFileUploader.upload(any()) }
    coVerify { reviewRepository.saveReview(match { it.logFileUrl == null }) }
    assertNotNull(viewModel.submitSuccess.value)
  }

  @Test
  fun `submitReview trims email whitespace`() = runTest {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = false
    val email = "  test@example.com  "

    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for coroutines to complete
    testScheduler.advanceUntilIdle()

    // Then
    coVerify {
      reviewRepository.saveReview(match { it.userEmail == "test@example.com" })
    }
  }

  @Test
  fun `submitReview completes successfully and resets loading state`() = runTest(testDispatcher) {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = false
    val email = null

    coEvery { reviewRepository.saveReview(any()) } returns Result.success(Unit)

    // When
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for completion
    testScheduler.advanceUntilIdle()

    // Then - verify the submission was successful
    assertNotNull("Submit success event should be triggered", viewModel.submitSuccess.value)

    // After completion, loading should be false (meaning not loading)
    // Note: with UnconfinedTestDispatcher, the coroutine completes synchronously
    assertEquals("Loading state should be false after completion", false, viewModel.isLoading.value)
  }

  @Test
  fun `submitReview prevents double submission`() = runTest {
    // Given
    val rating = 4f
    val comment = "Good app"
    val attachLog = false
    val email = null

    // Mock a slow operation
    coEvery { reviewRepository.saveReview(any()) } coAnswers {
      kotlinx.coroutines.delay(1000)
      Result.success(Unit)
    }

    // Set loading to true manually
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // When - try to submit again while loading
    viewModel.submitReview(rating, comment, attachLog, email, AppSource.PRO)

    // Wait for completion
    testScheduler.advanceUntilIdle()

    // Then - should only call save once
    coVerify(exactly = 1) { reviewRepository.saveReview(any()) }
  }
}
