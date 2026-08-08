package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.BaseTest
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.ReadyState
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.elementary.tasks.reminder.build.reminder.compose.ComposedRecurrence
import com.elementary.tasks.reminder.build.reminder.compose.ReminderActionCalculator
import com.elementary.tasks.reminder.build.reminder.compose.RecurrenceRuleCalculator
import com.elementary.tasks.reminder.build.reminder.validation.ReminderValidator
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class BuilderErrorFinderTest : BaseTest() {
  private val builderStateCalculator = mockk<BuilderStateCalculator>()
  private val reminderValidator = mockk<ReminderValidator>()
  private val recurrenceRuleCalculator = mockk<RecurrenceRuleCalculator>()
  private val reminderActionCalculator = mockk<ReminderActionCalculator>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)

  private lateinit var finder: BuilderErrorFinder

  @Before
  override fun setUp() {
    super.setUp()
    finder = BuilderErrorFinder(builderStateCalculator, reminderValidator, recurrenceRuleCalculator, reminderActionCalculator)
    every { builderStateCalculator(any()) } returns ReadyState
    every { recurrenceRuleCalculator(any()) } returns
      ComposedRecurrence(
        rule = RecurrenceRule.Once,
        schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      )
  }

  private fun reminder() = ReminderV2(schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  private fun invoke(items: List<BuilderItem<*>>): BuilderError = finder(reminder(), items)

  @Test
  fun `a TARGET failure with an Email action requests the Email item, not Date+Time`() {
    every { reminderValidator(any()) } returns
      ReminderValidator.ValidationResult.Failed(ReminderValidator.ValidationError.TARGET)
    val items =
      listOf(
        SummaryBuilderItem(title = "Summary", description = null),
        EmailBuilderItem(title = "Email", description = null),
      )

    val error = invoke(items)

    assertEquals(
      BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.EMAIL)),
      error,
    )
  }

  @Test
  fun `a TARGET failure with a Phone Call action requests the Phone Call item`() {
    every { reminderValidator(any()) } returns
      ReminderValidator.ValidationResult.Failed(ReminderValidator.ValidationError.TARGET)
    val items = listOf(PhoneCallBuilderItem(title = "Call", description = null))

    val error = invoke(items)

    assertEquals(
      BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.PHONE_CALL)),
      error,
    )
  }

  @Test
  fun `a SUB_TASKS failure requests the Shopping list item, not Date+Time`() {
    every { reminderValidator(any()) } returns
      ReminderValidator.ValidationResult.Failed(ReminderValidator.ValidationError.SUB_TASKS)
    val items =
      listOf(
        SummaryBuilderItem(title = "Summary", description = null),
        SubTasksBuilderItem(
          title = "Shopping list",
          description = null,
          shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true),
          dateTimeManager = dateTimeManager,
        ),
      )

    val error = invoke(items)

    assertEquals(
      BuilderError.RequiresBiType(BuilderError.BiTypeCollection.Single(BiType.SUB_TASKS)),
      error,
    )
  }

  @Test
  fun `an EVENT_TIME failure with only Summary requests Date and Time`() {
    every { reminderValidator(any()) } returns
      ReminderValidator.ValidationResult.Failed(ReminderValidator.ValidationError.EVENT_TIME)
    val items = listOf(SummaryBuilderItem(title = "Summary", description = null))

    val error = invoke(items)

    assertEquals(
      BuilderError.RequiresBiType(
        BuilderError.BiTypeCollection.Multiple.And(
          BiType.DATE,
          BiType.TIME,
        ),
      ),
      error,
    )
  }

  @Test
  fun `no validation failure is Unknown`() {
    every { reminderValidator(any()) } returns ReminderValidator.ValidationResult.Success

    val error = invoke(listOf(SummaryBuilderItem(title = "Summary", description = null)))

    assertEquals(BuilderError.Unknown, error)
  }
}
