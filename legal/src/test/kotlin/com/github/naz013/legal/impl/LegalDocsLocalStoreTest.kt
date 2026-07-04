package com.github.naz013.legal.impl

import android.content.Context
import android.content.SharedPreferences
import com.github.naz013.legal.LegalDocument
import com.github.naz013.legal.LegalDocumentType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LegalDocsLocalStoreTest {
  private val values = mutableMapOf<String, Any?>()
  private lateinit var localStore: LegalDocsLocalStore

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

    localStore = LegalDocsLocalStore(context)
  }

  @Test
  fun `getDocument returns null when nothing was saved yet`() {
    val result = localStore.getDocument(LegalDocumentType.PRIVACY_POLICY)

    assertNull(result)
  }

  @Test
  fun `getDocument returns the url and version passed to saveDocument`() {
    localStore.saveDocument(
      LegalDocument(LegalDocumentType.TERMS_OF_USE, "https://example.com/terms.html", 3),
    )

    val result = localStore.getDocument(LegalDocumentType.TERMS_OF_USE)

    assertEquals("https://example.com/terms.html", result?.url)
    assertEquals(3, result?.version)
  }

  @Test
  fun `getSeenVersion defaults to zero before any version has been marked seen`() {
    val result = localStore.getSeenVersion(LegalDocumentType.PRIVACY_POLICY)

    assertEquals(0, result)
  }

  @Test
  fun `setSeenVersion persists the version for later reads`() {
    localStore.setSeenVersion(LegalDocumentType.PRIVACY_POLICY, 2)

    val result = localStore.getSeenVersion(LegalDocumentType.PRIVACY_POLICY)

    assertEquals(2, result)
  }

  @Test
  fun `saving a document for one type does not affect the other type`() {
    localStore.saveDocument(
      LegalDocument(LegalDocumentType.PRIVACY_POLICY, "https://example.com/privacy.html", 5),
    )

    val termsOfUse = localStore.getDocument(LegalDocumentType.TERMS_OF_USE)

    assertNull(termsOfUse)
  }
}
