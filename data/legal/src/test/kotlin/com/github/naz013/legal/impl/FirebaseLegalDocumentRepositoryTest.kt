package com.github.naz013.legal.impl

import android.content.Context
import android.content.SharedPreferences
import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseLegalDocumentRepositoryTest {
  private val values = mutableMapOf<String, Any?>()
  private lateinit var repository: FirebaseLegalDocumentRepository

  @Before
  fun setUp() {
    values.clear()

    val editor = mockk<SharedPreferences.Editor>()
    every { editor.putString(any(), any()) } answers {
      values[firstArg()] = secondArg<String>()
      editor
    }
    every { editor.putInt(any(), any()) } answers {
      values[firstArg()] = secondArg<Int>()
      editor
    }
    every { editor.apply() } answers { }

    val sharedPreferences = mockk<SharedPreferences>()
    every { sharedPreferences.edit() } returns editor
    every { sharedPreferences.getString(any(), any()) } answers {
      values[firstArg()] as? String ?: secondArg()
    }
    every { sharedPreferences.getInt(any(), any()) } answers {
      values[firstArg()] as? Int ?: secondArg()
    }

    val context = mockk<Context>()
    every { context.applicationContext } returns context
    every { context.getSharedPreferences(any(), any()) } returns sharedPreferences

    repository = FirebaseLegalDocumentRepository(context)
  }

  @Test
  fun `getDocument falls back to the default privacy policy url when nothing was saved`() {
    val document = repository.getDocument(LegalDocumentType.PRIVACY_POLICY)

    assertEquals(LegalDocumentType.PRIVACY_POLICY, document.type)
    assertTrue(document.url.contains("privacy-policy"))
  }

  @Test
  fun `getDocument falls back to the default terms of use url when nothing was saved`() {
    val document = repository.getDocument(LegalDocumentType.TERMS_OF_USE)

    assertTrue(document.url.contains("terms-of-use"))
  }

  @Test
  fun `hasUpdate is false when the seen version matches the current version`() {
    repository.markSeen(LegalDocumentType.PRIVACY_POLICY)

    assertFalse(repository.hasUpdate(LegalDocumentType.PRIVACY_POLICY))
  }

  @Test
  fun `hasUpdate is true once a newer document version is saved`() {
    values["${LegalDocumentType.PRIVACY_POLICY.name}_seen_version"] = 1
    values["${LegalDocumentType.PRIVACY_POLICY.name}_url"] = "https://example.com/privacy.html"
    values["${LegalDocumentType.PRIVACY_POLICY.name}_version"] = 2

    assertTrue(repository.hasUpdate(LegalDocumentType.PRIVACY_POLICY))
  }

  @Test
  fun `resetSeen clears the seen version back to zero`() {
    repository.markSeen(LegalDocumentType.TERMS_OF_USE)

    repository.resetSeen(LegalDocumentType.TERMS_OF_USE)

    assertEquals(0, values["${LegalDocumentType.TERMS_OF_USE.name}_seen_version"])
  }

  @Test
  fun `getDocument prefers a saved document over the fallback`() {
    values["${LegalDocumentType.PRIVACY_POLICY.name}_url"] = "https://example.com/custom.html"
    values["${LegalDocumentType.PRIVACY_POLICY.name}_version"] = 5

    val document = repository.getDocument(LegalDocumentType.PRIVACY_POLICY)

    assertEquals(LegalDocument(LegalDocumentType.PRIVACY_POLICY, "https://example.com/custom.html", 5), document)
  }
}
