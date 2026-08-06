package com.elementary.tasks.settings.other

import androidx.lifecycle.ViewModel
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType

class PrivacyPolicyViewModel(
  private val legalDocumentRepository: LegalDocumentRepository,
) : ViewModel() {
  val url: String
    get() = legalDocumentRepository.getDocument(LegalDocumentType.PRIVACY_POLICY).url
}
