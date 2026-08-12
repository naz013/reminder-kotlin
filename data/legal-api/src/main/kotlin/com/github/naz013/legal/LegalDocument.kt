package com.github.naz013.legal

data class LegalDocument(
  val type: LegalDocumentType,
  val url: String,
  val version: Int,
)
