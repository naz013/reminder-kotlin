package com.elementary.tasks.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.elementary.tasks.navigation.BackPressHandler
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.compose.composeView

/**
 * Hosts the Notes feature as a self-contained Navigation 3 "island": this Fragment owns only the
 * internal backstack and forwards its [getArguments] bundle once to seed it. The actual screens
 * live in [NotesNavGraph] and are Fragment/Activity-independent themselves — they resolve what
 * they need (permissions, dialogs, date/time pickers, the host [android.app.Activity]) from the
 * Compose composition instead of from this Fragment.
 */
class NotesFragment :
  Fragment(),
  BackPressHandler {

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [androidx.navigation3.runtime.rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      NotesNavGraph(backStack, arguments)
    }

  override fun onDestroyView() {
    super.onDestroyView()
    currentBackStack = null
  }

  /** Seeds the island's backstack from a note deep link / app shortcut, read once from
   *  [getArguments] — see [com.elementary.tasks.home.ScreenDestinationIdResolver] and
   *  [com.elementary.tasks.home.BottomNavActivity], which both now route note deep links /
   *  shortcuts to this fragment (`R.id.actionNotes`) instead of a dedicated destination. */
  private fun initialBackStackKeys(): List<NotesNavKey> {
    val args = arguments ?: return listOf(NotesNavKey.List)
    val id = args.getString(IntentKeys.INTENT_ID)
    val statusBarColor = requireActivity().window.statusBarColor
    return when {
      args.getBoolean(ARG_OPEN_EDIT, false) -> {
        listOf(NotesNavKey.List, NotesNavKey.Edit(id))
      }

      // Reached from a reminder's linked-note image (imagesSingleton already holds the images) —
      // jumps straight to the image viewer, skipping the note preview screen, same as before.
      args.containsKey(IntentKeys.INTENT_POSITION) -> {
        listOf(NotesNavKey.List, NotesNavKey.ImagePreview(args.getInt(IntentKeys.INTENT_POSITION), statusBarColor))
      }

      id != null -> listOf(NotesNavKey.List, NotesNavKey.Preview(id, statusBarColor))
      else -> listOf(NotesNavKey.List)
    }
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1

  companion object {
    const val ARG_OPEN_EDIT = "notes_open_edit"
  }
}
