package com.github.naz013.reviews

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.reviews.form.ReviewDialogViewModel
import com.github.naz013.reviews.form.ReviewFormContent
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel

interface ReviewsFormLauncher {
  fun showFeedbackForm(title: String?, appSource: AppSource, allowLogsAttachment: Boolean)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberReviewsFormLauncher(onDismiss: () -> Unit = {}): ReviewsFormLauncher {
  val data = remember { mutableStateOf<Data?>(null) }

  // Resolving ReviewDialogViewModel touches Firebase (via the reviews Koin module's
  // FirestoreDatabase singleton), which throws if ReviewSdk.initialize() hasn't succeeded yet
  // (e.g. no network shortly after a fresh install) - every screen that calls
  // rememberReviewsFormLauncher() would otherwise crash on composition even though the user
  // never opened the feedback form. Deferring resolution to only while the sheet is shown keeps
  // that failure scoped to the rare case of actually submitting feedback offline.
  data.value?.let { request ->
    ReviewFormSheet(
      request = request,
      onDismissRequest = {
        data.value = null
        onDismiss()
      },
    )
  }

  return object : ReviewsFormLauncher {
    override fun showFeedbackForm(
      title: String?,
      appSource: AppSource,
      allowLogsAttachment: Boolean
    ) {
      data.value = Data(title, appSource, allowLogsAttachment)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewFormSheet(request: Data, onDismissRequest: () -> Unit) {
  val viewModel = koinViewModel<ReviewDialogViewModel>()

  val toastDispatcher = rememberToastDispatcher()
  val isLoading by viewModel.isLoading.observeAsState(false)

  viewModel.submitSuccess.observeAsEvent {
    toastDispatcher.showToast(messageRes = R.string.thank_you_for_your_feedback)
    onDismissRequest()
  }
  viewModel.submitError.observeAsEvent { message ->
    toastDispatcher.showToast(message = message)
  }

  ModalBottomSheet(onDismissRequest = onDismissRequest) {
    ReviewFormContent(
      title = request.title ?: stringResource(com.github.naz013.ui.common.R.string.feedback),
      isLoading = isLoading,
      allowLogs = request.allowLogsAttachment,
      onSubmit = { rating, comment, attachLog, email ->
        viewModel.submitReview(rating, comment, attachLog, email, request.appSource)
      },
      onDismiss = onDismissRequest,
      onShowError = { message -> toastDispatcher.showToast(message = message) },
    )
  }
}

@Composable
private fun <T> LiveData<out Event<T>?>.observeAsEvent(action: (T) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(this, lifecycleOwner) {
    val observer = Observer<Event<T>?> { event -> event?.getContentIfNotHandled()?.let(action) }
    observe(lifecycleOwner, observer)
    onDispose { removeObserver(observer) }
  }
}

private data class Data(
  val title: String?,
  val appSource: AppSource,
  val allowLogsAttachment: Boolean,
)
