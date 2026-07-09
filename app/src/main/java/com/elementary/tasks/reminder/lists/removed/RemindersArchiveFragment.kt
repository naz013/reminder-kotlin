package com.elementary.tasks.reminder.lists.removed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersArchiveFragment : Fragment() {
  private val viewModel by viewModel<RemindersArchiveViewModel>()
  private val dialogues by inject<Dialogues>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val state by viewModel.state.collectAsState()
      RemindersArchiveScreen(
        state = state,
        onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDeleteAllClick = viewModel::onDeleteAllClick,
        onItemClick = viewModel::onItemClick,
        onMenuAction = viewModel::onMenuAction,
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { handleNavigationEvent(it) }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  private fun handleNavigationEvent(event: RemindersArchiveViewModel.NavigationEvent) {
    when (event) {
      is RemindersArchiveViewModel.NavigationEvent.OpenEdit -> {
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteReminder(event.id)
        }
      }

      RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteAll -> {
        dialogues.askConfirmation(
          context = requireContext(),
          title = getString(R.string.delete_all_archived_reminders),
          positiveText = getString(R.string.yes_delete_all),
          negativeText = getString(R.string.cancel),
          onAction = { confirmed -> if (confirmed) viewModel.deleteAll() },
        )
      }

      RemindersArchiveViewModel.NavigationEvent.ArchiveEmptied -> {
        Toast.makeText(requireContext(), R.string.archive_was_emptied, Toast.LENGTH_SHORT).show()
      }
    }
  }
}
