package com.elementary.tasks.notes.list

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.Fragment
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.PhotoSelectionUtil
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.elementary.tasks.notes.NotesNavGraph
import com.elementary.tasks.notes.NotesNavKey
import com.elementary.tasks.notes.create.CreateNoteViewModel
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject

/**
 * Hosts the Notes feature as a self-contained Navigation 3 "island": this Fragment owns the
 * internal backstack and the Android-framework glue (permissions, photo picking, dialogs,
 * date/time pickers) that only a Fragment/Activity can provide, and is otherwise responsible only
 * for screen-to-screen navigation. The actual screens and their wiring live in [NotesNavGraph].
 */
class NotesFragment :
  Fragment(),
  RootFragment,
  BackPressHandler,
  PhotoSelectionUtil.UriCallback {
  internal val dialogues by inject<Dialogues>()
  internal val dateTimePickerProvider by inject<DateTimePickerProvider>()
  internal val adsProvider = AdsProvider()
  internal lateinit var permissionFlow: PermissionFlow
  internal lateinit var photoSelectionUtil: PhotoSelectionUtil

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  /** The [CreateNoteViewModel] currently on screen, if any — [photoSelectionUtil]'s callback is
   *  Fragment-scoped and long-lived (registered once in [onCreate]), so its results have to be
   *  routed to whichever edit entry is actually active right now. */
  internal var activeCreateNoteViewModel: CreateNoteViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
    photoSelectionUtil = PhotoSelectionUtil(this, this)
    lifecycle.addObserver(photoSelectionUtil)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      NotesNavGraph(backStack)
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
        listOf(NotesNavKey.List, NotesNavKey.Edit(id ?: "", statusBarColor))
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

  override fun onImageSelected(uris: List<Uri>) {
    activeCreateNoteViewModel?.addMultiple(uris)
  }

  override fun onBitmapReady(bitmap: Bitmap) {
    activeCreateNoteViewModel?.addBitmap(bitmap)
  }

  companion object {
    const val ARG_OPEN_EDIT = "notes_open_edit"
  }
}
