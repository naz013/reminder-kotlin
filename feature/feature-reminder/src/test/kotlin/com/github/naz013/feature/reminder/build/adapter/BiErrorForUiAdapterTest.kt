package com.github.naz013.feature.reminder.build.adapter

import android.content.Context
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import com.github.naz013.domain.reminder.BiType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BiErrorForUiAdapterTest : BaseTest() {
  private val context = mockk<Context>()
  private val biTypeForUiAdapter = mockk<BiTypeForUiAdapter>(relaxed = true)
  private lateinit var adapter: BiErrorForUiAdapter

  @Before
  override fun setUp() {
    super.setUp()
    adapter = BiErrorForUiAdapter(context, biTypeForUiAdapter)
    every { context.getString(R.string.builder_invalid_email_value) } returns "Enter a valid email address"
    every { context.getString(R.string.builder_invalid_web_address_value) } returns "Enter a valid web address"
    every { context.getString(R.string.builder_invalid_phone_number_value) } returns "Enter a valid phone number"
    every { context.getString(R.string.builder_shopping_list_empty_value) } returns "Add at least one item to the list"
    every { context.getString(R.string.builder_invalid_value_generic) } returns "This value isn't valid"
  }

  @Test
  fun `getInvalidValueMessage returns the email message for EMAIL`() {
    assertEquals("Enter a valid email address", adapter.getInvalidValueMessage(BiType.EMAIL))
  }

  @Test
  fun `getInvalidValueMessage returns the web address message for LINK`() {
    assertEquals("Enter a valid web address", adapter.getInvalidValueMessage(BiType.LINK))
  }

  @Test
  fun `getInvalidValueMessage returns the phone number message for PHONE_CALL and SMS`() {
    assertEquals("Enter a valid phone number", adapter.getInvalidValueMessage(BiType.PHONE_CALL))
    assertEquals("Enter a valid phone number", adapter.getInvalidValueMessage(BiType.SMS))
  }

  @Test
  fun `getInvalidValueMessage returns the empty-list message for SUB_TASKS`() {
    assertEquals("Add at least one item to the list", adapter.getInvalidValueMessage(BiType.SUB_TASKS))
  }

  @Test
  fun `getInvalidValueMessage falls back to a generic message for other types`() {
    assertEquals("This value isn't valid", adapter.getInvalidValueMessage(BiType.DATE))
  }
}
