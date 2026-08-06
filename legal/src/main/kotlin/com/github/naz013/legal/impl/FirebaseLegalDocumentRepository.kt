package com.github.naz013.legal.impl

import android.content.Context
import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType
import com.github.naz013.logging.Logger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

internal class FirebaseLegalDocumentRepository(
  context: Context,
) : LegalDocumentRepository {

  private val localStore = LegalDocsLocalStore(context)
  private val gson = Gson()
  private val remoteConfig: FirebaseRemoteConfig? =
    runCatching { FirebaseRemoteConfig.getInstance() }.getOrNull()

  override fun getDocument(type: LegalDocumentType): LegalDocument =
    localStore.getDocument(type) ?: fallback(type)

  override suspend fun refresh() {
    val config = remoteConfig ?: return
    runCatching { config.fetchAndActivate().await() }
      .onFailure { Logger.e(TAG, "Failed to fetch legal document config", it) }
    LegalDocumentType.entries.forEach { type -> readDocument(config, type) }
  }

  override fun hasUpdate(type: LegalDocumentType): Boolean =
    getDocument(type).version > localStore.getSeenVersion(type)

  override fun markSeen(type: LegalDocumentType) {
    localStore.setSeenVersion(type, getDocument(type).version)
  }

  override fun resetSeen(type: LegalDocumentType) {
    localStore.setSeenVersion(type, 0)
  }

  private fun readDocument(config: FirebaseRemoteConfig, type: LegalDocumentType) {
    val json = config.getString(remoteConfigKey(type))
    val metadata = runCatching { gson.fromJson(json, LegalDocumentMetadata::class.java) }.getOrNull()
    Logger.d(TAG, "readDocument: type=$type, json=$json, metadata=$metadata")
    if (metadata != null && metadata.url.isNotBlank()) {
      localStore.saveDocument(LegalDocument(type, metadata.url, metadata.version))
    }
  }

  private fun fallback(type: LegalDocumentType): LegalDocument =
    when (type) {
      LegalDocumentType.PRIVACY_POLICY -> LegalDocument(type, DEFAULT_PRIVACY_POLICY_URL, INITIAL_VERSION)
      LegalDocumentType.TERMS_OF_USE -> LegalDocument(type, DEFAULT_TERMS_URL, INITIAL_VERSION)
    }

  private fun remoteConfigKey(type: LegalDocumentType): String =
    when (type) {
      LegalDocumentType.PRIVACY_POLICY -> "privacy_policy_document"
      LegalDocumentType.TERMS_OF_USE -> "terms_of_use_document"
    }

  companion object {
    private const val TAG = "LegalDocumentRepository"
    private const val DEFAULT_PRIVACY_POLICY_URL = "https://future-graph-651.web.app/privacy-policy.html"
    private const val DEFAULT_TERMS_URL = "https://future-graph-651.web.app/terms-of-use.html"

    // Must match the "Version" badge in legal-docs/public/*.html for whichever content is
    // currently live at the URLs above - bump this only when you also bump the Remote Config
    // version (see legal-docs/README.md) so hasUpdate() doesn't fire for content users already saw.
    private const val INITIAL_VERSION = 1
  }
}
