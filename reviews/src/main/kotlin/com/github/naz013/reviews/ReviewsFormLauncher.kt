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
fun rememberReviewsFormLauncher(): ReviewsFormLauncher {
  val viewModel = koinViewModel<ReviewDialogViewModel>()

  val data = remember { mutableStateOf<Data?>(null) }

  val toastDispatcher = rememberToastDispatcher()
  val isLoading by viewModel.isLoading.observeAsState(false)

  viewModel.submitSuccess.observeAsEvent {
    toastDispatcher.showToast(messageRes = R.string.thank_you_for_your_feedback)
    data.value = null
  }
  viewModel.submitError.observeAsEvent { message ->
    toastDispatcher.showToast(message = message)
  }

  data.value?.let { request ->
    ModalBottomSheet(onDismissRequest = { data.value = null }) {
      ReviewFormContent(
        title = request.title ?: stringResource(com.github.naz013.ui.common.R.string.feedback),
        isLoading = isLoading,
        allowLogs = request.allowLogsAttachment,
        onSubmit = { rating, comment, attachLog, email ->
          viewModel.submitReview(rating, comment, attachLog, email, request.appSource)
        },
        onDismiss = { data.value = null },
        onShowError = { message -> toastDispatcher.showToast(message = message) },
      )
    }
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
