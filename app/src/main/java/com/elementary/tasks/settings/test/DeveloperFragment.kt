package com.elementary.tasks.settings.test

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.fragment.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeveloperFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<DeveloperViewModel>()
  private val reviewsApi by inject<ReviewsApi>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    DeveloperScreen(
      state = state,
      onResetBannersClick = viewModel::onResetBannersClick,
      onBirthdayDialogClick = viewModel::onBirthdayDialogClick,
      onReminderDialogClick = viewModel::onReminderDialogClick,
      onObjectExportClick = viewModel::onObjectExportClick,
      onReviewDialogClick = viewModel::onReviewDialogClick,
      onProVersionClick = viewModel::onProVersionClick,
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogConfirm = viewModel::onDialogConfirm,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.bannersReset.observeEvent(viewLifecycleOwner) {
      toast("Home Screen banners have been reset")
    }
  }

  private fun handleEvent(event: DeveloperEvent) {
    when (event) {
      DeveloperEvent.OpenObjectExport -> {
        safeNavigation(DeveloperFragmentDirections.actionDeveloperFragmentToObjectExportTestFragment())
      }

      DeveloperEvent.OpenReviewDialog -> {
        reviewsApi.showFeedbackForm(
          context = requireContext(),
          title = "Write a review",
          appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE,
          allowLogsAttachment = false,
        )
      }

      is DeveloperEvent.OpenReminderAction -> ReminderActionActivity.mockTest(requireContext(), event.reminderId)

      is DeveloperEvent.OpenBirthdayAction -> BirthdayActionActivity.mockTest(requireContext(), event.birthdayId)

      DeveloperEvent.OpenProVersion -> {
        safeNavigation(DeveloperFragmentDirections.actionDeveloperFragmentToProVersionFragment())
      }
    }
  }

  override fun getTitle(): String = "Developer"
}

@Composable
private fun DeveloperScreen(
  state: DeveloperState,
  onResetBannersClick: () -> Unit,
  onBirthdayDialogClick: () -> Unit,
  onReminderDialogClick: () -> Unit,
  onObjectExportClick: () -> Unit,
  onReviewDialogClick: () -> Unit,
  onProVersionClick: () -> Unit,
  onDialogOptionSelected: (Int) -> Unit,
  onDialogConfirm: () -> Unit,
  onDialogDismiss: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    DeveloperOption(
      title = "Reset banners state on Home Screen",
      subtitle = "Shows the privacy, login and what's new banners again",
      onClick = onResetBannersClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open Birthday Screen",
      subtitle = "Save a mock birthday and open its action screen",
      onClick = onBirthdayDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open Reminder Screen",
      subtitle = "Save a mock reminder and open its action screen",
      onClick = onReminderDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Save object to File",
      subtitle = "Export a sample domain object to a file",
      onClick = onObjectExportClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Show Review Dialog",
      subtitle = "Preview the in-app review request form",
      onClick = onReviewDialogClick,
    )
    HorizontalDivider()
    DeveloperOption(
      title = "Open PRO Version Screen",
      subtitle = "Preview the PRO version advertisement screen",
      onClick = onProVersionClick,
    )
    HorizontalDivider()
  }

  val dialog = state.dialog
  if (dialog != null) {
    DeveloperChoiceDialog(
      dialog = dialog,
      onOptionSelected = onDialogOptionSelected,
      onConfirm = onDialogConfirm,
      onDismiss = onDialogDismiss,
    )
  }
}

@Composable
private fun DeveloperOption(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    headlineContent = { Text(text = title) },
    supportingContent = { Text(text = subtitle) },
  )
}

@Composable
private fun DeveloperChoiceDialog(
  dialog: DeveloperChoiceDialog,
  onOptionSelected: (Int) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select action to test") },
    text = {
      Column(modifier = Modifier.selectableGroup()) {
        dialog.options.forEachIndexed { index, option ->
          val selected = index == dialog.selectedIndex
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .selectable(selected = selected, onClick = { onOptionSelected(index) }, role = Role.RadioButton)
              .padding(vertical = 8.dp),
          ) {
            RadioButton(selected = selected, onClick = null)
            Text(
              text = option,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onConfirm) { Text("Run") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
