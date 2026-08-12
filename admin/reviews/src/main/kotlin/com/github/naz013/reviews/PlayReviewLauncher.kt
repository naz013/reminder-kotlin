package com.github.naz013.reviews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.findActivity
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

/**
 * Wraps Google's official Play In-App Review flow - distinct from [ReviewsFormLauncher], which is
 * this app's own Firestore-backed feedback form and never touches the actual Play Store rating.
 * This is the only mechanism that can move the public star rating that affects ASO ranking.
 */
interface PlayReviewLauncher {
  fun launchReviewFlow()
}

@Composable
fun rememberPlayReviewLauncher(): PlayReviewLauncher {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  return remember {
    object : PlayReviewLauncher {
      override fun launchReviewFlow() {
        val activity = context.findActivity() ?: return
        scope.launch {
          runCatching {
            val manager = ReviewManagerFactory.create(context)
            val reviewInfo = manager.requestReview()
            manager.launchReview(activity, reviewInfo)
          }.onFailure { error ->
            Logger.i(TAG, "Play review flow failed: ${error.message}")
          }
        }
      }
    }
  }
}

private const val TAG = "PlayReviewLauncher"
