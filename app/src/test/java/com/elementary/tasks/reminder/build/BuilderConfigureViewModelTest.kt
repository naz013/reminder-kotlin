package com.elementary.tasks.reminder.build

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class BuilderConfigureViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>(relaxed = true)
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>()

  private lateinit var viewModel: BuilderConfigureViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.reminderCreatorParams } returns ReminderCreatorConfig()
    every { googleTasksAuthManager.isAuthorized() } returns true

    viewModel = BuilderConfigureViewModel(prefs = prefs, googleTasksAuthManager = googleTasksAuthManager)
  }

  @Test
  fun `isTasksRowVisible reflects google tasks authorization`() {
    assertEquals(true, viewModel.state.value.isTasksRowVisible)
  }

  @Test
  fun `onSummaryToggle flips isSummaryChecked and persists`() {
    val before = viewModel.state.value.isSummaryChecked

    viewModel.onSummaryToggle()

    assertNotEquals(before, viewModel.state.value.isSummaryChecked)
    verify { prefs.reminderCreatorParams = any() }
  }

  @Test
  fun `onBeforeToggle flips isBeforeChecked`() {
    val before = viewModel.state.value.isBeforeChecked

    viewModel.onBeforeToggle()

    assertNotEquals(before, viewModel.state.value.isBeforeChecked)
  }

  @Test
  fun `onRepeatToggle flips isRepeatChecked`() {
    val before = viewModel.state.value.isRepeatChecked

    viewModel.onRepeatToggle()

    assertNotEquals(before, viewModel.state.value.isRepeatChecked)
  }

  @Test
  fun `onRepeatLimitToggle flips isRepeatLimitChecked`() {
    val before = viewModel.state.value.isRepeatLimitChecked

    viewModel.onRepeatLimitToggle()

    assertNotEquals(before, viewModel.state.value.isRepeatLimitChecked)
  }

  @Test
  fun `onPriorityToggle flips isPriorityChecked`() {
    val before = viewModel.state.value.isPriorityChecked

    viewModel.onPriorityToggle()

    assertNotEquals(before, viewModel.state.value.isPriorityChecked)
  }

  @Test
  fun `onAttachmentToggle flips isAttachmentChecked`() {
    val before = viewModel.state.value.isAttachmentChecked

    viewModel.onAttachmentToggle()

    assertNotEquals(before, viewModel.state.value.isAttachmentChecked)
  }

  @Test
  fun `onCalendarToggle flips isCalendarChecked`() {
    val before = viewModel.state.value.isCalendarChecked

    viewModel.onCalendarToggle()

    assertNotEquals(before, viewModel.state.value.isCalendarChecked)
  }

  @Test
  fun `onTasksToggle flips isTasksChecked`() {
    val before = viewModel.state.value.isTasksChecked

    viewModel.onTasksToggle()

    assertNotEquals(before, viewModel.state.value.isTasksChecked)
  }

  @Test
  fun `onExtraToggle flips isExtraChecked`() {
    val before = viewModel.state.value.isExtraChecked

    viewModel.onExtraToggle()

    assertNotEquals(before, viewModel.state.value.isExtraChecked)
  }

  @Test
  fun `onLedToggle flips isLedChecked`() {
    val before = viewModel.state.value.isLedChecked

    viewModel.onLedToggle()

    assertNotEquals(before, viewModel.state.value.isLedChecked)
  }

  @Test
  fun `onICalendarToggle flips isICalendarChecked`() {
    val before = viewModel.state.value.isICalendarChecked

    viewModel.onICalendarToggle()

    assertNotEquals(before, viewModel.state.value.isICalendarChecked)
  }

  @Test
  fun `onMakeCallToggle flips isMakeCallChecked`() {
    val before = viewModel.state.value.isMakeCallChecked

    viewModel.onMakeCallToggle()

    assertNotEquals(before, viewModel.state.value.isMakeCallChecked)
  }

  @Test
  fun `onSendSmsToggle flips isSendSmsChecked`() {
    val before = viewModel.state.value.isSendSmsChecked

    viewModel.onSendSmsToggle()

    assertNotEquals(before, viewModel.state.value.isSendSmsChecked)
  }

  @Test
  fun `onOpenAppToggle flips isOpenAppChecked`() {
    val before = viewModel.state.value.isOpenAppChecked

    viewModel.onOpenAppToggle()

    assertNotEquals(before, viewModel.state.value.isOpenAppChecked)
  }

  @Test
  fun `onOpenLinkToggle flips isOpenLinkChecked`() {
    val before = viewModel.state.value.isOpenLinkChecked

    viewModel.onOpenLinkToggle()

    assertNotEquals(before, viewModel.state.value.isOpenLinkChecked)
  }

  @Test
  fun `onSendEmailToggle flips isSendEmailChecked`() {
    val before = viewModel.state.value.isSendEmailChecked

    viewModel.onSendEmailToggle()

    assertNotEquals(before, viewModel.state.value.isSendEmailChecked)
  }

  @Test
  fun `toggling twice returns to the original value`() {
    val before = viewModel.state.value.isSummaryChecked

    viewModel.onSummaryToggle()
    viewModel.onSummaryToggle()

    assertEquals(before, viewModel.state.value.isSummaryChecked)
  }
}
