package com.github.naz013.feature.reminder.build.reminder.compose

import com.github.naz013.testing.BaseTest
import com.github.naz013.feature.reminder.build.ApplicationBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.EmailBuilderItem
import com.github.naz013.feature.reminder.build.EmailSubjectBuilderItem
import com.github.naz013.feature.reminder.build.PhoneCallBuilderItem
import com.github.naz013.feature.reminder.build.SmsBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.WebAddressBuilderItem
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderAction
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

class ReminderActionCalculatorTest : BaseTest() {
  private val calculator = ReminderActionCalculator()

  @Before
  override fun setUp() {
    super.setUp()
    ensureWebUrlPatternIsUsable()
  }

  // android.util.Patterns.WEB_URL is a real static final field left null by the android-stub jar
  // used for JVM unit tests (isReturnDefaultValues only affects stub *method* bodies, not static
  // field initializers) - WebAddressModifier.isCorrect() dereferences it, so it needs patching to
  // a real Pattern first. Same fix as NoteEditViewModelTestSupport.ensureWebUrlPatternIsUsable().
  private fun ensureWebUrlPatternIsUsable() {
    val field = android.util.Patterns::class.java.getDeclaredField("WEB_URL")
    field.isAccessible = true
    val existing = field.get(null)
    if (existing != null) return
    val unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe")
    unsafeField.isAccessible = true
    val unsafe = unsafeField.get(null)
    val unsafeClass = unsafe.javaClass
    val base = unsafeClass.getMethod("staticFieldBase", java.lang.reflect.Field::class.java).invoke(unsafe, field)
    val offset = unsafeClass.getMethod("staticFieldOffset", java.lang.reflect.Field::class.java).invoke(unsafe, field) as Long
    val pattern = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].\\S*$", Pattern.CASE_INSENSITIVE)
    unsafeClass.getMethod("putObject", Any::class.java, Long::class.javaPrimitiveType, Any::class.java)
      .invoke(unsafe, base, offset, pattern)
  }

  @Test
  fun `no action items produces None`() {
    val result = calculator(itemsOf())

    assertEquals(ReminderAction.None, result)
  }

  @Test
  fun `phone call item produces Call`() {
    val item =
      PhoneCallBuilderItem(title = "call", description = null).apply {
        modifier.update("+123456")
      }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.Call("+123456"), result)
  }

  @Test
  fun `sms item produces Sms with an empty subject`() {
    val item = SmsBuilderItem(title = "sms", description = null).apply { modifier.update("+123456") }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.Sms("+123456", ""), result)
  }

  @Test
  fun `email item without a subject item produces Email with an empty subject`() {
    val item = EmailBuilderItem(title = "email", description = null).apply { modifier.update("a@b.com") }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.Email("a@b.com", ""), result)
  }

  @Test
  fun `email item with a subject item produces Email carrying that subject`() {
    val email = EmailBuilderItem(title = "email", description = null).apply { modifier.update("a@b.com") }
    val subject = EmailSubjectBuilderItem(title = "subj", description = null).apply { modifier.update("Hello") }

    val result = calculator(itemsOf(email, subject))

    assertEquals(ReminderAction.Email("a@b.com", "Hello"), result)
  }

  @Test
  fun `link item produces Link`() {
    val item = WebAddressBuilderItem(title = "link", description = null).apply { modifier.update("https://x.test") }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.Link("https://x.test"), result)
  }

  @Test
  fun `application item produces App`() {
    val item =
      ApplicationBuilderItem(title = "app", description = null, applicationFormatter = mockk(relaxed = true)).apply {
        modifier.update("com.example.app")
      }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.App("com.example.app"), result)
  }

  @Test
  fun `sub tasks item produces Shopping`() {
    val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
    val item =
      SubTasksBuilderItem(
        title = "shop",
        description = null,
        shopItemsFormatter = mockk(relaxed = true),
        dateTimeManager = dateTimeManager,
      ).apply {
        modifier.update(listOf(mockk(relaxed = true)))
      }

    val result = calculator(itemsOf(item))

    assertEquals(ReminderAction.Shopping, result)
  }

  private fun itemsOf(vararg items: BuilderItem<*>) = ProcessedBuilderItems(items.toList()).typeMap
}
