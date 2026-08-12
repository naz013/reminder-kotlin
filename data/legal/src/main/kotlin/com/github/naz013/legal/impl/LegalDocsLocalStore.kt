package com.github.naz013.legal.impl

import android.content.Context
import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentType

internal class LegalDocsLocalStore(
  context: Context,
) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun getDocument(type: LegalDocumentType): LegalDocument? {
    val url = prefs.getString(urlKey(type), null) ?: return null
    val version = prefs.getInt(versionKey(type), 0)
    return LegalDocument(type, url, version)
  }

  fun saveDocument(document: LegalDocument) {
    prefs.edit()
      .putString(urlKey(document.type), document.url)
      .putInt(versionKey(document.type), document.version)
      .apply()
  }

  fun getSeenVersion(type: LegalDocumentType): Int = prefs.getInt(seenVersionKey(type), 0)

  fun setSeenVersion(type: LegalDocumentType, version: Int) {
    prefs.edit().putInt(seenVersionKey(type), version).apply()
  }

  private fun urlKey(type: LegalDocumentType) = "${type.name}_url"

  private fun versionKey(type: LegalDocumentType) = "${type.name}_version"

  private fun seenVersionKey(type: LegalDocumentType) = "${type.name}_seen_version"

  companion object {
    private const val PREFS_NAME = "legal_docs_prefs"
  }
}
