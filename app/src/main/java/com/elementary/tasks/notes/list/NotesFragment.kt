package com.elementary.tasks.notes.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.fragment.startActivity
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NotesFragment : Fragment(), RootFragment {

  private val viewModel by viewModel<NotesViewModel> { parametersOf(false) }
  private val dialogues by inject<Dialogues>()
  private val themeProvider by inject<ThemeProvider>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()
  private lateinit var permissionFlow: PermissionFlow

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return composeView {
      val state by viewModel.notesScreenState.collectAsState()
      NotesScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onBackClick = { findNavController().popBackStack() },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onSortOrderSelected = { viewModel.onSortOrderSelected(it) },
        onGridToggleClick = { viewModel.onGridToggleClick() },
        onArchiveClick = { viewModel.onArchiveClick() },
        onSettingsClick = { viewModel.onSettingsClick() },
        onAddClick = { viewModel.onAddClick() },
        onNoteClick = { viewModel.onNoteClick(it) },
        onNoteMenuAction = { note, action -> viewModel.onNoteMenuAction(note, action) },
        onImageClick = { note, imageId -> viewModel.onImageClick(note, imageId) }
      )
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    analyticsEventSender.send(ScreenUsedEvent(Screen.NOTES_LIST))
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { handleNavigationEvent(it) }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
  }

  private fun handleNavigationEvent(event: NotesViewModel.NavigationEvent) {
    when (event) {
      is NotesViewModel.NavigationEvent.OpenNotePreview -> {
        safeNavigation(
          R.id.previewNoteFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions()
        )
      }

      is NotesViewModel.NavigationEvent.OpenCreateNote -> {
        safeNavigation(R.id.createNoteFragment, null, NavigationAnimations.inDepthNavOptions())
      }

      is NotesViewModel.NavigationEvent.OpenEditNote -> {
        safeNavigation(
          R.id.createNoteFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is NotesViewModel.NavigationEvent.OpenArchive -> {
        safeNavigation(NotesFragmentDirections.actionActionNotesToArchivedNotesFragment())
      }

      is NotesViewModel.NavigationEvent.OpenSettings -> {
        safeNavigation(
          NotesFragmentDirections.actionActionNotesToNoteSettingsFragment(
            getString(R.string.action_settings)
          )
        )
      }

      is NotesViewModel.NavigationEvent.OpenImagePreview -> {
        startActivity(ImagePreviewActivity::class.java) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          putExtra(IntentKeys.INTENT_ID, event.noteId)
          putExtra(IntentKeys.INTENT_POSITION, event.imagePosition)
        }
      }

      is NotesViewModel.NavigationEvent.ShareNote -> {
        TelephonyUtil.sendNote(event.file, requireContext(), event.summary)
      }

      is NotesViewModel.NavigationEvent.RequestNotificationPermission -> {
        permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
          viewModel.showNoteInNotification(event.id)
        }
      }

      is NotesViewModel.NavigationEvent.PickColor -> {
        dialogues.showColorDialog(
          requireActivity(),
          event.colorPosition,
          getString(R.string.color),
          themeProvider.noteColorsForSlider(event.colorPalette)
        ) { color -> viewModel.saveNoteColor(event.id, color) }
      }

      is NotesViewModel.NavigationEvent.ConfirmDelete -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteNote(event.id)
        }
      }
    }
  }
}
