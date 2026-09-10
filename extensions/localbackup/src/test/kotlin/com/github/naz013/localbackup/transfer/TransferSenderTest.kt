package com.github.naz013.localbackup.transfer

import android.content.Context
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.localbackup.BuildBackupEnvelopeUseCase
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.NoteRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransferSenderTest {

  private fun sender(applicationId: String): TransferSender {
    val buildInfo = mockk<BuildInfo> { every { this@mockk.applicationId } returns applicationId }
    return TransferSender(
      context = mockk<Context>(relaxed = true),
      buildInfo = buildInfo,
      buildBackupEnvelopeUseCase = mockk<BuildBackupEnvelopeUseCase>(relaxed = true),
      noteRepository = mockk<NoteRepository>(relaxed = true),
      transferPackageWriter = mockk<TransferPackageWriter>(relaxed = true)
    )
  }

  @Test
  fun `free build targets the PRO package`() {
    assertEquals(SystemInfo.PRO_PACKAGE_NAME, sender(SystemInfo.FREE_PACKAGE_NAME).counterpartPackageName())
  }

  @Test
  fun `PRO build targets the free package`() {
    assertEquals(SystemInfo.FREE_PACKAGE_NAME, sender(SystemInfo.PRO_PACKAGE_NAME).counterpartPackageName())
  }

  @Test
  fun `an unrecognized applicationId has no counterpart`() {
    assertNull(sender("com.example.other").counterpartPackageName())
  }
}
