package com.elementary.tasks.settings.other

import com.elementary.tasks.BaseTest
import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TermsViewModelTest : BaseTest() {
  private val legalDocumentRepository = mockk<LegalDocumentRepository>()

  private lateinit var viewModel: TermsViewModel

  @Before
  override fun setUp() {
    super.setUp()

    every { legalDocumentRepository.getDocument(LegalDocumentType.TERMS_OF_USE) } returns
      LegalDocument(
        type = LegalDocumentType.TERMS_OF_USE,
        url = "https://example.com/terms",
        version = 1,
      )

    viewModel = TermsViewModel(legalDocumentRepository)
  }

  @Test
  fun `url returns the terms of use document url`() {
    assertEquals("https://example.com/terms", viewModel.url)
    verify { legalDocumentRepository.getDocument(LegalDocumentType.TERMS_OF_USE) }
  }
}
