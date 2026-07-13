package com.elementary.tasks.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import org.koin.compose.koinInject

interface ReviewApiLauncher {
  fun launch(title: String?, appSource: AppSource, allowLogsAttachment: Boolean = true)
}

private class ReviewApiLauncherImpl(
  private val context: Context,
  private val reviewsApi: ReviewsApi,
) : ReviewApiLauncher {

  override fun launch(title: String?, appSource: AppSource, allowLogsAttachment: Boolean) {
    reviewsApi.showFeedbackForm(context, title, appSource, allowLogsAttachment)
  }
}

@Composable
fun rememberReviewApiLauncher(): ReviewApiLauncher {
  val context = LocalContext.current
  return ReviewApiLauncherImpl(
    context = context,
    reviewsApi = koinInject<ReviewsApi>()
  )
}
