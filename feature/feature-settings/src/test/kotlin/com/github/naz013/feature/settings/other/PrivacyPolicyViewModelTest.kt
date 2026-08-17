package com.github.naz013.feature.settings.other

import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.testing.BaseTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PrivacyPolicyViewModelTest : BaseTest() {
  private val legalDocumentRepository = mockk<LegalDocumentRepository>()

  private lateinit var viewModel: PrivacyPolicyViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { legalDocumentRepository.getDocument(LegalDocumentType.PRIVACY_POLICY) } returns
      LegalDocument(
        type = LegalDocumentType.PRIVACY_POLICY,
        url = "https://example.com/privacy",
        version = 1,
      )

    viewModel = PrivacyPolicyViewModel(legalDocumentRepository)
  }

  @Test
  fun `url returns the privacy policy document url`() {
    assertEquals("https://example.com/privacy", viewModel.url)
    verify { legalDocumentRepository.getDocument(LegalDocumentType.PRIVACY_POLICY) }
  }
}
