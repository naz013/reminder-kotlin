package com.github.nsy.reviewsadmin

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.nsy.reviewsadmin.ui.dashboard.DashboardScreen
import com.github.nsy.reviewsadmin.ui.login.LoginScreen
import com.github.nsy.reviewsadmin.ui.login.LoginUiState
import com.github.nsy.reviewsadmin.ui.login.LoginViewModel
import com.github.nsy.reviewsadmin.ui.logviewer.LogViewerScreen
import com.github.nsy.reviewsadmin.ui.reviewdetail.ReviewDetailScreen
import com.github.nsy.reviewsadmin.ui.reviewlist.ReviewListScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Main activity for the Reviews Admin app.
 *
 * Manages navigation between login, dashboard, review list, review detail, and log viewer screens.
 */
class MainActivity : ComposeActivity() {

  @Composable
  override fun ActivityContent() {
    val loginViewModel: LoginViewModel = koinViewModel()
    val loginState by loginViewModel.uiState.collectAsState(LoginUiState.Idle)
    var isLoggedIn by remember { mutableStateOf(value = false) }
    var selectedAppSource by remember { mutableStateOf<AppSource?>(value = null) }
    var selectedReview by remember { mutableStateOf<Review?>(value = null) }
    var selectedLogUrl by remember { mutableStateOf<String?>(value = null) }

    // Observe login state and update navigation accordingly
    LaunchedEffect(loginState) {
      if (loginState is LoginUiState.Success) {
        isLoggedIn = true
      }
    }

    // Handle back press for LogViewerScreen -> ReviewDetailScreen navigation
    BackHandler(enabled = selectedLogUrl != null) {
      selectedLogUrl = null
    }

    // Handle back press for ReviewDetailScreen -> ReviewListScreen navigation
    BackHandler(enabled = selectedReview != null && selectedLogUrl == null) {
      selectedReview = null
    }

    // Handle back press for ReviewListScreen -> DashboardScreen navigation
    BackHandler(enabled = selectedAppSource != null && selectedReview == null && selectedLogUrl == null) {
      selectedAppSource = null
    }

    // Handle back press for DashboardScreen -> close app
    BackHandler(enabled = isLoggedIn && selectedAppSource == null && selectedReview == null && selectedLogUrl == null) {
      finish()
    }

    when {
      !isLoggedIn -> {
        LoginScreen(
          onLoginSuccess = {
            isLoggedIn = true
          }
        )
      }
      selectedLogUrl != null -> {
        LogViewerScreen(
          logFileUrl = selectedLogUrl!!,
          onNavigateBack = {
            selectedLogUrl = null
          }
        )
      }
      selectedReview != null -> {
        ReviewDetailScreen(
          review = selectedReview!!,
          onNavigateBack = {
            selectedReview = null
          },
          onViewLogs = { logUrl ->
            selectedLogUrl = logUrl
          }
        )
      }
      selectedAppSource != null -> {
        ReviewListScreen(
          appSource = selectedAppSource!!,
          onNavigateBack = {
            selectedAppSource = null
          },
          onReviewClick = { review ->
            selectedReview = review
          }
        )
      }
      else -> {
        DashboardScreen(
          onSignOut = {
            loginViewModel.signOut()
            isLoggedIn = false
          },
          onAppSourceClick = { appSource ->
            selectedAppSource = appSource
          }
        )
      }
    }
  }
}
