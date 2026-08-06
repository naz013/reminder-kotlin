package com.github.naz013.legal

interface LegalDocumentRepository {
  fun getDocument(type: LegalDocumentType): LegalDocument

  suspend fun refresh()

  fun hasUpdate(type: LegalDocumentType): Boolean

  fun markSeen(type: LegalDocumentType)

  fun resetSeen(type: LegalDocumentType)
}
