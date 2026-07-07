package com.elementary.tasks.birthdays

import android.widget.FrameLayout
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.fragment.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.EditBirthdayScreen
import com.elementary.tasks.birthdays.create.EditBirthdayViewModel
import com.elementary.tasks.birthdays.preview.PreviewBirthdayScreen
import com.elementary.tasks.birthdays.preview.PreviewBirthdayViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.toast
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds the Birthdays island's [NavDisplay] — the "screens" (Nav3 entries) themselves and the
 * routing between them. [BirthdaysFragment] only owns the backstack and the Android-framework
 * glue (permissions, contact picking, date pickers) that these entries react to.
 */
@Composable
internal fun BirthdaysFragment.BirthdaysNavGraph(backStack: MutableList<NavKey>) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    transitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
      )
    },
    popTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    predictivePopTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    entryProvider =
      entryProvider {
        entry<BirthdaysNavKey.Preview> { key -> PreviewEntry(key, backStack) }
        entry<BirthdaysNavKey.Edit> { key -> EditEntry(key, backStack) }
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f

@Composable
private fun BirthdaysFragment.PreviewEntry(
  key: BirthdaysNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewBirthdayViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  viewModel.resultEvent.ObserveEvent { command ->
    if (command == Commands.DELETED) backStack.removeLastOrNull()
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

  val state by viewModel.state.collectAsState()
  PreviewBirthdayScreen(
    state = state,
    onBackClick = { popOrExit(backStack) },
    onEditClick = { backStack.add(BirthdaysNavKey.Edit(key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCallClick = {
      state.birthday?.number?.let { number ->
        permissionFlow.askPermission(Permissions.CALL_PHONE) { TelephonyUtil.makeCall(number, requireContext()) }
      }
    },
    onSmsClick = {
      state.birthday?.number?.let { number -> TelephonyUtil.sendSms(number, requireContext()) }
    },
    adsContent = { BirthdayAdBanner(adsProvider) },
  )
}

@Composable
private fun BirthdaysFragment.EditEntry(
  key: BirthdaysNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditBirthdayViewModel> { parametersOf(key.id) }
  DisposableEffect(viewModel) {
    onDispose { hideKeyboard() }
  }
  LaunchedEffect(Unit) { viewModel.checkArguments(arguments) }

  viewModel.resultEvent.ObserveEvent { command ->
    if (command == Commands.SAVED || command == Commands.DELETED) backStack.removeLastOrNull()
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

  val state by viewModel.state.collectAsState()
  EditBirthdayScreen(
    state = state,
    onBackClick = { popOrExit(backStack) },
    onSaveClick = {
      if (state.number.isNotEmpty()) {
        permissionFlow.askPermission(Permissions.READ_CONTACTS) { viewModel.onSaveClick() }
      } else {
        viewModel.onSaveClick()
      }
    },
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onNameChange = viewModel::onNameChanged,
    onYearCheckChanged = viewModel::onYearCheckChanged,
    onDateFieldClick = {
      dateTimePickerProvider.showDatePicker(
        fragmentManager = childFragmentManager,
        date = viewModel.selectedDate,
        title = getString(R.string.select_date),
      ) { viewModel.onDateChanged(it) }
    },
    onNumberChange = viewModel::onNumberChanged,
    onPickContactClick = {
      contactPicker.pickContact { contactData -> viewModel.onContactPicked(contactData) }
    },
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}

/** Unlike Notes/Google Tasks, this island has no shared "list" root entry behind Preview/Edit —
 *  either screen can be the sole backstack entry (reached directly from outside the island), so
 *  the in-screen back arrow must fall through to the real nav controller in that case, exactly
 *  like [BirthdaysFragment.canGoBack] already does for the system back button. */
private fun BirthdaysFragment.popOrExit(backStack: MutableList<NavKey>) {
  if (backStack.size <= 1) {
    findNavController().popBackStack()
  } else {
    backStack.removeLastOrNull()
  }
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

@Composable
private fun BirthdaysFragment.BirthdayAdBanner(adsProvider: AdsProvider) {
  if (BuildParams.isPro || !AdsProvider.hasAds()) return
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup -> adsProvider.showBanner(viewGroup, AdsProvider.BIRTHDAY_PREVIEW_BANNER_ID) },
  )
}
