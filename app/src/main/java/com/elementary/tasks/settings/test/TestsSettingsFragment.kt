package com.elementary.tasks.settings.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TestsSettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<TestsSettingsViewModel>()
  private val reviewsApi by inject<ReviewsApi>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    TestsSettingsScreen(
      state = state,
      onBirthdayDialogClick = viewModel::onBirthdayDialogClick,
      onReminderDialogClick = viewModel::onReminderDialogClick,
      onObjectExportClick = viewModel::onObjectExportClick,
      onReviewDialogClick = viewModel::onReviewDialogClick,
      onDeveloperOptionsClick = viewModel::onDeveloperOptionsClick,
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogConfirm = viewModel::onDialogConfirm,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun handleEvent(event: TestsSettingsEvent) {
    when (event) {
      TestsSettingsEvent.OpenObjectExport -> {
        safeNavigation(TestsSettingsFragmentDirections.actionTestsFragmentToObjectExportTestFragment())
      }

      TestsSettingsEvent.OpenDeveloperOptions -> {
        safeNavigation(TestsSettingsFragmentDirections.actionTestsFragmentToDeveloperFragment())
      }

      TestsSettingsEvent.OpenReviewDialog -> {
        reviewsApi.showFeedbackForm(
          context = requireContext(),
          title = "Write a review",
          appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE,
          allowLogsAttachment = false,
        )
      }

      is TestsSettingsEvent.OpenReminderAction -> ReminderActionActivity.mockTest(requireContext(), event.reminderId)

      is TestsSettingsEvent.OpenBirthdayAction -> BirthdayActionActivity.mockTest(requireContext(), event.birthdayId)
    }
  }

  override fun getTitle(): String = "Tests"
}
