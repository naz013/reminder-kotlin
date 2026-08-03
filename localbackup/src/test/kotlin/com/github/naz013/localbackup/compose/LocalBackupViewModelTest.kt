package com.github.naz013.localbackup.compose

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.localbackup.ImportSummary
import com.github.naz013.localbackup.LocalBackupApi
import com.github.naz013.localbackup.R
import com.github.naz013.localbackup.WrongPassphraseException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class LocalBackupViewModelTest {

  private val localBackupApi = mockk<LocalBackupApi>()
  private val context = mockk<Context>(relaxed = true)
  private val contentResolver = mockk<ContentResolver>()
  private val uri = mockk<Uri>()

  private fun dispatcherProvider(): DispatcherProvider {
    val provider = mockk<DispatcherProvider>()
    every { provider.io() }.returns(Dispatchers.Unconfined)
    every { provider.main() }.returns(Dispatchers.Unconfined)
    return provider
  }

  @Before
  fun setUp() {
    mockkStatic(Uri::class)
    every { Uri.parse("content://backup") } returns uri
    every { context.contentResolver } returns contentResolver
  }

  @After
  fun tearDown() {
    unmockkStatic(Uri::class)
  }

  private fun viewModel(mode: LocalBackupMode) =
    LocalBackupViewModel("content://backup", mode, dispatcherProvider(), context, localBackupApi)

  @Test
  fun `export shows an error when the passphrase is blank`() {
    val viewModel = viewModel(LocalBackupMode.EXPORT)

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.passphraseError)
  }

  @Test
  fun `export shows an error when the passphrases don't match`() {
    val viewModel = viewModel(LocalBackupMode.EXPORT)
    viewModel.onPassphraseChange("one")
    viewModel.onConfirmPassphraseChange("two")

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.passphraseError)
  }

  @Test
  fun `export succeeds when the api call succeeds`() = runTest {
    every { contentResolver.openOutputStream(uri) } returns ByteArrayOutputStream()
    coEvery { localBackupApi.export(any(), any()) } returns Result.success(Unit)

    val viewModel = viewModel(LocalBackupMode.EXPORT)
    viewModel.onPassphraseChange("passphrase")
    viewModel.onConfirmPassphraseChange("passphrase")

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.status is LocalBackupStatus.Success)
  }

  @Test
  fun `export surfaces a failure status when the api call fails`() = runTest {
    every { contentResolver.openOutputStream(uri) } returns ByteArrayOutputStream()
    coEvery { localBackupApi.export(any(), any()) } returns Result.failure(RuntimeException("disk full"))

    val viewModel = viewModel(LocalBackupMode.EXPORT)
    viewModel.onPassphraseChange("passphrase")
    viewModel.onConfirmPassphraseChange("passphrase")

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.status is LocalBackupStatus.Error)
  }

  @Test
  fun `import shows an error when the passphrase is blank`() {
    val viewModel = viewModel(LocalBackupMode.IMPORT)

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.passphraseError)
  }

  @Test
  fun `import succeeds and reports the total item count`() = runTest {
    every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(ByteArray(0))
    coEvery { localBackupApi.import(any(), any()) } returns Result.success(
      ImportSummary(
        remindersImported = 2,
        groupsImported = 1,
        birthdaysImported = 0,
        placesImported = 0,
        presetsImported = 0,
        tagsImported = 0,
        tagAssignmentsImported = 0
      )
    )

    val viewModel = viewModel(LocalBackupMode.IMPORT)
    viewModel.onPassphraseChange("passphrase")

    viewModel.onActionClick()

    assertTrue(viewModel.state.value.status is LocalBackupStatus.Success)
  }

  @Test
  fun `import surfaces the wrong passphrase error distinctly`() = runTest {
    every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(ByteArray(0))
    coEvery { localBackupApi.import(any(), any()) } returns Result.failure(WrongPassphraseException())

    val viewModel = viewModel(LocalBackupMode.IMPORT)
    viewModel.onPassphraseChange("wrong")

    viewModel.onActionClick()

    val status = viewModel.state.value.status as LocalBackupStatus.Error
    assertEquals(R.string.backup_wrong_passphrase, status.messageRes)
  }
}
