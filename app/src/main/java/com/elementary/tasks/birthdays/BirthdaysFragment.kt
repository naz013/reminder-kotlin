package com.elementary.tasks.birthdays

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
import com.elementary.tasks.core.os.datapicker.ContactPicker
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.BackPressHandler
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject

/**
 * Hosts the Birthdays feature (Preview + Edit) as a self-contained Navigation 3 "island": this
 * Fragment owns the internal backstack and the Android-framework glue (permissions, contact
 * picking, date pickers) that only a Fragment can provide, and is otherwise responsible only for
 * screen-to-screen navigation. The actual screens and their wiring live in [BirthdaysNavGraph].
 */
class BirthdaysFragment :
  Fragment(),
  RootFragment,
  BackPressHandler {
  internal val dialogues by inject<Dialogues>()
  internal val dateTimePickerProvider by inject<DateTimePickerProvider>()
  internal val adsProvider = AdsProvider()
  internal lateinit var permissionFlow: PermissionFlow
  internal lateinit var contactPicker: ContactPicker

  /** Bridge from the Fragment's plain methods into the Compose-owned Nav3 backstack — the
   *  backstack itself can only be created via [rememberNavBackStack] inside composition. */
  private var currentBackStack: MutableList<NavKey>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
    contactPicker = ContactPicker(this) { }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val backStack = rememberNavBackStack(*initialBackStackKeys().toTypedArray())
      SideEffect { currentBackStack = backStack }
      BirthdaysNavGraph(backStack)
    }

  override fun onDestroyView() {
    super.onDestroyView()
    currentBackStack = null
  }

  /** Seeds the island's backstack from a birthday deep link / cross-feature link, read once from
   *  [getArguments] — see [com.elementary.tasks.home.ScreenDestinationIdResolver] and
   *  [com.elementary.tasks.home.BottomNavActivity], which both route birthday deep links to this
   *  fragment (`R.id.birthdayFragment`) instead of two separate destinations. */
  private fun initialBackStackKeys(): List<BirthdaysNavKey> {
    val args = arguments ?: return listOf(BirthdaysNavKey.Edit())
    val id = args.getString(IntentKeys.INTENT_ID)
    return when {
      args.getBoolean(ARG_OPEN_EDIT, false) -> listOf(BirthdaysNavKey.Edit(id ?: ""))
      id != null -> listOf(BirthdaysNavKey.Preview(id))
      else -> listOf(BirthdaysNavKey.Edit())
    }
  }

  override fun canGoBack(): Boolean = (currentBackStack?.size ?: 1) <= 1

  companion object {
    const val ARG_OPEN_EDIT = "birthday_open_edit"
  }
}
