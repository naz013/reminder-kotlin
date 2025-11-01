package com.github.nsy.reviewsadmin.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.ReviewsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for handling user authentication with Firebase through ReviewsApi.
 *
 * Manages the login state and handles authentication through the secondary Firebase app.
 *
 * @property reviewsApi API for accessing authentication from secondary Firebase app
 * @property dispatcherProvider Provides coroutine dispatchers
 */
class LoginViewModel(
  private val reviewsApi: ReviewsApi,
  private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

  private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
  val uiState = _uiState.stateIn(
    viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),
    initialValue = LoginUiState.Idle
  ).onStart { checkExistingAuth() }

  /**
   * Checks if user is already authenticated.
   */
  private fun checkExistingAuth() {
    val currentUser = reviewsApi.getCurrentUser()
    if (currentUser != null) {
      Logger.i(TAG, "User already authenticated: ${currentUser.uid}")
      _uiState.value = LoginUiState.Success
    }
  }

  /**
   * Initiates Google Sign-In process.
   *
   * Note: Currently uses anonymous authentication through the secondary Firebase app.
   * In production, you can extend this to use proper Google Sign-In SDK.
   */
  fun signInWithGoogle() {
    viewModelScope.launch(dispatcherProvider.io()) {
      try {
        _uiState.value = LoginUiState.Loading

        val result = reviewsApi.signInAnonymously()

        result.fold(
          onSuccess = { user ->
            Logger.i(TAG, "Sign-in successful: ${user.uid}")
            _uiState.value = LoginUiState.Success
          },
          onFailure = { error ->
            Logger.e(TAG, "Sign-in failed", error)
            _uiState.value = LoginUiState.Error(
              error.message ?: "Authentication failed"
            )
          }
        )
      } catch (e: Exception) {
        Logger.e(TAG, "Sign-in error", e)
        _uiState.value = LoginUiState.Error(e.message ?: "Unknown error occurred")
      }
    }
  }

  /**
   * Signs out the current user from the secondary Firebase app.
   */
  fun signOut() {
    reviewsApi.signOut()
    _uiState.value = LoginUiState.Idle
    Logger.i(TAG, "User signed out")
  }

  companion object {
    private const val TAG = "LoginViewModel"
  }
}

/**
 * Represents the different states of the login UI.
 */
sealed class LoginUiState {
  /** Initial state, ready for user interaction */
  data object Idle : LoginUiState()

  /** Authentication in progress */
  data object Loading : LoginUiState()

  /** Authentication successful */
  data object Success : LoginUiState()

  /** Authentication failed with error message */
  data class Error(val message: String) : LoginUiState()
}
