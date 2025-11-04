package com.github.nsy.reviewsadmin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.github.nsy.reviewsadmin.ui.dashboard.DashboardViewModel
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
    val dashboardViewModel: DashboardViewModel = koinViewModel()
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

    // Reload dashboard data when returning to dashboard screen
    LaunchedEffect(isLoggedIn, selectedAppSource, selectedReview, selectedLogUrl) {
      if (isLoggedIn && selectedAppSource == null && selectedReview == null && selectedLogUrl == null) {
        dashboardViewModel.loadData()
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

    // Determine current screen for animation
    val currentScreen = when {
      !isLoggedIn -> Screen.Login
      selectedLogUrl != null -> Screen.LogViewer
      selectedReview != null -> Screen.ReviewDetail
      selectedAppSource != null -> Screen.ReviewList
      else -> Screen.Dashboard
    }

    AnimatedContent(
      targetState = currentScreen,
      transitionSpec = {
        getTransitionSpec(initialState, targetState)
      },
      label = "screen_transition"
    ) { screen ->
      when (screen) {
        Screen.Login -> {
          LoginScreen(
            onLoginSuccess = {
              isLoggedIn = true
            }
          )
        }
        Screen.LogViewer -> {
          val logUrl = selectedLogUrl ?: return@AnimatedContent
          LogViewerScreen(
            logFileUrl = logUrl,
            onNavigateBack = {
              selectedLogUrl = null
            }
          )
        }
        Screen.ReviewDetail -> {
          val review = selectedReview ?: return@AnimatedContent
          ReviewDetailScreen(
            review = review,
            onNavigateBack = {
              selectedReview = null
            },
            onViewLogs = { logUrl ->
              selectedLogUrl = logUrl
            }
          )
        }
        Screen.ReviewList -> {
          val appSource = selectedAppSource ?: return@AnimatedContent
          ReviewListScreen(
            appSource = appSource,
            onNavigateBack = {
              selectedAppSource = null
            },
            onReviewClick = { review ->
              selectedReview = review
            }
          )
        }
        Screen.Dashboard -> {
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

  /**
   * Defines the transition animation between screens.
   *
   * @param initialState The screen we're transitioning from
   * @param targetState The screen we're transitioning to
   * @return ContentTransform with appropriate slide and fade animations
   */
  private fun getTransitionSpec(
    initialState: Screen,
    targetState: Screen
  ): ContentTransform {
    val animationDuration = 400
    val isForwardNavigation = targetState.depth > initialState.depth

    return if (isForwardNavigation) {
      // Slide in from right and fade in when going forward
      (slideInHorizontally(
        animationSpec = tween(animationDuration),
        initialOffsetX = { fullWidth -> fullWidth }
      ) + fadeIn(
        animationSpec = tween(animationDuration)
      )).togetherWith(
        // Slide out to left and fade out when going forward
        slideOutHorizontally(
          animationSpec = tween(animationDuration),
          targetOffsetX = { fullWidth -> -fullWidth / 3 }
        ) + fadeOut(
          animationSpec = tween(animationDuration)
        )
      )
    } else {
      // Slide in from left when going back
      (slideInHorizontally(
        animationSpec = tween(animationDuration),
        initialOffsetX = { fullWidth -> -fullWidth / 3 }
      ) + fadeIn(
        animationSpec = tween(animationDuration)
      )).togetherWith(
        // Slide out to right and fade out when going back
        slideOutHorizontally(
          animationSpec = tween(animationDuration),
          targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(
          animationSpec = tween(animationDuration)
        )
      )
    }
  }
}

/**
 * Represents the different screens in the app with their navigation depth.
 *
 * @property depth Navigation depth for determining transition direction
 */
private enum class Screen(val depth: Int) {
  /** Login screen */
  Login(0),
  /** Dashboard screen */
  Dashboard(1),
  /** Review list screen */
  ReviewList(2),
  /** Review detail screen */
  ReviewDetail(3),
  /** Log viewer screen */
  LogViewer(4)
}

