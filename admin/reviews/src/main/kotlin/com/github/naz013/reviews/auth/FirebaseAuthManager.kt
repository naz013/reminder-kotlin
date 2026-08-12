package com.github.naz013.reviews.auth

import com.github.naz013.logging.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Manager class for handling Firebase Anonymous Authentication.
 * This allows users to interact with Firestore without creating accounts.
 *
 * @property firebaseAuth The Firebase Authentication instance
 */
internal class FirebaseAuthManager(
  private val firebaseAuth: FirebaseAuth
) {

  /**
   * Signs in a user anonymously if not already signed in.
   * This is required to use Firestore with authentication-based security rules.
   *
   * @return Result containing the authenticated user or failure with exception details
   */
  suspend fun signInAnonymously(): Result<FirebaseUser> {
    return try {
      // Check if user is already signed in
      val currentUser = firebaseAuth.currentUser
      if (currentUser != null) {
        Logger.i("FirebaseAuthManager", "User already signed in: ${currentUser.uid}")
        return Result.success(currentUser)
      }

      // Sign in anonymously
      Logger.d("FirebaseAuthManager", "Signing in anonymously...")
      val authResult = firebaseAuth.signInAnonymously().await()

      val user = authResult.user
      if (user != null) {
        Logger.i("FirebaseAuthManager", "Successfully signed in anonymously: ${user.uid}")
        Result.success(user)
      } else {
        val error = IllegalStateException("Anonymous sign-in succeeded but user is null")
        Logger.e("FirebaseAuthManager", "Sign-in failed: user is null", error)
        Result.failure(error)
      }
    } catch (e: Exception) {
      Logger.e("FirebaseAuthManager", "Failed to sign in anonymously", e)
      Result.failure(e)
    }
  }

  /**
   * Gets the current authenticated user.
   *
   * @return The current FirebaseUser if signed in, null otherwise
   */
  fun getCurrentUser(): FirebaseUser? {
    return firebaseAuth.currentUser
  }

  /**
   * Gets the current user's UID.
   *
   * @return The user's UID if signed in, null otherwise
   */
  fun getCurrentUserId(): String? {
    return firebaseAuth.currentUser?.uid
  }

  /**
   * Checks if a user is currently signed in.
   *
   * @return true if a user is signed in, false otherwise
   */
  fun isSignedIn(): Boolean {
    return firebaseAuth.currentUser != null
  }

  /**
   * Signs out the current user.
   * Note: For anonymous authentication, this will delete the anonymous account.
   */
  fun signOut() {
    try {
      val userId = firebaseAuth.currentUser?.uid
      firebaseAuth.signOut()
      Logger.i("FirebaseAuthManager", "User signed out: $userId")
    } catch (e: Exception) {
      Logger.e("FirebaseAuthManager", "Failed to sign out", e)
    }
  }

  /**
   * Ensures a user is signed in anonymously.
   * This should be called before performing any Firestore operations.
   *
   * @return Result indicating success or failure
   */
  suspend fun ensureAuthenticated(): Result<Unit> {
    return if (isSignedIn()) {
      Result.success(Unit)
    } else {
      signInAnonymously().map { Unit }
    }
  }
}
