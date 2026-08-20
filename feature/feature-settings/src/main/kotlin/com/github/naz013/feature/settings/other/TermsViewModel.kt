package com.github.naz013.feature.settings.other

import androidx.lifecycle.ViewModel
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType

internal class TermsViewModel(
  private val legalDocumentRepository: LegalDocumentRepository,
) : ViewModel() {
  val url: String
    get() = legalDocumentRepository.getDocument(LegalDocumentType.TERMS_OF_USE).url
}
